package com.phlourenco.definitions

class JucespRequest(var  companyName: String)

class JucespResponse(val companyType: String,
                     val date: String,
                     val initDate: String,
                     val cnpj: String,
                     val companyDescription: String,
                     val capital: String,
                     val address: String,
                     val number: String,
                     val locale: String,
                     val complement: String,
                     val postalCode: String,
                     val city: String)
