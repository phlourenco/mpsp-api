package com.phlourenco.definitions

// Caged Responsible

data class CagedResponsibleRequest(
    val searchType: Int,
    val term: String
)
data class CagedResponsibleResponse(
    val identification: CagedResponsibleIdentification,
    val address: CagedResponsibleAddress,
    val contact: CagedResponsibleContact
)

data class CagedResponsibleIdentification(
    val cnpjCeiCpf: String,
    val name: String
)

data class CagedResponsibleAddress(
    val street: String,
    val neighborhood: String,
    val city: String,
    val state: String,
    val cep: String
)

data class CagedResponsibleContact(
    val name: String,
    val cpf: String,
    val phone: String,
    val line: String,
    val email: String
)

// Caged Company

data class CagedCompanyRequest(
    val cnpj: String
)

data class CagedCompanyResponse(
    val cnpj: String,
    val name: String,
    val cnae: String,
    val subsidiaries: String,
    val admissions: String,
    val demissions: String
)

// Caged Worker

data class CagedWorkerRequest(
    val searchType: Int,
    val term: String
)

data class CagedWorkerResponse(
    val name: String,
    val pis: String,
    val convertedPis: String,
    val cpf: String,
    val birthDate: String,
    val ctpsSerie: String,
    val pisSituation: String,
    val sex: String,
    val nationality: String,
    val color: String,
    val study: String,
    val hasDisability: Boolean,
    val pdfUrl: String
)
