package com.phlourenco

import com.phlourenco.arisp.*
import com.phlourenco.cadesp.CadespResponse
import com.phlourenco.caged.*
import com.phlourenco.sitel.*
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.gson.*
import io.ktor.features.*
import io.ktor.request.receive
import org.eclipse.jetty.util.ajax.JSON
import org.json.JSONObject
import org.json.JSONStringer
import org.json.JSONWriter
import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.Keys
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.interactions.Actions
import org.openqa.selenium.support.ui.Select
import org.openqa.selenium.support.ui.WebDriverWait

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads

fun Application.module(testing: Boolean = false) {
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

    routing {
        get("/arisp") {
            val driver = ChromeDriver()
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

            driver.findElementById("TipoPesquisa").sendKeys(SearchType.pessoa.getTitle())
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
            driver.findElementById("chkHabilitar").click()
            driver.findElementById("Prosseguir").click()
            waitUntilPageIsReady(driver)

            driver.findElementById("filterTipo").sendKeys(PersonType.juridica.getTitle())
            driver.findElementById("filterDocumento").sendKeys("12019797658")
            driver.findElementById("btnPesquisar").click()
            waitUntilPageIsReady(driver)
            driver.executeScript("javascript:SelecionarTudo();")
            driver.findElementsById("btnProsseguir").first { it.isEnabled }?.getAttribute("onclick")?.let {
                driver.executeScript(it)
            }
            waitUntilPageIsReady(driver)

            driver.findElementById("panelMatriculas").findElements(By.tagName("tr")).filter { it.isDisplayed }.forEach {
                val td = it.findElements(By.tagName("td"))
                val registry = ArispRegistry(cityName = td[0].text, office = td[1].text, registryId = td[2].text, registryFileUrl = "http://link.com/arquivo.pdf")
                println(registry)
//                driver.executeScript("javascript:VisualizarMatricula(10,30098);")
            }
            driver.close();
        }


        post("/cadesp") {

            val driver = ChromeDriver()

            login(driver)
            driver.navigate().to("http://ec2-18-231-116-58.sa-east-1.compute.amazonaws.com/cadesp/login.html")
            inputElementById(driver, "ctl00_conteudoPaginaPlaceHolder_loginControl_UserName", "12345")
            inputElementById(driver, "ctl00_conteudoPaginaPlaceHolder_loginControl_Password", "12345")
            clickElementById(driver, "ctl00_conteudoPaginaPlaceHolder_loginControl_loginButton")
            waitUntilPageIsReady(driver)
            moveTo(driver, "Consultas",false)
            moveTo(driver, "Cadastro", true)
            dropSelectOption(driver, "ctl00_conteudoPaginaPlaceHolder_tcConsultaCompleta_TabPanel1_lstIdentificacao", "2")
            inputElementById(driver,"ctl00_conteudoPaginaPlaceHolder_tcConsultaCompleta_TabPanel1_txtIdentificacao", "12345678912")
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
            moveTo(driver, "Consultas Operacionais",false)
            moveTo(driver, "Autorizado/Responsável", true)
            driver.findElementById( "formPesquisarAutorizado:txtChavePesquisaAutorizado014").sendKeys(Keys.HOME,"00000000000000")
            clickElementById(driver, "formPesquisarAutorizado:bt027_8")

            val cnpjCeiCpf = driver.findElementById("txCnpj020_2").text
            val identificationName = driver.findElementById("txtrazaosocial020_4").text

            val addressStreet = driver.findElementById("txt3_logradouro020").text
            val addressCity = driver.findElementById("txt6_municipio020").text
            val addressState = driver.findElementById("txt7_uf020").text
            val addressNeighBorHood = driver.findElementById("txt4_bairro020").text
            val addressCep = driver.findElementById("txt5_codmunicipio020").text

            val ContactName = driver.findElementById("txt_nome_contato").text
            val ContactCpf = driver.findElementById("txt_contato_cpf").text
            val ContactLine = driver.findElementById("txt10_ramal020").text
            val ContactEmail = driver.findElementById("txt11_email").text
            val ContactDdd = driver.findElementById("txt21_ddd020").text
            val ContactPhone = driver.findElementById("txt9_telefone020").text

            waitUntilPageIsReady(driver)

            val responseResponsible: CagedResponseResponsible = CagedResponseResponsible(
                identification(cnpjCeiCpf,identificationName),
                address(addressStreet,addressNeighBorHood,addressCity,addressState,addressCep),
                contact(ContactName,ContactCpf, phone(ContactDdd,ContactPhone),ContactLine,ContactEmail)
            )

            waitUntilPageIsReady(driver)

            //COMPANY
            moveTo(driver, "Consultas Operacionais",false)
            moveTo(driver, "Empresa", true)

            driver.findElementById( "formPesquisarEmpresaCAGED:txtcnpjRaiz").sendKeys(Keys.HOME,"00000000000000")
            clickElementById(driver, "formPesquisarEmpresaCAGED:btConsultar")




            call.respond(responseResponsible)
            driver.close()

        }

        post("/sitel") {
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

                val response =  SitelResponse(
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

                call.respond(response)
            }

            driver.close()
        }

        get("/") {
            call.respondText("HELLO WORLD!", contentType = ContentType.Text.Plain)
        }


        get("/json/gson") {
            call.respond(mapOf("hello" to "world"))
        }
    }
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
