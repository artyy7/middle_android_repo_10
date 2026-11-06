package ru.yandex.buggyweatherapp.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import ru.yandex.buggyweatherapp.repository.LocationRepository
import ru.yandex.buggyweatherapp.repository.WeatherRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationSearch(
    onCitySearch: (String) -> Unit,
    onLocationRequest: () -> Unit
) {
    var searchText by remember { mutableStateOf("") }
    
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            label = { Text("Search city") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            leadingIcon = {
                IconButton(onClick = { onLocationRequest() }) {
                    Icon(Icons.Default.LocationOn, contentDescription = "Get current location")
                }
            },
            trailingIcon = {
                IconButton(onClick = { 
                    if (searchText.isNotBlank()) {
                        onCitySearch(searchText)
                    }
                }) {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { 
                if (searchText.isNotBlank()) {
                    onCitySearch(searchText)
                }
            })
        )
    }
}

@Composable
fun LocationSearchWithDirectApiCall() {
    val context = LocalContext.current
    var searchText by remember { mutableStateOf("") }
    
    
    val weatherRepository = WeatherRepository()
    val locationRepository = LocationRepository(context)
    
    OutlinedTextField(
        value = searchText,
        onValueChange = { searchText = it },
        label = { Text("Search city") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        trailingIcon = {
            IconButton(onClick = { 
                if (searchText.isNotBlank()) {
                    
                    weatherRepository.getWeatherByCity(searchText) { weatherData, location , error -> }
                }
            }) {
                Icon(Icons.Default.Search, contentDescription = "Search")
            }
        }
    )
}