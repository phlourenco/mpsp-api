package com.phlourenco.controllers

import com.phlourenco.definitions.JucespRequest
import com.phlourenco.definitions.JucespResponse
import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import org.openqa.selenium.By
import org.openqa.selenium.chrome.ChromeDriver
import java.net.URL

fun Route.jucespController() {

    post("/jucesp") {
        val req = this.call.receive<JucespRequest>()
        val driver = ChromeDriver()
        login(driver)
        driver.navigate().to("http://ec2-18-231-116-58.sa-east-1.compute.amazonaws.com/jucesp/index.html")
        waitUntilPageIsReady(driver)
        driver.findElementById("ctl00_cphContent_frmBuscaSimples_txtPalavraChave").sendKeys(req.companyName)
        driver.findElementById("ctl00_cphContent_frmBuscaSimples_txtPalavraChave").submit()
        waitUntilPageIsReady(driver)
        driver.findElementByClassName("btcadastro").click()
        moveTo(driver, "35225210133", true)
        waitUntilPageIsReady(driver)
        val companyName = driver.findElementById("ctl00_cphContent_frmPreVisualiza_lblEmpresa").text
        val date = driver.findElementById("ctl00_cphContent_frmPreVisualiza_lblConstituicao").text
        val initDate = driver.findElementById("ctl00_cphContent_frmPreVisualiza_lblAtividade").text
        val cnpj = driver.findElementById("ctl00_cphContent_frmPreVisualiza_lblCnpj").text
        val companyDescription = driver.findElementById("ctl00_cphContent_frmPreVisualiza_lblObjeto").text
        val capital = driver.findElementById("ctl00_cphContent_frmPreVisualiza_lblCapital").text
        val address = driver.findElementById("ctl00_cphContent_frmPreVisualiza_lblLogradouro").text
        val number = driver.findElementById("ctl00_cphContent_frmPreVisualiza_lblNumero").text
        val locale = driver.findElementById("ctl00_cphContent_frmPreVisualiza_lblBairro").text
        val complement = driver.findElementById("ctl00_cphContent_frmPreVisualiza_lblComplemento").text
        val postalCode = driver.findElementById("ctl00_cphContent_frmPreVisualiza_lblCep").text
        val city = driver.findElementById("ctl00_cphContent_frmPreVisualiza_lblMunicipio").text

        driver.findElementsByClassName("btcadastro").first { it.getAttribute("onclick").contains(".pdf") }.apply {
            var link = this.getAttribute("onclick")
            link =  "http://ec2-18-231-116-58.sa-east-1.compute.amazonaws.com/jucesp/" + link.substringAfter("'").substringBefore("'")
            val inputStream = URL(link).openStream()
            val s3Link = uploadToS3(inputStream)
            val response = JucespResponse(companyName,
                date,
                initDate,
                cnpj,
                companyDescription,
                capital,
                address,
                number,
                locale,
                complement,
                postalCode,
                city,
                s3Link)

            call.respond(response)
        }
    }
}