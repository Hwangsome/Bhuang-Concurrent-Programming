package com.bhuang;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BlockingQueueWithCondition {

    private final Queue<String> buffer = new LinkedList<>();
    private final int capacity;
    private final Lock lock = new ReentrantLock();
    private final Condition notFull = lock.newCondition();
    private final Condition notEmpty = lock.newCondition();

    public BlockingQueueWithCondition(int capacity) {
        this.capacity = capacity;
    }


    public void give(String data) throws InterruptedException {
        lock.lock();
        try {
            // 在 while 的条件里检测 queue 是不是已经满了，如果已经满了，则调用 notFull 的 await() 阻塞生产者线程并释放 Lock，
            // 如果没有满，则往队列放入数据并利用 notEmpty.signalAll() 通知正在等待的所有消费者并唤醒它们。
            while (buffer.size() == capacity) {
                notFull.await();  // 缓冲区满，等待消费者取走数据
            }
            buffer.add(data);
            System.out.println("Produced: " + data);
            notEmpty.signalAll();  // 唤醒等待在 notEmpty 上的消费者
        } finally {
            lock.unlock();
        }
    }

    public String take() throws InterruptedException {
        lock.lock();
        try {
            while (buffer.isEmpty()) {
                notEmpty.await();  // 缓冲区空，等待生产者添加数据
            }
            String data = buffer.remove();
            System.out.println("Consumed: " + data);
            notFull.signalAll();  // 唤醒等待在 notFull 上的生产者
            return data;
        } finally {
            lock.unlock();
        }
    }

    static class Producer implements Runnable {
        private final BlockingQueueWithCondition queue;

        public Producer(BlockingQueueWithCondition queue) {
            this.queue = queue;
        }

        @Override
        public void run() {
            try {
                for (int i = 1; i <= 10; i++) {
                    queue.give("Data-" + i);
                    //Thread.sleep(100);  // 模拟生产时间
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    static class Consumer implements Runnable {
        private final BlockingQueueWithCondition queue;

        public Consumer(BlockingQueueWithCondition queue) {
            this.queue = queue;
        }

        @Override
        public void run() {
            try {
                for (int i = 1; i <= 10; i++) {
                    queue.take();
                    Thread.sleep(1000);  // 模拟消费时间
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        BlockingQueueWithCondition queue = new BlockingQueueWithCondition(5);  // 缓冲区容量为5

        Thread producerThread = new Thread(new Producer(queue));
        Thread consumerThread = new Thread(new Consumer(queue));

        producerThread.start();
        consumerThread.start();

        producerThread.join();
        consumerThread.join();
    }
}
