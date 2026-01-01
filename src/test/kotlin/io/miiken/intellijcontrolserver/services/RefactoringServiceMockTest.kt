package io.miiken.intellijcontrolserver.services

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiNamedElement
import io.miiken.intellijcontrolserver.models.RenameRequest
import io.miiken.intellijcontrolserver.util.PsiUtils
import io.mockk.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@DisplayName("RefactoringService with Mocks")
class RefactoringServiceMockTest {
    
    private lateinit var mockProject: Project
    private lateinit var mockPsiFile: PsiFile
    private lateinit var mockPsiElement: PsiNamedElement
    
    @BeforeEach
    fun setUp() {
        mockProject = mockk(relaxed = true)
        mockPsiFile = mockk(relaxed = true)
        mockPsiElement = mockk(relaxed = true)
        
        mockkObject(PsiUtils)
    }
    
    @AfterEach
    fun tearDown() {
        unmockkAll()
    }
    
    @Test
    fun `should return FILE_NOT_FOUND when file does not exist`() {
        val request = RenameRequest(
            filePath = "/non/existent/file.kt",
            offset = 100,
            oldName = "oldName",
            newName = "newName"
        )
        
        every { PsiUtils.findPsiFile(mockProject, request.filePath) } returns null
        
        val result = RefactoringService.rename(mockProject, request)
        
        assertFalse(result.success)
        assertNotNull(result.error)
        assertEquals("FILE_NOT_FOUND", result.error?.code)
        assertTrue(result.error?.message?.contains("File not found") == true)
        assertTrue(result.error?.message?.contains(request.filePath) == true)
        
        verify { PsiUtils.findPsiFile(mockProject, request.filePath) }
    }
    
    @Test
    fun `should return INVALID_ELEMENT when no named element found at offset`() {
        val request = RenameRequest(
            filePath = "/path/to/file.kt",
            offset = 999,
            oldName = "oldName",
            newName = "newName"
        )
        
        every { PsiUtils.findPsiFile(mockProject, request.filePath) } returns mockPsiFile
        every { PsiUtils.findNamedElementAtOffset(mockPsiFile, request.offset) } returns null
        
        val result = RefactoringService.rename(mockProject, request)
        
        assertFalse(result.success)
        assertNotNull(result.error)
        assertEquals("INVALID_ELEMENT", result.error?.code)
        assertTrue(result.error?.message?.contains("No renameable element found") == true)
        assertTrue(result.error?.message?.contains(request.offset.toString()) == true)
        
        verify { PsiUtils.findPsiFile(mockProject, request.filePath) }
        verify { PsiUtils.findNamedElementAtOffset(mockPsiFile, request.offset) }
    }
    
    @Test
    fun `should return NAME_MISMATCH when element name does not match expected name`() {
        val request = RenameRequest(
            filePath = "/path/to/file.kt",
            offset = 100,
            oldName = "expectedName",
            newName = "newName"
        )
        
        val mismatchResult = PsiUtils.ValidationResult.Mismatch(
            expectedName = "expectedName",
            actualName = "actualName"
        )
        
        every { PsiUtils.findPsiFile(mockProject, request.filePath) } returns mockPsiFile
        every { PsiUtils.findNamedElementAtOffset(mockPsiFile, request.offset) } returns mockPsiElement
        every { PsiUtils.validateElementName(mockPsiElement, request.oldName) } returns mismatchResult
        
        val result = RefactoringService.rename(mockProject, request)
        
        assertFalse(result.success)
        assertNotNull(result.error)
        assertEquals("NAME_MISMATCH", result.error?.code)
        assertTrue(result.error?.message?.contains("Expected 'expectedName'") == true)
        assertTrue(result.error?.message?.contains("found 'actualName'") == true)
        
        assertNotNull(result.error?.details)
        assertEquals("expectedName", result.error?.details?.get("expectedName"))
        assertEquals("actualName", result.error?.details?.get("actualName"))
        assertEquals(request.offset, result.error?.details?.get("offset"))
        
        verify { PsiUtils.findPsiFile(mockProject, request.filePath) }
        verify { PsiUtils.findNamedElementAtOffset(mockPsiFile, request.offset) }
        verify { PsiUtils.validateElementName(mockPsiElement, request.oldName) }
    }
    
