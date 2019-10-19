import com.phlourenco.controllers.login
import com.phlourenco.controllers.moveTo
import com.phlourenco.controllers.waitUntilPageIsReady
import com.phlourenco.definitions.DetranTimeLineRequest
import com.phlourenco.definitions.DetranVehicleRequest
import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.routing.Route
import io.ktor.routing.post
import org.openqa.selenium.chrome.ChromeDriver

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
        moveTo(driver,"Pesquisar",true)
    }
}