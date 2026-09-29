package com.example.data.remote.ai

sealed interface AIResult {
    data class Success(val jsonText: String) : AIResult
    data class ConfigurationRequired(val message: String) : AIResult
    data class Error(val message: String, val cause: Throwable? = null) : AIResult
}
