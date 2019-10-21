package com.phlourenco.controllers

import com.phlourenco.definitions.*
import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import org.openqa.selenium.Keys
import org.openqa.selenium.chrome.ChromeDriver

fun Route.cagedCompanyController() {

    post("/cagedCompany"){
        val req = call.receive<CagedCompanyRequest>()
        val driver = ChromeDriver()
        login(driver)
        driver.navigate().to("http://ec2-18-231-116-58.sa-east-1.compute.amazonaws.com/caged/login.html")
        inputElementById(driver, "username", "12345")
        inputElementById(driver, "password", "12345")
        clickElementById(driver, "btn-submit")

        //COMPANY
        moveTo(driver, "Consultas Operacionais",false)
        moveTo(driver, "Empresa", true)

        driver.findElementById( "formPesquisarEmpresaCAGED:txtcnpjRaiz").sendKeys(Keys.HOME, req.cnpj)
        clickElementById(driver, "formPesquisarEmpresaCAGED:btConsultar")

        val companyCnpj = driver.findElementById("formResumoEmpresaCaged:txtCnpjRaiz").text
        val companySocialReason = driver.findElementById("formResumoEmpresaCaged:txtRazaoSocial").text
        val companyCnae = driver.findElementById("formResumoEmpresaCaged:txtCodigoAtividadeEconomica").text
        val companyCnae2 = driver.findElementById("formResumoEmpresaCaged:txtDescricaoAtividadeEconomica").text
        val companyCnaeFull = companyCnae + " - " + companyCnae2

        val subsidiaries = driver.findElementById("formResumoEmpresaCaged:txtNumFiliais").text
        val admissions = driver.findElementById("formResumoEmpresaCaged:txtTotalNumAdmissoes").text
        val demissions = driver.findElementById("formResumoEmpresaCaged:txtTotalNumDesligamentos").text

        val response = CagedCompanyResponse(companyCnpj, companySocialReason, companyCnaeFull, subsidiaries, admissions, demissions)

        call.respond(response)
        driver.close()

    }
}