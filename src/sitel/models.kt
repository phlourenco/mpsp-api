package com.phlourenco.sitel

class sitelSearch {
    constructor(
        name: String,
        motherName: String,
        birthDate: String,
        numTitulo: String,
        numProcess: String
    ) {
        this.name = name
        this.motherName = motherName
        this.birthDate = birthDate
        this.numTitulo = numTitulo
        this.numProcess = numProcess
    }
    var name: String;
    val motherName: String;
    val birthDate: String;
    val numTitulo: String;
    val numProcess: String;
}

 class sitelResponse {
    constructor(
        name: String,
        title: String,
        birthday: String,
        zone: String,
        address: String,
        city: String,
        uf: String,
        domesticDate: String,
        fatherName: String,
        motherName: String,
        natural: String,
        validationCode: String
    ) {
        this.name = name
        this.title = title
        this.birthday = birthday
        this.zone = zone
        this.address = address
        this.city = city
        this.uf = uf
        this.domesticDate = domesticDate
        this.fatherName = fatherName
        this.motherName = motherName
        this.natural = natural
        this.validationCode = validationCode
    }

    var name: String
    val title: String
    val birthday: String
    val zone: String
    val address: String
    val city: String
    val uf: String
    val domesticDate: String
    val fatherName: String
    val motherName: String
    val natural: String
    val validationCode: String
}