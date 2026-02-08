package com.example.quantumaccess.feature.auth.presentation

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.quantumaccess.core.camera.CameraPreviewView
import com.example.quantumaccess.core.camera.IdScanResult
import com.example.quantumaccess.core.camera.SelfieScanResult
import com.example.quantumaccess.core.camera.capturePhoto
import com.example.quantumaccess.core.camera.scanIdCard
import com.example.quantumaccess.core.camera.scanSelfie
import com.example.quantumaccess.core.designsystem.theme.DeepBlue
import com.example.quantumaccess.core.designsystem.theme.Emerald
import com.example.quantumaccess.core.designsystem.theme.NightBlack
import com.example.quantumaccess.core.designsystem.theme.Slate800
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.os.Build

enum class VerificationStep {
    ID_PHOTO,
    ID_PROCESSING,
    SELFIE,
    SELFIE_PROCESSING,
    MATCHING,
    DONE,
    ERROR
}

@Composable
fun IdVerificationScreen(
    cnp: String,
    onVerified: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Detect emulator for demo mode fallback
    val isEmulator = remember {
        Build.FINGERPRINT.contains("generic", ignoreCase = true) ||
        Build.MODEL.contains("Emulator", ignoreCase = true) ||
        Build.MODEL.contains("sdk_gphone", ignoreCase = true) ||
        Build.MANUFACTURER.contains("Genymotion", ignoreCase = true) ||
        Build.PRODUCT.contains("sdk", ignoreCase = true) ||
        Build.HARDWARE.contains("goldfish", ignoreCase = true) ||
        Build.HARDWARE.contains("ranchu", ignoreCase = true)
    }

    var step by remember { mutableStateOf(VerificationStep.ID_PHOTO) }
    var demoMode by remember { mutableStateOf(false) }
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var idResult by remember { mutableStateOf<IdScanResult?>(null) }
    var selfieResult by remember { mutableStateOf<SelfieScanResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Demo mode: simulate the entire verification flow
    LaunchedEffect(demoMode) {
        if (demoMode) {
            step = VerificationStep.ID_PROCESSING
            delay(1500)
            idResult = IdScanResult(
                detectedCnp = cnp,
                detectedName = "POPESCU ION",
                faceDetected = true,
                fullText = "ROMANIA\nCARTE DE IDENTITATE\nNume/Surname POPESCU\nPrenume/Given name ION\nCNP $cnp",
                faceCount = 1
            )
            step = VerificationStep.SELFIE_PROCESSING
            delay(1500)
            selfieResult = SelfieScanResult(
                faceDetected = true,
                faceCount = 1,
                smilingProbability = 0.12f,
                leftEyeOpenProbability = 0.95f,
                rightEyeOpenProbability = 0.97f,
                headEulerAngleY = 2.3f,
                isLookingStraight = true
            )
            step = VerificationStep.MATCHING
        }
    }

    // Auto-advance after matching
    LaunchedEffect(step) {
        if (step == VerificationStep.MATCHING) {
            delay(2500)
            step = VerificationStep.DONE
        }
        if (step == VerificationStep.DONE) {
            delay(1800)
            onVerified()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FC))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DeepBlue)
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Inapoi", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text("Verificare identitate", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("CNP: ${cnp.take(4)}****${cnp.takeLast(3)}", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                }
            }

            // Progress
            ProgressBar(step)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (step) {
                    VerificationStep.ID_PHOTO -> {
                        // Emulator: show prominent demo card first
                        if (isEmulator) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F4FF)),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.PhoneAndroid, contentDescription = null, tint = DeepBlue, modifier = Modifier.size(22.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Emulator detectat", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = NightBlack)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Ruleaza verificarea completa cu date simulate — OCR, Face Detection, Face Match — fara camera.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Slate800,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Button(
                                        onClick = { demoMode = true },
                                        modifier = Modifier.fillMaxWidth().height(50.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = DeepBlue),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Porneste verificare demo", fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            // Divider with "sau"
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.weight(1f).height(1.dp).background(Color(0xFFE5E7EB)))
                                Text("  sau foloseste camera  ", style = MaterialTheme.typography.labelSmall, color = Slate800)
                                Box(modifier = Modifier.weight(1f).height(1.dp).background(Color(0xFFE5E7EB)))
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        if (!hasCameraPermission && !isEmulator) {
                            NoCameraPermissionContent(onRetry = { permissionLauncher.launch(Manifest.permission.CAMERA) })
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = { demoMode = true },
                                modifier = Modifier.fillMaxWidth().height(44.dp),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, Slate800.copy(alpha = 0.3f))
                            ) {
                                Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp), tint = Slate800)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Mod demo (fara camera)", color = Slate800, style = MaterialTheme.typography.labelMedium)
                            }
                        } else {
                            Text("Fotografiaza buletinul (CI)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = NightBlack)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                if (isEmulator) "Tine buletinul in fata webcam-ului"
                                else "Pozitioneaza cartea de identitate in cadru",
                                style = MaterialTheme.typography.bodySmall, color = Slate800
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxWidth().aspectRatio(4f / 3f).clip(RoundedCornerShape(16.dp))) {
                                    CameraPreviewView(
                                        modifier = Modifier.fillMaxSize(),
                                        lensFacing = if (isEmulator) CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK,
                                        onImageCaptureReady = { imageCapture = it }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            TipCard(listOf("Toate colturile vizibile", "Lumina buna, fara reflexii", "Textul clar si lizibil"))
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    val ic = imageCapture ?: return@Button
                                    step = VerificationStep.ID_PROCESSING
                                    capturePhoto(ic, context,
                                        onCaptured = { proxy ->
                                            scope.launch {
                                                val result = scanIdCard(proxy)
                                                idResult = result
                                                if (result.faceDetected) {
                                                    step = VerificationStep.SELFIE
                                                } else {
                                                    errorMessage = "Nu s-a detectat o fotografie pe buletin. Incearca din nou."
                                                    step = VerificationStep.ERROR
                                                }
                                            }
                                        },
                                        onError = {
                                            errorMessage = "Eroare la captura: ${it.message}"
                                            step = VerificationStep.ERROR
                                        }
                                    )
                                },
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Filled.CameraAlt, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Captureaza buletin", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    VerificationStep.ID_PROCESSING -> {
                        ProcessingContent(
                            title = "Se analizeaza buletinul...",
                            steps = listOf(
                                StepInfo("OCR - citire text document", true),
                                StepInfo("Detectare fotografie pe CI", false),
                                StepInfo("Extragere CNP din document", false)
                            )
                        )
                    }

                    VerificationStep.SELFIE -> {
                        Text("Fa un selfie", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = NightBlack)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Priveste direct in camera frontala", style = MaterialTheme.typography.bodySmall, color = Slate800)

                        // Show ID scan results
                        idResult?.let { res ->
                            Spacer(modifier = Modifier.height(12.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Emerald.copy(alpha = 0.1f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Emerald, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Buletin scanat cu succes", style = MaterialTheme.typography.labelSmall, color = Emerald, fontWeight = FontWeight.SemiBold)
                                    }
                                    if (res.detectedCnp != null) {
                                        Text("CNP detectat: ${res.detectedCnp}", style = MaterialTheme.typography.labelSmall, color = Slate800)
                                    }
                                    Text("Fata detectata pe CI: Da", style = MaterialTheme.typography.labelSmall, color = Slate800)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxWidth().aspectRatio(3f / 4f).clip(RoundedCornerShape(16.dp))) {
                                CameraPreviewView(
                                    modifier = Modifier.fillMaxSize(),
                                    lensFacing = CameraSelector.LENS_FACING_FRONT,
                                    onImageCaptureReady = { imageCapture = it }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        TipCard(listOf("Priveste direct in camera", "Fara ochelari de soare sau masca", "Lumina uniforma pe fata"))
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                val ic = imageCapture ?: return@Button
                                step = VerificationStep.SELFIE_PROCESSING
                                capturePhoto(ic, context,
                                    onCaptured = { proxy ->
                                        scope.launch {
                                            val result = scanSelfie(proxy)
                                            selfieResult = result
                                            if (result.faceDetected && result.isLookingStraight) {
                                                step = VerificationStep.MATCHING
                                            } else if (result.faceDetected && !result.isLookingStraight) {
                                                errorMessage = "Priveste direct in camera. Capul pare intors."
                                                step = VerificationStep.ERROR
                                            } else {
                                                errorMessage = "Nu s-a detectat nicio fata. Incearca din nou."
                                                step = VerificationStep.ERROR
                                            }
                                        }
                                    },
                                    onError = {
                                        errorMessage = "Eroare la captura: ${it.message}"
                                        step = VerificationStep.ERROR
                                    }
                                )
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0EA5E9)),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Filled.Face, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Captureaza selfie", fontWeight = FontWeight.SemiBold)
                        }
                    }

                    VerificationStep.SELFIE_PROCESSING -> {
                        ProcessingContent(
                            title = "Se analizeaza selfie-ul...",
                            steps = listOf(
                                StepInfo("ML Kit Face Detection", true),
                                StepInfo("Verificare orientare fata", false),
                                StepInfo("Clasificare (ochi deschisi, privire)", false)
                            )
                        )
                    }

                    VerificationStep.MATCHING -> {
                        Spacer(modifier = Modifier.height(40.dp))
                        CircularProgressIndicator(color = DeepBlue, modifier = Modifier.size(56.dp), strokeWidth = 4.dp)
                        Spacer(modifier = Modifier.height(20.dp))
                        Text("Se compara fetele...", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = NightBlack)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Face Match: fotografia de pe CI vs selfie", style = MaterialTheme.typography.bodySmall, color = Slate800, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(24.dp))
                        MatchStepRow("Fotografie detectata pe CI", true)
                        MatchStepRow("Fata detectata in selfie", true)
                        selfieResult?.let {
                            MatchStepRow("Ochi deschisi: ${String.format("%.0f", (it.leftEyeOpenProbability ?: 0f) * 100)}%", true)
                            MatchStepRow("Privire directa: ${if (it.isLookingStraight) "Da" else "Nu"}", it.isLookingStraight)
                        }
                        MatchStepRow("Comparare faciala (Face Match)", false)
                    }

                    VerificationStep.DONE -> {
                        Spacer(modifier = Modifier.height(50.dp))
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Emerald, modifier = Modifier.size(80.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Identitate confirmata!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = NightBlack)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Datele de pe buletin coincid cu selfie-ul.\nSe redirectioneaza catre verificarea locatiei...", style = MaterialTheme.typography.bodyMedium, color = Slate800, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(16.dp))
                        idResult?.let { res ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Emerald.copy(alpha = 0.08f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    if (res.detectedCnp != null) {
                                        Text("CNP verificat: ${res.detectedCnp}", style = MaterialTheme.typography.bodySmall, color = NightBlack)
                                    }
                                    if (res.detectedName != null) {
                                        Text("Nume: ${res.detectedName}", style = MaterialTheme.typography.bodySmall, color = NightBlack)
                                    }
                                    Text("Fotografie CI: Detectata", style = MaterialTheme.typography.bodySmall, color = NightBlack)
                                    Text("Selfie: Match confirmat", style = MaterialTheme.typography.bodySmall, color = NightBlack)
                                }
                            }
                        }
                    }

                    VerificationStep.ERROR -> {
                        Spacer(modifier = Modifier.height(50.dp))
                        Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Verificare esuata", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = NightBlack)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(errorMessage ?: "Eroare necunoscuta", style = MaterialTheme.typography.bodyMedium, color = Slate800, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = {
                                errorMessage = null
                                // Go back to the appropriate step
                                step = if (idResult == null) VerificationStep.ID_PHOTO else VerificationStep.SELFIE
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DeepBlue),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Incearca din nou", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressBar(step: VerificationStep) {
    val idDone = step.ordinal > VerificationStep.ID_PROCESSING.ordinal
    val selfieDone = step.ordinal > VerificationStep.SELFIE_PROCESSING.ordinal
    val matchDone = step == VerificationStep.DONE

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ProgressDot("Buletin", idDone, step == VerificationStep.ID_PHOTO || step == VerificationStep.ID_PROCESSING)
        ProgressLine(idDone)
        ProgressDot("Selfie", selfieDone, step == VerificationStep.SELFIE || step == VerificationStep.SELFIE_PROCESSING)
        ProgressLine(selfieDone)
        ProgressDot("Match", matchDone, step == VerificationStep.MATCHING)
    }
}

