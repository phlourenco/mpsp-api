package com.phlourenco

import ch.qos.logback.core.util.FileUtil
import com.phlourenco.arisp.*
import com.phlourenco.sitel.*
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.gson.*
import io.ktor.features.*
import io.ktor.request.receive
import javafx.scene.chart.ValueAxis
import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.OutputType
import org.openqa.selenium.TakesScreenshot
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.support.ui.WebDriverWait
import java.io.File
import org.openqa.selenium.WebElement
import java.awt.SystemColor.window
import org.openqa.selenium.support.ui.ExpectedConditions





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
            val options = ChromeOptions()
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

        get(path = "/infocrim") {
            val driver = ChromeDriver()
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
                    val screenshot = (driver as TakesScreenshot).getScreenshotAs(OutputType.FILE)
                    driver.executeScript(" javascript:imprime();")
                    driver.executeScript("window.scrollBy(0,1000)");
                    return@get
                }
            }





//            val screenshotFile = (driver as TakesScreenshot)?.getScreenshotAs(OutputType.FILE)
//            print(screenshotFile.absolutePath)
//            driver.navigate().to(screenshotFile.absolutePath)

//            val firstLink =  driver.findElementsByClassName("linhaDet").first {
//                (it.findElement(By.tagName("a")) != null)
//            }
//
//            firstLink.click()



        }

        get("/json/gson") {
            call.respond(mapOf("hello" to "world"))
        }
    }
}

fun waitUntilPageIsReady(driver: ChromeDriver) {
    val executor = driver as JavascriptExecutor
    WebDriverWait(driver, 1)
        .until { executor.executeScript("return document.readyState") == "complete" }
}

fun  expandShadowRoot(parent: WebElement, driver: ChromeDriver): WebElement {
    return driver.executeScript("return arguments[0].shadowRoot", parent) as WebElement
}
