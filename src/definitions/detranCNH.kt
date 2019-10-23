package com.phlourenco.definitions

class DetranCNHRequest(val cpf: String)

class DetranCNHResponse(val imageUrl: String,
                        val renach: String,
                        val category: String,
                        val emission: String,
                        val birthDate: String,
                        val conductionName: String,
                        val fatherName: String,
                        val motherName: String,
                        val registerDate: String,
                        val typographic: String,
                        val identification: String,
                        val cpf : String)