package com.phlourenco.arisp

data class ArispRequest(
    val searchType: Int,
    val personType: Int,
    val cityNames: List<String>?,
    val cpfCnpj: String
)

data class ArispResponse(
    val registries: List<ArispRegistry>
)

data class ArispRegistry(
    val cityName: String,
    val office: String,
    val registryId: String,
    val registryFileUrl: String
)

interface IIntEnum {
    fun getValue(): Int
}

interface IStrEnum {
    fun getTitle(): String
}

enum class SearchType: IIntEnum, IStrEnum {
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
    },
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