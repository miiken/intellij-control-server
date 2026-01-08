package io.miiken.intellijcontrolserver.services

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiModifierListOwner
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiRecursiveElementVisitor
import com.intellij.refactoring.rename.RenameProcessor
import io.miiken.intellijcontrolserver.config.ConfigLoader
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
            
            return executeRename(project, namedElement, request.newName)
            
        } catch (e: ProcessCanceledException) {
            throw e
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
                    return executeExtractMethod(
                        project,
                        psiFile,
                        rangeResult.startOffset,
                        rangeResult.endOffset,
                        request.methodName,
                        request.visibility
                    )
                }
            }
        } catch (e: ProcessCanceledException) {
            throw e
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
        newName: String
    ): RefactoringResult {
        val oldName = ReadAction.compute<String?, RuntimeException> {
            (element as? PsiNamedElement)?.name
        } ?: throw IllegalArgumentException("Element has no name")
        
        val changedFiles = mutableSetOf<String>()
        
        ApplicationManager.getApplication().invokeAndWait {
            val processor = RenameProcessor(project, element, newName, false, false)
            val usages = processor.findUsages()
            usages.mapNotNullTo(changedFiles) { usage ->
                usage.file?.virtualFile?.path
            }
            processor.run()
        }
        
        FileDocumentManager.getInstance().saveAllDocuments()
        
        
        val config = ConfigLoader.load()
        if (element is PsiNamedElement && PsiUtils.isMethod(element) && 
            (config.renameStringsInMethodBody || config.renameInAnnotations)) {
            val additionalChanges = updateStringsInMethodBody(
                project, 
                element, 
                oldName, 
                newName, 
                updateMethodBody = config.renameStringsInMethodBody,
                updateAnnotations = config.renameInAnnotations
            )
            val allChangedFiles = changedFiles + additionalChanges
            
            return RefactoringResult(
                success = true,
                filesChanged = allChangedFiles.toList(),
                changesCount = allChangedFiles.size
            )
        }
        
        return RefactoringResult(
            success = true,
            filesChanged = changedFiles.toList(),
            changesCount = changedFiles.size
        )
    }
    
    /**
     * Updates all occurrences of the old method name in string literals within:
     * 1. Annotations attached to the method (e.g., @Timed("methodName")) - if updateAnnotations is true
     * 2. The method body (e.g., logger.info("Calling methodName...")) - if updateMethodBody is true
     * 3. Test method names (e.g., "GIVEN ... WHEN methodName THEN ...") - if updateMethodBody is true
     */
    private fun updateStringsInMethodBody(
        project: Project,
        methodElement: PsiElement,
        oldName: String,
        newName: String,
        updateMethodBody: Boolean = true,
        updateAnnotations: Boolean = true
    ): Set<String> {
        val psiFile = methodElement.containingFile
        val document = PsiDocumentManager.getInstance(project).getDocument(psiFile)
            ?: return emptySet()
        
        val replacements = ReadAction.compute<List<Pair<Int, Int>>, RuntimeException> {
            collectStringReplacements(methodElement, oldName, updateMethodBody, updateAnnotations)
        }
        
        if (replacements.isEmpty()) {
            return emptySet()
        }
        
        WriteCommandAction.runWriteCommandAction(project) {
            replacements.sortedByDescending { it.first }.forEach { (offset, length) ->
                document.replaceString(offset, offset + length, newName)
            }
            PsiDocumentManager.getInstance(project).commitDocument(document)
            FileDocumentManager.getInstance().saveDocument(document)
        }
        
        return psiFile.virtualFile?.path?.let { setOf(it) } ?: emptySet()
    }
    
    private fun collectStringReplacements(
        methodElement: PsiElement,
        oldName: String,
        updateMethodBody: Boolean,
        updateAnnotations: Boolean
    ): List<Pair<Int, Int>> {
        val replacements = mutableListOf<Pair<Int, Int>>()
        
        fun processElement(element: PsiElement) {
            val elementType = element.node?.elementType?.toString() ?: ""
            if (elementType.contains("STRING") || 
                elementType.contains("LITERAL_STRING_TEMPLATE_ENTRY") ||
                element.javaClass.simpleName.contains("StringLiteral")) {
                val text = element.text
                val matches = findWordBoundaryMatches(text, oldName)
                
                matches.forEach { index ->
                    val absoluteOffset = element.textRange.startOffset + index
                    replacements.add(Pair(absoluteOffset, oldName.length))
                }
            }
        }
        
        val visitor = object : PsiRecursiveElementVisitor() {
            override fun visitElement(element: PsiElement) {
                super.visitElement(element)
                processElement(element)
            }
        }
        
        if (updateAnnotations) {
            if (methodElement is PsiModifierListOwner) {
                methodElement.modifierList?.annotations?.forEach { it.accept(visitor) }
            }
            
            try {
                val ktAnnotationsMethod = methodElement.javaClass.getMethod("getAnnotationEntries")
                val annotations = ktAnnotationsMethod.invoke(methodElement) as? List<*>
                annotations?.forEach { (it as? PsiElement)?.accept(visitor) }
            } catch (e: Exception) {
            }
        }
        
        if (updateMethodBody) {
            methodElement.accept(visitor)
        }
        
        return replacements
    }
    
    private fun executeExtractMethod(
        project: Project,
        psiFile: PsiFile,
        startOffset: Int,
        endOffset: Int,
        methodName: String,
        visibility: String
    ): RefactoringResult {
        val changedFiles = mutableSetOf<String>()
        
        try {
            // For now, perform a simple implementation that works with both Java and Kotlin
            // This can be enhanced with language-specific processors later
            ApplicationManager.getApplication().invokeAndWait {
                WriteCommandAction.runWriteCommandAction(project) {
                    val document = PsiDocumentManager.getInstance(project).getDocument(psiFile)
                        ?: throw IllegalStateException("Cannot get document for file")
                    
                    // Extract the code text
                    val extractedCode = document.getText(TextRange(startOffset, endOffset))
                    
                    // Generate the new method signature based on file type
                    val isKotlin = psiFile.name.endsWith(".kt")
                    val visibilityModifier = if (visibility == "private" || visibility == "internal") visibility else "private"
                    
                    val newMethod = if (isKotlin) {
                        "\n\n    $visibilityModifier fun $methodName() {\n        $extractedCode\n    }"
                    } else {
                        "\n\n    $visibilityModifier void $methodName() {\n        $extractedCode\n    }"
                    }
                    
                    // Replace the selected code with a method call
                    val methodCall = if (isKotlin) {
                        "$methodName()"
                    } else {
                        "$methodName();"
                    }
                    
                    document.replaceString(startOffset, endOffset, methodCall)
                    
                    // Find a good place to insert the new method
                    // For simplicity, insert at the end of the class/file
                    val fileEndOffset = document.textLength
                    document.insertString(fileEndOffset, newMethod)
                    
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
        } catch (e: ProcessCanceledException) {
            throw e
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
    
    /**
     * Finds all indices in the text where the search term appears as a whole word/identifier.
     * A match is valid if it's not part of a larger word or identifier.
     * 
     * Examples:
     * - "start" in "method started" -> match at index 7 (word boundary)
     * - "start" in "Metrics.Timed.start" -> match at index 14 (after period)
     * - "start" in "starting" -> no match (part of larger word)
     * 
     * @param text The text to search in
     * @param searchTerm The term to search for
     * @return List of indices where valid matches occur
     */
    internal fun findWordBoundaryMatches(text: String, searchTerm: String): List<Int> {
        val matches = mutableListOf<Int>()
        var startIndex = 0
        
        while (true) {
            val index = text.indexOf(searchTerm, startIndex)
            if (index == -1) break
            
            val charBefore = if (index > 0) text[index - 1] else null
            val charAfter = if (index + searchTerm.length < text.length) text[index + searchTerm.length] else null
            
            val isValidBefore = charBefore == null || (!charBefore.isLetterOrDigit() && charBefore != '_')
            val isValidAfter = charAfter == null || (!charAfter.isLetterOrDigit() && charAfter != '_')
            
            if (isValidBefore && isValidAfter) {
                matches.add(index)
            }
            
            startIndex = index + searchTerm.length
        }
        
        return matches
    }
}

