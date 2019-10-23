package com.phlourenco.controllers

import com.google.gson.Gson
import com.phlourenco.definitions.*
import io.ktor.application.call
import io.ktor.request.header
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*
import org.openqa.selenium.By
import org.openqa.selenium.Keys
import org.openqa.selenium.chrome.ChromeDriver

fun Route.cagedResponsibleController() {

    post("/cagedResponsible") {
        val req = call.receive<CagedResponsibleRequest>()
        val driver = ChromeDriver()
        login(driver)
        driver.navigate().to("http://ec2-18-231-116-58.sa-east-1.compute.amazonaws.com/caged/login.html")
        inputElementById(driver, "username", "12345")
        inputElementById(driver, "password", "12345")
        clickElementById(driver, "btn-submit")

        //RESPONSIBLE
        moveTo(driver, "Consultas Operacionais",false)
        moveTo(driver, "Autorizado/Responsável", true)

        val select = driver.findElementById("formPesquisarAutorizado:slctTipoPesquisaAutorizado")

        select.findElements(By.tagName("option")).first { it.getAttribute("value") == req.searchType.toString() }.apply {
            select.sendKeys(this.text)
        }

//        driver.findElementById( "formPesquisarAutorizado:txtChavePesquisaAutorizado014").sendKeys(Keys.HOME, req.term)
//        clickElementById(driver, "formPesquisarAutorizado:bt027_8")
//
        waitUntilPageIsReady(driver)

        val cnpjCeiCpf = driver.findElementById("txCnpj020_2").text
        val identificationName = driver.findElementById("txtrazaosocial020_4").text

        val addressStreet = driver.findElementById("txt3_logradouro020").text
        val addressCity = driver.findElementById("txt6_municipio020").text
        val addressState = driver.findElementById("txt7_uf020").text
        val addressNeighBorHood = driver.findElementById("txt4_bairro020").text
        val addressCep = driver.findElementById("txt5_codmunicipio020").text

        val contactName = driver.findElementById("txt_nome_contato").text
        val contactCpf = driver.findElementById("txt_contato_cpf").text
        val contactLine = driver.findElementById("txt10_ramal020").text
        val contactEmail = driver.findElementById("txt11_email").text
        val contactDdd = driver.findElementById("txt21_ddd020").text
        val contactPhone = driver.findElementById("txt9_telefone020").text

        val response = CagedResponsibleResponse(
            CagedResponsibleIdentification(cnpjCeiCpf,identificationName),
            CagedResponsibleAddress(addressStreet,addressNeighBorHood,addressCity,addressState,addressCep),
            CagedResponsibleContact(contactName, contactCpf, "${contactDdd}${contactPhone}", contactLine, contactEmail)
        )

        driver.close()

        call.request.header("reportId")?.apply {
            val responseMap = response.serializeToMap().toMutableMap()
            responseMap["reportId"] = this
            DatabaseService.insert("cagedResponsible", Gson().toJson(responseMap).toString())
        }
        call.respond(response)
    }
}