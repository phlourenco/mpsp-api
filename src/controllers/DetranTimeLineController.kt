package com.phlourenco.controllers

import com.phlourenco.definitions.DetranTimeLineRequest
import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.routing.Route
import io.ktor.routing.post
import org.openqa.selenium.chrome.ChromeDriver

fun Route.detranTimeLineController() {
    post("/detranTimeLine") {
        val req = this.call.receive<DetranTimeLineRequest>()
        val driver = ChromeDriver()

        login(driver)
        driver.navigate().to("http://ec2-18-231-116-58.sa-east-1.compute.amazonaws.com/detran/login.html")
        waitUntilPageIsReady(driver)
        driver.findElementById("form:j_id563205015_44efc1ab").sendKeys("fiap")
        driver.findElementById("form:j_id563205015_44efc191").sendKeys("mpsp")
        driver.findElementById("form:j_id563205015_44efc15b").click()
        waitUntilPageIsReady(driver)
        driver.findElementById("navigation_a_M_16").click()
        moveTo(driver, "Linha da Vida do Condutor",true)
        waitUntilPageIsReady(driver)
        driver.findElementById("form:registro").sendKeys(req.registry)
        driver.findElementById("form:rg").sendKeys(req.rg)
        driver.findElementById("form:nome").sendKeys(req.conductorName)
        driver.findElementById("form:pgu").sendKeys(req.pgu)
        driver.findElementById("form:j_id2049423534_c43225e_focus").sendKeys(req.uf)
        moveTo(driver,"Pesquisar",true)
    }
}