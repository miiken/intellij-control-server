package io.miiken.intellijcontrolserver.server.controllers

import io.miiken.intellijcontrolserver.models.ExtractMethodRequest
import io.miiken.intellijcontrolserver.models.RenameRequest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@DisplayName("RefactoringController")
class RefactoringControllerTest {
    
    private val controller = RefactoringController()
    
    // RenameRequest validation tests
    
    @Test
    @DisplayName("Should throw exception for empty file path in rename")
    fun testRenameEmptyFilePath() {
        val request = RenameRequest(
            filePath = "",
            offset = 100,
            oldName = "foo",
            newName = "bar",
            searchInComments = false
        )
        
        val exception = assertThrows<RefactoringController.RefactoringException> {
            controller.validateRenameRequest(request)
        }
        
        assertEquals("INVALID_REQUEST", exception.code)
        assertTrue(exception.message.contains("filePath"))
    }
    
    @Test
    @DisplayName("Should throw exception for blank file path in rename")
    fun testRenameBlankFilePath() {
        val request = RenameRequest(
            filePath = "   ",
            offset = 100,
            oldName = "foo",
            newName = "bar",
            searchInComments = false
        )
        
        val exception = assertThrows<RefactoringController.RefactoringException> {
            controller.validateRenameRequest(request)
        }
        
        assertEquals("INVALID_REQUEST", exception.code)
    }
    
    @Test
    @DisplayName("Should throw exception for negative offset in rename")
    fun testRenameNegativeOffset() {
        val request = RenameRequest(
            filePath = "/path/to/file.kt",
            offset = -1,
            oldName = "foo",
            newName = "bar",
            searchInComments = false
        )
        
        val exception = assertThrows<RefactoringController.RefactoringException> {
            controller.validateRenameRequest(request)
        }
        
        assertEquals("INVALID_REQUEST", exception.code)
        assertTrue(exception.message.contains("offset"))
    }
    
    @Test
    @DisplayName("Should throw exception for empty oldName in rename")
    fun testRenameEmptyOldName() {
        val request = RenameRequest(
            filePath = "/path/to/file.kt",
            offset = 100,
            oldName = "",
            newName = "bar",
            searchInComments = false
        )
        
        val exception = assertThrows<RefactoringController.RefactoringException> {
            controller.validateRenameRequest(request)
        }
        
        assertEquals("INVALID_REQUEST", exception.code)
        assertTrue(exception.message.contains("oldName"))
    }
    
    @Test
    @DisplayName("Should throw exception for empty newName in rename")
    fun testRenameEmptyNewName() {
        val request = RenameRequest(
            filePath = "/path/to/file.kt",
            offset = 100,
            oldName = "foo",
            newName = "",
            searchInComments = false
        )
        
        val exception = assertThrows<RefactoringController.RefactoringException> {
            controller.validateRenameRequest(request)
        }
        
        assertEquals("INVALID_REQUEST", exception.code)
        assertTrue(exception.message.contains("newName"))
    }
    
    @Test
    @DisplayName("Should throw exception when oldName equals newName")
    fun testRenameSameNames() {
        val request = RenameRequest(
            filePath = "/path/to/file.kt",
            offset = 100,
            oldName = "foo",
            newName = "foo",
            searchInComments = false
        )
        
        val exception = assertThrows<RefactoringController.RefactoringException> {
            controller.validateRenameRequest(request)
        }
        
        assertEquals("INVALID_REQUEST", exception.code)
        assertTrue(exception.message.contains("same"))
    }
    
    // ExtractMethodRequest validation tests
    
    @Test
    @DisplayName("Should throw exception for empty file path in extract method")
    fun testExtractMethodEmptyFilePath() {
        val request = ExtractMethodRequest(
            filePath = "",
            startOffset = 100,
            endOffset = 200,
            methodName = "extractedMethod"
        )
        
        val exception = assertThrows<RefactoringController.RefactoringException> {
            controller.validateExtractMethodRequest(request)
        }
        
        assertEquals("INVALID_REQUEST", exception.code)
        assertTrue(exception.message.contains("filePath"))
    }
    
