package com.example.vbus.student

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun StudentComplaintScreen(navController: NavController,name:String){
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        Button(
            onClick = { navController.navigate("complaint_posting/$name") },
            modifier = Modifier.padding(16.dp)
        ) { Text("Post a complaint") }
        Button(
            onClick = { navController.navigate("complaint_tracking/$name") },
            modifier = Modifier.padding(16.dp)
        ) { Text("Track Complaints") }
    }
}