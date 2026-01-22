package com.example.quantumaccess.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO pentru tabela transaction_processing din Supabase.
 * Conține detalii despre cum a fost procesată tranzacția.
 */
@Serializable
data class TransactionProcessingDto(
    val id: String? = null,
    @SerialName("transaction_id") val transactionId: String,
    @SerialName("processing_mode") val processingMode: String, // "NORMAL" or "QUANTUM"
    val status: String, // "SUCCESS", "FAILED", "INTERCEPTED"
    @SerialName("processed_at") val processedAt: String? = null
)