    @Test
    @DisplayName("Should throw exception for negative startOffset")
    fun testExtractMethodNegativeStartOffset() {
        val request = ExtractMethodRequest(
            filePath = "/path/to/file.kt",
            startOffset = -1,
            endOffset = 200,
            methodName = "extractedMethod"
        )
        
        val exception = assertThrows<RefactoringController.RefactoringException> {
            controller.validateExtractMethodRequest(request)
        }
        
        assertEquals("INVALID_REQUEST", exception.code)
        assertTrue(exception.message.contains("startOffset"))
    }
    
    @Test
    @DisplayName("Should throw exception when endOffset <= startOffset")
    fun testExtractMethodInvalidOffsetRange() {
        val request = ExtractMethodRequest(
            filePath = "/path/to/file.kt",
            startOffset = 200,
            endOffset = 200,
            methodName = "extractedMethod"
        )
        
        val exception = assertThrows<RefactoringController.RefactoringException> {
            controller.validateExtractMethodRequest(request)
        }
        
        assertEquals("INVALID_REQUEST", exception.code)
        assertTrue(exception.message.contains("endOffset"))
    }
    
    @Test
    @DisplayName("Should throw exception for empty method name")
    fun testExtractMethodEmptyName() {
        val request = ExtractMethodRequest(
            filePath = "/path/to/file.kt",
            startOffset = 100,
            endOffset = 200,
            methodName = ""
        )
        
        val exception = assertThrows<RefactoringController.RefactoringException> {
            controller.validateExtractMethodRequest(request)
        }
        
        assertEquals("INVALID_REQUEST", exception.code)
        assertTrue(exception.message.contains("methodName"))
    }
    
    @Test
    @DisplayName("Should throw exception for invalid method name with spaces")
    fun testExtractMethodInvalidNameWithSpaces() {
        val request = ExtractMethodRequest(
            filePath = "/path/to/file.kt",
            startOffset = 100,
            endOffset = 200,
            methodName = "invalid name"
        )
        
        val exception = assertThrows<RefactoringController.RefactoringException> {
            controller.validateExtractMethodRequest(request)
        }
        
        assertEquals("INVALID_REQUEST", exception.code)
        assertTrue(exception.message.contains("methodName"))
    }
    
    @Test
    @DisplayName("Should throw exception for method name starting with number")
    fun testExtractMethodNameStartsWithNumber() {
        val request = ExtractMethodRequest(
            filePath = "/path/to/file.kt",
            startOffset = 100,
            endOffset = 200,
            methodName = "1invalidName"
        )
        
        val exception = assertThrows<RefactoringController.RefactoringException> {
            controller.validateExtractMethodRequest(request)
        }
        
        assertEquals("INVALID_REQUEST", exception.code)
        assertTrue(exception.message.contains("methodName"))
    }
    
    // ProjectNotFoundException tests
    
    @Test
    @DisplayName("ProjectNotFoundException should have correct message")
    fun testProjectNotFoundException() {
        val exception = RefactoringController.ProjectNotFoundException("test-project")
        
        assertTrue(exception.message?.contains("test-project") == true)
    }
    
    // RefactoringException tests
    
    @Test
    @DisplayName("RefactoringException should have default status code 400")
    fun testRefactoringExceptionDefaultStatusCode() {
        val exception = RefactoringController.RefactoringException("TEST_ERROR", "Test message")
        
        assertEquals(400, exception.statusCode)
    }
    
    @Test
    @DisplayName("RefactoringException should allow custom status code")
    fun testRefactoringExceptionCustomStatusCode() {
        val exception = RefactoringController.RefactoringException(
            "TEST_ERROR",
            "Test message",
            null,
            404
        )
        
        assertEquals(404, exception.statusCode)
    }
}

