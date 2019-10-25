package com.phlourenco.controllers
import com.google.gson.Gson
import com.phlourenco.definitions.*
import com.phlourenco.utils.closeAllTabs
import io.ktor.application.call
import io.ktor.request.header
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*
import org.openqa.selenium.chrome.ChromeDriver


fun Route.detranCNHController() {
    post("/detranCNH") {
        val req = this.call.receive<DetranCNHRequest>()
        val driver = ChromeDriver()

        login(driver)
        driver.navigate().to("http://ec2-18-231-116-58.sa-east-1.compute.amazonaws.com/detran/login.html")
        waitUntilPageIsReady(driver)
        driver.findElementById("form:j_id563205015_44efc1ab").sendKeys("fiap")
        driver.findElementById("form:j_id563205015_44efc191").sendKeys("mpsp")
        driver.findElementById("form:j_id563205015_44efc15b").click()
        waitUntilPageIsReady(driver)
        driver.findElementById("navigation_a_M_16").click()
        driver.findElementsById("navigation_a_F_16").first { it.text == "Consultar Imagem da CNH" }.click()
        driver.findElementById("form:cpf").sendKeys(req.cpf)
        moveTo(driver,"Pesquisar",true)
        val tabs = ArrayList(driver.windowHandles)
        driver.switchTo().window(tabs[1])
        val results = driver.findElementsByClassName("bold")
        val response = DetranCNHResponse(driver.findElementById("form:imgFoto").getAttribute("src"),
            results[1].text,
            results[2].text,
            results[3].text,
            results[4].text,
            results[5].text,
            results[6].text,
            results[7].text,
            results[8].text,
            results[9].text,
            results[10].text,
            results[11].text)

        driver.closeAllTabs()

        call.request.header("reportId")?.apply {
            val responseMap = response.serializeToMap().toMutableMap()
            responseMap["reportId"] = this
            DatabaseService.insert("detranCNH", Gson().toJson(responseMap).toString())
        }

        call.respond(response)
    }
}