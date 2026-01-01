package io.miiken.intellijcontrolserver.util

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.util.PsiTreeUtil
import java.io.File

object PsiUtils {
    
    fun findPsiFile(project: Project, filePath: String): PsiFile? {
        return ReadAction.compute<PsiFile?, RuntimeException> {
            val absolutePath = resolveAbsolutePath(project, filePath)
            val virtualFile = LocalFileSystem.getInstance().findFileByPath(absolutePath) ?: return@compute null
            PsiManager.getInstance(project).findFile(virtualFile)
        }
    }
    
    fun calculateTextRange(
        psiFile: PsiFile,
        startLine: Int,
        endLine: Int,
        startColumn: Int? = null,
        endColumn: Int? = null
    ): TextRangeResult {
        return ReadAction.compute<TextRangeResult, RuntimeException> {
            val document = com.intellij.psi.PsiDocumentManager.getInstance(psiFile.project).getDocument(psiFile)
                ?: return@compute TextRangeResult.Error("No document found for file")
            
            if (startLine < 1 || startLine > document.lineCount) {
                return@compute TextRangeResult.Error("startLine $startLine is out of range (1-${document.lineCount})")
            }
            if (endLine < 1 || endLine > document.lineCount) {
                return@compute TextRangeResult.Error("endLine $endLine is out of range (1-${document.lineCount})")
            }
            if (startLine > endLine) {
                return@compute TextRangeResult.Error("startLine ($startLine) cannot be greater than endLine ($endLine)")
            }
            
            val startLineOffset = document.getLineStartOffset(startLine - 1)
            val endLineOffset = document.getLineEndOffset(endLine - 1)
            
            val startOffset = if (startColumn != null) {
                val offset = startLineOffset + startColumn
                if (offset > document.getLineEndOffset(startLine - 1)) {
                    return@compute TextRangeResult.Error("startColumn $startColumn is out of range for line $startLine")
                }
                offset
            } else {
                startLineOffset
            }
            
            val endOffset = if (endColumn != null) {
                val offset = document.getLineStartOffset(endLine - 1) + endColumn
                if (offset > endLineOffset) {
                    return@compute TextRangeResult.Error("endColumn $endColumn is out of range for line $endLine")
                }
                offset
            } else {
                endLineOffset
            }
            
            if (startOffset >= endOffset) {
                return@compute TextRangeResult.Error("Start position must be before end position")
            }
            
            TextRangeResult.Success(startOffset, endOffset)
        }
    }
    
    fun findNamedElementByLineAndName(psiFile: PsiFile, line: Int, name: String): FindElementResult {
        return ReadAction.compute<FindElementResult, RuntimeException> {
            val document = com.intellij.psi.PsiDocumentManager.getInstance(psiFile.project).getDocument(psiFile)
                ?: return@compute FindElementResult.InvalidLine("No document found for file")
            
            if (line < 1 || line > document.lineCount) {
                return@compute FindElementResult.InvalidLine("Line $line is out of range (1-${document.lineCount})")
            }
            
            val lineStartOffset = document.getLineStartOffset(line - 1)
            val lineEndOffset = document.getLineEndOffset(line - 1)
            
            val elementsOnLine = mutableListOf<PsiNamedElement>()
            
            for (offset in lineStartOffset until lineEndOffset) {
                val element = psiFile.findElementAt(offset)
                if (element != null) {
                    val namedElement = PsiTreeUtil.getParentOfType(element, PsiNamedElement::class.java, false)
                    if (namedElement != null && namedElement.name == name) {
                        if (!elementsOnLine.contains(namedElement)) {
                            elementsOnLine.add(namedElement)
                        }
                    }
                }
            }
            
            when {
                elementsOnLine.isEmpty() -> FindElementResult.NotFound("No element named '$name' found on line $line")
                elementsOnLine.size > 1 -> FindElementResult.Ambiguous("Multiple elements named '$name' found on line $line. Please use a more specific location.")
                else -> FindElementResult.Found(elementsOnLine.first())
            }
        }
    }
    
    sealed class FindElementResult {
        data class Found(val element: PsiNamedElement) : FindElementResult()
        data class NotFound(val message: String) : FindElementResult()
        data class Ambiguous(val message: String) : FindElementResult()
        data class InvalidLine(val message: String) : FindElementResult()
    }
    
    sealed class TextRangeResult {
        data class Success(val startOffset: Int, val endOffset: Int) : TextRangeResult()
        data class Error(val message: String) : TextRangeResult()
    }
    
    fun findElementAtOffset(psiFile: PsiFile, offset: Int): PsiElement? {
        return ReadAction.compute<PsiElement?, RuntimeException> {
            if (offset < 0 || offset >= psiFile.textLength) {
                return@compute null
            }
            psiFile.findElementAt(offset)
        }
    }
    
    fun findNamedElementAtOffset(psiFile: PsiFile, offset: Int): PsiNamedElement? {
        return ReadAction.compute<PsiNamedElement?, RuntimeException> {
            val element = findElementAtOffset(psiFile, offset) ?: return@compute null
            PsiTreeUtil.getParentOfType(element, PsiNamedElement::class.java, false)
        }
    }
    
    fun validateElementName(element: PsiNamedElement, expectedName: String): ValidationResult {
        return ReadAction.compute<ValidationResult, RuntimeException> {
            val actualName = element.name
            if (actualName == null) {
                return@compute ValidationResult.Invalid("Element has no name")
            }
            if (actualName != expectedName) {
                return@compute ValidationResult.Mismatch(
                    expectedName = expectedName,
                    actualName = actualName
                )
            }
            ValidationResult.Valid
        }
    }
    
    fun getElementName(element: PsiNamedElement): String? {
        return ReadAction.compute<String?, RuntimeException> {
            element.name
        }
    }
    
    fun getElementText(element: PsiElement): String {
        return ReadAction.compute<String, RuntimeException> {
            element.text
        }
    }
    
    fun getTextRange(psiFile: PsiFile, startOffset: Int, endOffset: Int): String? {
        return ReadAction.compute<String?, RuntimeException> {
            if (startOffset < 0 || endOffset > psiFile.textLength || startOffset >= endOffset) {
                return@compute null
            }
            psiFile.text.substring(startOffset, endOffset)
        }
    }
    
    fun isMethod(element: PsiNamedElement): Boolean {
        return ReadAction.compute<Boolean, RuntimeException> {
            val elementClass = element.javaClass.name
            elementClass.contains("KtNamedFunction") ||
            elementClass.contains("PsiMethod") ||
            elementClass.contains("KtFunction")
        }
    }
    
    private fun resolveAbsolutePath(project: Project, filePath: String): String {
        val file = File(filePath)
        return if (file.isAbsolute) {
            filePath
        } else {
            File(project.basePath, filePath).absolutePath
        }
    }
    
    sealed class ValidationResult {
        object Valid : ValidationResult()
        data class Mismatch(val expectedName: String, val actualName: String) : ValidationResult()
        data class Invalid(val reason: String) : ValidationResult()
    }
}

