package com.example.quantumaccess.core.camera

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream

private const val TAG = "MlKitAnalyzer"

/**
 * Result from ID card OCR scan.
 */
data class IdScanResult(
    val detectedCnp: String?,
    val detectedName: String?,
    val faceDetected: Boolean,
    val fullText: String,
    val faceCount: Int = 0
)

/**
 * Result from selfie face detection.
 */
data class SelfieScanResult(
    val faceDetected: Boolean,
    val faceCount: Int,
    val smilingProbability: Float?,
    val leftEyeOpenProbability: Float?,
    val rightEyeOpenProbability: Float?,
    val headEulerAngleY: Float?,
    val isLookingStraight: Boolean
)

/**
 * Scan an ID card image: OCR for text (CNP extraction) + face detection.
 */
suspend fun scanIdCard(imageProxy: ImageProxy): IdScanResult {
    val bitmap = imageProxyToBitmap(imageProxy)
    imageProxy.close()
    if (bitmap == null) return IdScanResult(null, null, false, "")

    val inputImage = InputImage.fromBitmap(bitmap, 0)

    // OCR
    val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    val textResult = try {
        textRecognizer.process(inputImage).await()
    } catch (e: Exception) {
        Log.e(TAG, "OCR failed", e)
        null
    }

    val fullText = textResult?.text ?: ""
    val detectedCnp = extractCnpFromText(fullText)
    val detectedName = extractNameFromText(fullText)

    // Face detection on ID card photo
    val faceOptions = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
        .setMinFaceSize(0.1f)
        .build()
    val faceDetector = FaceDetection.getClient(faceOptions)
    val faces = try {
        faceDetector.process(inputImage).await()
    } catch (e: Exception) {
        Log.e(TAG, "Face detection on ID failed", e)
        emptyList()
    }

    textRecognizer.close()
    faceDetector.close()

    return IdScanResult(
        detectedCnp = detectedCnp,
        detectedName = detectedName,
        faceDetected = faces.isNotEmpty(),
        fullText = fullText,
        faceCount = faces.size
    )
}

/**
 * Scan a selfie for face presence, orientation, eyes open.
 */
suspend fun scanSelfie(imageProxy: ImageProxy): SelfieScanResult {
    val bitmap = imageProxyToBitmap(imageProxy)
    imageProxy.close()
    if (bitmap == null) return SelfieScanResult(false, 0, null, null, null, null, false)

    val inputImage = InputImage.fromBitmap(bitmap, 0)

    val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .setMinFaceSize(0.25f)
        .build()

    val faceDetector = FaceDetection.getClient(options)
    val faces: List<Face> = try {
        faceDetector.process(inputImage).await()
    } catch (e: Exception) {
        Log.e(TAG, "Selfie face detection failed", e)
        emptyList()
    }
    faceDetector.close()

    if (faces.isEmpty()) {
        return SelfieScanResult(false, 0, null, null, null, null, false)
    }

    val face = faces.first()
    val headY = face.headEulerAngleY
    val isLookingStraight = headY in -15f..15f

    return SelfieScanResult(
        faceDetected = true,
        faceCount = faces.size,
        smilingProbability = face.smilingProbability,
        leftEyeOpenProbability = face.leftEyeOpenProbability,
        rightEyeOpenProbability = face.rightEyeOpenProbability,
        headEulerAngleY = headY,
        isLookingStraight = isLookingStraight
    )
}

/**
 * Extract a 13-digit CNP from OCR text.
 */
private fun extractCnpFromText(text: String): String? {
    val regex = Regex("[1-8]\\d{12}")
    return regex.find(text.replace(" ", "").replace("\n", ""))?.value
}

/**
 * Attempt to extract a name from ID card OCR text.
 * Romanian ID cards have "Nume/Surname" and "Prenume/Given name" fields.
 */
private fun extractNameFromText(text: String): String? {
    val lines = text.split("\n").map { it.trim() }.filter { it.isNotBlank() }
    for (i in lines.indices) {
        val lower = lines[i].lowercase()
        if (lower.contains("nume") || lower.contains("surname")) {
            if (i + 1 < lines.size) return lines[i + 1]
        }
        if (lower.contains("prenume") || lower.contains("given")) {
            if (i + 1 < lines.size) return lines[i + 1]
        }
    }
    return null
}

/**
 * Convert ImageProxy to Bitmap (handles JPEG and YUV).
 */
private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap? {
    return try {
        val buffer = imageProxy.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

        // Apply rotation
        val rotation = imageProxy.imageInfo.rotationDegrees
        if (rotation != 0 && bitmap != null) {
            val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } else {
            bitmap
        }
    } catch (e: Exception) {
        Log.e(TAG, "ImageProxy to Bitmap failed", e)
        null
    }
}
