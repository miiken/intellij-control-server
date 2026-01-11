package io.miiken.intellijcontrolserver.fixtures.kotlin

/**
 * Sample Kotlin class for testing refactoring operations.
 * 
 * This class includes:
 * - A method to rename (marked with RENAME THIS)
        
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
