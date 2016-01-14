package primenumbers.core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DistributedPrimeNumbersService implements NotificationChannel {
	
	private List<NumberResultListener> numberResultListeners = new ArrayList<>();
	
	private NumbersGenerator numbersGenerator;
	private NumbersConsumer numbersConsumer;
	private PriorityBlockingQueue<FutureAndNumber> tasksQueue;
	private ExecutorService primeCheckExecutor;
    private PrimeNumberCheckersPool primeNumberCheckersPool;
    private PrimeNumberCheckerFactory primeNumberCheckerFactory;
    private CheckerSelectionStrategy checkerSelectionStrategy;
    
    private int poolSize = 10;
    private int queueSize = 50;
    private int checkersSize = 10;

	public int getPoolSize() {
		return poolSize;
	}

	public void setPoolSize(int poolSize) {
		this.poolSize = poolSize;
	}

	public int getQueueSize() {
		return queueSize;
	}

	public void setQueueSize(int queueSize) {
		this.queueSize = queueSize;
	}

	public int getCheckersSize() {
		return checkersSize;
	}

	public void setCheckersSize(int checkersSize) {
		this.checkersSize = checkersSize;
	}

	public PrimeNumberCheckerFactory getPrimeNumberCheckerFactory() {
		return primeNumberCheckerFactory;
	}

	public void setPrimeNumberCheckerFactory(PrimeNumberCheckerFactory primeNumberCheckerFactory) {
		this.primeNumberCheckerFactory = primeNumberCheckerFactory;
	}

	public CheckerSelectionStrategy getCheckerSelectionStrategy() {
		return checkerSelectionStrategy;
	}

	public void setCheckerSelectionStrategy(CheckerSelectionStrategy checkerSelectionStrategy) {
		this.checkerSelectionStrategy = checkerSelectionStrategy;
	}

	public void start(long lastNumber) {
		tasksQueue = new PriorityBlockingQueue<FutureAndNumber>();
		primeCheckExecutor = new ThreadPoolExecutor(poolSize, poolSize, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(queueSize));
		primeNumberCheckersPool = new PrimeNumberCheckersPool(checkersSize, primeNumberCheckerFactory, checkerSelectionStrategy);
		primeNumberCheckersPool.init();
		numbersConsumer = new NumbersConsumer(tasksQueue, numberResultListeners, primeCheckExecutor, primeNumberCheckersPool, this);
		numbersGenerator = new NumbersGenerator(tasksQueue, primeCheckExecutor, primeNumberCheckersPool, this);
		numbersConsumer.start();
		numbersGenerator.start(lastNumber);
	}
	
	public void stop() {
		numbersGenerator.stop();
		numbersConsumer.stop();
		primeCheckExecutor.shutdown();
	}
	
	public void hardStop() {
		numbersGenerator.hardStop();
		numbersConsumer.hardStop();
		primeCheckExecutor.shutdownNow();
	}
	
	public void awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		primeCheckExecutor.awaitTermination(timeout, unit);
	}
	
	@Override
	public void notifyError(Exception exception) {
		System.err.println(exception);
		hardStop();
	}

	public void addNumberResultListener(NumberResultListener numberResultListener) {
		numberResultListeners.add(numberResultListener);
	}
	
	public void removeNumberResultListener(NumberResultListener numberResultListener) {
		numberResultListeners.remove(numberResultListener);
	}
}
