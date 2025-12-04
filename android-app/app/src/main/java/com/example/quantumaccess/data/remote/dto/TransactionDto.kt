package com.example.quantumaccess.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TransactionDto(
    @SerialName("transaction_id") val transactionId: String? = null,
    @SerialName("user_id") val userId: String,
    val amount: Double,
    val type: String, // "NORMAL" or "QUANTUM"
    val status: String, // "SUCCESS", "FAILED", "INTERCEPTED"
    @SerialName("interception_detected") val interceptionDetected: Boolean,
    @SerialName("beneficiary_name") val beneficiaryName: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

