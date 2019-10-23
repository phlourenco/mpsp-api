package com.phlourenco.controllers

import com.google.gson.Gson
import com.phlourenco.definitions.ArpenspRequest
import com.phlourenco.definitions.ArpenspResponse
import io.ktor.application.call
import io.ktor.request.header
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import org.openqa.selenium.By
import org.openqa.selenium.chrome.ChromeDriver

fun Route.arpenspController() {

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

        call.request.header("reportId")?.apply {
            val responseMap = response.serializeToMap().toMutableMap()
            responseMap["reportId"] = this
            DatabaseService.insert("arpensp", Gson().toJson(responseMap).toString())
        }

        call.respond(response)
    }
}