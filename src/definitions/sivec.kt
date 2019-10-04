package com.phlourenco.definitions

import com.phlourenco.arisp.IIntEnum
import com.phlourenco.arisp.IStrEnum

class SivecRequest(
    var term: String,
    var searchType: String
)


enum class SivecSearchType: IIntEnum, IStrEnum {
    matricula {
        override fun getValue() = 3
        override fun getTitle() = "sap"
    },
    transcricao {
        override fun getValue() = 2
        override fun getTitle() = "name"
    },
    pessoa {
        override fun getValue() = 1
        override fun getTitle() = "document"
    }
}