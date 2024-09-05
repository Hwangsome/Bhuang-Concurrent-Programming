package com.bhuang.locksupport;

import java.sql.Time;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class LockSupportDemo {

    static class Task implements Runnable {
        @Override
        public void run() {
            System.out.println("Thread is going to park. - " + Thread.currentThread());
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            // park() 方法会检查线程是否有许可证，如果没有则阻塞线程
            LockSupport.park(); // 阻塞当前线程
            System.out.println("Thread is unparked. - " + Thread.currentThread());
        }
    }

    static class Task2 implements Runnable {
        @Override
        public void run() {
            System.out.println("Thread starts.");
            // 当线程调用 park() 时，它会检查是否有可用的许可证：
            //如果有许可证，park() 立即返回，并且消耗该许可证。
            //如果没有许可证，park() 会阻塞线程，直到有线程调用 unpark() 给它发放许可证，或者线程被中断。
            LockSupport.park();
            System.out.println("Thread unparked after first park. - " + Thread.currentThread());
            LockSupport.park();
            System.out.println("Thread unparked after second park. - " + Thread.currentThread());
        }
    }

    static void testLockSupport() {
        Thread thread = new Thread(new Task());
        thread.start();
        System.out.println("Main thread is going to unpark the thread. - " + Thread.currentThread());
        // unpark() 方法为线程授予许可证，如果线程当前被阻塞，则立即唤醒它。
        LockSupport.unpark(thread); // 解除阻塞
    }

    static void testIssuingMultiplePermit() throws InterruptedException {
        Thread thread = new Thread(new Task2());
        thread.start();

        // LockSupport.unpark() 发放的许可证最多只能有一个，连续调用多次 unpark() 只能累积一个许可证。
        // 因此，无论你调用 unpark() 一次还是多次，它的效果都是相同的——线程最多只能持有一个许可证。
        LockSupport.unpark(thread);
        LockSupport.unpark(thread);

        System.out.println("Main thread finished.");
    }
    public static void main(String[] args) throws InterruptedException {
        //testLockSupport();

        testIssuingMultiplePermit();
    }
}
