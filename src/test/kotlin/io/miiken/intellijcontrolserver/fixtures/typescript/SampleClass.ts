/**
 * Sample TypeScript class for testing refactoring operations.
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
    private oldFieldName: number = 10;
    
    // RENAME THIS - Test renaming this method
    oldMethodName(oldParameterName: number): number {
        // RENAME THIS - Test renaming this local variable
        const oldVariableName = oldParameterName * 2;
        return oldVariableName + this.oldFieldName;
    }
    
    /**
     * Calculate discount based on total amount.
     * Calls oldMethodName to apply bonus points.
     */
    calculateDiscount(total: number, membershipLevel: string): number {
        let baseDiscount: number;
        switch (membershipLevel) {
            case 'gold':
                baseDiscount = 0.20;
                break;
            case 'silver':
                baseDiscount = 0.10;
                break;
            case 'bronze':
                baseDiscount = 0.05;
                break;
            default:
                baseDiscount = 0.0;
        }
        
        const volumeDiscount = total > 100.0 ? 0.05 : 0.0;
        
        // Call oldMethodName to calculate bonus points
        const bonusPoints = this.oldMethodName(Math.floor(total));
        this.oldFieldName += bonusPoints;
        
        return Math.min(baseDiscount + volumeDiscount, 0.30);
    }
    
    /**
     * Process a purchase.
     * Calls calculateDiscount and oldMethodName.
     */
    processPurchase(amount: number, level: string): number {
        const discount = this.calculateDiscount(amount, level);
        const finalAmount = amount * (1.0 - discount);
        
        // Update oldFieldName through oldMethodName
        this.oldMethodName(Math.floor(amount));
        
        return finalAmount;
    }
}

export default SampleCalculator;
