package com.phlourenco.controllers
import com.google.gson.Gson
import com.phlourenco.definitions.CensecPart
import com.phlourenco.definitions.CensecRequest
import com.phlourenco.definitions.CensecResponse
import io.ktor.application.call
import io.ktor.request.header
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*
import org.openqa.selenium.By
import org.openqa.selenium.chrome.ChromeDriver


fun Route.censecController() {
    post("/censec") {
        val req = this.call.receive<CensecRequest>()
        val driver = ChromeDriver()

        login(driver)
        driver.navigate().to("http://ec2-18-231-116-58.sa-east-1.compute.amazonaws.com/censec/login.html")
       // waitUntilPageIsReady(driver)

        driver.findElementById("LoginTextBox").sendKeys("teste")
        driver.findElementById("SenhaTextBox").sendKeys("teste")
        driver.findElementById("EntrarButton").click()


        moveTo(driver,"Centrais",false)
        moveTo(driver,"CESDI",false)
        moveTo(driver,"Consulta Ato", true)

        driver.findElementById("ctl00_ContentPlaceHolder1_DocumentoTextBox").sendKeys(req.cpfCnpj)

        driver.findElementById("ctl00_ContentPlaceHolder1_BuscarButton").click()

        val results = driver.findElementsByName("SelecaoRadio")

        results[0].click() // Caso tenho necessidade de percorrer todos results

        driver.findElementById("ctl00_ContentPlaceHolder1_VisualizarButton").click()


        val office = driver.findElementById("ctl00_ContentPlaceHolder1_CodigoTextBox").getAttribute("value")
        val month = driver.findElementById("ctl00_ContentPlaceHolder1_MesReferenciaDropDownList").text.trim()
        val year = driver.findElementById("ctl00_ContentPlaceHolder1_AnoReferenciaDropDownList").text.trim()

        val date = "$month de $year"

        val act = driver.findElementById("ctl00_ContentPlaceHolder1_TipoAtoDropDownList").text.trim().replace("\n", "")
        val actDay = driver.findElementById("ctl00_ContentPlaceHolder1_DiaAtoTextBox").getAttribute("value")
        val actMonth = driver.findElementById("ctl00_ContentPlaceHolder1_MesAtoTextBox").getAttribute("value")
        val actYear = driver.findElementById("ctl00_ContentPlaceHolder1_AnoAtoTextBox").getAttribute("value")

        val actDate = "${actDay}/${actMonth}/${actYear}"

        val book = driver.findElementById("ctl00_ContentPlaceHolder1_LivroTextBox").getAttribute("value")
        val bookComplement = driver.findElementById("ctl00_ContentPlaceHolder1_LivroComplementoTextBox").getAttribute("value")
        val page = driver.findElementById("ctl00_ContentPlaceHolder1_FolhaTextBox").getAttribute("value")
        val pageComplement = driver.findElementById("ctl00_ContentPlaceHolder1_FolhaComplementoTextBox").getAttribute("value")

        val arrayParts = mutableListOf<CensecPart>()

        driver.findElementById("ctl00_ContentPlaceHolder1_PartesUpdatePanel").findElements(By.tagName("tr")).forEach {
            val name = it.findElements(By.tagName("td"))[1].text.trim()
            val cpfCnpj = it.findElements(By.tagName("td"))[2].text.trim()
            val role = it.findElements(By.tagName("td"))[3].text
            val part = CensecPart(name, cpfCnpj, role)
            arrayParts.add(part)
        }

        val response = CensecResponse(
            office,
            date,
            act,
            actDate,
            book,
            bookComplement,
            page,
            pageComplement,
            arrayParts
        )

//        val list = listOf<CensecResponseItem>(item)
//        val response = CensecResponse(list)

        driver.close()

        call.request.header("reportId")?.apply {
            val responseMap = response.serializeToMap().toMutableMap()
            responseMap["reportId"] = this
            DatabaseService.insert("censec", Gson().toJson(responseMap).toString())
        }

        call.respond(response)
    }
}