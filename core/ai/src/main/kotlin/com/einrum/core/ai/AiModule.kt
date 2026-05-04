package com.einrum.core.ai

import org.koin.dsl.module

interface AiService {
    suspend fun blurBackground(frame: ByteArray): ByteArray
    suspend fun summarizeMeeting(transcript: String): String
}

class GeminiAiService : AiService {
    override suspend fun blurBackground(frame: ByteArray): ByteArray {
        // Implementation for Gemini Nano / AICore blur
        return frame // Placeholder
    }

    override suspend fun summarizeMeeting(transcript: String): String {
        // Implementation for Gemini Nano summarization
        return "Summary of the meeting..." // Placeholder
    }
}

val aiModule = module {
    single<AiService> { GeminiAiService() }
}
