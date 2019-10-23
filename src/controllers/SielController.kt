package com.phlourenco.controllers

import com.phlourenco.definitions.SitelResponse
import com.phlourenco.definitions.SitelSearch
import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import org.openqa.selenium.By
import org.openqa.selenium.chrome.ChromeDriver

fun Route.sielController() {

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

            DatabaseService.insert("siel", response.toString())
            call.respond(response)
        }

        driver.close()
    }
}

