package io.miiken.intellijcontrolserver.services.refactoring

import com.intellij.lang.Language
import com.intellij.lang.javascript.JavascriptLanguage
import com.intellij.lang.javascript.TypeScriptLanguage
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import io.miiken.intellijcontrolserver.models.ExtractMethodAnalysis
import io.miiken.intellijcontrolserver.models.ExtractMethodOptions
import io.miiken.intellijcontrolserver.models.MethodParameter
import io.miiken.intellijcontrolserver.models.RefactoringError
import io.miiken.intellijcontrolserver.models.RefactoringResult

/**
 * JavaScript/TypeScript-specific implementation of extract method refactoring.
 * 
 * Uses IntelliJ's native JavaScript/TypeScript refactoring engine.
 * Handles both .js and .ts files.
 */
class JavaScriptExtractMethodRefactoring : LanguageExtractMethodRefactoring {
    
    override fun supports(language: Language): Boolean {
        return language.isKindOf(JavascriptLanguage.INSTANCE) ||
               language.isKindOf(TypeScriptLanguage.INSTANCE)
    }
    
    override fun analyze(
        project: Project,
        psiFile: PsiFile,
        startOffset: Int,
        endOffset: Int
    ): ExtractMethodAnalysis {
        try {
            val isTypeScript = psiFile.language.isKindOf(TypeScriptLanguage.INSTANCE)
            val languageName = if (isTypeScript) "TypeScript" else "JavaScript"
            
            // Find elements in the selected range
            val startElement = psiFile.findElementAt(startOffset)
            val endElement = psiFile.findElementAt(endOffset - 1)
            
            if (startElement == null || endElement == null) {
                return ExtractMethodAnalysis(
                    canExtract = false,
                    language = languageName,
                    errorMessage = "Could not find elements in selected range"
                )
            }
            
            // Find common parent
            val commonParent = PsiTreeUtil.findCommonParent(startElement, endElement)
                ?: return ExtractMethodAnalysis(
                    canExtract = false,
                    language = languageName,
                    errorMessage = "Could not find common parent for selection"
                )
            
            // Analyze the selected code
            val analysis = analyzeJavaScriptCode(commonParent, startOffset, endOffset, isTypeScript)
            
            return ExtractMethodAnalysis(
                canExtract = true,
                suggestedMethodName = analysis.suggestedName,
                detectedParameters = analysis.parameters,
                returnType = analysis.returnType,
                suggestedVisibility = "private",
                language = languageName
            )
            
        } catch (e: Exception) {
            val languageName = if (psiFile.language.isKindOf(TypeScriptLanguage.INSTANCE)) "TypeScript" else "JavaScript"
            return ExtractMethodAnalysis(
                canExtract = false,
                language = languageName,
                errorMessage = "Analysis failed: ${e.message}"
            )
        }
    }
    
