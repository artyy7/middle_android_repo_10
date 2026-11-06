package ru.yandex.buggyweatherapp.model

data class WeatherData(
    val cityName: String,
    val country: String,
    val temperature: Double,
    val feelsLike: Double,
    val minTemp: Double,
    val maxTemp: Double,
    val humidity: Int,
    val pressure: Int,
    val windSpeed: Double,
    val windDirection: Int,
    val description: String,
    val icon: String,
    val rain: Double? = null,
    val snow: Double? = null,
    val cloudiness: Int,
    val sunriseTime: Long,
    val sunsetTime: Long,
    val timezone: Int,
    val timestamp: Long,
    
    
    val rawApiData: String,
    

    val isFavorite: Boolean = false,
    val isSelected: Boolean = false
)