package com.example.vbus.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.max
import kotlin.math.min

@Composable
fun CameraView(
    modifier: Modifier = Modifier,
    onFaceRecognized: (Bitmap, RectF, String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> = remember {
        ProcessCameraProvider.getInstance(context)
    }

    var faceBoundingBox by remember { mutableStateOf<RectF?>(null) }
    var recognizedName by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(faceBoundingBox) {
        Log.d("FaceDetection", "Bounding box updated: $faceBoundingBox")
    }

    Box(modifier = modifier) {
        AndroidView(
            modifier = Modifier.matchParentSize(),
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        processImage(imageProxy, previewView, context) { bitmap, mappedRect, name ->
                            onFaceRecognized(bitmap, mappedRect, name.toString())
                            faceBoundingBox = mappedRect
                            recognizedName = name.toString()  // Store the recognized name
                        }
                    }

                    val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalysis
                        )
                    } catch (exc: Exception) {
                        Log.e("CameraView", "Camera binding failed", exc)
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            }
        )

        faceBoundingBox?.let { rect ->
            Canvas(modifier = Modifier.matchParentSize()) {
                drawRect(
                    color = Color.Green,
                    topLeft = Offset(rect.left, rect.top),
                    size = Size(rect.width(), rect.height()),
                    style = Stroke(width = 5f)
                )

                recognizedName?.let { name ->
                    drawContext.canvas.nativeCanvas.drawText(
                        name,
                        rect.left,
                        rect.top - 10, // Slightly above the bounding box
                        Paint().apply {
                            color = android.graphics.Color.WHITE
                            textSize = 48f
                            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        }
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalGetImage::class)
fun processImage(
    imageProxy: ImageProxy,
    previewView: PreviewView,
    context: Context, // Add context to call recognizeFace()
    onFaceRecognized: (Bitmap, RectF, Any?) -> Unit
) {
    val mediaImage = imageProxy.image ?: return
    val rotationDegrees = imageProxy.imageInfo.rotationDegrees
    val image = InputImage.fromMediaImage(mediaImage, rotationDegrees)

    val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
        .build()

    val detector = FaceDetection.getClient(options)

    detector.process(image)
        .addOnSuccessListener { faces ->
            if (faces.isNotEmpty()) {
                val face = faces.first()
                val bitmap = imageProxy.toBitmap()

                val croppedFace = cropFace(bitmap, face.boundingBox)

                if (croppedFace != null) {
                    val resizedFace = Bitmap.createScaledBitmap(croppedFace, 160, 160, true)
                    val mappedBox = mapBoundingBox(
                        face.boundingBox,
                        imageProxy.width.toFloat(),
                        imageProxy.height.toFloat(),
                        previewView
                    )

                    if (mappedBox != null) {
                        // Call the recognizeFace function here
                        val recognizedName = recognizeFace(context, resizedFace, face.boundingBox)
                        Log.d("Name of the person",recognizedName)

                        onFaceRecognized(croppedFace, mappedBox, recognizedName)
                    }
                    else {
                        Log.w("CameraView", "Mapping failed. Not drawing bounding box.")
                    }
                }
                else {
                    Log.w("CameraView", "Cropping failed. Skipping face detection for this frame.")
                }
            }
        }
        .addOnFailureListener { e ->
            Log.e("CameraView", "Face detection failed", e)
        }
        .addOnCompleteListener {
            imageProxy.close()
        }
}


fun cropFace(bitmap: Bitmap, rect: Rect): Bitmap? {
    return try {
        // Ensure the cropping region is valid
        val croppedX = max(0, rect.left)
        val croppedY = max(0, rect.top)
        val croppedWidth = min(rect.width(), bitmap.width - croppedX)
        val croppedHeight = min(rect.height(), bitmap.height - croppedY)

        if (croppedWidth <= 0 || croppedHeight <= 0) {
            Log.e("CropFace", "Invalid crop dimensions")
            return null
        }

        Bitmap.createBitmap(bitmap, croppedX, croppedY, croppedWidth, croppedHeight)
    } catch (e: Exception) {
        Log.e("CropFace", "Error cropping face: ${e.message}")
        null
    }
}


fun mapBoundingBox(
    originalBox: Rect,
    imageWidth: Float,
    imageHeight: Float,
    previewView: PreviewView
): RectF? {
    val previewWidth = previewView.width.toFloat()
    val previewHeight = previewView.height.toFloat()

    if (previewWidth == 0f || previewHeight == 0f) return null

    // Handle potential rotation: Swap width & height for portrait mode
    val isPortraitMode = previewHeight > previewWidth
    val scaleX = if (isPortraitMode) previewWidth / imageHeight else previewWidth / imageWidth
    val scaleY = if (isPortraitMode) previewHeight / imageWidth else previewHeight / imageHeight

    // Map coordinates
    val left = originalBox.left * scaleX
    val top = originalBox.top * scaleY
    val right = originalBox.right * scaleX
    val bottom = originalBox.bottom * scaleY

    // Clamp values to ensure they remain within bounds
    val mappedRect = RectF(
        left.coerceIn(0f, previewWidth),
        top.coerceIn(0f, previewHeight),
        right.coerceIn(0f, previewWidth),
        bottom.coerceIn(0f, previewHeight)
    )

    Log.d("FaceDetection", "Mapped bounding box: $mappedRect")

    return mappedRect
}

