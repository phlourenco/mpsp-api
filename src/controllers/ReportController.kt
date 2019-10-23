package com.phlourenco.controllers

import com.google.gson.Gson
import com.phlourenco.definitions.ArispResponse
import com.phlourenco.definitions.Report
import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import org.litote.kmongo.getCollection
import org.litote.kmongo.getCollectionOfName



inline fun <reified T: Any> mapToObject(map: Map<String, Any>): T {
    val jsonElement = Gson().toJsonTree(map)
    val obj = Gson().fromJson(jsonElement, T::class.java)
    return obj
}

fun <T> getReports(collectionName: String): List<Report> {
    val list = mutableListOf<Report>()
    val jsonMap = DatabaseService.allFromCollection(collectionName)
    jsonMap.forEach {
        val rep = mapToObject<Report>(it)
        list.add(rep)
    }
    return list
}

fun Route.reportController() {



    post("/reports") {

//        val arisp = DatabaseService.allFromCollection("arisp")

        val arispReports = getReports<ArispResponse>("arisp")
        print(arispReports)

        arispReports.forEach {
            val obj = it.getObject<ArispResponse>()
            print(obj)
        }

//        DatabaseService.database.listCollections().forEach {
//            it.getString("name")?.apply {
//                val collection = DatabaseService.database.getCollection(this)
//                val found = DatabaseService.allFromCollection(this)
//
//                val json = Gson().toJsonTree(found[0])
//                val arispResponse = Gson().fromJson(json, ArispResponse::class.java)
//
//                print(arispResponse)
//            }
//
//        }
    }

}