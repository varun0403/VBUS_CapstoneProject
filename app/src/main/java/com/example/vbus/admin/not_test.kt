package com.example.vbus.admin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.messaging.FirebaseMessaging

class NotTest : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NotificationTestScreen()
        }
    }
}

@Composable
fun NotificationTestScreen() {
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Test Notification Sender",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Textbox for Title
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Textbox for Message
        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text("Message") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Button to send notification
        Button(
            onClick = {
                if (title.isNotEmpty() && message.isNotEmpty()) {
                    sendNotificationToTopic("students", title, message) { result ->
                        status = result
                    }
                } else {
                    status = "Both fields are required!"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Send Notification")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Status display
        if (status.isNotEmpty()) {
            Text(
                text = status,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

fun sendNotificationToTopic(
    topic: String,
    title: String,
    message: String,
    onComplete: (String) -> Unit
) {
    // Construct the payload for the notification
    val payload = mapOf(
        "to" to "/topics/$topic",
        "notification" to mapOf(
            "title" to title,
            "body" to message
        )
    )

    // Use Firebase Admin SDK (or your custom server) to send the notification
    FirebaseMessaging.getInstance().subscribeToTopic(topic)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onComplete("Notification sent successfully to topic: $topic")
            } else {
                onComplete("Failed to send notification: ${task.exception?.localizedMessage}")
            }
        }
}