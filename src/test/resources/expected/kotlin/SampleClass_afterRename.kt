package io.miiken.intellijcontrolserver.fixtures.kotlin

import io.micrometer.core.annotation.Timed
import org.slf4j.LoggerFactory

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
    @Timed("metrics.timer.newMethodName")
    fun newMethodName(oldParameterName: Int): Int {
        logger.info("newMethodName: $oldParameterName")
        // RENAME THIS - Test renaming this local variable
        val oldVariableName = oldParameterName * 2
        return oldVariableName + oldFieldName
    }

    /**
     * Calculate discount based on total amount.
     * Calls newMethodName to apply bonus points.
     */
    fun calculateDiscount(
        total: Double,
        membershipLevel: String,
    ): Double {
        val baseDiscount =
            when (membershipLevel) {
                "gold" -> 0.20
                "silver" -> 0.10
                "bronze" -> 0.05
                else -> 0.0
            }

        val volumeDiscount = if (total > 100.0) 0.05 else 0.0

        // Call newMethodName to calculate bonus points
        val bonusPoints = newMethodName(total.toInt())
        oldFieldName += bonusPoints

        return (baseDiscount + volumeDiscount).coerceAtMost(0.30)
    }

    /**
     * Process a purchase.
     * Calls calculateDiscount and newMethodName.
     */
    fun processPurchase(
        amount: Double,
        level: String,
    ): Double {
        val discount = calculateDiscount(amount, level)
        val finalAmount = amount * (1.0 - discount)

        // Update oldFieldName through newMethodName
        newMethodName(amount.toInt())

        return finalAmount
    }
}
