/*  
 *  $ Id: $
 *  Copyright © 2016 Experian Ltd. All rights reserved.
 *  No Part of this file may be copied or distributed without the permission of Experian Ltd.
 */

package primenumbers.core;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;


public class NumbersConsumer implements Runnable {

    private PriorityBlockingQueue<FutureAndNumber> tasksQueue;
    private AtomicBoolean isRunning = new AtomicBoolean();
    private List<NumberResultListener> numberResultListeners;
    private ExecutorService primeCheckExecutor;
    private PrimeNumberCheckersPool primeNumberCheckersPool;
    private NotificationChannel notificationChannel;
    
    private long timeout=10;
    private TimeUnit timeUnit = TimeUnit.SECONDS;
    private int attempts = 3;
    
    private Thread executorThread;
    
    public NumbersConsumer(PriorityBlockingQueue<FutureAndNumber> tasksQueue,
    		List<NumberResultListener> numberResultListeners,
    		ExecutorService primeCheckExecutor,
            PrimeNumberCheckersPool primeNumberCheckersPool,
            NotificationChannel notificationChannel) {
        this.tasksQueue = tasksQueue;
        this.numberResultListeners = numberResultListeners;
        this.primeCheckExecutor = primeCheckExecutor;
        this.primeNumberCheckersPool = primeNumberCheckersPool;
        this.notificationChannel = notificationChannel;
    }
    
    public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public TimeUnit getTimeUnit() {
		return timeUnit;
	}

	public void setTimeUnit(TimeUnit timeUnit) {
		this.timeUnit = timeUnit;
	}

	public int getAttempts() {
		return attempts;
	}

	public void setAttempts(int attempts) {
		this.attempts = attempts;
	}

	public void start() {
        isRunning.set(true);
        executorThread = new Thread(this);
        executorThread.start();
    }
    
    public void stop() {
        isRunning.set(false);
    }
    
    public void hardStop() {
    	stop();
    	executorThread.interrupt();
    }

    @Override
    public void run() {
        while(isRunning.get()) {
            if(Thread.currentThread().isInterrupted()) {
                return;
            }
            FutureAndNumber futureAndNumber = null;
			try {
				futureAndNumber = tasksQueue.take();
			} catch (InterruptedException e1) {
				return;
			}
            try {
                NumberResult numberResult = futureAndNumber.getFuture().get(timeout, timeUnit);
                notifyListeners(numberResult);
            } catch (InterruptedException e) {
                return;
            } catch (ExecutionException | TimeoutException e) {
            	if(futureAndNumber.getAttempt() <= attempts) {
            		PrimeNumberChecker checker = primeNumberCheckersPool.getPrimeNumberChecker();
            		Future<NumberResult> result;
                    try {
                        result = primeCheckExecutor.submit(new PrimeCheck(futureAndNumber.getNumber(), checker));
                    } catch (RejectedExecutionException e2) {
                        if(primeCheckExecutor.isShutdown()) {
                            return;
                        } else {
                        	notificationChannel.notifyError(e2);
                        	return;
                        }
                    }
                    tasksQueue.put(new FutureAndNumber(result, futureAndNumber.getNumber(), futureAndNumber.getAttempt()+1));
            	} else {
            		notificationChannel.notifyError(e);
                	return;
            	}
            }
        }
    }
    
    private void notifyListeners(NumberResult numberResult) {
    	for(NumberResultListener listener : numberResultListeners) {
    		listener.numberResultReceived(numberResult);
    	}
    }

}
