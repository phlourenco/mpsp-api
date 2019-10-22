package com.phlourenco

import com.phlourenco.controllers.*
import com.phlourenco.definitions.*
import detranVehicleController
import io.ktor.application.*
import io.ktor.routing.*
import io.ktor.gson.*
import io.ktor.features.*
import io.ktor.server.netty.EngineMain

fun main(args: Array<String>): Unit = EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@JvmOverloads

fun Application.module(testing: Boolean = false) {
    install(CORS)
    {
        anyHost()
    }

    install(ContentNegotiation) {
        gson {
        }
    }

    routing {
        arispController() //falta pdf
        cadespController()
        cagedResponsibleController()
        cagedCompanyController()
        cagedWorkerController()
        sielController()
        arpenspController()
        infocrimController()
        sivecController()
        jucespController()
        detranCNHController()
        detranTimeLineController()
        detranVehicleController()
        censecController()
    }
}
