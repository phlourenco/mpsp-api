package definitions

class SitelSearch(var name: String,
    val motherName: String,
    val birthDate: String,
    val documentNumber: String,
    val processNumber: String)


 class SitelResponse(var name: String,
    val title: String,
    val birthday: String,
    val zone: String,
    val address: String,
    val city: String,
    val uf: String,
    val domesticDate: String,
    val fatherName: String,
    val motherName: String,
    val natural: String,
    val validationCode: String)