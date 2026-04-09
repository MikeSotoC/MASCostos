package com.uchi.mascostos.android

data class AndroidRuntimeConfig(
    val useRemote: Boolean,
    val baseUrl: String,
    val token: String?,
    val maxRetries: Int,
)

object AndroidConfigLoader {
    fun load(): AndroidRuntimeConfig {
        return AndroidRuntimeConfig(
            useRemote = BuildConfig.USE_REMOTE,
            baseUrl = BuildConfig.API_BASE_URL,
            token = BuildConfig.API_TOKEN.ifBlank { null },
            maxRetries = BuildConfig.API_MAX_RETRIES,
        )
    }
}
