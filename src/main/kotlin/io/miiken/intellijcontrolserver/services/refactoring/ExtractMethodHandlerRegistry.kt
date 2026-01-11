package io.miiken.intellijcontrolserver.services.refactoring

import com.intellij.lang.Language

/**
 * Registry of language-specific extract method refactoring handlers.
 * 
 * Routes refactoring requests to the appropriate handler based on the file's language.
 */
object ExtractMethodHandlerRegistry {
    
    private val handlers: List<LanguageExtractMethodRefactoring> by lazy {
        listOf(
            KotlinExtractMethodRefactoring(),
            JavaScriptExtractMethodRefactoring()
            // Note: Scala support requires Ultimate Edition with Scala plugin
        )
    }
    
    /**
     * Find the appropriate handler for the given language.
     * 
     * @param language The language of the file to refactor
     * @return The handler that supports this language, or null if no handler is available
     */
    fun findHandler(language: Language): LanguageExtractMethodRefactoring? {
        return handlers.firstOrNull { it.supports(language) }
    }
    
    /**
     * Get list of supported language names.
     * 
     * @return List of human-readable language names that are supported
     */
    fun getSupportedLanguages(): List<String> {
        return listOf("Kotlin", "JavaScript", "TypeScript")
    }
    
    /**
     * Check if a language is supported.
     * 
     * @param language The language to check
     * @return true if any handler supports this language
     */
    fun isLanguageSupported(language: Language): Boolean {
        return findHandler(language) != null
    }
}
