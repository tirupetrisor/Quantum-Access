package com.example.quantumaccess.data.local.mappers

import com.example.quantumaccess.data.local.entities.LocalTransactionEntity
import com.example.quantumaccess.domain.model.TransactionChannel
import com.example.quantumaccess.domain.model.TransactionDirection
import com.example.quantumaccess.domain.model.TransactionHistoryEntry
import com.example.quantumaccess.domain.model.TransactionSecurityState
import java.text.NumberFormat
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Mappers pentru conversia între Entități Room și Modele de Domeniu.
 */

fun LocalTransactionEntity.toDomainModel(): TransactionHistoryEntry {
    return TransactionHistoryEntry(
        id = this.transactionId.hashCode(), // Conversie UUID -> Int (limitare domain model existent)
        title = "Transaction ${this.mode}",
        beneficiary = this.beneficiary,
        dateTime = DateTimeFormatter.ofPattern("MMM dd, HH:mm")
            .withZone(ZoneId.systemDefault())
            .format(this.createdAt),
        amountFormatted = NumberFormat.getCurrencyInstance(Locale.US).format(this.amount),
        amountValue = this.amount,
        direction = if (this.amount < 0) TransactionDirection.DEBIT else TransactionDirection.CREDIT, // Presupunere: negativ = debit
        channel = if (this.mode == "QUANTUM") TransactionChannel.QUANTUM else TransactionChannel.NORMAL,
        statusMessage = this.status,
        securityState = when {
            this.intercepted -> TransactionSecurityState.ALERT
            this.mode == "QUANTUM" -> TransactionSecurityState.SECURE
            else -> TransactionSecurityState.NORMAL
        }
    )
}

fun TransactionHistoryEntry.toEntity(): LocalTransactionEntity? {
    // Notă: Conversia inversă este dificilă fără ID original (UUID) și timestamp exact.
    // Această metodă este folosită doar dacă salvăm din UI în DB, ceea ce e rar (de obicei UI doar citește).
    return null 
}

