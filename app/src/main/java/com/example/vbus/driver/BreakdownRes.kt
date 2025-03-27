package com.example.vbus.driver

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.example.vbus.routes

@Composable
fun Find_resolution(bus_no: String){
    val inoperable_bus_no = bus_no
    val inoperable_bus_route = findRouteForBus(bus_no)
    val other_buses_in_route = findOtherBusesInRoute(bus_no)
}

fun findRouteForBus(busNumber: String): String? {
    for ((routeName, stops) in routes) {
        if (busNumber in stops) {
            return routeName
        }
    }
    return null
}

fun findOtherBusesInRoute(busNumber: String): List<String> {
    for ((_, stops) in routes) {
        if (busNumber in stops) {
            return stops.filter { it != busNumber }
        }
    }
    return emptyList() // Bus or route not found
}