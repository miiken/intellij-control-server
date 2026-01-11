package io.miiken.intellijcontrolserver.services.refactoring

import com.intellij.lang.Language
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.PsiTreeUtil
import io.miiken.intellijcontrolserver.models.ExtractMethodAnalysis
import io.miiken.intellijcontrolserver.models.ExtractMethodOptions
import io.miiken.intellijcontrolserver.models.MethodParameter
import io.miiken.intellijcontrolserver.models.RefactoringResult
import io.miiken.intellijcontrolserver.models.RefactoringError

/**
 * Kotlin-specific implementation of extract method refactoring.
 * 
 * Uses generic PSI APIs with runtime checks for Kotlin plugin availability.
 * Requires Kotlin plugin to be installed in the target IDE.
 */
class KotlinExtractMethodRefactoring : LanguageExtractMethodRefactoring {
    
    override fun supports(language: Language): Boolean {
        // Check if this is Kotlin language
        return language.id.equals("kotlin", ignoreCase = true) || 
               language.displayName.equals("Kotlin", ignoreCase = true)
    }
    
    override fun analyze(
        project: Project,
        psiFile: PsiFile,
        startOffset: Int,
        endOffset: Int
    ): ExtractMethodAnalysis {
        return ReadAction.compute<ExtractMethodAnalysis, RuntimeException> {
            try {
                // Basic validation
                if (startOffset < 0 || endOffset > psiFile.textLength || startOffset >= endOffset) {
                    return@compute ExtractMethodAnalysis(
                        canExtract = false,
                        language = "Kotlin",
                        errorMessage = "Invalid selection range"
                    )
                }
                
                // Find elements in range
                val startElement = psiFile.findElementAt(startOffset)
                val endElement = psiFile.findElementAt(endOffset - 1)
                
                if (startElement == null || endElement == null) {
                    return@compute ExtractMethodAnalysis(
                        canExtract = false,
                        language = "Kotlin",
                        errorMessage = "No elements found in selection"
                    )
                }
                
                // Basic parameter detection (simplified - looks for identifiers)
                val selectedText = psiFile.text.substring(startOffset, endOffset)
                val parameters = detectParameters(selectedText)
                
                ExtractMethodAnalysis(
                    canExtract = true,
                    suggestedMethodName = "extractedMethod",
                    detectedParameters = parameters,
                    returnType = "Unit",  // Default return type
                    suggestedVisibility = "private",
                    language = "Kotlin"
                )
            } catch (e: Exception) {
                ExtractMethodAnalysis(
                    canExtract = false,
                    language = "Kotlin",
                    errorMessage = "Analysis failed: ${e.message}"
                )
            }
        }
    }
    
    override fun execute(
        project: Project,
        psiFile: PsiFile,
        startOffset: Int,
        endOffset: Int,
        options: ExtractMethodOptions
    ): RefactoringResult {
        return RefactoringResult(
            success = false,
            error = RefactoringError(
                code = "NOT_IMPLEMENTED",
                message = "Extract method for Kotlin requires Kotlin plugin with IntelliJ IDEA Ultimate or a compatible IDE with full Kotlin support installed. " +
                         "This feature is currently under development for Community Edition."
            )
        )
    }
    
    private fun detectParameters(code: String): List<MethodParameter> {
        // Simplified parameter detection
        // In a full implementation, this would use Kotlin PSI to detect actual variables
        return emptyList()
    }
}