    @Test
    fun `should return INVALID_ELEMENT when validation fails with reason`() {
        val request = RenameRequest(
            filePath = "/path/to/file.kt",
            offset = 100,
            oldName = "oldName",
            newName = "newName"
        )
        
        val invalidResult = PsiUtils.ValidationResult.Invalid("Element has no name")
        
        every { PsiUtils.findPsiFile(mockProject, request.filePath) } returns mockPsiFile
        every { PsiUtils.findNamedElementAtOffset(mockPsiFile, request.offset) } returns mockPsiElement
        every { PsiUtils.validateElementName(mockPsiElement, request.oldName) } returns invalidResult
        
        val result = RefactoringService.rename(mockProject, request)
        
        assertFalse(result.success)
        assertNotNull(result.error)
        assertEquals("INVALID_ELEMENT", result.error?.code)
        assertEquals("Element has no name", result.error?.message)
        
        verify { PsiUtils.findPsiFile(mockProject, request.filePath) }
        verify { PsiUtils.findNamedElementAtOffset(mockPsiFile, request.offset) }
        verify { PsiUtils.validateElementName(mockPsiElement, request.oldName) }
    }
    
    @Test
    fun `should return REFACTORING_FAILED when unexpected exception occurs`() {
        val request = RenameRequest(
            filePath = "/path/to/file.kt",
            offset = 100,
            oldName = "oldName",
            newName = "newName"
        )
        
        every { PsiUtils.findPsiFile(mockProject, request.filePath) } throws RuntimeException("Unexpected error")
        
        val result = RefactoringService.rename(mockProject, request)
        
        assertFalse(result.success)
        assertNotNull(result.error)
        assertEquals("REFACTORING_FAILED", result.error?.code)
        assertTrue(result.error?.message?.contains("Rename failed") == true)
        assertTrue(result.error?.message?.contains("Unexpected error") == true)
        assertNotNull(result.error?.details)
        assertEquals("RuntimeException", result.error?.details?.get("exception"))
        
        verify { PsiUtils.findPsiFile(mockProject, request.filePath) }
    }
    
    @Test
    fun `should handle validation with special characters in names`() {
        val request = RenameRequest(
            filePath = "/path/to/file.kt",
            offset = 100,
            oldName = "name_with_underscore",
            newName = "new-name-with-dash"
        )
        
        val mismatchResult = PsiUtils.ValidationResult.Mismatch(
            expectedName = "name_with_underscore",
            actualName = "name-with-dash"
        )
        
        every { PsiUtils.findPsiFile(mockProject, request.filePath) } returns mockPsiFile
        every { PsiUtils.findNamedElementAtOffset(mockPsiFile, request.offset) } returns mockPsiElement
        every { PsiUtils.validateElementName(mockPsiElement, request.oldName) } returns mismatchResult
        
        val result = RefactoringService.rename(mockProject, request)
        
        assertFalse(result.success)
        assertEquals("NAME_MISMATCH", result.error?.code)
        assertTrue(result.error?.message?.contains("name_with_underscore") == true)
        assertTrue(result.error?.message?.contains("name-with-dash") == true)
    }
    
    @Test
    fun `should include file path in FILE_NOT_FOUND error for debugging`() {
        val request = RenameRequest(
            filePath = "/very/long/path/to/some/deeply/nested/file.kt",
            offset = 100,
            oldName = "oldName",
            newName = "newName"
        )
        
        every { PsiUtils.findPsiFile(mockProject, request.filePath) } returns null
        
        val result = RefactoringService.rename(mockProject, request)
        
        assertFalse(result.success)
        assertEquals("FILE_NOT_FOUND", result.error?.code)
        assertTrue(result.error?.message?.contains("/very/long/path/to/some/deeply/nested/file.kt") == true)
    }
    
