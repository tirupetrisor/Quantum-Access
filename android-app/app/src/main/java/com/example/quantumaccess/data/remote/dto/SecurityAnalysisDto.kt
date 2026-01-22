package com.example.quantumaccess.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO pentru tabela security_analysis din Supabase.
 * Conține rezultatele analizei de securitate pentru o tranzacție.
 */
@Serializable
data class SecurityAnalysisDto(
    val id: String? = null,
    @SerialName("transaction_id") val transactionId: String,
    @SerialName("security_score_normal") val securityScoreNormal: Int? = null,
    @SerialName("security_score_quantum") val securityScoreQuantum: Int? = null,
    val qber: Double? = null, // Quantum Bit Error Rate
    @SerialName("eve_detected") val eveDetected: Boolean? = false,
    val compromised: Boolean? = false,
    @SerialName("analyzed_at") val analyzedAt: String? = null
)
