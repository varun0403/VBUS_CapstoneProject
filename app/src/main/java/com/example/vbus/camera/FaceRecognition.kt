package com.example.vbus.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import org.tensorflow.lite.Interpreter
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import org.json.JSONArray
import java.io.ByteArrayOutputStream

fun uploadToFirebaseStorage(bitmap: Bitmap) {
    val storage = FirebaseStorage.getInstance()
    val storageRef = storage.reference
    val fileName = "debug_faces/debug_face_${System.currentTimeMillis()}.jpg"
    val faceImageRef = storageRef.child(fileName)

    val baos = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
    val data = baos.toByteArray()
    val uploadTask = faceImageRef.putBytes(data)
    uploadTask
        .addOnSuccessListener {
            Log.d("FaceRecognition", "Uploaded face image to Firebase: $fileName")
        }
        .addOnFailureListener {
            Log.e("FaceRecognition", "Failed to upload face image: ${it.message}")
        }
}


fun loadModelFile(context: Context, modelPath: String): MappedByteBuffer {
    return try {
        val fileDescriptor = context.assets.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        fileChannel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.startOffset, fileDescriptor.length)
    } catch (e: Exception) {
        Log.e("FaceRecognition", "Error loading model: ${e.message}")
        throw RuntimeException("Model loading failed")
    }
}


fun createFaceNetInterpreter(context: Context): org.tensorflow.lite.Interpreter {
    val modelPath = "facenet.tflite"
    val model = loadModelFile(context, modelPath)
    return Interpreter(model)
}

fun preprocessFace(context: Context, bitmap: Bitmap, faceRect: Rect): Bitmap {
    Log.d("FaceRecognition", "Bitmap dimensions: ${bitmap.width}x${bitmap.height}")
    Log.d("FaceRecognition", "Face rectangle: $faceRect")

    val left = faceRect.left.coerceAtLeast(0).coerceAtMost(bitmap.width - 1)
    val top = faceRect.top.coerceAtLeast(0).coerceAtMost(bitmap.height - 1)
    val right = faceRect.right.coerceAtLeast(left + 1).coerceAtMost(bitmap.width)
    val bottom = faceRect.bottom.coerceAtLeast(top + 1).coerceAtMost(bitmap.height)

    if (right <= left || bottom <= top) {
        throw IllegalArgumentException("Invalid face rectangle dimensions: $faceRect")
    }

    val croppedBitmap = Bitmap.createBitmap(bitmap, left, top, right - left, bottom - top)
    val resizedBitmap = Bitmap.createScaledBitmap(croppedBitmap, 160, 160, true)

    uploadToFirebaseStorage(resizedBitmap)

    return resizedBitmap
}

fun bitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
    if (bitmap.width != 160 || bitmap.height != 160) {
        throw IllegalArgumentException("Bitmap must be 160x160 for FaceNet model")
    }

    val buffer = ByteBuffer.allocateDirect(4 * 160 * 160 * 3)
    buffer.order(ByteOrder.nativeOrder())

    val intValues = IntArray(160 * 160)
    bitmap.getPixels(intValues, 0, 160, 0, 0, 160, 160)

    // is this normalisation
    for (pixelValue in intValues) {
        buffer.putFloat(((pixelValue shr 16 and 0xFF) - 127.5f) / 127.5f)
        buffer.putFloat(((pixelValue shr 8 and 0xFF) - 127.5f) / 127.5f)
        buffer.putFloat(((pixelValue and 0xFF) - 127.5f) / 127.5f)
    }

    return buffer
}


fun getFaceEmbedding(interpreter: Interpreter, bitmap: Bitmap): FloatArray {
    val byteBuffer = bitmapToByteBuffer(bitmap)
    val output = Array(1) { FloatArray(128) }

    try {
        interpreter.run(byteBuffer, output)
    } catch (e: Exception) {
        Log.e("FaceRecognition", "Model inference failed: ${e.message}")
        throw RuntimeException("Model inference failed")
    }

    return output[0]
}



fun cosineSimilarity(embedding1: FloatArray, embedding2: List<Float>): Float {
    var dotProduct = 0.0f
    var normA = 0.0f
    var normB = 0.0f
    for (i in embedding1.indices) {
        dotProduct += embedding1[i] * embedding2[i]
        normA += embedding1[i] * embedding1[i]
        normB += embedding2[i] * embedding2[i]
    }
    return dotProduct / (Math.sqrt(normA.toDouble()).toFloat() * Math.sqrt(normB.toDouble()).toFloat())
}

data class FaceEmbedding(val label: String, val embedding: List<Float>)

fun loadEmbeddings(context: Context): List<FaceEmbedding> {
    return try {
        val jsonString = context.assets.open("face_embeddings.json").bufferedReader().use { it.readText() }
        val jsonArray = JSONArray(jsonString)
        val embeddingsList = mutableListOf<FaceEmbedding>()

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            val label = obj.getString("label")
            val embeddingArray = obj.getJSONArray("embedding")
            val embedding = List(128) { embeddingArray.getDouble(it).toFloat() }
            embeddingsList.add(FaceEmbedding(label, embedding))
        }
        embeddingsList
    } catch (e: Exception) {
        Log.e("FaceRecognition", "Error loading embeddings: ${e.message}")
        emptyList()  // Avoid crashing if the file has an issue
    }
}


fun recognizeFace(context: Context, bitmap: Bitmap, faceRect: Rect): String {
    // Load the FaceNet model
    val interpreter = try {
        createFaceNetInterpreter(context)
    } catch (e: Exception) {
        Log.e("FaceRecognition", "Interpreter creation failed: ${e.message}")
        return "Error loading model"
    }

    // Preprocess the face image
    val preprocessedFace = try {
        preprocessFace(context, bitmap, faceRect) // <-- uses updated version that saves the image
    } catch (e: IllegalArgumentException) {
        Log.e("FaceRecognition", "Face preprocessing failed: ${e.message}")
        return "Invalid face area"
    }

    // Generate the 128-D embedding
    val faceEmbedding = getFaceEmbedding(interpreter, preprocessedFace)

    // Load stored embeddings (your known faces)
    val storedEmbeddings = loadEmbeddings(context)
    if (storedEmbeddings.isEmpty()) return "No known faces available"

    // Compare the embeddings using cosine similarity
    var bestMatch: String? = null
    var bestSimilarity = -1f

    for (storedEmbedding in storedEmbeddings) {
        val similarity = cosineSimilarity(faceEmbedding, storedEmbedding.embedding)
        if (similarity > bestSimilarity) {
            bestSimilarity = similarity
            bestMatch = storedEmbedding.label
        }
    }

    Log.d("FaceRecognition", "Best match: $bestMatch with similarity: $bestSimilarity")

    return if (bestSimilarity > 0.7f) bestMatch ?: "Unknown" else "Unknown"
}

