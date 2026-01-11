package io.miiken.intellijcontrolserver.services.refactoring

import com.intellij.lang.Language
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import io.miiken.intellijcontrolserver.models.ExtractMethodAnalysis
import io.miiken.intellijcontrolserver.models.ExtractMethodOptions
import io.miiken.intellijcontrolserver.models.RefactoringResult

/**
 * Interface for language-specific extract method refactoring implementations.
 * 
 * Each language (Kotlin, JavaScript, Scala) has its own implementation that uses
 * IntelliJ's native refactoring engine for that language.
 */
interface LanguageExtractMethodRefactoring {
    
    /**
     * Check if this handler supports the given language.
     * 
     * @param language The language to check
     * @return true if this handler can process the language
     */
    fun supports(language: Language): Boolean
    
    /**
     * Analyze a code selection and return suggestions for extraction.
     * 
     * This phase uses IntelliJ's refactoring engine to:
     * - Detect parameters that need to be passed to the extracted method
     * - Infer the return type
     * - Suggest a method name
     * - Determine appropriate visibility
     * 
     * @param project The IntelliJ project
     * @param psiFile The file containing the code to extract
     * @param startOffset Start offset in the document
     * @param endOffset End offset in the document
     * @return Analysis results with suggestions
     */
    fun analyze(
        project: Project,
        psiFile: PsiFile,
        startOffset: Int,
        endOffset: Int
    ): ExtractMethodAnalysis
    
    /**
     * Execute the extraction with user-provided choices.
     * 
     * Uses IntelliJ's native refactoring engine to perform the actual extraction,
     * using the parameters, method name, and visibility provided by the user/AI.
     * 
     * @param project The IntelliJ project
     * @param psiFile The file containing the code to extract
     * @param startOffset Start offset in the document
     * @param endOffset End offset in the document
     * @param options User choices for the extraction (name, visibility, parameter order, etc.)
     * @return Result indicating success/failure and files changed
     */
    fun execute(
        project: Project,
        psiFile: PsiFile,
        startOffset: Int,
        endOffset: Int,
        options: ExtractMethodOptions
    ): RefactoringResult
}
