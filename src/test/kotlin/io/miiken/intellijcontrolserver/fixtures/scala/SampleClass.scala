package testdata.scala

/**
 * Sample Scala class for testing refactoring operations.
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
  
  // RENAME THIS - Test renaming this method
  def oldMethodName(oldParameterName: Int): Int = {
    // RENAME THIS - Test renaming this local variable
    val oldVariableName = oldParameterName * 2
    oldVariableName + oldFieldName
  }
  
  /**
   * Calculate discount based on total amount.
   * Calls oldMethodName to apply bonus points.
   */
  def calculateDiscount(total: Double, membershipLevel: String): Double = {
    val baseDiscount = membershipLevel match {
      case "gold" => 0.20
      case "silver" => 0.10
      case "bronze" => 0.05
      case _ => 0.0
    }
    
    val volumeDiscount = if (total > 100.0) 0.05 else 0.0
    
    // Call oldMethodName to calculate bonus points
    val bonusPoints = oldMethodName(total.toInt)
    oldFieldName += bonusPoints
    
    Math.min(baseDiscount + volumeDiscount, 0.30)
  }
  
  /**
   * Process a purchase.
   * Calls calculateDiscount and oldMethodName.
   */
  def processPurchase(amount: Double, level: String): Double = {
    val discount = calculateDiscount(amount, level)
    val finalAmount = amount * (1.0 - discount)
    
    // Update oldFieldName through oldMethodName
    oldMethodName(amount.toInt)
    
    finalAmount
  }
}
