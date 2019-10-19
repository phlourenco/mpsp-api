package com.phlourenco

import com.phlourenco.controllers.*
import io.ktor.application.*
import io.ktor.routing.*
import io.ktor.gson.*
import io.ktor.features.*
import io.ktor.server.netty.EngineMain

fun main(args: Array<String>): Unit = EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@JvmOverloads

fun Application.module(testing: Boolean = false) {

    install(ContentNegotiation) {
        gson {
        }
    }

    routing {
        arispController() //falta pdf
        cadespController()
        cagedController()
        sielController()
        arpenspController()
        infocrimController()
        sivecController()
        jucespController()
    }
}
