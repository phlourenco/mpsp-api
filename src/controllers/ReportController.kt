package com.phlourenco.controllers

import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.phlourenco.definitions.*
import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get

val gson = Gson()

//convert a data class to a map
fun <T> T.serializeToMap(): Map<String, Any> {
    return convert()
}

//convert a map to a data class
inline fun <reified T> Map<String, Any>.toDataClass(): T {
    return convert()
}

//convert an object of type I to type O
inline fun <I, reified O> I.convert(): O {
    val json = gson.toJson(this)
    return gson.fromJson(json, object : TypeToken<O>() {}.type)
}

//inline fun <reified T: Any> mapToObject(map: Map<String, Any>): T {
//    val jsonElement = Gson().toJsonTree(map)
//    val obj = Gson().fromJson(jsonElement, T::class.java)
//    return obj
//}
//
//fun <T> getReports(collectionName: String): List<Report> {
//    val list = mutableListOf<Report>()
//    val jsonMap = DatabaseService.allFromCollection(collectionName)
//    jsonMap.forEach {
//        val rep = mapToObject<Report>(it)
//        list.add(rep)
//    }
//    return list
//}


inline fun <reified T> getReports(collectionName: String): List<Report<T>> {
    val reports = mutableListOf<Report<T>>()

    DatabaseService.allFromCollection(collectionName).forEach {
        val reportId = it.getValue("reportId").toString()
        val json = Gson().toJsonTree(it)
        val responseObj = Gson().fromJson(json, T::class.java)
        reports.add(Report(reportId, responseObj))
    }

    return reports
}


fun Route.reportController() {

    get("/reports") {

        val allReports = mutableListOf<FullReport>()
        val allReportsIds = mutableSetOf<String>()

        val arispReports = getReports<ArispResponse>("arisp")
        val arpenspReports = getReports<ArpenspResponse>("arpensp")
        val cadespReports = getReports<CadespResponse>("cadesp")
        val cagedCompanyReports = getReports<CagedCompanyResponse>("cagedCompany")
        val cagedResponsibleReports = getReports<CagedResponsibleResponse>("cagedResponsible")
        val cagedWorkerReports = getReports<CagedWorkerResponse>("cagedWorker")
        val censecReports = getReports<CensecResponse>("censec")
        val detranCNHReports = getReports<DetranCNHResponse>("detranCNH")
        val detranTimeLineReports = getReports<DetranTimeLineResponse>("detranTimeLine")
        val detranVehicleReports = getReports<DetranVehicleResponse>("detranVehicle")
        val infocrimReports = getReports<InfocrimResponse>("infocrim")
        val jucespReports = getReports<JucespResponse>("jucesp")
        val sielReports = getReports<SielResponse>("siel")
        val sivecReports = getReports<SivecResponse>("sivec")

        val servicesReports = listOf(arispReports, arpenspReports, cadespReports, cagedCompanyReports, cagedResponsibleReports,
                                    cagedWorkerReports, censecReports, detranCNHReports, detranCNHReports, detranTimeLineReports,
                                    detranVehicleReports, infocrimReports, jucespReports, sielReports, sivecReports)

        servicesReports.forEach {
            it.forEach {
                allReportsIds.add(it.reportId)
            }
        }

        allReportsIds.forEach {
            val arisp = arispReports.firstOrNull { rep -> rep.reportId == it }?.obj
            val arpensp = arpenspReports.firstOrNull { rep -> rep.reportId == it }?.obj
            val cadesp = cadespReports.firstOrNull { rep -> rep.reportId == it }?.obj
            val cagedResponsible = cagedResponsibleReports.firstOrNull { rep -> rep.reportId == it }?.obj
            val cagedWorker = cagedWorkerReports.firstOrNull { rep -> rep.reportId == it }?.obj
            val cagedCompany = cagedCompanyReports.firstOrNull { rep -> rep.reportId == it }?.obj
            val censec = censecReports.firstOrNull { rep -> rep.reportId == it }?.obj
            val detranCNH = detranCNHReports.firstOrNull { rep -> rep.reportId == it }?.obj
            val detranTimeLine = detranTimeLineReports.firstOrNull { rep -> rep.reportId == it }?.obj
            val detranVehicle = detranVehicleReports.firstOrNull { rep -> rep.reportId == it }?.obj
            val infocrim = infocrimReports.firstOrNull { rep -> rep.reportId == it }?.obj
            val siel = sielReports.firstOrNull { rep -> rep.reportId == it }?.obj
            val sivec = sivecReports.firstOrNull { rep -> rep.reportId == it }?.obj
            val fullReport = FullReport(it, arisp, arpensp, cadesp, cagedResponsible, cagedWorker, cagedCompany, censec, detranCNH, detranTimeLine, detranVehicle, infocrim, siel, sivec)
            allReports.add(fullReport)
        }
        call.respond(allReports)

//        call.respond(HttpStatusCode(403, "Erro desconhecido"))

    }

}