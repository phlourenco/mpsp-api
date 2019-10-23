package com.phlourenco.definitions

import DetranCHNResponse
import com.google.gson.Gson

data class Report(
    val reportId: String,
    val obj: Map<String, Any>
) {
    inline fun <reified T: Any> getObject(): T? {
        return mapToObject(obj)
    }
    inline fun <reified T: Any> mapToObject(map: Map<String, Any>): T {
        val jsonElement = Gson().toJsonTree(map)
        val obj = Gson().fromJson(jsonElement, T::class.java)
        return obj
    }
}

data class FullReport(
    val arisp: ArispResponse?,
    val arpensp: ArpenspResponse?,
    val cadesp: CadespResponse?,
    val cagedResponsible: CagedResponsibleResponse?,
    val cagedWorker: CagedWorkerResponse?,
    val cagedCompany: CagedCompanyResponse?,
    val censec: CensecResponse?,
    val detranCHN: DetranCHNResponse?,
    val detranTimeLine: DetranTimeLineResponse?,
    val detranVehicle: DetranVehicleResponse?,
    val infocrim: InfocrimResponse?,
    val siel: SitelResponse?,
    val sivec: SivecResponse?
)