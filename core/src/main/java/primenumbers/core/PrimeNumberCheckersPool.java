package primenumbers.core;

import java.util.ArrayList;
import java.util.List;

public class PrimeNumberCheckersPool {
	private int size;
	private PrimeNumberCheckerFactory primeNumberCheckerFactory;
	private CheckerSelectionStrategy checkerSelectionStrategy;
	private List<PrimeNumberChecker> primeNumberCheckers = new ArrayList<PrimeNumberChecker>();
	
	public PrimeNumberCheckersPool(int size, PrimeNumberCheckerFactory primeNumberCheckerFactory,
			CheckerSelectionStrategy checkerSelectionStrategy) {
		this.size = size;
		this.primeNumberCheckerFactory = primeNumberCheckerFactory;
		this.checkerSelectionStrategy = checkerSelectionStrategy;
	}
	
	public void init() {
		for(int i=0; i<size; i++) {
			primeNumberCheckers.add(primeNumberCheckerFactory.createPrimeNumberChecker());
		}
	}
	
	public PrimeNumberChecker getPrimeNumberChecker() {
		return checkerSelectionStrategy.selectChecker(primeNumberCheckers);
	}
}