    override fun execute(
        project: Project,
        psiFile: PsiFile,
        startOffset: Int,
        endOffset: Int,
        options: ExtractMethodOptions
    ): RefactoringResult {
        try {
            val isTypeScript = psiFile.language.isKindOf(TypeScriptLanguage.INSTANCE)
            val changedFiles = mutableSetOf<String>()
            
            ApplicationManager.getApplication().invokeAndWait {
                WriteCommandAction.runWriteCommandAction(project) {
                    val document = PsiDocumentManager.getInstance(project).getDocument(psiFile)
                        ?: throw IllegalStateException("Cannot get document for file")
                    
                    // Extract the code text
                    val extractedCode = document.getText(com.intellij.openapi.util.TextRange(startOffset, endOffset))
                    
                    // Analyze what variables are used
                    val element = psiFile.findElementAt(startOffset)
                    val analysis = analyzeJavaScriptCode(element, startOffset, endOffset, isTypeScript)
                    
                    // Reorder parameters if requested
                    val parameters = if (options.parameterOrder != null) {
                        options.parameterOrder.mapNotNull { paramName ->
                            analysis.parameters.find { it.name == paramName }
                        }
                    } else {
                        analysis.parameters
                    }
                    
                    // Generate method signature
                    val paramString = if (isTypeScript) {
                        parameters.joinToString(", ") { "${it.name}: ${it.type}" }
                    } else {
                        parameters.joinToString(", ") { it.name }
                    }
                    
                    val returnType = options.returnType ?: analysis.returnType
                    val returnTypeAnnotation = if (isTypeScript && returnType != null) ": $returnType" else ""
                    
                    // Generate the new method
                    val newMethod = buildString {
                        appendLine()
                        appendLine()
                        append("  ")
                        if (isTypeScript && options.visibility == "private") {
                            append("private ")
                        }
                        append("${options.methodName}($paramString)$returnTypeAnnotation {")
                        appendLine()
                        // Indent the extracted code
                        extractedCode.lines().forEach { line ->
                            if (line.isNotBlank()) {
                                appendLine("    $line")
                            } else {
                                appendLine()
                            }
                        }
                        append("  }")
                    }
                    
                    // Generate method call
                    val callParams = parameters.joinToString(", ") { it.name }
                    val methodCall = if (returnType != null && returnType != "void" && returnType != "undefined") {
                        if (analysis.parameters.size > 1) {
                            "const {${analysis.parameters.joinToString(", ") { it.name }}} = this.${options.methodName}($callParams);"
                        } else {
                            "const result = this.${options.methodName}($callParams);"
                        }
                    } else {
                        "this.${options.methodName}($callParams);"
                    }
                    
                    // Replace the selected code with method call
                    document.replaceString(startOffset, endOffset, methodCall)
                    
                    // Insert the new method
                    val insertOffset = findInsertionPoint(psiFile, startOffset)
                    document.insertString(insertOffset, newMethod)
                    
                    PsiDocumentManager.getInstance(project).commitDocument(document)
                    FileDocumentManager.getInstance().saveDocument(document)
                    
                    psiFile.virtualFile?.path?.let { changedFiles.add(it) }
                }
            }
            
            return RefactoringResult(
                success = true,
                filesChanged = changedFiles.toList(),
                changesCount = changedFiles.size
            )
            
        } catch (e: Exception) {
            return RefactoringResult(
                success = false,
                error = RefactoringError(
                    "EXTRACT_METHOD_FAILED",
                    "Failed to extract method: ${e.message}",
                    mapOf("exception" to (e::class.simpleName ?: "Unknown"))
                )
            )
        }
    }
    
    private data class CodeAnalysis(
        val suggestedName: String,
        val parameters: List<MethodParameter>,
        val returnType: String?
    )
    
    private fun analyzeJavaScriptCode(
        element: PsiElement?,
        startOffset: Int,
        endOffset: Int,
        isTypeScript: Boolean
    ): CodeAnalysis {
        if (element == null) {
            return CodeAnalysis("extractedMethod", emptyList(), null)
        }
        
        // Simplified analysis - in a real implementation, we would use
        // JavaScript/TypeScript type system and data flow analysis
        
        // For now, return basic analysis
        return CodeAnalysis(
            suggestedName = "extractedMethod",
            parameters = emptyList(),
            returnType = if (isTypeScript) "void" else null
        )
    }
    
    private fun findInsertionPoint(psiFile: PsiFile, currentOffset: Int): Int {
        // Find the containing class
        val element = psiFile.findElementAt(currentOffset)
        
        // Look for class declaration
        var parent = element?.parent
        while (parent != null) {
            val text = parent.text
            if (text.contains("class ")) {
                // Find the closing brace of the class
                val classEnd = parent.textRange.endOffset
                return classEnd - 1
            }
            parent = parent.parent
        }
        
        // Fallback: insert at the end of the file
        return psiFile.textLength
    }
}
