package com.example.quantumaccess.domain.repository

import com.example.quantumaccess.domain.model.Election
import com.example.quantumaccess.domain.model.Vote
import com.example.quantumaccess.domain.model.VoteReceipt
import kotlinx.coroutines.flow.Flow

interface VoteRepository {
    fun getActiveElections(): Flow<List<Election>>
    suspend fun getElection(electionId: String): Election?
    fun getVoteHistory(): Flow<List<Vote>>
    suspend fun castVote(
        electionId: String,
        optionId: String,
        optionLabel: String,
        electionName: String,
        simulateEve: Boolean = false
    ): Result<VoteReceipt>
    suspend fun seedElectionsIfNeeded()
}
