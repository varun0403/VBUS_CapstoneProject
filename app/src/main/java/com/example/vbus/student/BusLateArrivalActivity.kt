package com.example.vbus.student

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun GenerateLateArrivalLetter(navController: NavController,email: String, context: Context) {
    var isGenerating by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Late Arrival E-Letter",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                isGenerating = true
                successMessage = generateAndSaveLetter(email, context)
                isGenerating = false
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isGenerating) {
                Text("Generating...")
            }
            else {
                Text("Generate and Download Letter")
            }
        }

        if (successMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = successMessage,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
fun generateAndSaveLetter(email: String, context: Context): String {
    val letterContent = """
        Dear Student,
        
        This is to inform you that the bus carrying students reached the campus after 8:00 AM today.
        
        Please find the details below:
        - Student Email: $email
        - Arrival Time: [Enter Arrival Time Here]
        
        Kindly reach out to the transport department for further assistance.
        
        Regards,
        Transport Department
    """.trimIndent()

    return try {
        val fileName = "LateArrivalLetter_${email}.txt"

        // Use MediaStore for saving in Downloads (Scoped Storage)
        val contentResolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            ?: return "Failed to save letter. Uri is null."

        contentResolver.openOutputStream(uri)?.use { outputStream ->
            outputStream.write(letterContent.toByteArray())
        }

        // Optional: Notify user of exact file location
        Log.d("FileSave", "Saved file URI: $uri")
        "Letter saved successfully to Downloads: $fileName"
    } catch (e: Exception) {
        e.printStackTrace()
        "Failed to generate the letter. Please try again."
    }
}
