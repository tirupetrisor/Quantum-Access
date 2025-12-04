package com.example.quantumaccess.data.remote

import com.example.quantumaccess.data.remote.dto.TransactionDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order

class RemoteTransactionDataSource(private val supabase: SupabaseClient) {

    suspend fun createTransaction(transaction: TransactionDto): TransactionDto {
        return supabase.from("transactions")
            .insert(transaction) {
                select() 
            }.decodeSingle()
    }

    suspend fun getTransactions(userId: String): List<TransactionDto> {
        return supabase.from("transactions")
            .select {
                filter {
                    eq("user_id", userId)
                }
                order("created_at", Order.DESCENDING)
            }.decodeList()
    }
}

