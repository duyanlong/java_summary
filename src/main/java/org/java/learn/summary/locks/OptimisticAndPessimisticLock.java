package org.java.learn.summary.locks;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 乐观锁& 悲观锁.
 *
 * @author : duyanlong
 * @version V1.0
 * @Project: java_summary
 * @Package org.java.learn.summary.locks
 * @date Date : 2020年11月10日 21:15
 */
public class OptimisticAndPessimisticLock {

    /**
     * 悲观锁的调用方式.
     */
    public synchronized void optimisticMethod1() throws InterruptedException {
        Thread.sleep(200L);
        System.out.println("悲观锁 optimisticMethod1 调用-");
    }

    // 悲观锁
    private ReentrantLock lock = new ReentrantLock();

    public void optimisticMethod2() throws InterruptedException {
        lock.lock();
        Thread.sleep(200L);
        System.out.println("悲观锁 optimisticMethod2 调用-");
        lock.unlock();
    }

    // 乐观锁
    private AtomicInteger atomicInteger = new AtomicInteger();

    public void pessimisticMethod() {
        atomicInteger.getAndAdd(1);
        System.out.println("乐观锁调用值为：" + atomicInteger.get());
    }

    class ThreadA implements Runnable{

        OptimisticAndPessimisticLock lock = null;
        ThreadA(OptimisticAndPessimisticLock lock){
            this.lock = lock;
        }

        @Override
        public void run() {
            try {

                lock.optimisticMethod1();
                lock.optimisticMethod2();
                lock.pessimisticMethod();
            }catch (Exception exp){
                exp.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        OptimisticAndPessimisticLock lock = new OptimisticAndPessimisticLock();
        ThreadA threadA = lock.new ThreadA(lock);
        Thread[] threads = new Thread[5];
        for(int idx = 0;idx<5;idx++){
            threads[idx] = new Thread(threadA);
        }

        for(int idx=0;idx<5;idx++){
            threads[idx].start();
        }

    }
}
