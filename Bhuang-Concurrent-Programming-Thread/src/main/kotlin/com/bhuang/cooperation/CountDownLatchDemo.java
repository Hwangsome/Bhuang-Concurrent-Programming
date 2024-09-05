package com.bhuang.cooperation;

import java.util.concurrent.CountDownLatch;

/**
 * CountDownLatch 是一个同步工具类，它允许一个或多个线程等待，直到一组操作完成。
 * 它的核心机制是一个计数器，线程可以减少这个计数器的值，计数器到达零时，所有等待的线程将被唤醒继续执行。
 *
 * 典型使用场景
 * 1. 启动多个线程：主线程等待一组任务完成，然后继续执行。
 * 2. 并行任务的结果汇总：等待多个线程完成各自的任务，然后汇总结果。
 */
public class CountDownLatchDemo {

    static class CookingTask implements Runnable {
        private final String course;
        private final CountDownLatch latch;

        public CookingTask(String course, CountDownLatch latch) {
            this.course = course;
            this.latch = latch;
        }

        @Override
        public void run() {
            // Simulate cooking time
            try {
                Thread.sleep((long) (Math.random() * 1000));
                System.out.println(course + " is ready!");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                latch.countDown(); // Signal that this course is ready // 完成工作后减少计数器
                System.out.println("latch.getCount() = " + latch.getCount());
            }
        }
    }
    public static void main(String[] args) throws InterruptedException {
        // CountDownLatch 初始化时会设置一个计数器，表示需要等待的事件数量。
        // 每当一个事件完成时，通过调用 countDown() 方法减少计数器
        // 线程调用 await() 方法进入等待状态，直到计数器变为零。计数器变为零时，所有等待的线程会被唤醒。
        CountDownLatch coursesReady = new CountDownLatch(5);

        // Kitchen tasks for each course
        new Thread(new CookingTask("Appetizer", coursesReady)).start();
        new Thread(new CookingTask("Soup", coursesReady)).start();
        new Thread(new CookingTask("Main Course", coursesReady)).start();
        new Thread(new CookingTask("Dessert", coursesReady)).start();
        new Thread(new CookingTask("Coffee", coursesReady)).start();

        // Wait for all courses to be ready
        // 在 await() 被调用时，主线程会进入阻塞状态。主线程会停在这行代码，直到 CountDownLatch 的计数器变为 0
        // 当最后一个 CookingTask 完成，并调用了 latch.countDown() 后，CountDownLatch 的计数器会减为 0。此时，主线程会从 await() 方法中解除阻塞状态，继续执行后续的代码。

        // 主线程调用 await() 的主要作用是：
        //同步多个线程：确保所有 CookingTask 线程都完成后，主线程才继续执行。这种机制可以用于协调多个并发任务，使得主线程能够等待一组任务全部完成后再继续执行后续操作。
        //控制程序流程：通过使用 CountDownLatch 和 await()，主线程可以明确知道什么时候所有的准备工作（在这个例子中是所有菜肴的准备）已经完成，从而可以有序地继续后续的流程。
        coursesReady.await();

        System.out.println("All courses are ready! Let's serve the meal.");
    }
}
