package com.phlourenco.controllers

import com.amazonaws.AmazonServiceException
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.CannedAccessControlList
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
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

fun uploadToS3(inputStream: InputStream): String {
    val awsCreds = BasicAWSCredentials("AKIAIWP3B2KH5SFIODVQ", "nem79NqVWVJqsAr2KxwDX6FVZpM39LCE8GrQgyF/")
    val awsCredentials = AWSStaticCredentialsProvider(awsCreds)

    val awsS3 = AmazonS3ClientBuilder.standard().withCredentials(awsCredentials).withRegion(Regions.SA_EAST_1).build();

    val metadata = ObjectMetadata()
    metadata.contentType = "application/pdf"

    val fileName = UUID.randomUUID().toString()
    val putObjReq = PutObjectRequest("fiap-mpsp-morcegos", fileName, inputStream, metadata).withCannedAcl(
        CannedAccessControlList.PublicRead)

    awsS3.putObject(putObjReq)

    return "https://fiap-mpsp-morcegos.s3.sa-east-1.amazonaws.com/$fileName"
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