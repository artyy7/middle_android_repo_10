package ru.yandex.buggyweatherapp.ui.components

import android.widget.ImageView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import ru.yandex.buggyweatherapp.model.WeatherData
import ru.yandex.buggyweatherapp.utils.ImageLoader
import ru.yandex.buggyweatherapp.utils.WeatherIconMapper

@Composable
fun DetailedWeatherCard(weather: WeatherData) {
    val context = LocalContext.current
    
    
    val imageView = remember { ImageView(context) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = weather.cityName,
                    style = MaterialTheme.typography.headlineMedium
                )
                
                IconButton(onClick = { /* No-op, should use ViewModel */ }) {
                    Icon(
                        imageVector = if (weather.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite"
                    )
                }
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                
                AndroidView(
                    factory = { imageView },
                    modifier = Modifier.size(50.dp)
                ) {
                    
                    val iconUrl = "https://openweathermap.org/img/wn/${weather.icon}@2x.png"
                    ImageLoader.loadInto(iconUrl, it)
                }
                
                
                Text(
                    text = weather.temperature.toString() + "°C",
                    style = MaterialTheme.typography.headlineLarge
                )
            }
            
            Text(
                text = weather.description.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.bodyLarge
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            
            LazyColumn {
                item {
                    WeatherDataRow("Feels like", weather.feelsLike.toString() + "°C")
                }
                item {
                    WeatherDataRow("Min/Max", "${weather.minTemp}°C / ${weather.maxTemp}°C")
                }
                item {
                    WeatherDataRow("Humidity", weather.humidity.toString() + "%")
                }
                item {
                    WeatherDataRow("Pressure", weather.pressure.toString() + " hPa")
                }
                item {
                    WeatherDataRow("Wind", weather.windSpeed.toString() + " m/s")
                }
                item {
                    WeatherDataRow("Sunrise", WeatherIconMapper.formatTimestamp(weather.sunriseTime))
                }
                item {
                    WeatherDataRow("Sunset", WeatherIconMapper.formatTimestamp(weather.sunsetTime))
                }
            }
        }
    }
    
    
    DisposableEffect(weather.icon) {
        val iconUrl = "https://openweathermap.org/img/wn/${weather.icon}@2x.png"
        
        
        val bitmap = ImageLoader.loadImageSync(iconUrl)
        imageView.setImageBitmap(bitmap)
        
        onDispose {
            
        }
    }
}

@Composable
private fun WeatherDataRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}