package com.phlourenco.definitions

data class CensecRequest(
    val cpfCnpj: String
)

data class CensecResponse(
    val list: List<CensecResponseItem>
)

data class CensecResponseItem(
    val office: String,
    val date: String,
    val act: String,
    val actDate: String,
    val book: String,
    val bookComplement: String,
    val page: String,
    val pageComplement: String,
    val parts: List<CensecPart>
)

data class CensecPart(
    val name: String,
    val document: String,
    val role: String
)
