package com.phlourenco.controllers

import com.google.gson.Gson
import com.phlourenco.definitions.CadespRequest
import com.phlourenco.definitions.CadespResponse
import io.ktor.application.call
import io.ktor.request.header
import io.ktor.response.respond
import io.ktor.routing.*
import org.openqa.selenium.chrome.ChromeDriver


fun Route.cadespController() {
    get("/cadesp/{cnpj}") {

        val driver = ChromeDriver()

        val cadespRequest: CadespRequest = CadespRequest(call.parameters["cnpj"]!!)

        login(driver)
        driver.navigate().to("http://ec2-18-231-116-58.sa-east-1.compute.amazonaws.com/cadesp/login.html")
        inputElementById(driver, "ctl00_conteudoPaginaPlaceHolder_loginControl_UserName", "12345")
        inputElementById(driver, "ctl00_conteudoPaginaPlaceHolder_loginControl_Password", "12345")
        clickElementById(driver, "ctl00_conteudoPaginaPlaceHolder_loginControl_loginButton")
        waitUntilPageIsReady(driver)
        moveTo(driver, "Consultas",false)
        moveTo(driver, "Cadastro", true)
        dropSelectOption(driver, "ctl00_conteudoPaginaPlaceHolder_tcConsultaCompleta_TabPanel1_lstIdentificacao", "2")
        inputElementById(driver,"ctl00_conteudoPaginaPlaceHolder_tcConsultaCompleta_TabPanel1_txtIdentificacao", cadespRequest.cnpj)
        clickElementById(driver,"ctl00_conteudoPaginaPlaceHolder_tcConsultaCompleta_TabPanel1_btnConsultarEstabelecimento")

        val td = driver.findElementsByClassName("dadoDetalhe")
        val tdAll = driver.findElementsByTagName("td")

        var situation: Boolean = false
        var registrationSituation: Boolean = false
        var taxOccurrence: Boolean = false

        if(td[4].text == "Situação:  Ativo"){
            situation = true
        }

        if(td[23].text == "Ativo"){
            registrationSituation = true
        }

        if(tdAll[121].text == "Ativa"){
            taxOccurrence = true
        }

        val ie = td[3].text
        val cnpj = td[5].text
        val businessName = td[7].text
        val drt = td[9].text
        val dateStateRegistration = td[6].text
        val stateRegime = td[8].text
        val taxOffice = td[10].text
        val fantasyName = td[15].text
        val nire = td[22].text
        val unitType = td[27].text
        val ieStartDate = td[20].text
        val dateStartedSituation = td[24].text
        val practices = td[29].text
        val response: CadespResponse = CadespResponse(ie, cnpj, businessName, drt, situation, dateStateRegistration, stateRegime, taxOffice, fantasyName, nire, registrationSituation, taxOccurrence, unitType, ieStartDate, dateStartedSituation, practices);

        driver.close()

        call.request.header("reportId")?.apply {
            val responseMap = response.serializeToMap().toMutableMap()
            responseMap["reportId"] = this
            DatabaseService.insert("cadesp", Gson().toJson(responseMap).toString())
        }

        call.respond(response)
    }
}

