package com.phlourenco.controllers
import com.phlourenco.definitions.CensecPart
import com.phlourenco.definitions.CensecResponse
import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.*
import org.openqa.selenium.By
import org.openqa.selenium.chrome.ChromeDriver


fun Route.censecController() {
    post("/censec") {
        //val req = this.call.receive<CensecRequest>()
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



        driver.findElementById("ctl00_ContentPlaceHolder1_DocumentoTextBox").sendKeys("12312312312")

        driver.findElementById("ctl00_ContentPlaceHolder1_BuscarButton").click()

        val results = driver.findElementsByName("SelecaoRadio")

        results[0].click() // Caso tenho necessidade de percorrer todos results

        driver.findElementById("ctl00_ContentPlaceHolder1_VisualizarButton").click()


        val office = driver.findElementById("ctl00_ContentPlaceHolder1_CodigoTextBox").getAttribute("value")
        val month = driver.findElementById("ctl00_ContentPlaceHolder1_MesReferenciaDropDownList").text
        val year = driver.findElementById("ctl00_ContentPlaceHolder1_AnoReferenciaDropDownList").text

        val date = ""+ month + " " + year

//        val date = "${month}${year}"
//

        val act = driver.findElementById("ctl00_ContentPlaceHolder1_TipoAtoDropDownList").text
        val actDay = driver.findElementById("ctl00_ContentPlaceHolder1_DiaAtoTextBox").text
        val actMonth = driver.findElementById("ctl00_ContentPlaceHolder1_MesAtoTextBox").text
        val actYear = driver.findElementById("ctl00_ContentPlaceHolder1_AnoAtoTextBox").text

       // val actDate = "${actDay}${actMonth}${actYear}"

        val actDate = ""+ actDay + "" + actMonth + "" + actYear


        val book = driver.findElementById("ctl00_ContentPlaceHolder1_LivroTextBox").text
        val bookComplement = driver.findElementById("ctl00_ContentPlaceHolder1_LivroComplementoTextBox").text
        val page = driver.findElementById("ctl00_ContentPlaceHolder1_FolhaTextBox").text
        val pageComplement = driver.findElementById("ctl00_ContentPlaceHolder1_FolhaComplementoTextBox").text

        val arrayParts = mutableListOf<CensecPart>()

        driver.findElementById("ctl00_ContentPlaceHolder1_PartesUpdatePanel").findElements(By.tagName("tr")).forEach {
            val name = it.findElements(By.tagName("td"))[1].text
            val cpfCnpj = it.findElements(By.tagName("td"))[2].text
            val role = it.findElements(By.tagName("td"))[3].text
            val part = CensecPart(name, cpfCnpj, role)
            arrayParts.add(part)
        }

        val response: CensecResponse = CensecResponse(
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

        call.respond(response)


    }
}