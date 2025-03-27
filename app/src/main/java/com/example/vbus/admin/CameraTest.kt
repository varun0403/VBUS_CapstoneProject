package com.example.vbus.admin

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.vbus.camera.CameraView

@Composable
fun CameraScreen() {
    CameraView(
        modifier = Modifier.fillMaxSize(),
        onFaceRecognized = { faceBitmap, faceRect, recognizedName ->
            Log.d("CameraScreen", "Face recognized as: $recognizedName")
        }
    )
}

