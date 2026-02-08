package com.example.quantumaccess.data.local.mappers

import com.example.quantumaccess.data.local.entities.ElectionEntity
import com.example.quantumaccess.data.local.entities.VoteEntity
import com.example.quantumaccess.domain.model.Election
import com.example.quantumaccess.domain.model.ElectionType
import com.example.quantumaccess.domain.model.Vote
import com.example.quantumaccess.domain.model.VoteOption
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

@Serializable
private data class VoteOptionDto(val id: String, val label: String, val shortLabel: String? = null)

private val json = Json { ignoreUnknownKeys = true }

fun ElectionEntity.toDomainModel(nowMillis: Long = System.currentTimeMillis()): Election {
    val options = try {
        json.decodeFromString<List<VoteOptionDto>>(optionsJson).map {
            VoteOption(id = it.id, label = it.label, shortLabel = it.shortLabel)
        }
    } catch (_: Exception) {
        emptyList()
    }
    val isActive = nowMillis in startTimeMillis..endTimeMillis
    return Election(
        id = id,
        type = when (type) {
            "PRESIDENTIAL" -> ElectionType.PRESIDENTIAL
            "PARLIAMENTARY" -> ElectionType.PARLIAMENTARY
            "LOCAL" -> ElectionType.LOCAL
            else -> ElectionType.PARLIAMENTARY
        },
        name = name,
        nameRo = nameRo,
        startTimeMillis = startTimeMillis,
        endTimeMillis = endTimeMillis,
        options = options,
        isActive = isActive
    )
}

fun VoteEntity.toDomainModel(): Vote = Vote(
    id = id,
    electionId = electionId,
    electionName = electionName,
    optionId = optionId,
    optionLabel = optionLabel,
    quantumKeyId = quantumKeyId,
    receiptToken = receiptToken,
    createdAtMillis = createdAtMillis,
    isRealQkd = isRealQkd,
    eveDetected = eveDetected
)

fun List<VoteOption>.toOptionsJson(): String {
    val dtoList = map { VoteOptionDto(it.id, it.label, it.shortLabel) }
    return Json.encodeToString(ListSerializer(VoteOptionDto.serializer()), dtoList)
}
