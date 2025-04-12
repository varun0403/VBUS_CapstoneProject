package com.example.vbus.admin

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.vbus.notification.NotificationBody
import com.example.vbus.notification.NotificationMessage
import com.example.vbus.notification.NotificationWrapper
import com.example.vbus.notification.sendingNotification
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AdminAnnouncementScreen() {
    val context = LocalContext.current
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
                    itemsIndexed(announcements) { index, item ->
                        val (id, announcement) = item
                        AnnouncementCard(
                            text = announcement["text"] as? String ?: "No content",
                            timestamp = announcement["timestamp"] as? Long ?: 0L,
                            fileUrl = announcement["fileUrl"] as? String,
                            context = LocalContext.current,
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
            onPost = { text, uri ->
                postAnnouncementWithFile(context,db,text,uri) { result, newAnnouncement ->
                    if (result == "Announcement posted") {
                        showDialog = false
                        announcements = announcements + ("" to newAnnouncement)
                    }
                }
            }
        )
    }
}

@Composable
fun AnnouncementDialog(
    onDismiss: () -> Unit,
    onPost: (String, Uri?) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        selectedFileUri = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Post Announcement") },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Announcement") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { launcher.launch("*/*") }) {
                    Text(if (selectedFileUri != null) "File Selected" else "Attach File")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { if (text.isNotEmpty()) onPost(text, selectedFileUri) }) {
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
fun AnnouncementCard(
    text: String,
    timestamp: Long,
    fileUrl: String?,
    context: Context,
    onDelete: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
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
            fileUrl?.let {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fileUrl))
                    context.startActivity(intent)
                }) {
                    Text("Open Attachment")
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

fun postAnnouncementWithFile(
    context: Context,
    db: FirebaseFirestore,
    text: String,
    fileUri: Uri?,
    onComplete: (String, Map<String, Any>) -> Unit
) {
    val timestamp = System.currentTimeMillis()
    val postAndNotify: (Map<String, Any>) -> Unit = { announcement ->
        db.collection("announcements").add(announcement)
            .addOnSuccessListener {
                onComplete("Announcement posted", announcement)

                // ✅ Send notification to all students with valid FCM token
                db.collection("students").get().addOnSuccessListener { snapshot ->
                    for (doc in snapshot.documents) {
                        val fcmToken = doc.getString("fcmToken")
                        if (!fcmToken.isNullOrEmpty()) {
                            val notification = NotificationBody(
                                title = "New Announcement",
                                body = text
                            )
                            val message = NotificationMessage(
                                token = fcmToken,
                                notification = notification
                            )
                            val wrapper = NotificationWrapper(message = message)
                            sendingNotification(wrapper, context)
                        }
                    }
                }

            }
            .addOnFailureListener {
                onComplete("Failed to post announcement", emptyMap())
            }
    }

    if (fileUri == null) {
        val announcement = mapOf("text" to text, "timestamp" to timestamp)
        postAndNotify(announcement)
    }
    else {
        val storageRef = FirebaseStorage.getInstance().reference
            .child("announcements/${UUID.randomUUID()}")
        storageRef.putFile(fileUri)
            .continueWithTask { task ->
                if (!task.isSuccessful) throw task.exception!!
                storageRef.downloadUrl
            }.addOnSuccessListener { uri ->
                val announcement = mapOf(
                    "text" to text,
                    "timestamp" to timestamp,
                    "fileUrl" to uri.toString()
                )
                postAndNotify(announcement)
            }.addOnFailureListener {
                onComplete("Failed to post announcement", emptyMap())
            }
    }
}


fun deleteAnnouncement(db: FirebaseFirestore, id: String, onComplete: (Boolean) -> Unit) {
    db.collection("announcements").document(id).delete()
        .addOnSuccessListener { onComplete(true) }
        .addOnFailureListener { onComplete(false) }
}
