package ru.yandex.buggyweatherapp.repository

import android.util.Log
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.yandex.buggyweatherapp.api.RetrofitInstance
import ru.yandex.buggyweatherapp.model.Location
import ru.yandex.buggyweatherapp.model.WeatherData

class WeatherRepository {
    
    
    private val weatherApi = RetrofitInstance.weatherApi

    private var cachedWeatherData: WeatherData? = null
    
    
    fun getWeatherData(location: Location, callback: (WeatherData?, Exception?) -> Unit) {

        val call = weatherApi.getCurrentWeather(location.latitude, location.longitude)


        call.enqueue(
            object : Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    if (response.isSuccessful && response.body() != null) {
                        try {
                            val weatherData = parseWeatherData(response.body()!!, location)
                            cachedWeatherData = weatherData
                            callback(weatherData, null)
                        } catch (e: Exception) {
                            callback(null, e)
                        }
                    } else {
                        if (response.isSuccessful && response.body() == null) callback(
                            null, Exception("Response is empty, try again")
                        )
                        else callback(null, Exception("API Error: ${response.code()}"))
                    }
                }

                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    Log.e("WeatherRepository", "Error fetching weather", t)
                    callback(null, Exception(t))
                }
            },
        )
    }

    // Отдаем дополнительно location для исправления бага, см. WeatherViewModel
    fun getWeatherByCity(cityName: String, callback: (WeatherData?, Location?, Exception?) -> Unit) {
        weatherApi.getWeatherByCity(cityName).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (response.isSuccessful && response.body() != null) {
                    try {
                        val json = response.body()!!
                        val location = extractLocationFromResponse(json)
                        val weatherData = parseWeatherData(json, location)
                        callback(weatherData, location, null)
                    } catch (e: Exception) {
                        callback(null, null, e)
                    }
                } else {
                    if (response.isSuccessful && response.body() == null) callback(
                        null, null, Exception("Response is empty, try again")
                    )
                    else callback(null, null, Exception("API Error: ${response.code()}"))
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.e("WeatherRepository", "Error fetching weather by city", t)
                callback(null, null, Exception(t))
            }
        })
    }

    private fun parseWeatherData(json: JsonObject, location: Location): WeatherData {

        val main = json.getAsJsonObject("main")
        val wind = json.getAsJsonObject("wind")
        val sys = json.getAsJsonObject("sys")
        val weather = json.getAsJsonArray("weather").get(0).asJsonObject
        val clouds = json.getAsJsonObject("clouds")

        return WeatherData(
            cityName = json.get("name").asString,
            country = sys.get("country").asString,
            temperature = main.get("temp").asDouble,
            feelsLike = main.get("feels_like").asDouble,
            minTemp = main.get("temp_min").asDouble,
            maxTemp = main.get("temp_max").asDouble,
            humidity = main.get("humidity").asInt,
            pressure = main.get("pressure").asInt,
            windSpeed = wind.get("speed").asDouble,
            windDirection = if (wind.has("deg")) wind.get("deg").asInt else 0,
            description = weather.get("description").asString,
            icon = weather.get("icon").asString,
            cloudiness = clouds.get("all").asInt,
            sunriseTime = sys.get("sunrise").asLong,
            sunsetTime = sys.get("sunset").asLong,
            timezone = json.get("timezone").asInt,
            timestamp = json.get("dt").asLong,
            rawApiData = json.toString(),
            rain = if (json.has("rain") && json.getAsJsonObject("rain")
                    .has("1h")
            ) json.getAsJsonObject("rain").get("1h").asDouble else null,
            snow = if (json.has("snow") && json.getAsJsonObject("snow")
                    .has("1h")
            ) json.getAsJsonObject("snow").get("1h").asDouble else null
        )
    }

    private fun extractLocationFromResponse(json: JsonObject): Location {
        val coord = json.getAsJsonObject("coord")
        val lat = coord.get("lat").asDouble
        val lon = coord.get("lon").asDouble
        val name = json.get("name").asString

        return Location(lat, lon, name)
    }
}