    @Test
    fun `should handle null pointer exceptions gracefully`() {
        val request = RenameRequest(
            filePath = "/path/to/file.kt",
            offset = 100,
            oldName = "oldName",
            newName = "newName"
        )
        
        every { PsiUtils.findPsiFile(mockProject, request.filePath) } throws NullPointerException("Null project component")
        
        val result = RefactoringService.rename(mockProject, request)
        
        assertFalse(result.success)
        assertEquals("REFACTORING_FAILED", result.error?.code)
        assertEquals("NullPointerException", result.error?.details?.get("exception"))
    }
    
    @Test
    fun `should preserve offset in NAME_MISMATCH error details`() {
        val request = RenameRequest(
            filePath = "/path/to/file.kt",
            offset = 12345,
            oldName = "oldName",
            newName = "newName"
        )
        
        val mismatchResult = PsiUtils.ValidationResult.Mismatch(
            expectedName = "oldName",
            actualName = "differentName"
        )
        
        every { PsiUtils.findPsiFile(mockProject, request.filePath) } returns mockPsiFile
        every { PsiUtils.findNamedElementAtOffset(mockPsiFile, request.offset) } returns mockPsiElement
        every { PsiUtils.validateElementName(mockPsiElement, request.oldName) } returns mismatchResult
        
        val result = RefactoringService.rename(mockProject, request)
        
        assertFalse(result.success)
        assertEquals(12345, result.error?.details?.get("offset"))
    }
    
    @Test
    fun `should verify all PsiUtils methods called in correct order`() {
        val request = RenameRequest(
            filePath = "/path/to/file.kt",
            offset = 100,
            oldName = "oldName",
            newName = "newName"
        )
        
        every { PsiUtils.findPsiFile(mockProject, request.filePath) } returns mockPsiFile
        every { PsiUtils.findNamedElementAtOffset(mockPsiFile, request.offset) } returns mockPsiElement
        every { PsiUtils.validateElementName(mockPsiElement, request.oldName) } returns PsiUtils.ValidationResult.Invalid("test")
        
        RefactoringService.rename(mockProject, request)
        
        verifyOrder {
            PsiUtils.findPsiFile(mockProject, request.filePath)
            PsiUtils.findNamedElementAtOffset(mockPsiFile, request.offset)
            PsiUtils.validateElementName(mockPsiElement, request.oldName)
        }
    }
    
    @Test
    fun `should not call validateElementName if findPsiFile returns null`() {
        val request = RenameRequest(
            filePath = "/path/to/file.kt",
            offset = 100,
            oldName = "oldName",
            newName = "newName"
        )
        
        every { PsiUtils.findPsiFile(mockProject, request.filePath) } returns null
        
        RefactoringService.rename(mockProject, request)
        
        verify { PsiUtils.findPsiFile(mockProject, request.filePath) }
        verify(exactly = 0) { PsiUtils.findNamedElementAtOffset(any(), any()) }
        verify(exactly = 0) { PsiUtils.validateElementName(any(), any()) }
    }
    
    @Test
    fun `should not call validateElementName if findNamedElementAtOffset returns null`() {
        val request = RenameRequest(
            filePath = "/path/to/file.kt",
            offset = 100,
            oldName = "oldName",
            newName = "newName"
        )
        
        every { PsiUtils.findPsiFile(mockProject, request.filePath) } returns mockPsiFile
        every { PsiUtils.findNamedElementAtOffset(mockPsiFile, request.offset) } returns null
        
        RefactoringService.rename(mockProject, request)
        
        verify { PsiUtils.findPsiFile(mockProject, request.filePath) }
        verify { PsiUtils.findNamedElementAtOffset(mockPsiFile, request.offset) }
        verify(exactly = 0) { PsiUtils.validateElementName(any(), any()) }
    }
}

