package com.phlourenco.arisp


interface IIntEnum {
    fun getValue(): Int
}

interface IStrEnum {
    fun getTitle(): String
}

enum class SearchType: IIntEnum, IStrEnum {
    matricula {
        override fun getValue() = 4
        override fun getTitle() = "Matrícula"
    },
    transcricao {
        override fun getValue() = 5
        override fun getTitle() = "Transcrição"
    },
    pessoa {
        override fun getValue() = 6
        override fun getTitle() = "Pessoa"
    },
    enderecoRua {
        override fun getValue() = 1
        override fun getTitle() = "Endereço Rua"
    },
    enderecoEdificio {
        override fun getValue() = 2
        override fun getTitle() = "Endereço Edifício"
    },
    enderecoLoteamento {
        override fun getValue() = 3
        override fun getTitle() = "Endereço Loteamento"
    }
}

enum class PersonType: IIntEnum, IStrEnum {
    fisica {
        override fun getValue() = 1
        override fun getTitle() = "Pessoa Física"
    },
    juridica {
        override fun getValue() = 2
        override fun getTitle() = "Pessoa Jurídica"
    }
}