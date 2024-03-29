package com.phlourenco.definitions

class SivecRequest(
    var searchType: String,
    var term: String
)

data class Address (
    var residential : String,
    var city : String
)

class  SivecResponse(
    val name : String,
    val sex : String,
    val birth : String,
    val rg : String,
    val issueDateDocument : String,
    val nickName : String,
    val maritalStatus : String,
    val naturalness : String,
    val naturalized : Boolean,
    val study : String,
    val fatherName : String,
    val motherName : String,
    val color : String,
    val profession : String,
    val hair : String,
    val eyeColor : String,
    val address : Address
)


