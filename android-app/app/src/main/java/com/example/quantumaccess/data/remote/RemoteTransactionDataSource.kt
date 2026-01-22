package com.example.quantumaccess.data.remote

import android.util.Log
import com.example.quantumaccess.data.remote.dto.SecurityAnalysisDto
import com.example.quantumaccess.data.remote.dto.TransactionDto
import com.example.quantumaccess.data.remote.dto.TransactionFullDto
import com.example.quantumaccess.data.remote.dto.TransactionProcessingDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class RemoteTransactionDataSource(private val supabase: SupabaseClient) {

    companion object {
        private const val TAG = "RemoteTransactionDS"
    }

    /**
     * Creează o tranzacție completă în cele 3 tabele.
     * Returnează tranzacția creată.
     * Aruncă excepție dacă orice parte a inserării eșuează.
     */
    suspend fun createFullTransaction(
        transaction: TransactionDto,
        processing: TransactionProcessingDto,
        securityAnalysis: SecurityAnalysisDto
    ): TransactionDto {
        Log.d(TAG, "Creating full transaction: ${transaction.transactionId}")
        
        // 1. Insert în transactions
        val createdTransaction = try {
            supabase.from("transactions")
                .insert(transaction) {
                    select()
                }.decodeSingle<TransactionDto>()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to insert transaction: ${transaction.transactionId}", e)
            Log.e(TAG, "Transaction DTO: $transaction")
            throw Exception("Failed to insert transaction into Supabase: ${e.message}", e)
        }

        val txId = createdTransaction.transactionId ?: throw IllegalStateException("Transaction ID is null after insert")
        Log.d(TAG, "Transaction inserted with ID: $txId")

        // 2. Insert în transaction_processing
        // Folosim buildJsonObject pentru a exclude câmpul 'id' care este generat automat de Supabase
        val processingJson = buildJsonObject {
            put("transaction_id", txId)
            put("processing_mode", processing.processingMode)
            put("status", processing.status)
            // Nu includem 'id' - Supabase îl generează automat
            // Nu includem 'processed_at' - Supabase îl setează automat cu default now()
        }
        try {
            Log.d(TAG, "Inserting processing data: transactionId=$txId, mode=${processing.processingMode}, status=${processing.status}")
            supabase.from("transaction_processing")
                .insert(processingJson)
            Log.d(TAG, "Processing data inserted successfully for transaction: $txId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to insert processing for transaction: $txId", e)
            Log.e(TAG, "Processing JSON: $processingJson")
            Log.e(TAG, "Error details: ${e.javaClass.simpleName} - ${e.message}")
            e.printStackTrace()
            throw Exception("Failed to insert processing data: ${e.message}", e)
        }

        // 3. Insert în security_analysis
        // Folosim buildJsonObject pentru a exclude câmpul 'id' care este generat automat de Supabase
        val securityJson = buildJsonObject {
            put("transaction_id", txId)
            securityAnalysis.securityScoreNormal?.let { put("security_score_normal", it) }
            securityAnalysis.securityScoreQuantum?.let { put("security_score_quantum", it) }
            securityAnalysis.qber?.let { put("qber", it) }
            securityAnalysis.eveDetected?.let { put("eve_detected", it) }
            securityAnalysis.compromised?.let { put("compromised", it) }
            // Nu includem 'id' - Supabase îl generează automat
            // Nu includem 'analyzed_at' - Supabase îl setează automat cu default now()
        }
        try {
            Log.d(TAG, "Inserting security analysis: transactionId=$txId, normalScore=${securityAnalysis.securityScoreNormal}, quantumScore=${securityAnalysis.securityScoreQuantum}, eveDetected=${securityAnalysis.eveDetected}, compromised=${securityAnalysis.compromised}")
            supabase.from("security_analysis")
                .insert(securityJson)
            Log.d(TAG, "Security analysis inserted successfully for transaction: $txId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to insert security analysis for transaction: $txId", e)
            Log.e(TAG, "Security JSON: $securityJson")
            Log.e(TAG, "Error details: ${e.javaClass.simpleName} - ${e.message}")
            e.printStackTrace()
            throw Exception("Failed to insert security analysis: ${e.message}", e)
        }

        Log.d(TAG, "Full transaction created successfully: $txId")
        return createdTransaction
    }

    /**
     * Legacy method - creează doar în tabela transactions.
     * Folosit pentru backward compatibility.
     */
    suspend fun createTransaction(transaction: TransactionDto): TransactionDto {
        return supabase.from("transactions")
            .insert(transaction) {
                select()
            }.decodeSingle()
    }

    /**
     * Obține tranzacțiile cu datele de procesare și securitate (JOIN).
     */
    suspend fun getTransactionsWithDetails(userId: String): List<TransactionFullDto> {
        return try {
            supabase.from("transactions")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                    order("created_at", Order.DESCENDING)
                    // Supabase auto-joins when you select related tables
                }.decodeList<TransactionFullDto>()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get transactions with details, falling back to simple query", e)
            // Fallback to simple query
            emptyList()
        }
    }

    /**
     * Obține tranzacțiile simple (fără JOIN).
     */
    suspend fun getTransactions(userId: String): List<TransactionDto> {
        return supabase.from("transactions")
            .select {
                filter {
                    eq("user_id", userId)
                }
                order("created_at", Order.DESCENDING)
            }.decodeList()
    }

    /**
     * Obține procesarea pentru o tranzacție.
     */
    suspend fun getProcessing(transactionId: String): TransactionProcessingDto? {
        return try {
            supabase.from("transaction_processing")
                .select {
                    filter {
                        eq("transaction_id", transactionId)
                    }
                }.decodeSingleOrNull()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get processing for $transactionId", e)
            null
        }
    }

    /**
     * Obține analiza de securitate pentru o tranzacție.
     */
    suspend fun getSecurityAnalysis(transactionId: String): SecurityAnalysisDto? {
        return try {
            supabase.from("security_analysis")
                .select {
                    filter {
                        eq("transaction_id", transactionId)
                    }
                }.decodeSingleOrNull()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get security analysis for $transactionId", e)
            null
        }
    }
}

