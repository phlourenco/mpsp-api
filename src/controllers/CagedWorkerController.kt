package com.phlourenco.controllers

import com.phlourenco.definitions.*
import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import org.apache.xpath.operations.Bool
import org.openqa.selenium.By
import org.openqa.selenium.Keys
import org.openqa.selenium.chrome.ChromeDriver
import java.net.URL


fun Route.cagedWorkerController() {

    post("/cagedWorker") {
        val req = call.receive<CagedWorkerRequest>()
        val driver = ChromeDriver()
        login(driver)
        driver.navigate().to("http://ec2-18-231-116-58.sa-east-1.compute.amazonaws.com/caged/login.html")
        inputElementById(driver, "username", "12345")
        inputElementById(driver, "password", "12345")
        clickElementById(driver, "btn-submit")

        //WORKER
        moveTo(driver, "Consultas Operacionais",false)
        moveTo(driver, "Trabalhador", true)

        val select = driver.findElementById("formPesquisarTrabalhador:slctTipoPesquisaTrabalhador")

        select.findElements(By.tagName("option")).first { it.getAttribute("value") == req.searchType.toString() }.apply {
            select.sendKeys(this.text)
        }

        driver.findElementById( "formPesquisarTrabalhador:txtChavePesquisa").sendKeys(Keys.HOME, req.term)
        clickElementById(driver, "formPesquisarTrabalhador:submitPesqTrab")

        waitUntilPageIsReady(driver)


        var pdfLink = ""
        driver.findElementById("HistoricoMov_Trabalhador_2:panelTabbedPane_resumo_trabalhador_2:movimentos_rais_caged_4").findElements(By.tagName("a")).first().apply {
            val pdfUrl = this.getAttribute("href")
            val inputStream = URL(pdfUrl).openStream()
            pdfLink = uploadToS3(inputStream)
        }


        val name = driver.findElementById("txt2_Nome027").text
        val pis = driver.findElementById("txt1_Pis028").text
        var pisConverted = driver.findElementById("txt1_elos028").text
        if (pisConverted.isNullOrEmpty()) {
            pisConverted = pis
        }
        val cpf = driver.findElementById("txt3_Cpf027").text
        val birthDate = driver.findElementById("txt4_datanasc027").text
        val ctpsSerie = driver.findElementById("txt5_Ctps027").text
        val pisSituation = driver.findElementById("txt4_SitPis027").text
        val sex = driver.findElementById("txt6_Sexo027").text
        val nationality = driver.findElementById("txt7_CdNac027").text
        val color = driver.findElementById("txt9_CdRaca027").text
        val study = driver.findElementById("txt12_Instr027").text
        var hasDisability = true
        if (driver.findElementById("txt13_Def027").text == "Não") {
            hasDisability = false
        }

        val response = CagedWorkerResponse(name, pis, pisConverted, cpf, birthDate, ctpsSerie, pisSituation, sex, nationality, color, study, hasDisability, pdfLink)

        driver.close()
        call.respond(response)
    }
}