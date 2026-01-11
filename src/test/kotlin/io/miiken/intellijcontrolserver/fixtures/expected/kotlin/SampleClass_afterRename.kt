package io.miiken.intellijcontrolserver.fixtures.kotlin

/**
 * Sample Kotlin class for testing refactoring operations.
 * 
 * This class includes:
 * - A method to rename (marked with RENAME THIS)
 * - A method with extractable code (marked with EXTRACT START/END)
 */
class SampleCalculator {
    
    // RENAME THIS - Test renaming this method
    fun newMethodName(value: Int): Int {
        return value * 2
    }
    
    /**
     * Process an order with multiple items.
     * Contains code that should be extracted into a separate method.
     */
    fun processOrder(userId: String, items: List<String>) {
        println("Processing order for user: $userId")
        
        // EXTRACT START (lines 22-27)
        var total = 0.0
        for (item in items) {
            val price = getPrice(item)
            total += price
        }
        val tax = total * 0.1
        // EXTRACT END
        
        val finalTotal = total + tax
        println("Total for $userId: $finalTotal")
        saveOrder(userId, finalTotal)
    }
    
    private fun getPrice(item: String): Double {
        return when (item) {
            "book" -> 15.99
            "pen" -> 2.50
            "notebook" -> 5.99
            else -> 10.0
        }
    }
    
    private fun saveOrder(userId: String, total: Double) {
        println("Order saved: $userId -> $$total")
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
