package com.phlourenco.definitions

data class CagedRequest(
    val responsability: responsability,
    val company: company,
    val establishment: establishment,
    val worker: worker
)

data class responsability(
    val searchType: String,
    val term: String
)

data class company(
    val cnpj: String
)

data class establishment(
    val searchType: String,
    val term: String
)

data class worker(
    val searchType: String,
    val term: String
)

data class identification(
    val cnpjCeiCpf: String,
    val name: String
)

data class address(
    val street: String,
    val neighborhood: String,
    val city: String,
    val state: String,
    val cep: String
)

data class CagedResponseResponsible(
    val identification: identification,
    val address: address,
    val contact: contact
)



data class contact(
    val name: String,
    val cpf: String,
    val phone: phone,
    val line: String,
    val email: String
)

data class phone(
    val ddd: String,
    val phone: String
)

data class CagedResponseCompany(
    val company: companyResponse,
    val detail: detailResponse
)

data class companyResponse(
    val cnpj: String,
    val name: String,
    val cnae: String
)

data class detailResponse(
    val subsidiaries: String,
    val admissions: String,
    val demissions: String
)

data class identificationResponse(
    val name: String,
    val pis: String,
    val convertedPis: String
)

data class sumaryResponse(
    val cpf: String,
    val birthDate: String,
    val ctpsSerie: String,
    val pisSituation: String,
    val sex: String,
    val nationality: String,
    val color: String,
    val study: String,
    val hasDisability: Boolean
)
