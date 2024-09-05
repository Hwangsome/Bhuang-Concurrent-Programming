package com.bhuang;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public class WaitNotifyExample {
    private final Object lock = new Object();

    public void producer() throws InterruptedException {
        // 在使用 wait 方法时，必须把 wait 方法写在 synchronized 保护的 while 代码块中，并始终判断执行条件是否满足，
        // 如果满足就往下继续执行，如果不满足就执行 wait 方法，而在执行 wait 方法之前，必须先持有对象的 monitor 锁，
        // 也就是通常所说的 synchronized 锁
        // 释放了锁之后，其他线程可以进入这个同步块，然后还是执行 wait 方法，进入等待状态， 进入 等待队列中
        // 等待队列： 先进先出

        /**
         * Thread[producerThread2,5,main]Producer is waiting...
         * Thread[producerThread1,5,main]Producer is waiting...
         * Thread[consumerThread1,5,main]Consumer is notifying...
         * Thread[producerThread2,5,main]Producer resumed.
         * Thread[producerThread1,5,main]Producer resumed.
         *
         * 可以看到线程2 是先进入等待队列的， 然后才是线程1 再进入等待队列的。
         * notifyall的时候： 也是先唤醒线程2 然后再唤醒线程1
         */
        synchronized (lock) {
            System.out.println(Thread.currentThread() + "Producer is waiting...");
            lock.wait();  // 进入等待状态，并释放锁
            System.out.println(Thread.currentThread() + "Producer resumed.");
        }
    }

    public void consumer() throws InterruptedException {
        synchronized (lock) {
            Thread.sleep(1000); // 模拟一些工作
            System.out.println(Thread.currentThread() + "Consumer is notifying...");
            lock.notifyAll();  // 唤醒在等待锁的线程, notifyAll 表示的唤醒等待队列中的所有线程，而notify表示的是唤醒等待队列中的一个线程
        }
    }

    public static void main(String[] args) {
        WaitNotifyExample example = new WaitNotifyExample();
        Thread producerThread = new Thread(() -> {
            try {
                Thread.sleep(100);
                example.producer();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        producerThread.setName("producerThread1");

        Thread producerThread2 = new Thread(() -> {
            try {
                example.producer();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        producerThread2.setName("producerThread2");

        Thread consumerThread = new Thread(() -> {
            try {
                // 为了让 producerThread 先执行，这里让 consumerThread 睡眠 101 毫秒
                Thread.sleep(101);
                example.consumer();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        consumerThread.setName("consumerThread1");

//        Thread consumerThread2 = new Thread(() -> {
//            try {
//                example.consumer();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        });
//        consumerThread2.setName("consumerThread2");


        producerThread2.start();
        producerThread.start();
        consumerThread.start();
//        consumerThread2.start();


    }
}
