/*  
 *  $ Id: $
 *  Copyright © 2016 Experian Ltd. All rights reserved.
 *  No Part of this file may be copied or distributed without the permission of Experian Ltd.
 */

package primenumbers.core;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;


public class NumbersGenerator implements Runnable {
    
    private AtomicBoolean isRunning = new AtomicBoolean();
    
    private ExecutorService primeCheckExecutor;
    private PriorityBlockingQueue<FutureAndNumber> tasksQueue;
    private PrimeNumberCheckersPool primeNumberCheckersPool;
    private NotificationChannel notificationChannel;
    private Thread executorThread;
    
    private long lastNumber = 0;
    
    private long delay = 100;

    public NumbersGenerator(PriorityBlockingQueue<FutureAndNumber> tasksQueue,
            ExecutorService primeCheckExecutor,
            PrimeNumberCheckersPool primeNumberCheckersPool,
            NotificationChannel notificationChannel) {
        this.tasksQueue = tasksQueue;
        this.primeCheckExecutor = primeCheckExecutor;
        this.primeNumberCheckersPool = primeNumberCheckersPool;
        this.notificationChannel = notificationChannel;
    }

    public long getDelay() {
		return delay;
	}

	public void setDelay(long delay) {
		this.delay = delay;
	}

    public void start(long lastNumber) {
    	this.lastNumber = lastNumber;
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
        while(isRunning.get() && !primeCheckExecutor.isShutdown()) {
            if(Thread.currentThread().isInterrupted()) {
                return;
            }
            lastNumber++;
            PrimeNumberChecker checker = primeNumberCheckersPool.getPrimeNumberChecker();
            Future<NumberResult> result;
            try {
                result = primeCheckExecutor.submit(new PrimeCheck(lastNumber, checker));
            } catch (RejectedExecutionException e) {
                if(primeCheckExecutor.isShutdown()) {
                    return;
                } else {
                	notificationChannel.notifyError(e);
                	return;
                }
            }
            tasksQueue.put(new FutureAndNumber(result, lastNumber));
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                return;
            }
        }
    }
}
