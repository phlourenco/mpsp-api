package com.phlourenco.controllers

import com.phlourenco.definitions.CagedResponseCompany
import com.phlourenco.definitions.companyResponse
import com.phlourenco.definitions.detailResponse
import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import org.openqa.selenium.Keys
import org.openqa.selenium.chrome.ChromeDriver

fun Route.cagedController() {

    post("/caged"){
        val driver = ChromeDriver()
        login(driver)
        driver.navigate().to("http://ec2-18-231-116-58.sa-east-1.compute.amazonaws.com/caged/login.html")
        inputElementById(driver, "username", "12345")
        inputElementById(driver, "password", "12345")
        clickElementById(driver, "btn-submit")

        //RESPONSIBLE
//            moveTo(driver, "Consultas Operacionais",false)
//            moveTo(driver, "Autorizado/Responsável", true)
//            driver.findElementById( "formPesquisarAutorizado:txtChavePesquisaAutorizado014").sendKeys(Keys.HOME,"00000000000000")
//            clickElementById(driver, "formPesquisarAutorizado:bt027_8")
//
//            val cnpjCeiCpf = driver.findElementById("txCnpj020_2").text
//            val identificationName = driver.findElementById("txtrazaosocial020_4").text
//
//            val addressStreet = driver.findElementById("txt3_logradouro020").text
//            val addressCity = driver.findElementById("txt6_municipio020").text
//            val addressState = driver.findElementById("txt7_uf020").text
//            val addressNeighBorHood = driver.findElementById("txt4_bairro020").text
//            val addressCep = driver.findElementById("txt5_codmunicipio020").text
//
//            val ContactName = driver.findElementById("txt_nome_contato").text
//            val ContactCpf = driver.findElementById("txt_contato_cpf").text
//            val ContactLine = driver.findElementById("txt10_ramal020").text
//            val ContactEmail = driver.findElementById("txt11_email").text
//            val ContactDdd = driver.findElementById("txt21_ddd020").text
//            val ContactPhone = driver.findElementById("txt9_telefone020").text
//
//            waitUntilPageIsReady(driver)
//
//            val responseResponsible: CagedResponseResponsible = CagedResponseResponsible(
//                identification(cnpjCeiCpf,identificationName),
//                address(addressStreet,addressNeighBorHood,addressCity,addressState,addressCep),
//                contact(ContactName,ContactCpf, phone(ContactDdd,ContactPhone),ContactLine,ContactEmail)
//            )
//
//            waitUntilPageIsReady(driver)

        //COMPANY
        moveTo(driver, "Consultas Operacionais",false)
        moveTo(driver, "Empresa", true)

        driver.findElementById( "formPesquisarEmpresaCAGED:txtcnpjRaiz").sendKeys(Keys.HOME,"00000000000000")
        clickElementById(driver, "formPesquisarEmpresaCAGED:btConsultar")
        driver.findElementById( "formPesquisarEmpresaCAGED:txtcnpjRaiz").sendKeys(Keys.HOME,"00000000000000")
        clickElementById(driver, "formPesquisarEmpresaCAGED:btConsultar")

        val companyCnpj = driver.findElementById("formResumoEmpresaCaged:txtCnpjRaiz").text
        val companySocialReason = driver.findElementById("formResumoEmpresaCaged:txtRazaoSocial").text
        val companyCnae = driver.findElementById("formResumoEmpresaCaged:txtCodigoAtividadeEconomica").text
        val companyCnae2 = driver.findElementById("formResumoEmpresaCaged:txtDescricaoAtividadeEconomica").text
        val companyCnaeFull = companyCnae + " - " + companyCnae2

        val companyResponse: companyResponse =
            companyResponse(
                companyCnpj,
                companySocialReason,
                companyCnaeFull
            )

        val subsidiaries = driver.findElementById("formResumoEmpresaCaged:txtNumFiliais").text
        val admissions = driver.findElementById("formResumoEmpresaCaged:txtTotalNumAdmissoes").text
        val demissions = driver.findElementById("formResumoEmpresaCaged:txtTotalNumDesligamentos").text

        val detailResponse: detailResponse = detailResponse(
            subsidiaries,
            admissions,
            demissions
        )

        val CagedResponseCompany: CagedResponseCompany =
            CagedResponseCompany(
                companyResponse,
                detailResponse
            )


        call.respond(CagedResponseCompany)
        driver.close()

    }
}