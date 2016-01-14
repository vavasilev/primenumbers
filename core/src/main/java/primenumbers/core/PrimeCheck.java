/*  
 *  $ Id: $
 *  Copyright © 2016 Experian Ltd. All rights reserved.
 *  No Part of this file may be copied or distributed without the permission of Experian Ltd.
 */

package primenumbers.core;

import java.util.concurrent.Callable;


public class PrimeCheck implements Callable<NumberResult> {

    private long number;
    private PrimeNumberChecker checker;
    
    public PrimeCheck(long number, PrimeNumberChecker checker) {
        this.number = number;
        this.checker = checker;
    }

    @Override
    public NumberResult call() throws Exception {
        boolean isPrime = checker.isPrime(number);
        return new NumberResult(number, isPrime);
    }

}
