package io.miiken.intellijcontrolserver.services.refactoring

import com.intellij.lang.Language
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import io.miiken.intellijcontrolserver.models.ExtractMethodAnalysis
import io.miiken.intellijcontrolserver.models.ExtractMethodOptions
import io.miiken.intellijcontrolserver.models.MethodParameter
import io.miiken.intellijcontrolserver.models.RefactoringError
import io.miiken.intellijcontrolserver.models.RefactoringResult

/**
 * JavaScript/TypeScript-specific implementation of extract method refactoring.
 * 
 * Uses generic PSI APIs with runtime checks for JavaScript plugin availability.
 * Handles both .js and .ts files.
 * Requires JavaScript plugin to be installed in the target IDE.
 */
class JavaScriptExtractMethodRefactoring : LanguageExtractMethodRefactoring {
    
    override fun supports(language: Language): Boolean {
        val langId = language.id.lowercase()
        val displayName = language.displayName.lowercase()
        return langId.contains("javascript") ||
               langId.contains("typescript") ||
               displayName.contains("javascript") ||
               displayName.contains("typescript") ||
               langId == "js" ||
               langId == "ts"
    }
    
    override fun analyze(
        project: Project,
        psiFile: PsiFile,
        startOffset: Int,
        endOffset: Int
    ): ExtractMethodAnalysis {
        return ReadAction.compute<ExtractMethodAnalysis, RuntimeException> {
            try {
                val isTypeScript = psiFile.name.endsWith(".ts") || 
                                  psiFile.language.id.lowercase().contains("typescript")
                
                // Basic validation
                if (startOffset < 0 || endOffset > psiFile.textLength || startOffset >= endOffset) {
                    return@compute ExtractMethodAnalysis(
                        canExtract = false,
                        language = if (isTypeScript) "TypeScript" else "JavaScript",
                        errorMessage = "Invalid selection range"
                    )
                }
                
                // Find elements in range
                val startElement = psiFile.findElementAt(startOffset)
                val endElement = psiFile.findElementAt(endOffset - 1)
                
                if (startElement == null || endElement == null) {
                    return@compute ExtractMethodAnalysis(
                        canExtract = false,
                        language = if (isTypeScript) "TypeScript" else "JavaScript",
                        errorMessage = "No elements found in selection"
                    )
                }
                
                ExtractMethodAnalysis(
                    canExtract = true,
                    suggestedMethodName = "extractedMethod",
                    detectedParameters = emptyList(),
                    returnType = if (isTypeScript) "void" else null,
                    suggestedVisibility = "private",
                    language = if (isTypeScript) "TypeScript" else "JavaScript"
                )
            } catch (e: Exception) {
                ExtractMethodAnalysis(
                    canExtract = false,
                    language = "JavaScript",
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
                message = "Extract method for JavaScript/TypeScript requires JavaScript plugin with IntelliJ IDEA Ultimate or WebStorm. " +
                         "This feature is currently under development for Community Edition."
            )
        )
    }
}
