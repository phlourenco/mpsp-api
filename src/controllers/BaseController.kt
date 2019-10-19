package com.phlourenco.controllers

import com.phlourenco.Database.dbConnection
import id.jasoet.funpdf.HtmlToPdf
import id.jasoet.funpdf.PageOrientation
import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.interactions.Actions
import org.openqa.selenium.support.ui.Select
import org.openqa.selenium.support.ui.WebDriverWait
import java.io.InputStream
import java.util.*

val dbConnection: dbConnection = dbConnection()

val pdf by lazy {
    HtmlToPdf(executable = "/usr/bin/wkhtmltopdf") {
        orientation(PageOrientation.LANDSCAPE)
        pageSize("Letter")
        marginTop("1in")
        marginBottom("1in")
        marginLeft("1in")
        marginRight("1in")
    }
}
fun login(driver: ChromeDriver) {
    // Faz login no portal do professor. Vai ser removido posteriormente
    driver.get("http://ec2-18-231-116-58.sa-east-1.compute.amazonaws.com/login")
    waitUntilPageIsReady(driver)
    driver.findElementById("username").sendKeys("fiap")
    driver.findElementById("password").sendKeys("mpsp")
    driver.findElementByTagName("form").submit()
    waitUntilPageIsReady(driver)
}

fun fillForm(values: Map<String, String>, driver: ChromeDriver) {
    values.forEach {
        driver.findElementByName(it.key).sendKeys(it.value)
    }
}

fun stringToPdf(str: String): InputStream? {
    return pdf.convert(input = str)
}

fun streamToBase64(stream: InputStream): String {
    val bytes = stream.readBytes()
    return Base64.getEncoder().encodeToString(bytes)
}

fun stringToPdfBase64(str: String): String? {
    stringToPdf(str)?.let {
        streamToBase64(it)?.let {
            return it
        }
    }
    return null
}

fun convertToBolean(term: String): Boolean {
    return term == "NÃO"
}

fun moveTo(driver: ChromeDriver,name: String, click: Boolean){
    if(click){
        val element = driver.findElementByLinkText(name)
        Actions(driver).moveToElement(element).perform()
        element.click()
    }else {
        val element = driver.findElementByLinkText(name)
        Actions(driver).moveToElement(element).perform()
    }
}

fun inputElementById(driver: ChromeDriver, elementId: String, keys: String){
    driver.findElementById(elementId).sendKeys(keys)
}

fun clickElementById(driver: ChromeDriver, elementId: String){
    driver.findElementById(elementId).click()
}

fun dropSelectOption(driver: ChromeDriver, elementId: String, optionValue: String){
    val drpIdentification = Select(driver.findElement(By.id(elementId)))
    drpIdentification.selectByValue(optionValue)
}

fun waitUntilPageIsReady(driver: ChromeDriver) {
    val executor = driver as JavascriptExecutor
    WebDriverWait(driver, 1)
        .until { executor.executeScript("return document.readyState") == "complete" }
}

fun expandShadowRoot(parent: WebElement, driver: ChromeDriver): WebElement {
    return driver.executeScript("return arguments[0].shadowRoot", parent) as WebElement
}

fun validateSivecSearchType(searchType: String) : String {
    if (searchType == "document") {
        return "Por RG"
    }
    else if (searchType == "name") {
        return "Por Nome"
    }
    else {
        return "Matrícula SAP"
    }
}

fun getIdTextFieldForType(searchType: String) : String {
    if (searchType == "document") {
        return "idValorPesq"
    }
    else if (searchType == "name") {
        return "idNomePesq"
    }
    else {
        return "idValorPesq"
    }
}