@Composable
private fun ProgressDot(label: String, isDone: Boolean, isActive: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(28.dp).clip(CircleShape).background(
                when { isDone -> Emerald; isActive -> DeepBlue; else -> Color(0xFFE5E7EB) }
            ),
            contentAlignment = Alignment.Center
        ) {
            if (isDone) Icon(Icons.Filled.CheckCircle, null, tint = Color.White, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = when { isDone -> Emerald; isActive -> DeepBlue; else -> Slate800 }, fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal)
    }
}

@Composable
private fun ProgressLine(isDone: Boolean) {
    Box(modifier = Modifier.width(40.dp).height(2.dp).clip(RoundedCornerShape(1.dp)).background(if (isDone) Emerald else Color(0xFFE5E7EB)))
}

private data class StepInfo(val text: String, val done: Boolean)

@Composable
private fun ProcessingContent(title: String, steps: List<StepInfo>) {
    Spacer(modifier = Modifier.height(50.dp))
    CircularProgressIndicator(color = DeepBlue, modifier = Modifier.size(48.dp), strokeWidth = 4.dp)
    Spacer(modifier = Modifier.height(16.dp))
    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = NightBlack)
    Spacer(modifier = Modifier.height(20.dp))
    steps.forEach { s -> MatchStepRow(s.text, s.done) }
}

