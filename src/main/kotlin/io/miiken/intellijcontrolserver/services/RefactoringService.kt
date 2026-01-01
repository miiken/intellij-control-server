package io.miiken.intellijcontrolserver.services

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.refactoring.rename.RenameProcessor
import io.miiken.intellijcontrolserver.models.*
import io.miiken.intellijcontrolserver.util.PsiUtils

object RefactoringService {
    
    fun rename(project: Project, request: RenameRequest): RefactoringResult {
        try {
            val psiFile = PsiUtils.findPsiFile(project, request.filePath)
                ?: return RefactoringResult(
                    success = false,
                    error = RefactoringError("FILE_NOT_FOUND", "File not found: ${request.filePath}")
                )
            
            val findResult = PsiUtils.findNamedElementByLineAndName(psiFile, request.line, request.oldName)
            
            val namedElement = when (findResult) {
                is PsiUtils.FindElementResult.Found -> findResult.element
                is PsiUtils.FindElementResult.NotFound -> {
                    return RefactoringResult(
                        success = false,
                        error = RefactoringError(
                            "ELEMENT_NOT_FOUND",
                            findResult.message,
                            mapOf("line" to request.line, "name" to request.oldName)
                        )
                    )
                }
                is PsiUtils.FindElementResult.Ambiguous -> {
                    return RefactoringResult(
                        success = false,
                        error = RefactoringError(
                            "AMBIGUOUS_LOCATION",
                            findResult.message,
                            mapOf("line" to request.line, "name" to request.oldName)
                        )
                    )
                }
                is PsiUtils.FindElementResult.InvalidLine -> {
                    return RefactoringResult(
                        success = false,
                        error = RefactoringError(
                            "INVALID_LINE",
                            findResult.message,
                            mapOf("line" to request.line)
                        )
                    )
                }
            }
            
            val isMethod = PsiUtils.isMethod(namedElement)
            val shouldSearchInStrings = request.searchInStrings ?: isMethod
            
            return executeRename(project, namedElement, request.newName, shouldSearchInStrings)
            
        } catch (e: Exception) {
            return RefactoringResult(
                success = false,
                error = RefactoringError(
                    "REFACTORING_FAILED",
                    "Rename failed: ${e.message}",
                    mapOf("exception" to e.javaClass.simpleName)
                )
            )
        }
    }
    
    fun extractMethod(project: Project, request: ExtractMethodRequest): RefactoringResult {
        try {
            val psiFile = PsiUtils.findPsiFile(project, request.filePath)
                ?: return RefactoringResult(
                    success = false,
                    error = RefactoringError("FILE_NOT_FOUND", "File not found: ${request.filePath}")
                )
            
            val rangeResult = PsiUtils.calculateTextRange(
                psiFile,
                request.startLine,
                request.endLine,
                request.startColumn,
                request.endColumn
            )
            
            when (rangeResult) {
                is PsiUtils.TextRangeResult.Error -> {
                    return RefactoringResult(
                        success = false,
                        error = RefactoringError(
                            "INVALID_RANGE",
                            rangeResult.message,
                            buildMap {
                                put("startLine", request.startLine)
                                put("endLine", request.endLine)
                                request.startColumn?.let { put("startColumn", it) }
                                request.endColumn?.let { put("endColumn", it) }
                            }
                        )
                    )
                }
                is PsiUtils.TextRangeResult.Success -> {
                    return RefactoringResult(
                        success = false,
                        error = RefactoringError(
                            "NOT_IMPLEMENTED",
                            "Extract method processor integration is not yet implemented. Text range calculation works (${rangeResult.startOffset}-${rangeResult.endOffset})."
                        )
                    )
                }
            }
        } catch (e: Exception) {
            return RefactoringResult(
                success = false,
                error = RefactoringError(
                    "INTERNAL_ERROR",
                    "Failed to extract method: ${e.message}",
                    mapOf("exception" to (e::class.simpleName ?: "Unknown"))
                )
            )
        }
    }
    
    private fun executeRename(
        project: Project,
        element: PsiElement,
        newName: String,
        searchInStrings: Boolean
    ): RefactoringResult {
        val processor = RenameProcessor(project, element, newName, false, searchInStrings)
        
        val changedFiles = ReadAction.compute<Set<String>, RuntimeException> {
            val usages = processor.findUsages()
            usages.mapNotNull { usage ->
                usage.file?.virtualFile?.path
            }.toSet()
        }
        
        ApplicationManager.getApplication().invokeAndWait {
            processor.run()
        }
        
        return RefactoringResult(
            success = true,
            filesChanged = changedFiles.toList(),
            changesCount = changedFiles.size
        )
    }
}

