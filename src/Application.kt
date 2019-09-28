package com.phlourenco

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.mongodb.MongoClientURI
import com.mongodb.util.JSON
import com.phlourenco.Database.dbConnection
import com.phlourenco.arisp.*
import com.phlourenco.cadesp.CadespResponse
import com.phlourenco.arpensp.ArpenspRequest
import com.phlourenco.arpensp.ArpenspResponse
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
import jdk.nashorn.internal.parser.JSONParser
import org.bson.Document
import org.litote.kmongo.KMongo
import org.litote.kmongo.insertOne
import org.litote.kmongo.json
import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.interactions.Actions
import org.openqa.selenium.support.ui.Select
import org.openqa.selenium.support.ui.WebDriverWait

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads

fun Application.module(testing: Boolean = false) {
    val dbConnection: dbConnection = dbConnection()

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

        post("/arpensp") {
            val req = call.receive<ArpenspRequest>()

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
            driver.findElementByName("numero_processo").sendKeys(req.processNumber)
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
            call.respond(response)
            val gson = Gson()
            val objJson = gson.toJson(response)

            dbConnection.insert("Arpensp", objJson.toString())


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