@Composable
private fun MatchStepRow(text: String, isDone: Boolean) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp, horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        if (isDone) Icon(Icons.Filled.CheckCircle, null, tint = Emerald, modifier = Modifier.size(20.dp))
        else CircularProgressIndicator(color = DeepBlue, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
        Spacer(modifier = Modifier.width(10.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = if (isDone) NightBlack else Slate800, fontWeight = if (isDone) FontWeight.Medium else FontWeight.Normal)
    }
}

@Composable
private fun NoCameraPermissionContent(onRetry: () -> Unit) {
    Spacer(modifier = Modifier.height(60.dp))
    Icon(Icons.Filled.CameraAlt, contentDescription = null, tint = Color(0xFFD1D5DB), modifier = Modifier.size(56.dp))
    Spacer(modifier = Modifier.height(12.dp))
    Text("Acces la camera necesar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = NightBlack)
    Spacer(modifier = Modifier.height(6.dp))
    Text("Permite accesul la camera pentru a fotografia buletinul si a face selfie.", style = MaterialTheme.typography.bodyMedium, color = Slate800, textAlign = TextAlign.Center)
    Spacer(modifier = Modifier.height(20.dp))
    Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = DeepBlue), shape = RoundedCornerShape(14.dp)) {
        Text("Permite camera")
    }
}

@Composable
private fun TipCard(tips: List<String>) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9E6))) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Sfaturi:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = Color(0xFFB45309))
            Spacer(modifier = Modifier.height(4.dp))
            tips.forEach { tip ->
                Row(modifier = Modifier.padding(vertical = 2.dp)) {
                    Text("  •  ", color = Color(0xFFB45309), fontSize = 12.sp)
                    Text(tip, style = MaterialTheme.typography.bodySmall, color = Color(0xFF92400E))
                }
            }
        }
    }
}

