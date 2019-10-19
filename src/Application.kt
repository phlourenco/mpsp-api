package com.phlourenco

import com.google.gson.Gson
import com.phlourenco.Database.dbConnection
import com.phlourenco.definitions.*
import id.jasoet.funpdf.HtmlToPdf
import id.jasoet.funpdf.PageOrientation
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.gson.*
import io.ktor.features.*
import io.ktor.request.receive
import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.Keys
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.interactions.Actions
import org.openqa.selenium.support.ui.Select
import org.openqa.selenium.support.ui.WebDriverWait
import org.openqa.selenium.remote.CapabilityType
import org.openqa.selenium.remote.DesiredCapabilities
import io.ktor.server.netty.EngineMain
import java.io.InputStream
import java.net.URL
import java.util.*
import kotlin.collections.HashMap
import java.util.ArrayList


fun main(args: Array<String>): Unit = EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@JvmOverloads

fun Application.module(testing: Boolean = false) {
    val dbConnection: dbConnection = dbConnection()

    val pdf by lazy {
        HtmlToPdf(executable = "/usr/bin/wkhtmltopdf") {
            orientation(PageOrientation.LANDSCAPE)
            pageSize("Letter")
            marginTop("1in")
            marginBottom("1in")
            marginLeft("1in")
            marginRight("1in")
        }
    }

    install(ContentNegotiation) {
        gson {
        }
    }

    fun login(driver: ChromeDriver) {
        // Faz login no portal do professor. Vai ser removido posteriormente
        driver.get("http://ec2-18-231-116-58.sa-east-1.compute.amazonaws.com/login")
        waitUntilPageIsReady(driver)
        driver.findElementById("username").sendKeys("fiap")
        driver.findElementById("password").sendKeys("mpsp")
        driver.findElementByTagName("form").submit()
        waitUntilPageIsReady(driver)
    }

    fun fillForm(values: Map<String, String>, driver: ChromeDriver) {
        values.forEach {
            driver.findElementByName(it.key).sendKeys(it.value)
        }
    }

    fun stringToPdf(str: String): InputStream? {
        return pdf.convert(input = str)
    }

    fun streamToBase64(stream: InputStream): String {
        val bytes = stream.readBytes()
        return Base64.getEncoder().encodeToString(bytes)
    }

    fun stringToPdfBase64(str: String): String? {
        stringToPdf(str)?.let {
            streamToBase64(it)?.let {
                return it
            }
        }
        return null
    }

    routing {
        post("/arisp") {
            val req = call.receive<ArispRequest>()

            val options = ChromeOptions()

            val searchType: SearchType;
            options.addArguments("disable-infobars")
            options.addArguments("--print-to-pdf")

            val driver = ChromeDriver(options)
            login(driver);

            driver.navigate().to("http://ec2-18-231-116-58.sa-east-1.compute.amazonaws.com/arisp/login.html")
            waitUntilPageIsReady(driver)
            driver.findElementById("btnCallLogin").click()
            waitUntilPageIsReady(driver)
            driver.findElementById("btnAutenticar").click()
            waitUntilPageIsReady(driver)

            val solicitacoesMenuItem = driver.findElementById("liInstituicoes").findElement(By.tagName("ul")).findElements(By.tagName("a")).last()
            driver.navigate().to(solicitacoesMenuItem.getAttribute("href"))
            waitUntilPageIsReady(driver)

            driver.executeScript("document.getElementById('TipoPesquisa').value =" + req.searchType);
            driver.findElementById("Prosseguir").click()
            waitUntilPageIsReady(driver)

            val cities = listOf<String>() //listOf<String>("AGUAÍ", "ÁGUAS DE LINDÓIA")

            if (cities.isNullOrEmpty()) {
                driver.executeScript("javascript:SelecionarTudo();")
            } else {
                driver.findElementsByTagName("tr").forEach {
                    var canCheck = false
                    it.findElements(By.cssSelector("td.title")).forEach {
                        canCheck = cities.contains(it.text)
                    }
                    if (canCheck) {
                        it.findElement(By.name("chkCidades")).click()
                    }
                }
            }
            waitUntilPageIsReady(driver)
            driver.findElementById("chkHabilitar").click()
            waitUntilPageIsReady(driver)
            (driver as JavascriptExecutor).executeScript("window.scrollBy(0,500)")
            driver.findElementById("Prosseguir").click()
            waitUntilPageIsReady(driver)

            driver.findElementById("filterTipo").sendKeys(PersonType.juridica.getTitle())

            driver.executeScript("document.getElementById('filterTipo').value =" + req.personType);
            driver.findElementById("filterDocumento").sendKeys(req.cpfCnpj)
            driver.findElementById("btnPesquisar").click()
            waitUntilPageIsReady(driver)
            driver.executeScript("javascript:SelecionarTudo();")
            driver.findElementsById("btnProsseguir").first { it.isEnabled }?.getAttribute("onclick")?.let {
                driver.executeScript(it)
            }
            waitUntilPageIsReady(driver)

            val registries = mutableListOf<ArispRegistry>()
            driver.findElementById("panelMatriculas").findElements(By.tagName("tr")).filter { it.isDisplayed }.forEach {
                val td = it.findElements(By.tagName("td"))
                val cityName = td[0].text
                val office = td[1].text
                val registryId = td[2].text

                td[3].findElements(By.tagName("a")).first().click()

                val tabs = ArrayList(driver.windowHandles)
                driver.switchTo().window(tabs[1])

                driver.findElements(By.tagName("a")).first { it.getAttribute("href").contains(".pdf") }.apply {
                    val link = this.getAttribute("href")
                    val inputStream = URL(link).openStream()
                    val pdfBase64 = streamToBase64(inputStream)
                    val registry = ArispRegistry(cityName = cityName, office = office, registryId = registryId, registryFileUrl = pdfBase64)
                    registries.add(registry)
                    return@forEach
                }

                driver.close()
                driver.switchTo().window(tabs[0])
            }

            val response = ArispResponse(registries);
            dbConnection.insert("arisp", response.toString())
            call.respond(response)
            driver.close();
        }

        get("/cadesp/{cnpj}") {

            val driver = ChromeDriver()

            val cadespRequest: CadespRequest = CadespRequest(call.parameters["cnpj"]!!)

            login(driver)
            driver.navigate().to("http://ec2-18-231-116-58.sa-east-1.compute.amazonaws.com/cadesp/login.html")
            inputElementById(driver, "ctl00_conteudoPaginaPlaceHolder_loginControl_UserName", "12345")
            inputElementById(driver, "ctl00_conteudoPaginaPlaceHolder_loginControl_Password", "12345")
            clickElementById(driver, "ctl00_conteudoPaginaPlaceHolder_loginControl_loginButton")
            waitUntilPageIsReady(driver)
            moveTo(driver, "Consultas",false)
            moveTo(driver, "Cadastro", true)
            dropSelectOption(driver, "ctl00_conteudoPaginaPlaceHolder_tcConsultaCompleta_TabPanel1_lstIdentificacao", "2")
            inputElementById(driver,"ctl00_conteudoPaginaPlaceHolder_tcConsultaCompleta_TabPanel1_txtIdentificacao", cadespRequest.cnpj)
            clickElementById(driver,"ctl00_conteudoPaginaPlaceHolder_tcConsultaCompleta_TabPanel1_btnConsultarEstabelecimento")

            val td = driver.findElementsByClassName("dadoDetalhe")
            val tdAll = driver.findElementsByTagName("td")

            var situation: Boolean = false
            var registrationSituation: Boolean = false
            var taxOccurrence: Boolean = false

            if(td[4].text == "Situação:  Ativo"){
                situation = true
            }

            if(td[23].text == "Ativo"){
                registrationSituation = true
            }

            if(tdAll[121].text == "Ativa"){
                taxOccurrence = true
            }

            val ie = td[3].text
            val cnpj = td[5].text
            val businessName = td[7].text
            val drt = td[9].text
            val dateStateRegistration = td[6].text
            val stateRegime = td[8].text
            val taxOffice = td[10].text
            val fantasyName = td[15].text
            val nire = td[22].text
            val unitType = td[27].text
            val ieStartDate = td[20].text
            val dateStartedSituation = td[24].text
            val practices = td[29].text
            val response: CadespResponse = CadespResponse(ie, cnpj, businessName, drt, situation, dateStateRegistration, stateRegime, taxOffice, fantasyName, nire, registrationSituation, taxOccurrence, unitType, ieStartDate, dateStartedSituation, practices);

            call.respond(response)
            dbConnection.insert("cadesp", response.toString())
            driver.close()
        }


        post("/caged"){
            val driver = ChromeDriver()
            login(driver)
            driver.navigate().to("http://ec2-18-231-116-58.sa-east-1.compute.amazonaws.com/caged/login.html")
            inputElementById(driver, "username", "12345")
            inputElementById(driver, "password", "12345")
            clickElementById(driver, "btn-submit")

            //RESPONSIBLE
//            moveTo(driver, "Consultas Operacionais",false)
//            moveTo(driver, "Autorizado/Responsável", true)
//            driver.findElementById( "formPesquisarAutorizado:txtChavePesquisaAutorizado014").sendKeys(Keys.HOME,"00000000000000")
//            clickElementById(driver, "formPesquisarAutorizado:bt027_8")
//
//            val cnpjCeiCpf = driver.findElementById("txCnpj020_2").text
//            val identificationName = driver.findElementById("txtrazaosocial020_4").text
//
//            val addressStreet = driver.findElementById("txt3_logradouro020").text
//            val addressCity = driver.findElementById("txt6_municipio020").text
//            val addressState = driver.findElementById("txt7_uf020").text
//            val addressNeighBorHood = driver.findElementById("txt4_bairro020").text
//            val addressCep = driver.findElementById("txt5_codmunicipio020").text
//
//            val ContactName = driver.findElementById("txt_nome_contato").text
//            val ContactCpf = driver.findElementById("txt_contato_cpf").text
//            val ContactLine = driver.findElementById("txt10_ramal020").text
//            val ContactEmail = driver.findElementById("txt11_email").text
//            val ContactDdd = driver.findElementById("txt21_ddd020").text
//            val ContactPhone = driver.findElementById("txt9_telefone020").text
//
//            waitUntilPageIsReady(driver)
//
//            val responseResponsible: CagedResponseResponsible = CagedResponseResponsible(
//                identification(cnpjCeiCpf,identificationName),
//                address(addressStreet,addressNeighBorHood,addressCity,addressState,addressCep),
//                contact(ContactName,ContactCpf, phone(ContactDdd,ContactPhone),ContactLine,ContactEmail)
//            )
//
//            waitUntilPageIsReady(driver)

            //COMPANY
            moveTo(driver, "Consultas Operacionais",false)
            moveTo(driver, "Empresa", true)

            driver.findElementById( "formPesquisarEmpresaCAGED:txtcnpjRaiz").sendKeys(Keys.HOME,"00000000000000")
            clickElementById(driver, "formPesquisarEmpresaCAGED:btConsultar")
            driver.findElementById( "formPesquisarEmpresaCAGED:txtcnpjRaiz").sendKeys(Keys.HOME,"00000000000000")
            clickElementById(driver, "formPesquisarEmpresaCAGED:btConsultar")

            val companyCnpj = driver.findElementById("formResumoEmpresaCaged:txtCnpjRaiz").text
            val companySocialReason = driver.findElementById("formResumoEmpresaCaged:txtRazaoSocial").text
            val companyCnae = driver.findElementById("formResumoEmpresaCaged:txtCodigoAtividadeEconomica").text
            val companyCnae2 = driver.findElementById("formResumoEmpresaCaged:txtDescricaoAtividadeEconomica").text
            val companyCnaeFull = companyCnae + " - " + companyCnae2

            val companyResponse: companyResponse =
                companyResponse(
                    companyCnpj,
                    companySocialReason,
                    companyCnaeFull
                )

            val subsidiaries = driver.findElementById("formResumoEmpresaCaged:txtNumFiliais").text
            val admissions = driver.findElementById("formResumoEmpresaCaged:txtTotalNumAdmissoes").text
            val demissions = driver.findElementById("formResumoEmpresaCaged:txtTotalNumDesligamentos").text

            val detailResponse: detailResponse = detailResponse(
                subsidiaries,
                admissions,
                demissions
            )

            val CagedResponseCompany: CagedResponseCompany =
                CagedResponseCompany(
                    companyResponse,
                    detailResponse
                )


            call.respond(CagedResponseCompany)
            driver.close()

        }

        post("/siel") {
            val req = call.receive<SitelSearch>()
            val driver = ChromeDriver()
            login(driver)

            driver.navigate().to("http://ec2-18-231-116-58.sa-east-1.compute.amazonaws.com/siel/login.html")
            driver.findElementByTagName("form").submit()

            waitUntilPageIsReady(driver)
            driver.findElementByName("nome").sendKeys(req.name)
            driver.findElementByName("nome_mae").sendKeys(req.motherName)
            driver.findElementByName("dt_nascimento").sendKeys(req.birthDate)
            driver.findElementByName("num_titulo").sendKeys(req.documentNumber)
            driver.findElementByName("num_processo").sendKeys(req.processNumber)

            driver.findElementByCssSelector("input[type='image']").click()

            driver.findElements(By.tagName("table")).filter { it.isDisplayed }.forEach {
                val td = it.findElements(By.tagName("td"))

                val response = SitelResponse(
                    td[1].text,
                    td[3].text,
                    td[5].text,
                    td[7].text,
                    td[9].text,
                    td[11].text,
                    td[13].text,
                    td[15].text,
                    td[17].text,
                    td[19].text,
                    td[21].text,
                    td[23].text
                )

                dbConnection.insert("siel", response.toString())
                call.respond(response)
            }

            driver.close()
        }

        post("/arpensp") {
            val arpenspRequest = call.receive<ArpenspRequest>()

            val driver = ChromeDriver()
            login(driver)

            driver.navigate().to("http://ec2-18-231-116-58.sa-east-1.compute.amazonaws.com/arpensp/login.html")
            val firstRow =  driver.findElementById("main").findElement(By.className("container")).findElements(By.className("row")).elementAt(1)
            firstRow.findElements(By.tagName("a")).first().click()
            waitUntilPageIsReady(driver)

            driver.findElementByLinkText("C. R. C.").click()
            driver.findElementByLinkText("Busca na CRC").click()
            waitUntilPageIsReady(driver)

            driver.findElementById("c").click()
            driver.findElementByName("numero_processo").sendKeys(arpenspRequest.processNumber)
            driver.findElementByName("vara_juiz_id").sendKeys("MPSP - Ministério Público de São Paulo")
            driver.findElementByName("btn_pesquisar").click()
            waitUntilPageIsReady(driver)

            var spouse1OldName = driver.findElementByName("nome_registrado_1").getAttribute("value")
            var spouse1NewName = driver.findElementByName("novo_nome_registrado_1").getAttribute("value")
            var spouse2OldName = driver.findElementByName("nome_registrado_2").getAttribute("value")
            var spouse2NewName = driver.findElementByName("novo_nome_registrado_2").getAttribute("value")
            val marriageDate = driver.findElementByName("data_ocorrido").getAttribute("value")

            if (spouse1NewName.isNullOrEmpty()) {
                spouse1NewName = spouse1OldName
            }

            if (spouse2NewName.isNullOrEmpty()) {
                spouse2NewName = spouse2OldName
            }

            val response: ArpenspResponse = ArpenspResponse(spouse1OldName, spouse1NewName, spouse2OldName, spouse2NewName, marriageDate)

            driver.close()

            val gson = Gson()
            val objJson = gson.toJson(response)

            dbConnection.insert("Arpensp", objJson.toString())
            call.respond(response)
        }

        get("/infocrim") {
            var driver = ChromeDriver()
            val diretorio = System.getProperty("user.dir")+"/downloads"
            val chromePref = HashMap<String, Any>()
            chromePref.put("profile.default_content_settings.popups", 0)
            chromePref.put("download.default_directory", diretorio)
            chromePref.put("download.prompt_for_download" ,false)
            chromePref.put("download.directory_upgrade",true)
            chromePref.put("plugins.always_open_pdf_externally", true)
            val options = ChromeOptions()
            options.setExperimentalOption("prefs", chromePref)
            options.addArguments("--kiosk-printing")
            options.addArguments("--print-to-pdf")
            options.addArguments("disable-popup-blocking")
            val cap = DesiredCapabilities.chrome()
            cap.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true)
            cap.setCapability(ChromeOptions.CAPABILITY, options)

            driver = ChromeDriver(cap)
            login(driver)
            driver.navigate().to("http://ec2-18-231-116-58.sa-east-1.compute.amazonaws.com/infocrim/login.html")
            waitUntilPageIsReady(driver)
            driver.findElementByName("cd_usuario").sendKeys("fiap")
            driver.findElementByName("cd_senha").sendKeys("mpsp")

            driver.findElementByName("cd_senha").findElement(By.xpath("./..")).findElement(By.xpath("./..")).findElements(By.tagName("td")).last().findElements(By.tagName("a")).first().click()
            waitUntilPageIsReady(driver)
            driver.findElementById("enviar").click()
            waitUntilPageIsReady(driver)

            driver.findElementsByClassName("linhaDet").forEach {
                if (!it.findElements(By.tagName("a")).isNullOrEmpty()) {
                    it.findElement(By.tagName("a")).click()
                    waitUntilPageIsReady(driver)

                    stringToPdfBase64(driver.pageSource)?.let {
                        val response = InfocrimResponse(it)
                        call.respond(response)
                    }

                    driver.close()
                    return@get
                }
            }

        }

        post("/sivec") {
            val req = this.call.receive<SivecRequest>()
            val driver = ChromeDriver()
            login(driver)
            driver.navigate().to("http://ec2-18-231-116-58.sa-east-1.compute.amazonaws.com/sivec/login.html")
            waitUntilPageIsReady(driver)
            driver.findElementByName("nomeusuario").sendKeys("fiap")
            driver.findElementByName("senhausuario").sendKeys("mpsp")
            driver.findElementByName("Acessar").click()
            moveTo(driver,"Pesquisa", true)
            moveTo(driver,"Por Réu", true)
            val type = validateSivecSearchType(req.searchType)
            moveTo(driver,type, true)
            waitUntilPageIsReady(driver)
            driver.findElementById(getIdTextFieldForType(req.searchType)).sendKeys(req.term)
            driver.findElementById("procura").click()
            waitUntilPageIsReady(driver)
            moveTo(driver,"1.157.644",true)
            val results = driver.findElementsByClassName("textotab")
            val response = SivecResponse(results[8].text,
                results[9].text,
                results[10].text,
                results[11].text,
                results[14].text,
                results[15].text,
                results[16].text,
                results[17].text,
                convertToBolean(results[18].text),
                results[20].text,
                results[22].text,
                results[24].text,
                results[26].text,
                results[27].text,
                results[25].text,
                results[23].text,
                Address(results[28].text, results[29].text)
            )

            call.respond(response)
            driver.close()
        }

        post("/jucesp") {
            val req = this.call.receive<JucespRequest>()
            val driver = ChromeDriver()
            login(driver)
            driver.navigate().to("http://ec2-18-231-116-58.sa-east-1.compute.amazonaws.com/jucesp/index.html")
            waitUntilPageIsReady(driver)
            driver.findElementById("ctl00_cphContent_frmBuscaSimples_txtPalavraChave").sendKeys(req.companyName)
            driver.findElementById("ctl00_cphContent_frmBuscaSimples_txtPalavraChave").submit()
            waitUntilPageIsReady(driver)
            driver.findElementByClassName("btcadastro").click()
            moveTo(driver, "35225210133", true)
            waitUntilPageIsReady(driver)
            val companyName = driver.findElementById("ctl00_cphContent_frmPreVisualiza_lblEmpresa").text
            val date = driver.findElementById("ctl00_cphContent_frmPreVisualiza_lblConstituicao").text
            val initDate = driver.findElementById("ctl00_cphContent_frmPreVisualiza_lblAtividade").text
            val cnpj = driver.findElementById("ctl00_cphContent_frmPreVisualiza_lblCnpj").text
            val companyDescription = driver.findElementById("ctl00_cphContent_frmPreVisualiza_lblObjeto").text
            val capital = driver.findElementById("ctl00_cphContent_frmPreVisualiza_lblCapital").text
            val address = driver.findElementById("ctl00_cphContent_frmPreVisualiza_lblLogradouro").text
            val number = driver.findElementById("ctl00_cphContent_frmPreVisualiza_lblNumero").text
            val locale = driver.findElementById("ctl00_cphContent_frmPreVisualiza_lblBairro").text
            val complement = driver.findElementById("ctl00_cphContent_frmPreVisualiza_lblComplemento").text
            val postalCode = driver.findElementById("ctl00_cphContent_frmPreVisualiza_lblCep").text
            val city = driver.findElementById("ctl00_cphContent_frmPreVisualiza_lblMunicipio").text

            val response = JucespResponse(companyName,
                date,
                initDate,
                cnpj,
                companyDescription,
                capital,
                address,
                number,
                locale,
                complement,
                postalCode,
                city)

            call.respond(response)
            val results = driver.findElementsByClassName("btcadastro")


        }


        get("/") {
            call.respondText("chambra", contentType = ContentType.Text.Plain)
        }

        get("/json/gson") {
            call.respond(mapOf("hello" to "world"))
        }
    }
}

