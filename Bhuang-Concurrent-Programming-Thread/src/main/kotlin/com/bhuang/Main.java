package com.bhuang;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Main {

    static class RunnableThread implements Runnable{
        @Override
        public void run() {
            System.out.println("Thread is running");
        }
    }

    static class ThreadDemo extends Thread {
        @Override
        public void run() {
            System.out.println("ThreadDemo - Thread is running");
        }
    }

    static class StopThread implements Runnable {
        @Override
        public void run() {
            int count = 0;
            // 首先判断线程是否被中断，然后判断 count 值是否小于 1000
            while (!Thread.currentThread().isInterrupted() && count < 1000) {
                System.out.println("count = " + count++);
            }
        }
    }

    static class StopDuringSleep implements Runnable {
        @Override
        public void run() {
            int num = 0;
            while (!Thread.currentThread().isInterrupted() && num <= 1000) {
                num++;
                try {
                    System.out.println("num = " + num++);
                    Thread.sleep(1000000);
                } catch (InterruptedException e) {
                    // 一旦线程被中断并抛出 InterruptedException，程序可以在 catch 块中决定如何处理这个中断：
                    //继续运行：可以选择忽略中断并继续执行。
                    //退出线程：在处理中断后，可以选择让线程安全地终止。
                    //重置中断状态：如果希望在捕获异常后继续保留中断状态，可以在 catch 块中再次调用 Thread.currentThread().interrupt() 来重置中断状态为 true。
                    e.printStackTrace();
                    // 使线程可以终止
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    static class Producer implements Runnable {
        public volatile boolean canceled = false;
        BlockingQueue storage;
        public Producer(BlockingQueue storage) {
            this.storage = storage;
        }
        @Override
        public void run() {
            int num = 0;
            try {
                while (num <= 100000 && !canceled) {
                    if (num % 50 == 0) {
                        // 生产者在执行 storage.put(num) 时发生阻塞，在它被叫醒之前是没有办法进入下一次循环判断 canceled 的值的，
                        // 所以在这种情况下用 volatile 是没有办法让生产者停下来的，相反如果用 interrupt 语句来中断，
                        // 即使生产者处于阻塞状态，仍然能够感受到中断信号，并做响应处理。
                        storage.put(num);
                        System.out.println(num + "是50的倍数,被放到仓库中了。");
                    }
                    num++;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                System.out.println("生产者结束运行");
            }
        }
    }

    static class Consumer {
        BlockingQueue storage;
        public Consumer(BlockingQueue storage) {
            this.storage = storage;
        }
        public boolean needMoreNums() {
            if (Math.random() > 0.97) {
                return false;
            }
            return true;
        }
    }

    public static void main(String[] args) throws InterruptedException {
//        RunnableThread runnableThread = new RunnableThread();
//        Thread thread = new Thread(runnableThread);
//        thread.start();
//
//        ThreadDemo threadDemo = new ThreadDemo();
//        threadDemo.start();
//
//        Thread stopThread = new Thread(new StopThread());
//        stopThread.start();
//        Thread.sleep(5);
//        // 去修改interrupted 的标记位 为true, 修改为true后，Thread.currentThread().isInterrupted() 会返回true
//        // private volatile boolean interrupted;
//        stopThread.interrupt();

//        Thread stopDuringSleep = new Thread(new StopDuringSleep());
//        stopDuringSleep.start();
//        Thread.sleep(500);
//        stopDuringSleep.interrupt();

        ArrayBlockingQueue storage = new ArrayBlockingQueue(8);
        Producer producer = new Producer(storage);
        Thread producerThread = new Thread(producer);
        producerThread.start();
        Thread.sleep(500);
        Consumer consumer = new Consumer(storage);
        while (consumer.needMoreNums()) {
            System.out.println(consumer.storage.take() + "被消费了");
            Thread.sleep(100);
        }
        System.out.println("消费者不需要更多数据了。");
        //一旦消费不需要更多数据了，我们应该让生产者也停下来，但是实际情况却停不下来
        producer.canceled = true;
        System.out.println(producer.canceled);
    }
}
