package com.example.quantumaccess.data.quantum

import android.util.Log
import kotlin.random.Random

/**
 * Eve Detection System for Quantum Key Distribution
 * 
 * Simulates eavesdropping detection in QKD protocols (BB84, E91)
 * 
 * In real QKD:
 * - Eve's measurement disturbs quantum states
 * - QBER (Quantum Bit Error Rate) increases above threshold (~11%)
 * - System detects and aborts key exchange
 * 
 * This implementation:
 * - Simulates Eve's presence through settings
 * - Calculates QBER based on interception
 * - Provides detection confidence levels
 */
class EveDetector {
    
    /**
     * Detect eavesdropping based on quantum bit error rate
     * 
     * @param quantumEntropy Original entropy from QKD service
     * @param keySize Size of quantum key in bits
     * @param isEveEnabled If true, simulates Eve's presence
     * @return EveDetectionResult with interception status
     */
    fun detectEavesdropping(
        quantumEntropy: Double,
        keySize: Int,
        isEveEnabled: Boolean
    ): EveDetectionResult {
        
        if (!isEveEnabled) {
            // No Eve simulation - secure transaction
            return EveDetectionResult(
                isIntercepted = false,
                qber = 0.0,
                confidence = 0.0,
                detectionMethod = "BB84-Baseline",
                message = "No eavesdropping detected"
            )
        }
        
        // Simulate Eve's interception with random probability
        val eveInterceptionChance = Random.nextDouble(0.0, 1.0)
        
        // Eve detection threshold (in real QKD, ~11% QBER triggers abort)
        val detectionThreshold = 0.15
        
        return if (eveInterceptionChance > 0.7) {
            // Eve is intercepting! (30% chance when enabled)
            val qber = Random.nextDouble(0.12, 0.25) // High error rate
            val confidence = calculateConfidence(qber)
            
            Log.w(TAG, "🚨 EVE DETECTED! QBER: ${qber * 100}%")
            
            EveDetectionResult(
                isIntercepted = true,
                qber = qber,
                confidence = confidence,
                detectionMethod = "BB84-QBER-Analysis",
                message = "⚠️ Eavesdropping detected! QBER: ${String.format("%.1f", qber * 100)}%"
            )
        } else {
            // Eve attempted but not detected, or no Eve
            val qber = Random.nextDouble(0.0, 0.08) // Normal error rate
            
            EveDetectionResult(
                isIntercepted = false,
                qber = qber,
                confidence = 0.0,
                detectionMethod = "BB84-Privacy-Amplification",
                message = "Transaction secure. QBER: ${String.format("%.1f", qber * 100)}%"
            )
        }
    }
    
    /**
     * Calculate detection confidence based on QBER
     * Higher QBER = higher confidence of eavesdropping
     */
    private fun calculateConfidence(qber: Double): Double {
        return when {
            qber > 0.20 -> 0.99 // Very high confidence
            qber > 0.15 -> 0.95 // High confidence
            qber > 0.11 -> 0.85 // Medium-high confidence
            qber > 0.08 -> 0.60 // Medium confidence
            else -> 0.0         // Below threshold
        }
    }
    
    /**
     * Simulate Eve's intercept-resend attack
     * In BB84, Eve measures qubits in random basis, causing errors
     */
    fun simulateInterceptResendAttack(
        originalKey: String,
        eveStrategy: EveStrategy = EveStrategy.RANDOM_BASIS
    ): InterceptedKeyData {
        val keyLength = originalKey.length
        val corruptedBits = mutableListOf<Int>()
        val corruptedKey = StringBuilder(originalKey)
        
        // Eve measures ~50% of qubits in wrong basis → 25% error rate
        repeat(keyLength) { index ->
            if (Random.nextDouble() < 0.5) {
                // Eve measured in wrong basis
                if (Random.nextDouble() < 0.5) {
                    // Bit flip occurs
                    val originalChar = originalKey[index]
                    val flippedChar = if (originalChar in "0123456789") {
                        val newDigit = (originalChar.digitToInt() + Random.nextInt(1, 10)) % 10
                        newDigit.toString()[0]
                    } else {
                        if (originalChar.isLowerCase()) originalChar.uppercaseChar()
                        else originalChar.lowercaseChar()
                    }
                    corruptedKey[index] = flippedChar
                    corruptedBits.add(index)
                }
            }
        }
        
        val errorRate = corruptedBits.size.toDouble() / keyLength
        
        Log.d(TAG, "Eve attack: ${corruptedBits.size}/${keyLength} bits corrupted (${errorRate * 100}%)")
        
        return InterceptedKeyData(
            corruptedKey = corruptedKey.toString(),
            corruptedBitPositions = corruptedBits,
            errorRate = errorRate,
            strategy = eveStrategy
        )
    }
    
    companion object {
        private const val TAG = "EveDetector"
    }
}

/**
 * Result of eavesdropping detection
 */
data class EveDetectionResult(
    val isIntercepted: Boolean,
    val qber: Double,              // Quantum Bit Error Rate (0.0 - 1.0)
    val confidence: Double,         // Detection confidence (0.0 - 1.0)
    val detectionMethod: String,    // BB84, E91, etc.
    val message: String
)

/**
 * Data from intercepted key
 */
data class InterceptedKeyData(
    val corruptedKey: String,
    val corruptedBitPositions: List<Int>,
    val errorRate: Double,
    val strategy: EveStrategy
)

/**
 * Eve's attack strategies
 */
enum class EveStrategy {
    RANDOM_BASIS,           // Measure in random basis (BB84)
    INTERCEPT_RESEND,       // Intercept and resend
    PHOTON_NUMBER_SPLITTING, // PNS attack
    TROJAN_HORSE            // Send probes back
}
