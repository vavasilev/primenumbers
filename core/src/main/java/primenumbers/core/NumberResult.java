/*  
 *  $ Id: $
 *  Copyright © 2016 Experian Ltd. All rights reserved.
 *  No Part of this file may be copied or distributed without the permission of Experian Ltd.
 */

package primenumbers.core;


public class NumberResult {
    private long number;
    private boolean isPrime;
    
    public NumberResult(long number, boolean isPrime) {
        super();
        this.number = number;
        this.isPrime = isPrime;
    }

    public long getNumber() {
        return number;
    }
    
    public boolean isPrime() {
        return isPrime;
    }
}
