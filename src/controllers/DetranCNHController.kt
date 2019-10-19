package com.phlourenco.controllers
import DetranCNHRequest
import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.routing.*
import org.openqa.selenium.TakesScreenshot
import org.openqa.selenium.chrome.ChromeDriver


fun Route.detranCNHController() {
    post("/detran") {
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
        moveTo(driver, "Consultar Imagem da CNH",true)
        driver.findElementById("form:cpf").sendKeys(req.cpf)
        moveTo(driver,"Pesquisar",true)
        waitUntilPageIsReady(driver)
        val cnh  = driver.getScreenshotAs()
    }
}