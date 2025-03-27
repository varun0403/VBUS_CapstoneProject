package com.example.vbus.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun AdminAnnouncementScreen() {
    val db = FirebaseFirestore.getInstance()
    var announcements by remember { mutableStateOf<List<Pair<String, Map<String, Any>>>>(emptyList()) }
    var errorMessage by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            val snapshot = db.collection("announcements")
                .orderBy("timestamp")
                .get()
                .await()
            announcements = snapshot.documents.map { it.id to (it.data ?: emptyMap()) }
        } catch (e: Exception) {
            e.printStackTrace()
            errorMessage = "Failed to load announcements. Please try again."
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Announcement")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(text = "Admin Announcements", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            if (announcements.isEmpty() && errorMessage.isEmpty()) {
                CircularProgressIndicator()
            } else if (errorMessage.isNotEmpty()) {
                Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(announcements.size) { index ->
                        val (id, announcement) = announcements[index]
                        AnnouncementCard(
                            text = announcement["text"] as? String ?: "No content",
                            timestamp = announcement["timestamp"] as? Long ?: 0L,
                            onDelete = {
                                deleteAnnouncement(db, id) { success ->
                                    if (success) {
                                        announcements = announcements.filterNot { it.first == id }
                                    } else {
                                        errorMessage = "Failed to delete announcement. Please try again."
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showDialog) {
        AnnouncementDialog(
            onDismiss = { showDialog = false },
            onPost = { text ->
                postAnnouncement(db, text) { result ->
                    if (result == "Announcement posted") {
                        showDialog = false
                        announcements = announcements + ("" to mapOf("text" to text, "timestamp" to System.currentTimeMillis()))
                    }
                }
            }
        )
    }
}

@Composable
fun AnnouncementDialog(onDismiss: () -> Unit, onPost: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Post Announcement") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Announcement") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = { if (text.isNotEmpty()) onPost(text) }) {
                Text("Post")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AnnouncementCard(text: String, timestamp: Long, onDelete: () -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = text, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Posted: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(timestamp)}",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { showDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Announcement",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Delete Announcement") },
            text = { Text("Are you sure you want to delete this announcement?") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDialog = false
                }) {
                    Text("Confirm", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

fun postAnnouncement(db: FirebaseFirestore, text: String, onComplete: (String) -> Unit) {
    val announcement = mapOf("text" to text, "timestamp" to System.currentTimeMillis())
    db.collection("announcements").add(announcement)
        .addOnSuccessListener { onComplete("Announcement posted") }
        .addOnFailureListener { onComplete("Failed to post announcement") }
}

fun deleteAnnouncement(db: FirebaseFirestore, id: String, onComplete: (Boolean) -> Unit) {
    db.collection("announcements").document(id).delete()
        .addOnSuccessListener { onComplete(true) }
        .addOnFailureListener { onComplete(false) }
}
