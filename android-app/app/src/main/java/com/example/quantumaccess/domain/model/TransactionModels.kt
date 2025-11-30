package com.example.quantumaccess.domain.model

data class TransactionHistoryEntry(
    val id: Int,
    val title: String,
    val beneficiary: String,
    val dateTime: String,
    val amountFormatted: String,
    val amountValue: Double,
    val direction: TransactionDirection,
    val channel: TransactionChannel,
    val statusMessage: String,
    val securityState: TransactionSecurityState
)

enum class TransactionDirection { CREDIT, DEBIT }

enum class TransactionChannel { QUANTUM, NORMAL }

enum class TransactionSecurityState { SECURE, NORMAL, ALERT }

data class TransactionAnalyticsSlice(
    val label: String,
    val value: Float,
    val category: AnalyticsCategory
)

enum class AnalyticsCategory { QUANTUM, NORMAL, INTERCEPTED }

data class QuantumProcessStep(
    val progress: Float,
    val status: String,
    val detail: String,
    val isTerminal: Boolean = false
)

