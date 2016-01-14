package primenumbers.core;

import java.util.concurrent.Future;

public class FutureAndNumber implements Comparable<FutureAndNumber> {

	private Future<NumberResult> future;
	private long number;
	private int attempt;
	
	public FutureAndNumber(Future<NumberResult> future, long number) {
		this(future, number, 1);
	}
	
	public FutureAndNumber(Future<NumberResult> future, long number, int attempt) {
		super();
		this.future = future;
		this.number = number;
		this.attempt = attempt;
	}

	public Future<NumberResult> getFuture() {
		return future;
	}

	public long getNumber() {
		return number;
	}

	public int getAttempt() {
		return attempt;
	}

	@Override
	public int compareTo(FutureAndNumber o) {
		return Long.valueOf(number).compareTo(Long.valueOf(o.number));
	}

}
