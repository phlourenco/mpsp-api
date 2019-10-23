package com.phlourenco.Database

import com.mongodb.MongoClientURI
import org.apache.xpath.operations.Bool
import org.bson.Document
import org.bson.types.ObjectId
import org.litote.kmongo.KMongo
import org.litote.kmongo.insertOne

class DatabaseService {
//    private val uri = MongoClientURI("mongodb://admin:Fiap#1418@ds133094.mlab.com:33094/mpsp-official")
    private val uri = MongoClientURI("mongodb+srv://phlourenco:Z0uz2nGmBpB59neh@fiap-morcegos-rytvg.mongodb.net/mpsp?retryWrites=true&w=majority")

    val database = KMongo.createClient(uri = uri).getDatabase(uri.database)

     fun insert(collection:String, value:String) {
         val db = database.getCollection(collection)
         db.insertOne(value)
     }

    private fun mongoDocumentToMap(document: Document): Map<String, Any> {
        val asMap: MutableMap<String, Any> = document.toMutableMap()
        if (asMap.containsKey("_id")) {
            val id = asMap.getValue("_id")
            if (id is ObjectId) {
                asMap.set("_id", id.toHexString())
            }
        }
        return asMap
    }


    fun allFromCollection(collection: String):
            ArrayList<Map<String, Any>> {
        val mongoResult =
            database.getCollection(collection, Document::class.java)
        val result = ArrayList<Map<String, Any>>()
        mongoResult.find()
            .forEach {
                val asMap: Map<String, Any> = mongoDocumentToMap(it)
                result.add(asMap)
            }
        return result
    }



}