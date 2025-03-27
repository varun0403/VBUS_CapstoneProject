package com.example.vbus.student

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun StudentAnnouncementScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    var announcements by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        try {
            val snapshot = db.collection("announcements")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            announcements = snapshot.documents.map { it.data ?: emptyMap() }
        } catch (e: Exception) {
            e.printStackTrace()
            errorMessage = "Failed to load announcements. Please try again."
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Announcements",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (announcements.isEmpty() && errorMessage.isEmpty()) {
            CircularProgressIndicator()
        }
        else if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error
            )
        }
        else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(announcements.size) { index ->
                    val announcement = announcements[index]
                    AnnouncementCard(
                        text = announcement["text"] as? String ?: "No content",
                        timestamp = announcement["timestamp"] as? Long ?: 0L,
                        isHighlighted = (index == 0) // Pass true for the latest announcement
                    )
                }
            }
        }
    }
}

@SuppressLint("SimpleDateFormat")
@Composable
fun AnnouncementCard(text: String, timestamp: Long, isHighlighted: Boolean) {
    val backgroundColor = if (isHighlighted) {
        MaterialTheme.colorScheme.primaryContainer // Highlight color
    } else {
        MaterialTheme.colorScheme.surface // Default background color
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = text, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Posted: ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(timestamp)}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
