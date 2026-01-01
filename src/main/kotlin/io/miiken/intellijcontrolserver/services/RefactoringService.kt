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
            
            val namedElement = PsiUtils.findNamedElementAtOffset(psiFile, request.offset)
                ?: return RefactoringResult(
                    success = false,
                    error = RefactoringError(
                        "INVALID_ELEMENT",
                        "No renameable element found at offset ${request.offset}"
                    )
                )
            
            val validationResult = PsiUtils.validateElementName(namedElement, request.oldName)
            when (validationResult) {
                is PsiUtils.ValidationResult.Mismatch -> {
                    return RefactoringResult(
                        success = false,
                        error = RefactoringError(
                            "NAME_MISMATCH",
                            "Expected '${validationResult.expectedName}' at offset ${request.offset}, but found '${validationResult.actualName}'. File may have changed.",
                            mapOf(
                                "expectedName" to validationResult.expectedName,
                                "actualName" to validationResult.actualName,
                                "offset" to request.offset
                            )
                        )
                    )
                }
                is PsiUtils.ValidationResult.Invalid -> {
                    return RefactoringResult(
                        success = false,
                        error = RefactoringError("INVALID_ELEMENT", validationResult.reason)
                    )
                }
                PsiUtils.ValidationResult.Valid -> {
                }
            }
            
            return executeRename(project, namedElement, request.newName, request.searchInComments)
            
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
    
    @Suppress("UNUSED_PARAMETER")
    fun extractMethod(project: Project, request: ExtractMethodRequest): RefactoringResult {
        return RefactoringResult(
            success = false,
            error = RefactoringError(
                "NOT_IMPLEMENTED",
                "Extract method is not yet implemented. Coming in next iteration."
            )
        )
    }
    
    private fun executeRename(
        project: Project,
        element: PsiElement,
        newName: String,
        searchInComments: Boolean
    ): RefactoringResult {
        val processor = RenameProcessor(project, element, newName, searchInComments, false)
        
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

