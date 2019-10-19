package com.phlourenco.controllers

import com.amazonaws.AmazonServiceException
import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.regions.Regions
import com.phlourenco.definitions.*
import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*
import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import java.net.URL
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.CannedAccessControlList
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import com.amazonaws.services.s3.model.Region
import org.litote.kmongo.MongoOperator
import java.io.File
import java.lang.Exception
import com.amazonaws.auth.BasicAWSCredentials
import com.google.gson.Gson
import java.util.*

fun Route.arispController() {

    post("/arisp") {

        val req = call.receive<ArispRequest>()

        val options = ChromeOptions()

        val searchType: SearchType
        options.addArguments("disable-infobars")
        options.addArguments("--print-to-pdf")

        val driver = ChromeDriver(options)
        login(driver);

        driver.navigate().to("http://ec2-18-231-116-58.sa-east-1.compute.amazonaws.com/arisp/login.html")
        waitUntilPageIsReady(driver)
        driver.findElementById("btnCallLogin").click()
        waitUntilPageIsReady(driver)
        driver.findElementById("btnAutenticar").click()
        waitUntilPageIsReady(driver)

        val solicitacoesMenuItem = driver.findElementById("liInstituicoes").findElement(By.tagName("ul")).findElements(
            By.tagName("a")).last()
        driver.navigate().to(solicitacoesMenuItem.getAttribute("href"))
        waitUntilPageIsReady(driver)

        driver.executeScript("document.getElementById('TipoPesquisa').value =" + req.searchType);
        driver.findElementById("Prosseguir").click()
        waitUntilPageIsReady(driver)

        val cities = listOf<String>() //listOf<String>("AGUAÍ", "ÁGUAS DE LINDÓIA")

        if (cities.isNullOrEmpty()) {
            driver.executeScript("javascript:SelecionarTudo();")
        } else {
            driver.findElementsByTagName("tr").forEach {
                var canCheck = false
                it.findElements(By.cssSelector("td.title")).forEach {
                    canCheck = cities.contains(it.text)
                }
                if (canCheck) {
                    it.findElement(By.name("chkCidades")).click()
                }
            }
        }
        waitUntilPageIsReady(driver)
        driver.findElementById("chkHabilitar").click()
        waitUntilPageIsReady(driver)
        (driver as JavascriptExecutor).executeScript("window.scrollBy(0,500)")
        driver.findElementById("Prosseguir").click()
        waitUntilPageIsReady(driver)

        driver.findElementById("filterTipo").sendKeys(PersonType.juridica.getTitle())

        driver.executeScript("document.getElementById('filterTipo').value =" + req.personType);
        driver.findElementById("filterDocumento").sendKeys(req.cpfCnpj)
        driver.findElementById("btnPesquisar").click()
        waitUntilPageIsReady(driver)
        driver.executeScript("javascript:SelecionarTudo();")
        driver.findElementsById("btnProsseguir").first { it.isEnabled }?.getAttribute("onclick")?.let {
            driver.executeScript(it)
        }
        waitUntilPageIsReady(driver)

        val registries = mutableListOf<ArispRegistry>()

        driver.findElementById("panelMatriculas").findElements(By.tagName("tr")).filter { it.isDisplayed }.first().apply {
            val td = this.findElements(By.tagName("td"))
            val cityName = td[0].text
            val office = td[1].text
            val registryId = td[2].text

            td[3].findElements(By.tagName("a")).first().click()

            val tabs = ArrayList(driver.windowHandles)
            driver.switchTo().window(tabs[1])

            driver.findElements(By.tagName("a")).first { it.getAttribute("href").contains(".pdf") }.apply {
                val link = this.getAttribute("href")
                val inputStream = URL(link).openStream()
                val s3Link = uploadToS3(inputStream)
                val registry = ArispRegistry(cityName = cityName, office = office, registryId = registryId, registryFileUrl = s3Link)
                registries.add(registry)
            }

            driver.close()
            driver.switchTo().window(tabs[0])
        }

        driver.close();
        val response = ArispResponse(registries);
//        dbConnection.insert("arisp", response.toString())
        call.respond(Gson().toJson(response).toString())
    }
}
