package io.miiken.intellijcontrolserver.fixtures.kotlin

import org.slf4j.LoggerFactory
import io.micrometer.core.annotation.Timed

/**
 * Sample Kotlin class for testing refactoring operations.
 * 
 * This class includes test cases for renaming:
 * - Methods
 * - Fields
 * - Parameters
 * - Local variables
 * - Classes
 */
class SampleCalculator {
    
    // RENAME THIS - Test renaming this field
    private var oldFieldName: Int = 10

    private val logger = LoggerFactory.getLogger(javaClass)
    
    // RENAME THIS - Test renaming this method
    @Timed("metrics.timer.oldMethodName")
    fun oldMethodName(oldParameterName: Int): Int {
        logger.info("oldMethodName: $oldParameterName")
        // RENAME THIS - Test renaming this local variable
        val oldVariableName = oldParameterName * 2
        return oldVariableName + oldFieldName
    }
    
    /**
     * Calculate discount based on total amount.
     * Calls oldMethodName to apply bonus points.
     */
    fun calculateDiscount(total: Double, membershipLevel: String): Double {
        val baseDiscount = when (membershipLevel) {
            "gold" -> 0.20
            "silver" -> 0.10
            "bronze" -> 0.05
            else -> 0.0
        }
        
        val volumeDiscount = if (total > 100.0) 0.05 else 0.0
        
        // Call oldMethodName to calculate bonus points
        val bonusPoints = oldMethodName(total.toInt())
        oldFieldName += bonusPoints
        
        return (baseDiscount + volumeDiscount).coerceAtMost(0.30)
    }
    
    /**
     * Process a purchase.
     * Calls calculateDiscount and oldMethodName.
     */
    fun processPurchase(amount: Double, level: String): Double {
        val discount = calculateDiscount(amount, level)
        val finalAmount = amount * (1.0 - discount)
        
        // Update oldFieldName through oldMethodName
        oldMethodName(amount.toInt())
        
        return finalAmount
    }
}
