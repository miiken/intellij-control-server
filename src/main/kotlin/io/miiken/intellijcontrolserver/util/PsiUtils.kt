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

