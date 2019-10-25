import com.google.gson.Gson
import com.phlourenco.controllers.*
import com.phlourenco.definitions.DetranVehicleRequest
import com.phlourenco.definitions.DetranVehicleResponse
import com.phlourenco.utils.closeAllTabs
import io.ktor.application.call
import io.ktor.request.header
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import org.openqa.selenium.By
import org.openqa.selenium.chrome.ChromeDriver
import java.net.URL

fun Route.detranVehicleController() {
    post("/detranVehicle") {
        val req = this.call.receive<DetranVehicleRequest>()
        val driver = ChromeDriver()

        login(driver)
        driver.navigate().to("http://ec2-18-231-116-58.sa-east-1.compute.amazonaws.com/detran/login.html")
        waitUntilPageIsReady(driver)
        driver.findElementById("form:j_id563205015_44efc1ab").sendKeys("fiap")
        driver.findElementById("form:j_id563205015_44efc191").sendKeys("mpsp")
        driver.findElementById("form:j_id563205015_44efc15b").click()
        waitUntilPageIsReady(driver)
        driver.findElementById("navigation_a_M_18").click()
        moveTo(driver, "Consultar Veículo Base Estadual",true)
        driver.findElementById("form:j_id2124610415_1b3be1bd").sendKeys(req.board)
        driver.findElementById("form:j_id2124610415_1b3be1e3").sendKeys(req.document)
        driver.findElementById("form:j_id2124610415_1b3be1d7").sendKeys(req.renavam)
        driver.findElements(By.tagName("a")).first { it.getAttribute("href").contains(".pdf") }.apply {
            val link = this.getAttribute("href")
            val inputStream = URL(link).openStream()
            val s3Link = uploadToS3(inputStream)
            val response = DetranVehicleResponse(s3Link)

            driver.closeAllTabs()

            call.request.header("reportId")?.apply {
                val responseMap = response.serializeToMap().toMutableMap()
                responseMap["reportId"] = this
                DatabaseService.insert("detranVehicle", Gson().toJson(responseMap).toString())
            }

            call.respond(response)
        }
    }
}