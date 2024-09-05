package com.bhuang.cooperation;

import java.util.concurrent.Semaphore;

/**
 * Semaphore 是一个计数信号量，用于控制同时访问某个特定资源的线程数量。
 * 它通过一个计数器来控制对共享资源的访问权限。线程通过 acquire() 获取许可，
 * 如果许可数量足够，则可以继续执行；否则，线程会被阻塞，直到有许可可用。
 *
 * 典型使用场景
 * 1. 资源池管理：限制同时访问某个资源的线程数量，如数据库连接池、限流器等。
 * 2. 限流控制：控制并发请求的数量，以保护系统免受过载。
 */
public class SemaphoreDemo {

    static class User implements Runnable {
        private final String name;
        private final Semaphore printerSemaphore;

        public User(String name, Semaphore printerSemaphore) {
            this.name = name;
            this.printerSemaphore = printerSemaphore;
        }

        /**
         * 在此示例中，确保Semaphore只有两个用户可以同时访问打印机。
         * 用户必须在打印前从信号量获取许可，并在打印后释放许可。
         * 这可防止更多用户打印超出可用资源允许的范围。
         */
        @Override
        public void run() {
            try {
                // Acquire a permit from the Semaphore
                System.out.println(name + " is waiting to use the printer.");
                printerSemaphore.acquire();

                // Simulate printing time
                System.out.println(name + " is printing a document.");
                Thread.sleep((long) (Math.random() * 1000));

                // Release the permit after printing is completed
                System.out.println(name + " has finished printing.");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                // 释放许可证
                printerSemaphore.release(); // Release the permit
            }
        }
    }

    public static void main(String[] args) {
        // 工作原理
        // Semaphore 初始化时设置许可的数量。
        // 线程调用 acquire() 方法请求获取许可，如果当前有许可可用，则许可数减一，线程继续执行；否则，线程被阻塞，直到有许可可用。
        // 线程执行完任务后，通过 release() 方法释放许可，许可数加一，可能会唤醒等待的线程。
        Semaphore printerSemaphore = new Semaphore(2);

        // Users trying to print documents
        new Thread(new User("Alice", printerSemaphore)).start();
        new Thread(new User("Bob", printerSemaphore)).start();
        new Thread(new User("Charlie", printerSemaphore)).start();
        new Thread(new User("David", printerSemaphore)).start();
        new Thread(new User("Five", printerSemaphore)).start();
        new Thread(new User("Six", printerSemaphore)).start();
    }
}
