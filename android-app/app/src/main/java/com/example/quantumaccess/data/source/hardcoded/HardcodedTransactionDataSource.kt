package com.example.quantumaccess.data.source.hardcoded

import com.example.quantumaccess.domain.model.AnalyticsCategory
import com.example.quantumaccess.domain.model.QuantumProcessStep
import com.example.quantumaccess.domain.model.TransactionAnalyticsSlice
import com.example.quantumaccess.domain.model.TransactionChannel
import com.example.quantumaccess.domain.model.TransactionDirection
import com.example.quantumaccess.domain.model.TransactionHistoryEntry
import com.example.quantumaccess.domain.model.TransactionSecurityState

object HardcodedTransactionDataSource {

    val transactionHistory: List<TransactionHistoryEntry> = listOf(
        TransactionHistoryEntry(
            id = 1,
            title = "Payment to John Smith",
            dateTime = "Dec 15, 2024 14:32",
            amountFormatted = "-$1,250.00",
            direction = TransactionDirection.DEBIT,
            channel = TransactionChannel.QUANTUM,
            statusMessage = "Quantum-encrypted - Secure",
            securityState = TransactionSecurityState.SECURE
        ),
        TransactionHistoryEntry(
            id = 2,
            title = "Salary Deposit",
            dateTime = "Dec 14, 2024 09:15",
            amountFormatted = "+$5,500.00",
            direction = TransactionDirection.CREDIT,
            channel = TransactionChannel.NORMAL,
            statusMessage = "Standard encryption - Normal",
            securityState = TransactionSecurityState.NORMAL
        ),
        TransactionHistoryEntry(
            id = 3,
            title = "ATM Withdrawal",
            dateTime = "Dec 13, 2024 16:45",
            amountFormatted = "-$200.00",
            direction = TransactionDirection.DEBIT,
            channel = TransactionChannel.QUANTUM,
            statusMessage = "Quantum breach detected - Intercepted",
            securityState = TransactionSecurityState.ALERT
        ),
        TransactionHistoryEntry(
            id = 4,
            title = "Mortgage Payment",
            dateTime = "Dec 10, 2024 12:00",
            amountFormatted = "-$2,100.00",
            direction = TransactionDirection.DEBIT,
            channel = TransactionChannel.QUANTUM,
            statusMessage = "Quantum-encrypted - Secure",
            securityState = TransactionSecurityState.SECURE
        )
    )

    val analyticsSlices: List<TransactionAnalyticsSlice> = listOf(
        TransactionAnalyticsSlice(label = "Quantum", value = 65f, category = AnalyticsCategory.QUANTUM),
        TransactionAnalyticsSlice(label = "Normal", value = 30f, category = AnalyticsCategory.NORMAL),
        TransactionAnalyticsSlice(label = "Intercepted", value = 5f, category = AnalyticsCategory.INTERCEPTED)
    )

    val quantumSteps: List<QuantumProcessStep> = listOf(
        QuantumProcessStep(
            progress = 0.25f,
            status = "Establishing quantum-secured channel...",
            detail = "Initializing quantum key distribution"
        ),
        QuantumProcessStep(
            progress = 0.5f,
            status = "Quantum entanglement verified...",
            detail = "Generating cryptographic keys"
        ),
        QuantumProcessStep(
            progress = 0.75f,
            status = "Transmitting via QKD protocol...",
            detail = "Quantum entanglement verified"
        ),
        QuantumProcessStep(
            progress = 1f,
            status = "Quantum-secured transaction complete",
            detail = "Transaction verified and confirmed",
            isTerminal = true
        )
    )
}

