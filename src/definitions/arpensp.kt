package com.phlourenco.arpensp

data class ArpenspRequest(
    val registryType: String,
    val processNumber: String,
    val placeId: Int
)

data class ArpenspResponse(
    val spouse1OldName: String,
    val spouse1NewName: String,
    val spouse21OldName: String,
    val spouse21NewName: String,
    val marriageDate: String
)