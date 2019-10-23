package com.phlourenco.controllers

import com.google.gson.Gson
import com.phlourenco.definitions.Address
import com.phlourenco.definitions.SivecRequest
import com.phlourenco.definitions.SivecResponse
import io.ktor.application.call
import io.ktor.request.header
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import org.openqa.selenium.chrome.ChromeDriver

fun Route.sivecController() {
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

        driver.close()

        call.request.header("reportId")?.apply {
            val responseMap = response.serializeToMap().toMutableMap()
            responseMap["reportId"] = this
            DatabaseService.insert("sivec", Gson().toJson(responseMap).toString())
        }

        call.respond(response)
    }
}