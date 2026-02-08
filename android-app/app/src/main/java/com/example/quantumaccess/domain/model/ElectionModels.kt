package com.example.quantumaccess.domain.model

/**
 * Domain models for quantum-secured voting.
 * Elections: presidential, parliamentary, local.
 * Votes are encrypted with QKD keys; Eve detection aborts submission.
 */

enum class ElectionType {
    PRESIDENTIAL,
    PARLIAMENTARY,
    LOCAL
}

data class VoteOption(
    val id: String,
    val label: String,
    val shortLabel: String? = null
)

data class Election(
    val id: String,
    val type: ElectionType,
    val name: String,
    val nameRo: String? = null,
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val options: List<VoteOption>,
    val isActive: Boolean = true
) {
    fun displayName(localeRo: Boolean = true): String = if (localeRo && !nameRo.isNullOrBlank()) nameRo else name
}

data class Vote(
    val id: String,
    val electionId: String,
    val electionName: String,
    val optionId: String,
    val optionLabel: String,
    val quantumKeyId: String,
    val receiptToken: String,
    val createdAtMillis: Long,
    val isRealQkd: Boolean,
    val eveDetected: Boolean = false
)

data class VoteReceipt(
    val voteId: String,
    val electionName: String,
    val receiptToken: String,
    val createdAtMillis: Long,
    val quantumSecured: Boolean
)
