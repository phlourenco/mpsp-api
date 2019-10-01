package com.phlourenco.cadesp

import java.util.*

data class CadespRequest(
    val cnpj: String
)

data class CadespResponse(
    val ie: String,
    val cnpj: String,
    val businessName: String,
    val drt: String,
    val situation: Boolean,
    val dateStateRegistration: String,
    val stateRegime: String,
    val taxOffice: String,
    val fantasyName: String,
    val nire: String,
    val registrationSituation: Boolean,
    val taxOccurrence: Boolean,
    val unitType: String,
    val ieStartDate: String,
    val dateStartedSituation: String,
    val practices: String
)