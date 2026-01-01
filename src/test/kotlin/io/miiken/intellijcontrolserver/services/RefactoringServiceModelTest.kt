package io.miiken.intellijcontrolserver.services

import io.miiken.intellijcontrolserver.models.ExtractMethodRequest
import io.miiken.intellijcontrolserver.models.RefactoringError
import io.miiken.intellijcontrolserver.models.RefactoringResult
import io.miiken.intellijcontrolserver.models.RenameRequest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@DisplayName("RefactoringService Data Models")
class RefactoringServiceModelTest {
    
    @Test
    @DisplayName("RenameRequest should hold all required fields")
    fun testRenameRequestCreation() {
        val request = RenameRequest(
            filePath = "src/Test.kt",
            offset = 100,
            oldName = "oldName",
            newName = "newName",
            searchInComments = true
        )
        
        assertEquals("src/Test.kt", request.filePath)
        assertEquals(100, request.offset)
        assertEquals("oldName", request.oldName)
        assertEquals("newName", request.newName)
        assertTrue(request.searchInComments)
    }
    
    @Test
    @DisplayName("RenameRequest should default searchInComments to false")
    fun testRenameRequestDefaultSearchInComments() {
        val request = RenameRequest(
            filePath = "src/Test.kt",
            offset = 100,
            oldName = "oldName",
            newName = "newName"
        )
        
        assertFalse(request.searchInComments)
    }
    
    @Test
    @DisplayName("ExtractMethodRequest should hold all required fields")
    fun testExtractMethodRequestCreation() {
        val request = ExtractMethodRequest(
            filePath = "src/Test.kt",
            startOffset = 100,
            endOffset = 200,
            methodName = "newMethod",
            parameterOrder = listOf("param1", "param2"),
            visibility = "public"
        )
        
        assertEquals("src/Test.kt", request.filePath)
        assertEquals(100, request.startOffset)
        assertEquals(200, request.endOffset)
        assertEquals("newMethod", request.methodName)
        assertEquals(listOf("param1", "param2"), request.parameterOrder)
        assertEquals("public", request.visibility)
    }
    
    @Test
    @DisplayName("ExtractMethodRequest should default to null parameterOrder and private visibility")
    fun testExtractMethodRequestDefaults() {
        val request = ExtractMethodRequest(
            filePath = "src/Test.kt",
            startOffset = 100,
            endOffset = 200,
            methodName = "newMethod"
        )
        
        assertEquals(null, request.parameterOrder)
        assertEquals("private", request.visibility)
    }
    
    @Test
    @DisplayName("RefactoringResult should indicate success with files changed")
    fun testRefactoringResultSuccess() {
        val result = RefactoringResult(
            success = true,
            filesChanged = listOf("file1.kt", "file2.kt"),
            changesCount = 5
        )
        
        assertTrue(result.success)
        assertEquals(listOf("file1.kt", "file2.kt"), result.filesChanged)
        assertEquals(5, result.changesCount)
        assertEquals(null, result.error)
    }
    
    @Test
    @DisplayName("RefactoringResult should indicate failure with error details")
    fun testRefactoringResultFailure() {
        val error = RefactoringError(
            code = "FILE_NOT_FOUND",
            message = "File not found",
            details = mapOf("path" to "src/Test.kt")
        )
        
        val result = RefactoringResult(
            success = false,
            error = error
        )
        
        assertFalse(result.success)
        assertEquals(emptyList(), result.filesChanged)
        assertEquals(0, result.changesCount)
        assertNotNull(result.error)
        assertEquals("FILE_NOT_FOUND", result.error?.code)
        assertEquals("File not found", result.error?.message)
    }
    
    @Test
    @DisplayName("RefactoringError should contain code and message")
    fun testRefactoringErrorCreation() {
        val error = RefactoringError(
            code = "NAME_COLLISION",
            message = "Name already exists",
            details = mapOf("existingName" to "TestClass")
        )
        
        assertEquals("NAME_COLLISION", error.code)
        assertEquals("Name already exists", error.message)
        assertEquals(mapOf("existingName" to "TestClass"), error.details)
    }
    
}

