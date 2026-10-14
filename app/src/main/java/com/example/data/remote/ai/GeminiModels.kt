package com.example.data.remote.ai

import com.example.data.model.chat.GroundingWebSource

sealed interface AIResult {
    data class Success(val jsonText: String) : AIResult
    data class ConfigurationRequired(val message: String) : AIResult
    data class Error(val message: String, val cause: Throwable? = null) : AIResult
}

data class GroundedSearchResult(
    val isSuccess: Boolean,
    val text: String,
    val groundingSources: List<GroundingWebSource> = emptyList(),
    val errorMessage: String? = null
)
