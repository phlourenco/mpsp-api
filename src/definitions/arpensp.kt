package com.phlourenco.definitions

data class ArpenspRequest(
    val registryType: String,
    val processNumber: String,
    val placeId: Int
)

data class ArpenspResponse(
    val spouse1OldName: String,
    val spouse1NewName: String,
    val spouse2OldName: String,
    val spouse2NewName: String,
    val marriageDate: String
)