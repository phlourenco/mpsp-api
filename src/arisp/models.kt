package com.phlourenco.arisp

data class ArispRequest(
    val searchType: SearchType,
    val personType: PersonType,
    val cityNames: List<String>?,
    val cpfCnpj: String
)

data class ArispResponse(
    val registries: List<ArispRegistry>
)

data class ArispRegistry(
    val cityName: String,
    val office: String,
    val registryId: String,
    val registryFileUrl: String
)