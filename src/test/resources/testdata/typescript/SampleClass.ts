/**
 * Sample TypeScript class for testing refactoring operations.
 * 
 * This class includes:
 * - A method to rename (marked with RENAME THIS)
 * - A method with extractable code (marked with EXTRACT START/END)
 */
class SampleCalculator {
    
    // RENAME THIS - Test renaming this method
    oldMethodName(value: number): number {
        return value * 2;
    }
    
    /**
     * Process an order with multiple items.
     * Contains code that should be extracted into a separate method.
     */
    processOrder(userId: string, items: string[]): void {
        console.log(`Processing order for user: ${userId}`);
        
        // EXTRACT START (lines 23-28)
        let total: number = 0.0;
        for (const item of items) {
            const price = this.getPrice(item);
            total += price;
        }
        const tax: number = total * 0.1;
        // EXTRACT END
        
        const finalTotal = total + tax;
        console.log(`Total for ${userId}: $${finalTotal}`);
        this.saveOrder(userId, finalTotal);
    }
    
    private getPrice(item: string): number {
        switch (item) {
            case 'book':
                return 15.99;
            case 'pen':
                return 2.50;
            case 'notebook':
                return 5.99;
            default:
                return 10.0;
        }
    }
    
    private saveOrder(userId: string, total: number): void {
        console.log(`Order saved: ${userId} -> $${total}`);
    }
    
    /**
     * Calculate discount based on total amount.
     * This demonstrates TypeScript features with type annotations.
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
        return Math.min(baseDiscount + volumeDiscount, 0.30);
    }
}

export default SampleCalculator;
