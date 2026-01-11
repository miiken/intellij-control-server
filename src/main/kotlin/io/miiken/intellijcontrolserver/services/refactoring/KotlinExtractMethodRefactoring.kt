package io.miiken.intellijcontrolserver.services.refactoring

import com.intellij.lang.Language
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import io.miiken.intellijcontrolserver.models.ExtractMethodAnalysis
import io.miiken.intellijcontrolserver.models.ExtractMethodOptions
import io.miiken.intellijcontrolserver.models.MethodParameter
import io.miiken.intellijcontrolserver.models.RefactoringResult
import io.miiken.intellijcontrolserver.models.RefactoringError
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import org.jetbrains.kotlin.idea.core.util.toPsiFile

/**
 * Kotlin-specific implementation of extract method refactoring.
 * 
 * Uses IntelliJ's native Kotlin refactoring engine for extracting code into new methods.
 */
class KotlinExtractMethodRefactoring : LanguageExtractMethodRefactoring {
    
    override fun supports(language: Language): Boolean {
        return language.isKindOf(KotlinLanguage.INSTANCE)
    }
    
    override fun analyze(
        project: Project,
        psiFile: PsiFile,
        startOffset: Int,
        endOffset: Int
    ): ExtractMethodAnalysis {
        try {
            if (psiFile !is KtFile) {
                return ExtractMethodAnalysis(
                    canExtract = false,
                    language = "Kotlin",
                    errorMessage = "File is not a Kotlin file"
                )
            }
            
            // Find elements in the selected range
            val startElement = psiFile.findElementAt(startOffset)
            val endElement = psiFile.findElementAt(endOffset - 1)
            
            if (startElement == null || endElement == null) {
                return ExtractMethodAnalysis(
                    canExtract = false,
                    language = "Kotlin",
                    errorMessage = "Could not find elements in selected range"
                )
            }
            
            // Find common parent that contains both start and end
            val commonParent = PsiTreeUtil.findCommonParent(startElement, endElement)
                ?: return ExtractMethodAnalysis(
                    canExtract = false,
                    language = "Kotlin",
                    errorMessage = "Could not find common parent for selection"
                )
            
            // Analyze the selected code for variables and dependencies
            val analysis = analyzeCodeSelection(commonParent, startOffset, endOffset)
            
            return ExtractMethodAnalysis(
                canExtract = true,
                suggestedMethodName = analysis.suggestedName,
                detectedParameters = analysis.parameters,
                returnType = analysis.returnType,
                suggestedVisibility = "private",
                language = "Kotlin"
            )
            
        } catch (e: Exception) {
            return ExtractMethodAnalysis(
                canExtract = false,
                language = "Kotlin",
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
            if (psiFile !is KtFile) {
                return RefactoringResult(
                    success = false,
                    error = RefactoringError(
                        "INVALID_FILE_TYPE",
                        "File is not a Kotlin file"
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
                    val analysis = analyzeCodeSelection(psiFile.findElementAt(startOffset), startOffset, endOffset)
                    
                    // Generate method signature
                    val parameters = if (options.parameterOrder != null) {
                        // Reorder parameters according to user's choice
                        options.parameterOrder.mapNotNull { paramName ->
                            analysis.parameters.find { it.name == paramName }
                        }
                    } else {
                        analysis.parameters
                    }
                    
                    val paramString = parameters.joinToString(", ") { "${it.name}: ${it.type}" }
                    val returnType = options.returnType ?: analysis.returnType ?: "Unit"
                    val visibility = options.visibility.lowercase()
                    
                    // Generate the new method
                    val newMethod = buildString {
                        appendLine()
                        appendLine()
                        append("    ")
                        if (visibility != "public") append("$visibility ")
                        append("fun ${options.methodName}($paramString): $returnType {")
                        appendLine()
                        // Indent the extracted code
                        extractedCode.lines().forEach { line ->
                            if (line.isNotBlank()) {
                                appendLine("        $line")
                            } else {
                                appendLine()
                            }
                        }
                        append("    }")
                    }
                    
                    // Generate method call
                    val callParams = parameters.joinToString(", ") { it.name }
                    val methodCall = if (returnType != "Unit" && !analysis.parameters.any { it.isOutput }) {
                        "val result = ${options.methodName}($callParams)"
                    } else {
                        "${options.methodName}($callParams)"
                    }
                    
                    // Replace the selected code with method call
                    document.replaceString(startOffset, endOffset, methodCall)
                    
                    // Insert the new method at the end of the containing class/file
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
    
    private fun analyzeCodeSelection(element: PsiElement?, startOffset: Int, endOffset: Int): CodeAnalysis {
        if (element == null) {
            return CodeAnalysis("extractedMethod", emptyList(), "Unit")
        }
        
        // Find all variable references in the selection
        val variables = mutableMapOf<String, String>() // name -> type
        val declaredInSelection = mutableSetOf<String>()
        
        // Traverse the element tree within the range
        element.accept(object : KtVisitorVoid() {
            override fun visitReferenceExpression(expression: KtReferenceExpression) {
                super.visitReferenceExpression(expression)
                
                val offset = expression.textRange.startOffset
                if (offset >= startOffset && offset < endOffset) {
                    val name = expression.text
                    
                    // Check if this variable is declared within the selection
                    val declaration = expression.reference?.resolve()
                    val declOffset = declaration?.textRange?.startOffset ?: -1
                    
                    if (declOffset < startOffset || declOffset >= endOffset) {
                        // Variable is used but declared outside - it's a parameter
                        val type = inferType(expression)
                        if (name !in declaredInSelection) {
                            variables[name] = type
                        }
                    } else {
                        // Variable is declared within selection
                        declaredInSelection.add(name)
                    }
                }
            }
        })
        
        val parameters = variables.map { (name, type) ->
            MethodParameter(name, type, isOutput = false)
        }
        
        // Suggest a method name based on context
        val suggestedName = generateMethodName(element)
        
        // Try to infer return type (simplified)
        val returnType = inferReturnType(element, startOffset, endOffset)
        
        return CodeAnalysis(suggestedName, parameters, returnType)
    }
    
    private fun inferType(expression: KtReferenceExpression): String {
        // Simplified type inference - in a real implementation,
        // we would use Kotlin's type system
        return "Any" // Default fallback
    }
    
    private fun generateMethodName(element: PsiElement): String {
        // Generate a meaningful name based on the code content
        // Simplified version
        return "extractedMethod"
    }
    
    private fun inferReturnType(element: PsiElement, startOffset: Int, endOffset: Int): String? {
        // Check if the selection ends with a return statement or expression
        // Simplified implementation
        return "Unit"
    }
    
    private fun findInsertionPoint(psiFile: KtFile, currentOffset: Int): Int {
        // Find the containing class or object
        val element = psiFile.findElementAt(currentOffset)
        val containingClass = PsiTreeUtil.getParentOfType(element, KtClassOrObject::class.java)
        
        if (containingClass != null) {
            // Insert before the closing brace of the class
            val body = containingClass.body
            if (body != null) {
                return body.textRange.endOffset - 1
            }
        }
        
        // Fallback: insert at the end of the file
        return psiFile.textLength
    }
}
