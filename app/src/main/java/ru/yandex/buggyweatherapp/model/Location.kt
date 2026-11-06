package ru.yandex.buggyweatherapp.model

data class Location(
    val latitude: Double,
    val longitude: Double,
    val name: String? = null
) {
    
    override fun toString(): String {
        var result = ""
        result += "Latitude: $latitude, "
        result += "Longitude: $longitude"
        name?.let {
            result += ", Name: $it"
        }
        return result
    }
}