package io.miiken.intellijcontrolserver.fixtures.kotlin

/**
 * Sample Kotlin class for testing refactoring operations.
 * 
 * This class includes:
 * - A method to rename (marked with RENAME THIS)
 */
class SampleClass {
    
    // RENAME THIS - Test renaming this method
    fun newMethodName(value: Int): Int {
        return value * 2
    }
    
    /**
     * Calculate discount based on total amount.
     * This is another method that demonstrates Kotlin features.
     */
    fun calculateDiscount(total: Double, membershipLevel: String): Double {
        val baseDiscount = when (membershipLevel) {
            "gold" -> 0.20
            "silver" -> 0.10
            "bronze" -> 0.05
            else -> 0.0
        }
        
        val volumeDiscount = if (total > 100.0) 0.05 else 0.0
        return (baseDiscount + volumeDiscount).coerceAtMost(0.30)
    }
}
