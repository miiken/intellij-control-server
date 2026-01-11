package testdata.scala

/**
 * Sample Scala class for testing refactoring operations.
 * 
 * This class includes:
 * - A method to rename (marked with RENAME THIS)
 * - A method with extractable code (marked with EXTRACT START/END)
 */
class SampleCalculator {
  
  // RENAME THIS - Test renaming this method
  def oldMethodName(value: Int): Int = {
    value * 2
  }
  
  /**
   * Process an order with multiple items.
   * Contains code that should be extracted into a separate method.
   */
  def processOrder(userId: String, items: List[String]): Unit = {
    println(s"Processing order for user: $userId")
    
    // Code was extracted from here
    val (total, tax) = calculateOrderTotal(items)
    
    val finalTotal = total + tax
    println(s"Total for $userId: $$$finalTotal")
    saveOrder(userId, finalTotal)
  }
  
  private def calculateOrderTotal(items: List[String]): (Double, Double) = {
    var total = 0.0
    for (item <- items) {
      val price = getPrice(item)
      total += price
    }
    val tax = total * 0.1
    (total, tax)
  }
  
  private def getPrice(item: String): Double = item match {
    case "book" => 15.99
    case "pen" => 2.50
    case "notebook" => 5.99
    case _ => 10.0
  }
  
  private def saveOrder(userId: String, total: Double): Unit = {
    println(s"Order saved: $userId -> $$$total")
  }
  
  /**
   * Calculate discount based on total amount.
   * This demonstrates Scala features like pattern matching.
   */
  def calculateDiscount(total: Double, membershipLevel: String): Double = {
    val baseDiscount = membershipLevel match {
      case "gold" => 0.20
      case "silver" => 0.10
      case "bronze" => 0.05
      case _ => 0.0
    }
    
    val volumeDiscount = if (total > 100.0) 0.05 else 0.0
    Math.min(baseDiscount + volumeDiscount, 0.30)
  }
}
