package ru.yandex.buggyweatherapp.viewmodel

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ru.yandex.buggyweatherapp.model.Location
import ru.yandex.buggyweatherapp.model.WeatherData
import ru.yandex.buggyweatherapp.repository.LocationRepository
import ru.yandex.buggyweatherapp.repository.WeatherRepository
import ru.yandex.buggyweatherapp.utils.ImageLoader
import java.util.Timer
import java.util.TimerTask

class WeatherViewModel : ViewModel() {

    private lateinit var appContext: Context

    private val weatherRepository = WeatherRepository()
    private val locationRepository by lazy {
        LocationRepository(appContext)
    }

    val weatherData = MutableLiveData<WeatherData>()
    val currentLocation = MutableLiveData<Location>()
    val isLoading = MutableLiveData<Boolean>()
    val error = MutableLiveData<String>()
    val cityName = MutableLiveData<String>()


    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())

    private var refreshTimer: Timer? = null

    private var initialized = false

    fun initialize(context: Context) {
        this.appContext = context.applicationContext

        if (initialized) return
        initialized = true
        fetchCurrentLocationWeather()

        startAutoRefresh()
    }


    fun fetchCurrentLocationWeather() {
        isLoading.value = true
        error.value = null

        locationRepository.getCurrentLocation { location ->
            if (location != null) {
                currentLocation.value = location

                viewModelScope.launch {
                    val cityNameFromLocation = locationRepository.getCityNameFromLocation(location)

                    cityName.value = cityNameFromLocation
                    getWeatherForLocation(location)
                }
            } else {
                isLoading.value = false
                error.value = "Unable to get current location"
            }
        }
    }

    fun refreshCurrentLocationWeather() {
        currentLocation.value?.let { location ->
            getWeatherForLocation(location)
        }
    }

    fun getWeatherForLocation(location: Location) {
        isLoading.value = true
        error.value = null

        weatherRepository.getWeatherData(location) { data, exception ->
            isLoading.value = false

            if (data != null) {
                weatherData.value = data
            } else {
                error.value = exception?.message ?: "Unknown error"
            }
        }
    }

    fun searchWeatherByCity(city: String) {
        if (city.isBlank()) {
            error.value = "City name cannot be empty"
            return
        }

        isLoading.value = true
        error.value = null

        weatherRepository.getWeatherByCity(city) { data, location, exception ->

            isLoading.value = false

            if (data != null) {
                weatherData.value = data
                cityName.value = data.cityName
                if (location != null) {
                    currentLocation.value = location
                }
            } else {
                error.value = exception?.message ?: "Unknown error"
            }
        }
    }


    fun formatTemperature(temp: Double): String {
        return "${temp.toInt()}°C"
    }


    fun loadWeatherIcon(iconCode: String) {
        coroutineScope.launch {
            val iconUrl = "https://openweathermap.org/img/wn/$iconCode@2x.png"
            ImageLoader.loadImage(iconUrl)
        }
    }

    private fun startAutoRefresh() {
        refreshTimer = Timer()
        refreshTimer?.schedule(object : TimerTask() {
            override fun run() {
                Handler(Looper.getMainLooper()).post {
                    refreshCurrentLocationWeather()
                }
            }
        }, 60000, 60000)
    }

    fun toggleFavorite() {
        weatherData.value?.let {
            weatherData.value = it.copy(isFavorite = !it.isFavorite)
        }
    }

    override fun onCleared() {
        super.onCleared()

        refreshTimer?.cancel()
    }
}