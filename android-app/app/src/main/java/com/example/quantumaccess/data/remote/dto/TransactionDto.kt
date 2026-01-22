package com.example.quantumaccess.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO pentru tabela transactions din Supabase.
 * Conține doar datele core ale tranzacției (fără procesare și securitate).
 */
@Serializable
data class TransactionDto(
    @SerialName("transaction_id") val transactionId: String? = null,
    @SerialName("user_id") val userId: String,
    val amount: Double? = null, // nullable pentru medical
    @SerialName("beneficiary_name") val beneficiaryName: String? = null,
    val scenario: String? = null, // "BANKING_PAYMENT" | "MEDICAL_RECORD_ACCESS"
    @SerialName("patient_id") val patientId: String? = null,
    @SerialName("access_reason") val accessReason: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

/**
 * DTO combinat pentru citirea datelor complete dintr-un JOIN.
 * Folosit la fetch, nu la insert.
 */
@Serializable
data class TransactionFullDto(
    @SerialName("transaction_id") val transactionId: String? = null,
    @SerialName("user_id") val userId: String,
    val amount: Double? = null,
    @SerialName("beneficiary_name") val beneficiaryName: String? = null,
    val scenario: String? = null,
    @SerialName("patient_id") val patientId: String? = null,
    @SerialName("access_reason") val accessReason: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("transaction_processing") val processing: TransactionProcessingDto? = null,
    @SerialName("security_analysis") val securityAnalysis: SecurityAnalysisDto? = null
)