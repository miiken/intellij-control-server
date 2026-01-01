package io.miiken.intellijcontrolserver.util

import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@DisplayName("PsiUtils")
class PsiUtilsTest {
    
    @Test
    @DisplayName("ValidationResult Valid should be a singleton object")
    fun testValidationResultValid() {
        val result1 = PsiUtils.ValidationResult.Valid
        val result2 = PsiUtils.ValidationResult.Valid
        assertTrue(result1 === result2, "Valid should be the same instance")
    }
    
    @Test
    @DisplayName("ValidationResult Mismatch should contain expected and actual names")
    fun testValidationResultMismatch() {
        val result = PsiUtils.ValidationResult.Mismatch(
            expectedName = "oldName",
            actualName = "newName"
        )
        
        assertEquals("oldName", result.expectedName)
        assertEquals("newName", result.actualName)
    }
    
    @Test
    @DisplayName("ValidationResult Invalid should contain reason")
    fun testValidationResultInvalid() {
        val reason = "Element has no name"
        val result = PsiUtils.ValidationResult.Invalid(reason)
        
        assertEquals(reason, result.reason)
    }
}

