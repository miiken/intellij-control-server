package io.miiken.intellijcontrolserver.services

import com.intellij.lang.Language
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
import io.miiken.intellijcontrolserver.services.refactoring.ExtractMethodHandlerRegistry
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
    
    /**
     * TWO-PHASE API: Analyze code selection for extract method refactoring.
     * 
     * This is Phase 1 of the two-phase extract method API. It analyzes the selected
     * code and returns suggestions without making any changes.
     * 
     * @param project The IntelliJ project
     * @param request Request containing file path and line range to analyze
     * @return Analysis results with suggestions for method extraction
     */
    fun analyzeExtractMethod(project: Project, request: ExtractMethodRequest): ExtractMethodAnalysis {
        try {
            val psiFile = PsiUtils.findPsiFile(project, request.filePath)
                ?: return ExtractMethodAnalysis(
                    canExtract = false,
                    language = "Unknown",
                    errorMessage = "File not found: ${request.filePath}"
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
                    return ExtractMethodAnalysis(
                        canExtract = false,
                        language = psiFile.language.displayName,
                        errorMessage = rangeResult.message
                    )
                }
                is PsiUtils.TextRangeResult.Success -> {
                    val handler = ExtractMethodHandlerRegistry.findHandler(psiFile.language)
                        ?: return ExtractMethodAnalysis(
                            canExtract = false,
                            language = psiFile.language.displayName,
                            errorMessage = "Language not supported: ${psiFile.language.displayName}. " +
                                "Supported languages: ${ExtractMethodHandlerRegistry.getSupportedLanguages().joinToString()}"
                        )
                    
                    return handler.analyze(
                        project,
                        psiFile,
                        rangeResult.startOffset,
                        rangeResult.endOffset
                    )
                }
            }
        } catch (e: ProcessCanceledException) {
            throw e
        } catch (e: Exception) {
            return ExtractMethodAnalysis(
                canExtract = false,
                language = "Unknown",
                errorMessage = "Analysis failed: ${e.message}"
            )
        }
    }
    
    /**
     * TWO-PHASE API: Execute extract method refactoring with user-provided options.
     * 
     * This is Phase 2 of the two-phase extract method API. It performs the actual
     * extraction using the options provided by the user/AI after analyzing.
     * 
     * @param project The IntelliJ project
     * @param request Request containing file path and line range
     * @param options User choices for the extraction (method name, visibility, etc.)
     * @return Result indicating success/failure and files changed
     */
    fun executeExtractMethodWithOptions(
        project: Project,
        request: ExtractMethodRequest,
        options: ExtractMethodOptions
    ): RefactoringResult {
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
                    val handler = ExtractMethodHandlerRegistry.findHandler(psiFile.language)
                        ?: return RefactoringResult(
                            success = false,
                            error = RefactoringError(
                                "LANGUAGE_NOT_SUPPORTED",
                                "Language not supported: ${psiFile.language.displayName}. " +
                                    "Supported languages: ${ExtractMethodHandlerRegistry.getSupportedLanguages().joinToString()}",
                                mapOf(
                                    "requestedLanguage" to psiFile.language.displayName,
                                    "supportedLanguages" to ExtractMethodHandlerRegistry.getSupportedLanguages()
                                )
                            )
                        )
                    
                    return handler.execute(
                        project,
                        psiFile,
                        rangeResult.startOffset,
                        rangeResult.endOffset,
                        options
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
    
    /**
     * LEGACY API: Extract method refactoring (single-phase).
     * 
     * @deprecated Use analyzeExtractMethod and executeExtractMethodWithOptions for the two-phase API
     */
    @Deprecated("Use two-phase API: analyzeExtractMethod and executeExtractMethodWithOptions")
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
        
        // Create processor and find usages in ReadAction (required for PSI access)
        val processor = ReadAction.compute<RenameProcessor, RuntimeException> {
            RenameProcessor(project, element, newName, false, false)
        }
        
        val usages = ReadAction.compute<Array<out com.intellij.usageView.UsageInfo>, RuntimeException> {
            processor.findUsages()
        }
        
        usages.mapNotNullTo(changedFiles) { usage ->
            usage.file?.virtualFile?.path
        }
        
        // Execute the actual rename on EDT (UI operation)
        ApplicationManager.getApplication().invokeAndWait {
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
            ApplicationManager.getApplication().invokeAndWait {
                WriteCommandAction.runWriteCommandAction(project) {
                    val document = PsiDocumentManager.getInstance(project).getDocument(psiFile)
                        ?: throw IllegalStateException("Cannot get document for file")
                    
                    // Extract the code text
                    val extractedCode = document.getText(TextRange(startOffset, endOffset))
                    
                    // Detect language using IntelliJ's language detection
                    val language = psiFile.language.id.lowercase()
                    val fileExtension = psiFile.name.substringAfterLast('.')
                    
                    // Generate method based on detected language
                    val (newMethod, methodCall) = generateMethodForLanguage(
                        language, fileExtension, methodName, visibility, extractedCode
                    )
                    
                    // Replace the selected code with a method call
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
    
    private fun generateMethodForLanguage(
        language: String,
        fileExtension: String,
        methodName: String,
        visibility: String,
        extractedCode: String
    ): Pair<String, String> {
        return when {
            // Kotlin
            language.contains("kotlin") || fileExtension == "kt" -> {
                val vis = mapVisibilityForKotlin(visibility)
                val method = "\n\n    $vis fun $methodName() {\n        $extractedCode\n    }"
                val call = "$methodName()"
                Pair(method, call)
            }
            
            // Java
            language.contains("java") || fileExtension == "java" -> {
                val vis = mapVisibilityForJava(visibility)
                val method = "\n\n    $vis void $methodName() {\n        $extractedCode\n    }"
                val call = "$methodName();"
                Pair(method, call)
            }
            
            // JavaScript / TypeScript
            language.contains("javascript") || language.contains("typescript") || 
            fileExtension in listOf("js", "jsx", "ts", "tsx") -> {
                // For JavaScript/TypeScript, visibility is handled via naming convention or TypeScript keywords
                val vis = if (visibility == "private" && fileExtension in listOf("ts", "tsx")) "private " else ""
                val method = "\n\n  ${vis}$methodName() {\n    $extractedCode\n  }"
                val call = "this.$methodName()"
                Pair(method, call)
            }
            
            // Python
            language.contains("python") || fileExtension == "py" -> {
                val prefix = if (visibility == "private") "__" else ""
                val method = "\n\n    def $prefix$methodName(self):\n        ${extractedCode.prependIndent("    ")}"
                val call = "self.$prefix$methodName()"
                Pair(method, call)
            }
            
            // Go
            language.contains("go") || fileExtension == "go" -> {
                // In Go, visibility is controlled by first letter case (uppercase = public)
                val funcName = if (visibility == "public") 
                    methodName.replaceFirstChar { it.uppercase() } 
                else 
                    methodName.replaceFirstChar { it.lowercase() }
                val method = "\n\nfunc $funcName() {\n\t$extractedCode\n}"
                val call = "$funcName()"
                Pair(method, call)
            }
            
            // C# / C++
            language.contains("c#") || language.contains("c++") || fileExtension in listOf("cs", "cpp", "cc", "cxx") -> {
                val vis = mapVisibilityForCFamily(visibility)
                val method = "\n\n    $vis void $methodName() {\n        $extractedCode\n    }"
                val call = "$methodName();"
                Pair(method, call)
            }
            
            // Ruby
            language.contains("ruby") || fileExtension == "rb" -> {
                val vis = if (visibility == "private") "private\n  " else ""
                val method = "\n\n  ${vis}def $methodName\n    $extractedCode\n  end"
                val call = "$methodName"
                Pair(method, call)
            }
            
            // PHP
            language.contains("php") || fileExtension == "php" -> {
                val vis = mapVisibilityForPHP(visibility)
                val method = "\n\n    $vis function $methodName() {\n        $extractedCode\n    }"
                val call = "\$this->$methodName();"
                Pair(method, call)
            }
            
            // Scala
            language.contains("scala") || fileExtension == "scala" -> {
                val vis = if (visibility == "public") "" else visibility
                val method = "\n\n  ${vis}def $methodName(): Unit = {\n    $extractedCode\n  }"
                val call = "$methodName()"
                Pair(method, call)
            }
            
            // Rust
            language.contains("rust") || fileExtension == "rs" -> {
                val vis = if (visibility == "public") "pub " else ""
                val method = "\n\n${vis}fn $methodName() {\n    $extractedCode\n}"
                val call = "$methodName();"
                Pair(method, call)
            }
            
            // Swift
            language.contains("swift") || fileExtension == "swift" -> {
                val vis = mapVisibilityForSwift(visibility)
                val method = "\n\n  $vis func $methodName() {\n    $extractedCode\n  }"
                val call = "$methodName()"
                Pair(method, call)
            }
            
            // Default to Java-like syntax
            else -> {
                val vis = mapVisibilityForJava(visibility)
                val method = "\n\n    $vis void $methodName() {\n        $extractedCode\n    }"
                val call = "$methodName();"
                Pair(method, call)
            }
        }
    }
    
    private fun mapVisibilityForKotlin(visibility: String): String {
        return when (visibility.lowercase()) {
            "private" -> "private"
            "protected" -> "protected"
            "public" -> "public"
            "internal" -> "internal"
            else -> "private"
        }
    }
    
    private fun mapVisibilityForJava(visibility: String): String {
        return when (visibility.lowercase()) {
            "private" -> "private"
            "protected" -> "protected"
            "public" -> "public"
            "internal" -> "private" // internal doesn't exist in Java
            else -> "private"
        }
    }
    
    private fun mapVisibilityForCFamily(visibility: String): String {
        return when (visibility.lowercase()) {
            "private" -> "private"
            "protected" -> "protected"
            "public" -> "public"
            else -> "private"
        }
    }
    
    private fun mapVisibilityForPHP(visibility: String): String {
        return when (visibility.lowercase()) {
            "private" -> "private"
            "protected" -> "protected"
            "public" -> "public"
            else -> "private"
        }
    }
    
    private fun mapVisibilityForSwift(visibility: String): String {
        return when (visibility.lowercase()) {
            "private" -> "private"
            "public" -> "public"
            "internal" -> "internal"
            "protected" -> "fileprivate" // Swift uses fileprivate instead of protected
            else -> "private"
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

