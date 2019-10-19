package com.phlourenco.Database

import com.mongodb.MongoClientURI
import org.litote.kmongo.KMongo
import org.litote.kmongo.insertOne

class dbConnection {
    val uri = MongoClientURI("mongodb://admin:Fiap#1418@ds133094.mlab.com:33094/mpsp-official")

     fun insert(collection:String, value:String){
        val db = KMongo.createClient(uri = uri).getDatabase(uri.database).getCollection(collection)
        db.insertOne(value)
    }

}