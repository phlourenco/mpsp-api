package com.phlourenco.controllers

import com.google.gson.Gson
import com.phlourenco.definitions.InfocrimRequest
import com.phlourenco.definitions.InfocrimResponse
import io.ktor.application.call
import io.ktor.request.header
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*
import org.openqa.selenium.By
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.remote.CapabilityType
import org.openqa.selenium.remote.DesiredCapabilities

fun Route.infocrimController() {

    post("/infocrim") {
        val req = this.call.receive<InfocrimRequest>()
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

        driver.findElementByName("cd_senha").findElement(By.xpath("./..")).findElement(By.xpath("./..")).findElements(
            By.tagName("td")).last().findElements(By.tagName("a")).first().click()
        waitUntilPageIsReady(driver)

        val select = driver.findElementByName("inst")
        select.findElements(By.tagName("option")).first { it.getAttribute("value") == req.institution.toString() }.apply {
            select.sendKeys(this.text)
        }

        driver.findElementById("enviar").click()
        waitUntilPageIsReady(driver)

        driver.findElementsByClassName("linhaDet").forEach {
            if (!it.findElements(By.tagName("a")).isNullOrEmpty()) {
                it.findElement(By.tagName("a")).click()
                waitUntilPageIsReady(driver)

                stringToPdf(driver.pageSource)?.let {
                    val pdfUrl = uploadToS3(it)
                    val response = InfocrimResponse(pdfUrl)
                    driver.close()


                    call.request.header("reportId")?.apply {
                        val responseMap = response.serializeToMap().toMutableMap()
                        responseMap["reportId"] = this
                        DatabaseService.insert("infocrim", Gson().toJson(responseMap).toString())
                    }

                    call.respond(response)
                }
//                return@post
            }
        }
    }
}