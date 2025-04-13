package com.example.vbus.driver

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.maps.android.compose.*

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DriverRescueUi(bus_no: String){
    val context = LocalContext.current
    val db = Firebase.firestore
    val driver_name = remember { mutableStateOf("NA") }
    val driver_contact = remember { mutableStateOf("NA") }
    var rescue_bus_no by remember { mutableStateOf("") }
    var latLng by remember { mutableStateOf(LatLng(0.0, 0.0)) }

    LaunchedEffect(Unit) {
        val date_ = getCurrentDate()
        val docRef = db.collection("bus_status").document(bus_no)

        docRef.get()
            .addOnSuccessListener { document ->
                val dateMap = document.get(date_) as? Map<*, *>
                rescue_bus_no = (dateMap?.get("rescue_bus") as? String).toString()
                db.collection("buses").document(rescue_bus_no).get()
                    .addOnSuccessListener {
                        driver_name.value = it.getString("driver_name") ?: "NA"
                        driver_contact.value = it.getString("driver_contact") ?: "NA"
                    }
                val geoPoint = dateMap?.get("rescue_location") as? GeoPoint
                if (geoPoint != null) {
                    latLng = LatLng(geoPoint.latitude, geoPoint.longitude)
                    Log.d("Firestore", "Rescue location: $latLng")
                }
                else {
                    Log.d("Firestore", "No rescue location found for date $date_")
                }
            }
            .addOnFailureListener {
                Log.e("Firestore", "Error fetching data for $bus_no", it)
            }
    }


    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(0.82f).fillMaxWidth()) {
                GoogleMapWithMarkerRescue(latLng)
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Bus No: $rescue_bus_no")
                Text("Driver Name: ${driver_name.value}")
                Text("Driver Contact: ${driver_contact.value}")
                IconButton(onClick = {
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:${driver_contact.value}")
                    }
                    context.startActivity(intent)
                }) {
                    Icon(Icons.Default.Phone, contentDescription = "Call Driver")
                }
            }
        }
}

@Composable
fun GoogleMapWithMarkerRescue(rescue_loc: LatLng) {
    val cameraPositionState = rememberCameraPositionState()

    LaunchedEffect(rescue_loc) {
        cameraPositionState.animate(
            update = CameraUpdateFactory.newLatLngZoom(rescue_loc, 15f),
            durationMs = 1000
        )
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        uiSettings = MapUiSettings(compassEnabled = true, zoomControlsEnabled = true)
    ) {
        Marker(
            state = MarkerState(position = rescue_loc),
            title = "Rescue Location",
            snippet = "Help needed here!"
        )
    }
}


