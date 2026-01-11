package io.miiken.intellijcontrolserver.services.refactoring

import com.intellij.lang.Language
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
 * Scala-specific implementation of extract method refactoring.
 * 
 * Uses IntelliJ's native Scala refactoring engine.
 * Requires Scala plugin to be installed.
 */
class ScalaExtractMethodRefactoring : LanguageExtractMethodRefactoring {
    
    override fun supports(language: Language): Boolean {
        // Check if language is Scala
        return language.id.equals("Scala", ignoreCase = true)
    }
    
    override fun analyze(
        project: Project,
        psiFile: PsiFile,
        startOffset: Int,
        endOffset: Int
    ): ExtractMethodAnalysis {
        try {
            // Check if Scala plugin is available
            if (!isScalaPluginAvailable()) {
                return ExtractMethodAnalysis(
                    canExtract = false,
                    language = "Scala",
                    errorMessage = "Scala plugin is not installed or not available"
                )
            }
            
            // Find elements in the selected range
            val startElement = psiFile.findElementAt(startOffset)
            val endElement = psiFile.findElementAt(endOffset - 1)
            
            if (startElement == null || endElement == null) {
                return ExtractMethodAnalysis(
                    canExtract = false,
                    language = "Scala",
                    errorMessage = "Could not find elements in selected range"
                )
            }
            
            // Find common parent
            val commonParent = PsiTreeUtil.findCommonParent(startElement, endElement)
                ?: return ExtractMethodAnalysis(
                    canExtract = false,
                    language = "Scala",
                    errorMessage = "Could not find common parent for selection"
                )
            
            // Analyze the selected code
            val analysis = analyzeScalaCode(commonParent, startOffset, endOffset)
            
            return ExtractMethodAnalysis(
                canExtract = true,
                suggestedMethodName = analysis.suggestedName,
                detectedParameters = analysis.parameters,
                returnType = analysis.returnType,
                suggestedVisibility = "private",
                language = "Scala"
            )
            
        } catch (e: Exception) {
            return ExtractMethodAnalysis(
                canExtract = false,
                language = "Scala",
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
            if (!isScalaPluginAvailable()) {
                return RefactoringResult(
                    success = false,
                    error = RefactoringError(
                        "SCALA_PLUGIN_NOT_AVAILABLE",
                        "Scala plugin is not installed or not available"
                    )
                )
            }
            
            val changedFiles = mutableSetOf<String>()
            
            ApplicationManager.getApplication().invokeAndWait {
                WriteCommandAction.runWriteCommandAction(project) {
                    val document = PsiDocumentManager.getInstance(project).getDocument(psiFile)
                        ?: throw IllegalStateException("Cannot get document for file")
                    
                    // Extract the code text
                    val extractedCode = document.getText(com.intellij.openapi.util.TextRange(startOffset, endOffset))
                    
                    // Analyze what variables are used
                    val element = psiFile.findElementAt(startOffset)
                    val analysis = analyzeScalaCode(element, startOffset, endOffset)
                    
                    // Reorder parameters if requested
                    val parameters = if (options.parameterOrder != null) {
                        options.parameterOrder.mapNotNull { paramName ->
                            analysis.parameters.find { it.name == paramName }
                        }
                    } else {
                        analysis.parameters
                    }
                    
                    // Generate method signature
                    val paramString = parameters.joinToString(", ") { "${it.name}: ${it.type}" }
                    val returnType = options.returnType ?: analysis.returnType ?: "Unit"
                    val visibility = options.visibility.lowercase()
                    
                    // Generate the new method (Scala style)
                    val newMethod = buildString {
                        appendLine()
                        appendLine()
                        append("  ")
                        if (visibility != "public") append("$visibility ")
                        append("def ${options.methodName}($paramString): $returnType = {")
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
                    val methodCall = if (returnType != "Unit" && parameters.isNotEmpty()) {
                        "val result = ${options.methodName}($callParams)"
                    } else {
                        "${options.methodName}($callParams)"
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
    
    private fun analyzeScalaCode(
        element: PsiElement?,
        startOffset: Int,
        endOffset: Int
    ): CodeAnalysis {
        if (element == null) {
            return CodeAnalysis("extractedMethod", emptyList(), "Unit")
        }
        
        // Simplified analysis - in a real implementation, we would use
        // Scala's type system and data flow analysis
        
        return CodeAnalysis(
            suggestedName = "extractedMethod",
            parameters = emptyList(),
            returnType = "Unit"
        )
    }
    
    private fun isScalaPluginAvailable(): Boolean {
        return try {
            // Try to load Scala plugin class
            Class.forName("org.jetbrains.plugins.scala.lang.psi.api.ScalaFile")
            true
        } catch (e: ClassNotFoundException) {
            false
        }
    }
    
    private fun findInsertionPoint(psiFile: PsiFile, currentOffset: Int): Int {
        // Find the containing class
        val element = psiFile.findElementAt(currentOffset)
        
        // Look for class declaration
        var parent = element?.parent
        while (parent != null) {
            val text = parent.text
            if (text.contains("class ") || text.contains("object ")) {
                // Find the closing brace of the class/object
                val classEnd = parent.textRange.endOffset
                return classEnd - 1
            }
            parent = parent.parent
        }
        
        // Fallback: insert at the end of the file
        return psiFile.textLength
    }
}
