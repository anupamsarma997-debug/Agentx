package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.reel.ReelDraft
import com.example.data.model.reel.ReelGenerationStatus
import com.example.data.model.reel.ReelLanguage
import com.example.data.model.reel.ReelSafetyStatus
import com.example.data.model.reel.ReelScene
import com.example.data.model.reel.ReelType
import org.json.JSONArray
import org.json.JSONObject

@Entity(
    tableName = "reel_drafts",
    indices = [
        Index(value = ["contentHash"], unique = false),
        Index(value = ["generationStatus"]),
        Index(value = ["safetyStatus"]),
        Index(value = ["createdAt"])
    ]
)
data class ReelDraftEntity(
    @PrimaryKey
    val id: String,
    val sourceOpportunityId: String?,
    val sourceContentId: String?,
    val reelType: String,
    val title: String,
    val hook: String,
    val durationSeconds: Int,
    val scenesJson: String,
    val voiceover: String,
    val caption: String,
    val hashtags: String,
    val sourceName: String,
    val sourceUrl: String,
    val verificationStatus: String,
    val safetyStatus: String,
    val generationStatus: String,
    val language: String,
    val contentHash: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val errorMessage: String? = null
) {
    val reelTypeEnum: ReelType
        get() = ReelType.fromString(reelType)

    val safetyStatusEnum: ReelSafetyStatus
        get() = ReelSafetyStatus.fromString(safetyStatus)

    val generationStatusEnum: ReelGenerationStatus
        get() = ReelGenerationStatus.fromString(generationStatus)

    val languageEnum: ReelLanguage
        get() = ReelLanguage.fromString(language)

    fun getHashtagList(): List<String> {
        return hashtags.split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
    }

    fun parseScenes(): List<ReelScene> {
        val list = mutableListOf<ReelScene>()
        if (scenesJson.isBlank()) return list
        try {
            val array = JSONArray(scenesJson)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    ReelScene(
                        sceneNumber = obj.optInt("sceneNumber", i + 1),
                        durationSeconds = obj.optInt("durationSeconds", 5),
                        visualDescription = obj.optString("visualDescription", ""),
                        onScreenText = obj.optString("onScreenText", ""),
                        voiceoverText = obj.optString("voiceoverText", ""),
                        transition = obj.optString("transition", "Cut"),
                        backgroundSuggestion = if (obj.has("backgroundSuggestion") && !obj.isNull("backgroundSuggestion")) {
                            obj.getString("backgroundSuggestion")
                        } else null
                    )
                )
            }
        } catch (_: Exception) {
            // fallback gracefully
        }
        return list
    }

    fun toDraft(): ReelDraft {
        return ReelDraft(
            id = id,
            sourceOpportunityId = sourceOpportunityId,
            sourceContentId = sourceContentId,
            reelType = reelTypeEnum,
            title = title,
            hook = hook,
            durationSeconds = durationSeconds,
            scenes = parseScenes(),
            voiceover = voiceover,
            caption = caption,
            hashtags = getHashtagList(),
            sourceName = sourceName,
            sourceUrl = sourceUrl,
            verificationStatus = verificationStatus,
            safetyStatus = safetyStatusEnum,
            generationStatus = generationStatusEnum,
            language = languageEnum,
            contentHash = contentHash,
            createdAt = createdAt,
            updatedAt = updatedAt,
            errorMessage = errorMessage
        )
    }

    companion object {
        fun serializeScenes(scenes: List<ReelScene>): String {
            val array = JSONArray()
            for (scene in scenes) {
                val obj = JSONObject()
                obj.put("sceneNumber", scene.sceneNumber)
                obj.put("durationSeconds", scene.durationSeconds)
                obj.put("visualDescription", scene.visualDescription)
                obj.put("onScreenText", scene.onScreenText)
                obj.put("voiceoverText", scene.voiceoverText)
                obj.put("transition", scene.transition)
                if (scene.backgroundSuggestion != null) {
                    obj.put("backgroundSuggestion", scene.backgroundSuggestion)
                }
                array.put(obj)
            }
            return array.toString()
        }

        fun fromDraft(draft: ReelDraft): ReelDraftEntity {
            return ReelDraftEntity(
                id = draft.id,
                sourceOpportunityId = draft.sourceOpportunityId,
                sourceContentId = draft.sourceContentId,
                reelType = draft.reelType.name,
                title = draft.title,
                hook = draft.hook,
                durationSeconds = draft.durationSeconds,
                scenesJson = serializeScenes(draft.scenes),
                voiceover = draft.voiceover,
                caption = draft.caption,
                hashtags = draft.hashtags.joinToString(","),
                sourceName = draft.sourceName,
                sourceUrl = draft.sourceUrl,
                verificationStatus = draft.verificationStatus,
                safetyStatus = draft.safetyStatus.name,
                generationStatus = draft.generationStatus.name,
                language = draft.language.name,
                contentHash = draft.contentHash,
                createdAt = draft.createdAt,
                updatedAt = draft.updatedAt,
                errorMessage = draft.errorMessage
            )
        }
    }
}