fun convertToBolean(term: String): Boolean {
    return term == "NÃO"
}

fun moveTo(driver: ChromeDriver,name: String, click: Boolean){
    if(click){
        val element = driver.findElementByLinkText(name)
        Actions(driver).moveToElement(element).perform()
        element.click()
    }else {
        val element = driver.findElementByLinkText(name)
        Actions(driver).moveToElement(element).perform()
    }
}

fun inputElementById(driver: ChromeDriver, elementId: String, keys: String){
    driver.findElementById(elementId).sendKeys(keys)
}

fun clickElementById(driver: ChromeDriver, elementId: String){
    driver.findElementById(elementId).click()
}

fun dropSelectOption(driver: ChromeDriver, elementId: String, optionValue: String){
    val drpIdentification = Select(driver.findElement(By.id(elementId)))
    drpIdentification.selectByValue(optionValue)
}

fun waitUntilPageIsReady(driver: ChromeDriver) {
    val executor = driver as JavascriptExecutor
    WebDriverWait(driver, 1)
        .until { executor.executeScript("return document.readyState") == "complete" }
}

fun  expandShadowRoot(parent: WebElement, driver: ChromeDriver): WebElement {
    return driver.executeScript("return arguments[0].shadowRoot", parent) as WebElement
}

fun validateSivecSearchType(searchType: String) : String {
    if (searchType == "document") {
        return "Por RG"
    }
    else if (searchType == "name") {
        return "Por Nome"
    }
    else {
        return "Matrícula SAP"
    }
}

fun getIdTextFieldForType(searchType: String) : String {
    if (searchType == "document") {
        return "idValorPesq"
    }
    else if (searchType == "name") {
        return "idNomePesq"
    }
    else {
        return "idValorPesq"
    }
}