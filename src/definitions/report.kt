package com.phlourenco.definitions

data class Report<T>(
    val reportId: String,
    val obj: T
)

data class FullReport(
    val reportId: String,
    val arisp: ArispResponse?,
    val arpensp: ArpenspResponse?,
    val cadesp: CadespResponse?,
    val cagedResponsible: CagedResponsibleResponse?,
    val cagedWorker: CagedWorkerResponse?,
    val cagedCompany: CagedCompanyResponse?,
    val censec: CensecResponse?,
    val detranCNH: DetranCNHResponse?,
    val detranTimeLine: DetranTimeLineResponse?,
    val detranVehicle: DetranVehicleResponse?,
    val infocrim: InfocrimResponse?,
    val siel: SitelResponse?,
    val sivec: SivecResponse?
)