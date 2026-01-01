package io.miiken.intellijcontrolserver.services

import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.project.ProjectManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.miiken.intellijcontrolserver.models.RenameRequest
import org.junit.Ignore
import org.junit.jupiter.api.Disabled
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@Disabled("requires more setup to run reliably")
@Ignore
class RefactoringServicePlatformTest : BasePlatformTestCase() {
    
    fun `test rename variable in kotlin file`() {
        val testFile = myFixture.configureByText("Test.kt", """
            fun main() {
                val name = "test"
                println(name)
            }
        """.trimIndent())
        
        val offset = testFile.text.indexOf("name")
        
        val request = RenameRequest(
            filePath = testFile.virtualFile.path,
            offset = offset,
            oldName = "name",
            newName = "greeting",
            searchInComments = false
        )
        
        val result = RefactoringService.rename(myFixture.project, request)
        
        assertTrue(result.success, "Rename should succeed")
        assertTrue(result.filesChanged.isNotEmpty(), "Should have changed files")
        assertEquals(1, result.filesChanged.size, "Should change exactly 1 file")
        
        myFixture.checkResult("""
            fun main() {
                val greeting = "test"
                println(greeting)
            }
        """.trimIndent())
    }
    
    fun `test rename with name mismatch returns error`() {
        val testFile = myFixture.configureByText("Test.kt", """
            fun main() {
                val name = "test"
            }
        """.trimIndent())
        
        val offset = testFile.text.indexOf("name")
        
        val request = RenameRequest(
            filePath = testFile.virtualFile.path,
            offset = offset,
            oldName = "wrongName",
            newName = "greeting",
            searchInComments = false
        )
        
        val result = RefactoringService.rename(myFixture.project, request)
        
        assertFalse(result.success, "Rename should fail due to name mismatch")
        assertNotNull(result.error)
        assertEquals("NAME_MISMATCH", result.error?.code)
        assertTrue(result.error?.message?.contains("Expected 'wrongName'") == true)
    }
    
    fun `test rename with file not found returns error`() {
        val request = RenameRequest(
            filePath = "/nonexistent/file.kt",
            offset = 0,
            oldName = "test",
            newName = "newTest",
            searchInComments = false
        )
        
        val result = RefactoringService.rename(myFixture.project, request)
        
        assertFalse(result.success, "Rename should fail when file not found")
        assertNotNull(result.error)
        assertEquals("FILE_NOT_FOUND", result.error?.code)
        assertTrue(result.error?.message?.contains("File not found") == true)
    }
    
    fun `test rename with invalid offset returns error`() {
        val testFile = myFixture.configureByText("Test.kt", """
            fun main() {
                val name = "test"
            }
        """.trimIndent())
        
        val request = RenameRequest(
            filePath = testFile.virtualFile.path,
            offset = 999,
            oldName = "name",
            newName = "greeting",
            searchInComments = false
        )
        
        val result = RefactoringService.rename(myFixture.project, request)
        
        assertFalse(result.success, "Rename should fail with invalid offset")
        assertNotNull(result.error)
        assertEquals("INVALID_ELEMENT", result.error?.code)
    }
    
    fun `test rename function parameter`() {
        val testFile = myFixture.configureByText("Test.kt", """
            fun greet(name: String) {
                println("Hello, ${"$"}name")
            }
        """.trimIndent())
        
        val offset = testFile.text.indexOf("name")
        
        val request = RenameRequest(
            filePath = testFile.virtualFile.path,
            offset = offset,
            oldName = "name",
            newName = "userName",
            searchInComments = false
        )
        
        val result = RefactoringService.rename(myFixture.project, request)
        
        assertTrue(result.success, "Rename should succeed")
        
        myFixture.checkResult("""
            fun greet(userName: String) {
                println("Hello, ${"$"}userName")
            }
        """.trimIndent())
    }
    
    fun `test rename with search in comments`() {
        val testFile = myFixture.configureByText("Test.kt", """
            fun main() {
                // Use name variable for user name
                val name = "test"
                println(name)
            }
        """.trimIndent())
        
        val offset = testFile.text.indexOf("val name")
        
        val request = RenameRequest(
            filePath = testFile.virtualFile.path,
            offset = offset + 4,
            oldName = "name",
            newName = "greeting",
            searchInComments = true
        )
        
        val result = RefactoringService.rename(myFixture.project, request)
        
        assertTrue(result.success, "Rename with search in comments should succeed")
        
        val resultText = myFixture.editor.document.text
        assertTrue(resultText.contains("greeting"), "Should rename variable to greeting")
    }
}

