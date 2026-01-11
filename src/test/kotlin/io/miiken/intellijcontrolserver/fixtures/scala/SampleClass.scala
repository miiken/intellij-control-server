package testdata.scala

/**
 * Sample Scala class for testing refactoring operations.
 * 
 * This class includes:
 * - A method to rename (marked with RENAME THIS)
    
    val finalTotal = total + tax
    println(s"Total for $userId: $$$finalTotal")
    saveOrder(userId, finalTotal)
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
