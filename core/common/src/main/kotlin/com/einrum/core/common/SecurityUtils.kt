package com.einrum.core.common

/**
 * Head of Cybersecurity mandated security utilities.
 */
object SecurityUtils {
    
    private const val PROMPT_DELIMITER = "\"\"\""

    /**
     * Sanitizes and wraps user input in triple-quotes to prevent prompt injection.
     * Mandated by GEMINI.md Section 3.
     */
    fun secureAiInput(input: String): String {
        val sanitized = input
            .replace(PROMPT_DELIMITER, "") // Remove potential delimiter escapes
            .replace("\\", "\\\\") // Escape backslashes
        
        return "USER_INPUT_START\n$PROMPT_DELIMITER\n$sanitized\n$PROMPT_DELIMITER\nUSER_INPUT_END"
    }

    /**
     * Validates AI output to prevent UI spoofing or injection.
     */
    fun validateAiOutput(output: String, maxLength: Int = 500): String {
        return output.take(maxLength).filter { it.isLetterOrDigit() || it.isWhitespace() || it in ".,!?" }
    }
}
