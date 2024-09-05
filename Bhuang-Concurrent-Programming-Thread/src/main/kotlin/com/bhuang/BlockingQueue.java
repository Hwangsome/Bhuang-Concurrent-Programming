package com.bhuang;

import java.util.LinkedList;
import java.util.Queue;

class BlockingQueue {
    private final Queue<String> buffer = new LinkedList<>();
    private final int capacity;

    public BlockingQueue(int capacity) {
        this.capacity = capacity;
    }

    public synchronized void give(String data) throws InterruptedException {
        while (buffer.size() == capacity) {
            // wait() 方法是 Java 中线程间通信的一部分，用于让当前线程进入等待状态，直到被其他线程唤醒。
            // 它通常与 notify() 或 notifyAll() 方法配合使用，以实现线程之间的协作
            wait();  // 缓冲区满，等待消费者取走数据
        }
        buffer.add(data);
        System.out.println("Produced: " + data);
        notifyAll();  // 唤醒等待的消费者
    }

    public synchronized String take() throws InterruptedException {
        while (buffer.isEmpty()) {
            wait();  // 缓冲区空，等待生产者添加数据
        }
        String data = buffer.remove();
        System.out.println("Consumed: " + data);
        notifyAll();  // 唤醒等待的生产者
        return data;
    }

    static class Producer implements Runnable {
        private final BlockingQueue queue;

        public Producer(BlockingQueue queue) {
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
        private final BlockingQueue queue;

        public Consumer(BlockingQueue queue) {
            this.queue = queue;
        }

        @Override
        public void run() {
            try {
                for (int i = 1; i <= 10; i++) {
                    Thread.sleep(2000);  // 模拟消费时间
                    queue.take();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void main(String[] args) {
        BlockingQueue queue = new BlockingQueue(5);  // 缓冲区容量为5

        Thread producerThread = new Thread(new Producer(queue));
        Thread consumerThread = new Thread(new Consumer(queue));

        producerThread.start();
        consumerThread.start();

        try {
            producerThread.join();
            consumerThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
