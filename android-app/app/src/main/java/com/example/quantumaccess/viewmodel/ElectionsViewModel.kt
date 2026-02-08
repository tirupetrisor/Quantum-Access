package com.example.quantumaccess.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quantumaccess.domain.model.Election
import com.example.quantumaccess.domain.model.Vote
import com.example.quantumaccess.domain.model.VoteReceipt
import com.example.quantumaccess.feature.elections.presentation.CastVoteState
import com.example.quantumaccess.data.sample.RepositoryProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ElectionsUiState(
    val elections: List<Election> = emptyList(),
    val votes: List<Vote> = emptyList(),
    val isLoadingElections: Boolean = true,
    val isLoadingVotes: Boolean = true,
    val castVoteState: CastVoteState = CastVoteState.Idle,
    val selectedElection: Election? = null,
    val selectedOption: com.example.quantumaccess.domain.model.VoteOption? = null
)

class ElectionsViewModel : ViewModel() {

    private val repo get() = RepositoryProvider.voteRepository

    private val _uiState = MutableStateFlow(ElectionsUiState())
    val uiState: StateFlow<ElectionsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repo.getActiveElections()
                .catch { _uiState.update { it.copy(isLoadingElections = false) } }
                .collect { list ->
                    _uiState.update {
                        it.copy(elections = list, isLoadingElections = false)
                    }
                }
        }
        viewModelScope.launch {
            repo.getVoteHistory()
                .catch { _uiState.update { it.copy(isLoadingVotes = false) } }
                .collect { list ->
                    _uiState.update {
                        it.copy(votes = list, isLoadingVotes = false)
                    }
                }
        }
    }

    fun selectElection(election: Election?) {
        _uiState.update {
            it.copy(selectedElection = election, selectedOption = null)
        }
    }

    fun selectOption(option: com.example.quantumaccess.domain.model.VoteOption?) {
        _uiState.update { it.copy(selectedOption = option) }
    }

    fun castVote(simulateEve: Boolean = false) {
        val election = _uiState.value.selectedElection ?: return
        val option = _uiState.value.selectedOption ?: return
        viewModelScope.launch {
            // Phase 1: Generate quantum key — show photon animation for ~3 seconds
            _uiState.update { it.copy(castVoteState = CastVoteState.GeneratingKey) }
            delay(3000)
            // Phase 2: Check channel (Eve detection) — another ~2 seconds
            _uiState.update { it.copy(castVoteState = CastVoteState.CheckingChannel) }
            delay(2000)
            // Phase 3: Actually cast vote via repository (QKD + Eve detection)
            val result = repo.castVote(
                electionId = election.id,
                optionId = option.id,
                optionLabel = option.label,
                electionName = election.displayName(localeRo = true),
                simulateEve = simulateEve
            )
            result.fold(
                onSuccess = { receipt ->
                    _uiState.update { it.copy(castVoteState = CastVoteState.Success(receipt)) }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(castVoteState = CastVoteState.Error(e.message ?: "Eroare necunoscută"))
                    }
                }
            )
        }
    }

    fun resetCastState() {
        _uiState.update { it.copy(castVoteState = CastVoteState.Idle) }
    }
}
