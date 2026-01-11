/**
 * Sample TypeScript class for testing refactoring operations.
 * 
 * This class includes:
 * - A method to rename (marked with RENAME THIS)
        
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
