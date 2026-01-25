package com.example.quantumaccess.core.security

import com.example.quantumaccess.domain.model.TransactionScenario
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Security Scorer - calculează scorurile de securitate pentru QKD vs Criptografie Clasică
 * 
 * QKD (Quantum Key Distribution) oferă securitate bazată pe legile fizicii cuantice:
 * - Detectare automată a interceptărilor (Eve) prin QBER
 * - Regenerare automată a cheilor compromise
 * 
 * Formula:
 * - Base: QKD = 90, AES = 30
 * - Key Bonus: min(keySize / 16, 10)
 * - QBER Penalty: qber * 100 * 0.8
 * - Eve Penalty: 50 dacă eveDetected
 * - Scenario Multiplier: Banking = 1.2, Medical = 1.5
 */
object SecurityScorer {
    
    /**
     * Calculează scorul de securitate pentru o tranzacție
     * 
     * @param encryptionType "QKD" pentru quantum, "AES" pentru normal
     * @param keySize Dimensiunea cheii în biți (256 standard)
     * @param qber Quantum Bit Error Rate (0.0 - 1.0), null dacă nu e disponibil
     * @param eveDetected True dacă s-a detectat eavesdropping
     * @param scenario Tipul tranzacției (Banking sau Medical)
     * @return Scor de securitate între 0 și 100
     */
    fun computeSecurityScore(
        encryptionType: String,
        keySize: Int,
        qber: Double?,
        eveDetected: Boolean,
        scenario: TransactionScenario
    ): Int {
        // Base score: QKD = 90, altfel = 30
        val base = if (encryptionType == "QKD") 90.0 else 30.0
        
        // Key bonus: până la 10 puncte bazat pe keySize
        val keyBonus = min(keySize / 16.0, 10.0)
        
        // QBER penalty: penalizare bazată pe erori (dacă există)
        val qberPenalty = if (qber != null) qber * 100.0 * 0.8 else 0.0
        
        // Eve penalty: penalizare majoră dacă s-a detectat Eve
        val evePenalty = if (eveDetected) 50.0 else 0.0
        
        // Scenario multiplier: medical e mai sensibil
        val scenarioMultiplier = when (scenario) {
            TransactionScenario.BANKING_PAYMENT -> 1.2
            TransactionScenario.MEDICAL_RECORD_ACCESS -> 1.5
        }
        
        // Calculează scorul raw
        val raw = (base + keyBonus - qberPenalty - evePenalty) * scenarioMultiplier
        
        // Returnează scorul limitat între 0 și 100
        return raw.roundToInt().coerceIn(0, 100)
    }
    
    /**
     * Calculează scorul pentru modul Normal (AES)
     */
    fun computeNormalScore(
        keySize: Int = 256,
        qber: Double? = null,
        compromised: Boolean = false,
        scenario: TransactionScenario
    ): Int {
        val score = computeSecurityScore(
            encryptionType = "AES",
            keySize = keySize,
            qber = qber,
            eveDetected = false, // Normal nu poate detecta Eve
            scenario = scenario
        )
        
        // Dacă e compromis, scorul maxim e 15
        return if (compromised) min(score, 15) else score
    }
    
    /**
     * Calculează scorul pentru modul Quantum (QKD)
     */
    fun computeQuantumScore(
        keySize: Int = 256,
        qber: Double? = null,
        eveDetected: Boolean = false,
        scenario: TransactionScenario
    ): Int {
        return computeSecurityScore(
            encryptionType = "QKD",
            keySize = keySize,
            qber = qber,
            eveDetected = eveDetected,
            scenario = scenario
        )
    }
}
