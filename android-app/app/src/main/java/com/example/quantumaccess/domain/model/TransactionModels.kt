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
    val securityState: TransactionSecurityState,
    // New security fields
    val scenario: TransactionScenario? = null,
    val securityScoreNormal: Int? = null,
    val securityScoreQuantum: Int? = null,
    val qber: Double? = null,
    val eveDetected: Boolean? = null,
    val compromised: Boolean? = null
)

enum class TransactionDirection { CREDIT, DEBIT }

enum class TransactionChannel { QUANTUM, NORMAL }

enum class TransactionSecurityState { SECURE, NORMAL, ALERT }

/**
 * Transaction scenario - defines the type of operation
 */
enum class TransactionScenario(val displayName: String, val description: String) {
    BANKING_PAYMENT(
        displayName = "Banking Payment",
        description = "Secure bank transfer"
    ),
    MEDICAL_RECORD_ACCESS(
        displayName = "Medical Record Access",
        description = "Access confidential medical data"
    )
}

/**
 * Rezultatul procesării unei tranzacții, include scoruri de securitate
 */
data class TransactionResult(
    val transactionId: String,
    val scenario: TransactionScenario,
    val mode: TransactionChannel,
    val normalScore: Int, // 0-100
    val quantumScore: Int, // 0-100
    val qber: Double, // Quantum Bit Error Rate (0.0 - 1.0)
    val eveDetected: Boolean, // Eavesdropper detected
    val compromised: Boolean, // Security compromised
    val success: Boolean,
    val message: String
)

/**
 * Request pentru procesarea unei tranzacții noi.
 * Banking: amount + beneficiary. Medical: patientId + accessReason, amount = null.
 */
data class TransactionRequest(
    val amount: Double?,
    val beneficiary: String?,
    val patientId: String?,
    val accessReason: String?,
    val scenario: TransactionScenario,
    val mode: TransactionChannel,
    val simulateAttack: Boolean = false
)

/**
 * Scor de securitate agreat pentru afișare în Dashboard
 */
data class SecurityScoreSummary(
    val normalScore: Int, // Media sau ultima valoare
    val quantumScore: Int,
    val transactionCount: Int,
    val lastUpdated: String
)

/**
 * Status pentru fiecare pas în timeline-ul de comparație
 */
enum class TimelineStepStatus {
    OK,       // Pas finalizat cu succes
    WARNING,  // Pas finalizat dar cu avertisment
    COMPROMISED, // Pas compromis/eșuat
    PENDING   // Pas în așteptare
}

/**
 * Un pas individual în timeline-ul de comparație
 */
data class ComparisonTimelineStep(
    val stepName: String,
    val normalStatus: TimelineStepStatus,
    val quantumStatus: TimelineStepStatus,
    val normalDetail: String,
    val quantumDetail: String
)

data class TransactionAnalyticsSlice(
    val label: String,
    val value: Float,
    val category: AnalyticsCategory
)

enum class AnalyticsCategory { 
    QUANTUM_SECURE,    // Quantum transactions (always secure)
    NORMAL_SECURE,     // Normal transactions without issues
    VULNERABLE         // Normal transactions that were compromised
}

data class QuantumProcessStep(
    val progress: Float,
    val status: String,
    val detail: String,
    val isTerminal: Boolean = false
)

