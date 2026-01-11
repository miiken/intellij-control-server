package io.miiken.intellijcontrolserver.services

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.refactoring.rename.RenameProcessor
import io.miiken.intellijcontrolserver.models.RefactoringError
import io.miiken.intellijcontrolserver.models.RefactoringResult
import io.miiken.intellijcontrolserver.models.RenameRequest
import io.miiken.intellijcontrolserver.util.PsiUtils
import java.util.concurrent.atomic.AtomicReference

/**
 * Service responsible for performing code refactoring operations using IntelliJ's native APIs.
 * 
 * Threading model:
 * - PSI reads/writes must run on EDT or in read/write actions
 * - Rename processor operations require proper EDT and command context
 * - Uses background threads for findUsages(), EDT for actual refactoring
 */
object RefactoringService {
    private val logger = Logger.getInstance(RefactoringService::class.java)

    /**
     * Rename a symbol (variable, function, class, etc.) at the specified location.
     * Uses IntelliJ's native RenameProcessor to ensure all references are updated.
     */
    fun rename(project: Project, request: RenameRequest): RefactoringResult {
        try {
            logger.info("Rename request: ${request.filePath}:${request.line} ${request.oldName} -> ${request.newName}")
            
            val psiFile = PsiUtils.findPsiFile(project, request.filePath)
                ?: return RefactoringResult(
                    success = false,
                    error = RefactoringError(
                        "FILE_NOT_FOUND",
                        "File not found: ${request.filePath}"
                    )
                )

            // Find the named element on the specified line
            val findResult = PsiUtils.findNamedElementByLineAndName(psiFile, request.line, request.oldName)
            
            val element = when (findResult) {
                is PsiUtils.FindElementResult.Found -> findResult.element
                is PsiUtils.FindElementResult.NotFound -> return RefactoringResult(
                    success = false,
                    error = RefactoringError(
                        "NO_SYMBOL_FOUND",
                        findResult.message
                    )
                )
                is PsiUtils.FindElementResult.Ambiguous -> return RefactoringResult(
                    success = false,
                    error = RefactoringError(
                        "AMBIGUOUS_SYMBOL",
                        findResult.message
                    )
                )
                is PsiUtils.FindElementResult.InvalidLine -> return RefactoringResult(
                    success = false,
                    error = RefactoringError(
                        "INVALID_LINE",
                        findResult.message,
                        mapOf("line" to request.line)
                    )
                )
            }

            val resultRef = AtomicReference<RefactoringResult>()
            
            // Execute rename on a background thread (for findUsages) then EDT (for actual rename)
            ApplicationManager.getApplication().executeOnPooledThread {
                try {
                    // Create rename processor and find usages (can run in background)
                    val processor = ApplicationManager.getApplication().runReadAction<RenameProcessor> {
                        RenameProcessor(project, element, request.newName, false, false)
                    }
                    
                    ApplicationManager.getApplication().runReadAction<Unit> {
                        processor.findUsages()
                    }

                    // Execute the actual rename on EDT within a command
                    ApplicationManager.getApplication().invokeAndWait {
                        CommandProcessor.getInstance().executeCommand(project, {
                            processor.run()
                            
                            // Save all documents
                            ApplicationManager.getApplication().runWriteAction {
                                FileDocumentManager.getInstance().saveAllDocuments()
                                PsiDocumentManager.getInstance(project).commitAllDocuments()
                            }
                        }, "Rename ${request.oldName} to ${request.newName}", null)
                    }

                    // Collect affected files
                    val affectedFiles = mutableListOf<String>()
                    val virtualFile = psiFile.virtualFile
                    if (virtualFile != null) {
                        affectedFiles.add(virtualFile.path)
                    }

                    resultRef.set(RefactoringResult(
                        success = true,
                        filesChanged = affectedFiles,
                        changesCount = 1 // Simplified count
                    ))
                    
                    logger.info("Rename successful: ${request.oldName} -> ${request.newName}")
                } catch (e: ProcessCanceledException) {
                    throw e
                } catch (e: Exception) {
                    logger.error("Rename failed", e)
                    resultRef.set(RefactoringResult(
                        success = false,
                        error = RefactoringError(
                            "RENAME_FAILED",
                            "Rename operation failed: ${e.message}",
                            mapOf("exception" to e::class.java.simpleName)
                        )
                    ))
                }
            }.get() // Wait for completion

            return resultRef.get() ?: RefactoringResult(
                success = false,
                error = RefactoringError("INTERNAL_ERROR", "Rename operation did not complete")
            )
        } catch (e: ProcessCanceledException) {
            throw e
        } catch (e: Exception) {
            logger.error("Unexpected error in rename", e)
            return RefactoringResult(
                success = false,
                error = RefactoringError(
                    "INTERNAL_ERROR",
                    "Unexpected error: ${e.message}",
                    mapOf("stackTrace" to e.stackTraceToString())
                )
            )
        }
    }
}
