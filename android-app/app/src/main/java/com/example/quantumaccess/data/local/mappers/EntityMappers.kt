package com.example.quantumaccess.data.local.mappers

import com.example.quantumaccess.data.local.entities.LocalTransactionEntity
import com.example.quantumaccess.domain.model.TransactionChannel
import com.example.quantumaccess.domain.model.TransactionDirection
import com.example.quantumaccess.domain.model.TransactionHistoryEntry
import com.example.quantumaccess.domain.model.TransactionScenario
import com.example.quantumaccess.domain.model.TransactionSecurityState
import java.text.NumberFormat
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Mappers pentru conversia între Entități Room și Modele de Domeniu.
 */

fun LocalTransactionEntity.toDomainModel(): TransactionHistoryEntry {
    val isMedical = this.scenario == "MEDICAL_RECORD_ACCESS"
    val beneficiaryDisplay = if (isMedical && (this.patientId != null || this.accessReason != null)) {
        listOfNotNull(
            this.patientId?.let { "Patient: $it" },
            this.accessReason?.let { "Reason: $it" }
        ).joinToString(" | ")
    } else {
        this.beneficiary
    }
    return TransactionHistoryEntry(
        id = this.transactionId.hashCode(),
        title = buildTransactionTitle(this.mode, this.scenario),
        beneficiary = beneficiaryDisplay,
        dateTime = DateTimeFormatter.ofPattern("MMM dd, HH:mm")
            .withZone(ZoneId.systemDefault())
            .format(this.createdAt),
        amountFormatted = if (isMedical) "Access" else NumberFormat.getCurrencyInstance(Locale.US).format(this.amount),
        amountValue = this.amount,
        direction = if (this.amount < 0) TransactionDirection.DEBIT else TransactionDirection.CREDIT,
        channel = if (this.mode == "QUANTUM") TransactionChannel.QUANTUM else TransactionChannel.NORMAL,
        statusMessage = this.status,
        securityState = when {
            // Compromisă = ALERT (indiferent de mod)
            this.compromised == true -> TransactionSecurityState.ALERT
            // Pentru Quantum: intercepted (eveDetected) nu înseamnă compromisă - atacul e detectat și blocat
            // Pentru Normal: intercepted înseamnă compromisă (nu poate detecta)
            this.intercepted && this.mode == "NORMAL" -> TransactionSecurityState.ALERT
            // Quantum cu eveDetected dar fără compromisă = SECURE (atac detectat și blocat)
            this.mode == "QUANTUM" -> TransactionSecurityState.SECURE
            // Normal fără probleme = NORMAL
            else -> TransactionSecurityState.NORMAL
        },
        // New security fields
        scenario = this.scenario?.let { parseScenario(it) },
        securityScoreNormal = this.securityScoreNormal,
        securityScoreQuantum = this.securityScoreQuantum,
        qber = this.qber,
        eveDetected = this.eveDetected,
        compromised = this.compromised
    )
}

/**
 * Builds transaction title based on mode and scenario
 */
private fun buildTransactionTitle(mode: String, scenario: String?): String {
    val scenarioLabel = when (scenario) {
        "BANKING_PAYMENT" -> "Banking Payment"
        "MEDICAL_RECORD_ACCESS" -> "Medical Record Access"
        else -> "Transaction"
    }
    val modeLabel = if (mode == "QUANTUM") "Quantum" else "Normal"
    return "$scenarioLabel ($modeLabel)"
}

/**
 * Parsează string-ul de scenariu în enum
 */
private fun parseScenario(scenario: String): TransactionScenario? {
    return when (scenario) {
        "BANKING_PAYMENT" -> TransactionScenario.BANKING_PAYMENT
        "MEDICAL_RECORD_ACCESS" -> TransactionScenario.MEDICAL_RECORD_ACCESS
        else -> null
    }
}

fun TransactionHistoryEntry.toEntity(): LocalTransactionEntity? {
    // Notă: Conversia inversă este dificilă fără ID original (UUID) și timestamp exact.
    // Această metodă este folosită doar dacă salvăm din UI în DB, ceea ce e rar (de obicei UI doar citește).
    return null 
}

