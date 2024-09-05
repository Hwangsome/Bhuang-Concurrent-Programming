package com.bhuang.cooperation;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * CyclicBarrier 是一个同步工具类，它允许一组线程互相等待，直到所有线程都到达一个共同的屏障点。
 * CyclicBarrier 可以被重复使用（即它是循环的），这与 CountDownLatch 只能使用一次不同。
 *
 * 典型使用场景
 * 1. 多阶段任务：将一个任务分为多个阶段，每个阶段各线程并行执行，所有线程完成一个阶段后再开始下一个阶段。
 * 2. 分布式计算：多个子任务需要在某个阶段同步，然后继续后续计算。
 */
public class CyclicBarrierDemo {

    static class TeamMember implements Runnable {
        private final String role;
        private final CyclicBarrier barrier;

        public TeamMember(String role, CyclicBarrier barrier) {
            this.role = role;
            this.barrier = barrier;
        }

        @Override
        public void run() {
            // Work on the component
            System.out.println(role + " is working on the component. - " + Thread.currentThread());

            // Synchronize at the barrier
            try {
                // barrier.await()用于在屏障处进行同步。当一名团队成员到达此点时，
                // 它会等待所有三名团队成员都到达屏障。一旦第三名成员到达，屏障就会被触发，
                // 并执行传递给构造函数的可运行对象CyclicBarrier（打印“Sprint 计划会议”）。
                // 然后每个团队成员继续进入下一阶段
                barrier.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                Thread.currentThread().interrupt();
            }

            // Continue with the next phase after synchronization
            System.out.println(role + " continues to the next phase. - " + Thread.currentThread());
        }
    }
    public static void main(String[] args) {
        // CyclicBarrier 初始化时指定参与的线程数量（即屏障点的参与者）。
        // 每个线程调用 await() 方法等待其他线程。所有线程到达屏障点时，屏障被解除，所有线程继续执行。
        // 可以为 CyclicBarrier 设置一个 Runnable 任务，当所有线程到达屏障点时，首先执行这个任务。

        // 如果这里设置的初始化参数指定了参与线程的数量为 2的时候， 但是这里实际参与的线程是3个，那么会一直等待，不会继续执行
        // 因为另外一个线程将 一直等待下一个线程到达屏障。第三个线程将永远等待，因为它在等待一个从未到达的线程。
        CyclicBarrier syncPoint = new CyclicBarrier(3, () -> System.out.println("Sprint planning meeting- " + Thread.currentThread()));

        // Teams working on different components
        new Thread(new TeamMember("Frontend", syncPoint)).start();
        new Thread(new TeamMember("Backend", syncPoint)).start();
        new Thread(new TeamMember("QA", syncPoint)).start();
    }
}
