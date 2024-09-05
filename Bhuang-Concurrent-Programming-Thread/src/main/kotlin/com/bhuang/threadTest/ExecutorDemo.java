package com.bhuang.threadTest;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Executor;


/**
 * 任务的执行策略可以变化：
 * Executor 实现类决定了任务的执行策略(根据Executor的实现类)。比如，任务可以在多个线程中并发执行（如线程池中的线程）、在单一线程中顺序执行（如 SingleThreadExecutor），或者每个任务都启动一个新线程执行（如 ThreadPerTaskExecutor）。
 * 因为任务提交者并不依赖具体的执行机制，所以可以根据需要更换不同的 Executor 实现而无需修改任务提交的代码。这种灵活性使得代码更加可复用和易于扩展。
 */
public class ExecutorDemo {


    // Executor 可以将任务的提交与任务的执行机制解耦，使代码更加灵活和可扩展。
    // 任务执行者：由 Executor 的实现类负责如何执行任务，任务可能在新线程中执行、
    // 复用线程池中的线程、延迟执行、或在调用者线程中同步执行。
    static class DirectExecutor implements Executor {
        // execute 定义了任务的执行方式
        public void execute(Runnable r) {
            //new Thread(r).start(); // 表示新启一个线程去执行任务
            r.run(); // 表示在调用者当前线程执行任务
        }
    }

    // 为每个任务创建一个新线程 去执行
    static class ThreadPerTaskExecutor implements Executor {
        public void execute(Runnable r) {
            new Thread(r).start();  // 为每个任务创建一个新线程
        }
    }

    static class SerialExecutor implements Executor {
        final Queue<Runnable> tasks = new ArrayDeque<>();
        // executor：这是 SerialExecutor 内部使用的另一个 Executor，负责实际执行任务。
        // SerialExecutor 本质上是一个包装器，它将任务按顺序提交给这个 executor。
        final Executor executor;

        // active：这是一个 Runnable 引用，指向当前正在执行的任务。如果 active 为 null，表示当前没有任务在执行。
        // 这个值的初始化是从任务队列中取出一个任务，然后给active 赋值。
        Runnable active;

        SerialExecutor(Executor executor) {
            this.executor = executor;
        }



        static class TaskWrapper implements Runnable {
            final Runnable task;
            final SerialExecutor serialExecutor;

            TaskWrapper(Runnable task, SerialExecutor serialExecutor) {
                this.task = task;
                this.serialExecutor = serialExecutor;
            }

            public void run() {
                try {
                    task.run();
                } finally {
                    // 无论任务执行是否成功，都会调用 scheduleNext() 方法去调度下一个任务。
                    serialExecutor.scheduleNext();
                }
            }
        }

        public synchronized void execute(Runnable r) {
            // 将提交的任务 r 包装成一个新的 Runnable 并添加到 tasks 队列中。
            // 这个包装的 Runnable 在 run 方法结束后会调用 scheduleNext()，确保下一个任务被执行。
            tasks.add(new TaskWrapper(r, this));
            // 如果当前没有任务在执行（即 active 为 null），则调用 scheduleNext() 去启动下一个任务
            if (active == null) {
                scheduleNext();
            }
        }

        protected synchronized void scheduleNext() {
            // 从任务队列中取出一个任务，提交给 executor 执行。
            if ((active = tasks.poll()) != null) {
                executor.execute(active);
            }
        }
    }


    // ① 定义任务
    static class Task implements Runnable {
        public void run() {
            System.out.println(Thread.currentThread() + " running task");
        }
    }

    public static void main(String[] args) {
//        Executor executor = new DirectExecutor();
//        // 任务提交者：只需负责定义和提交任务，而不需要创建和管理线程。例如，只需调用 executor.execute(task) 提交任务。
//        // ② 提交任务
//        executor.execute(new Task());
//
        Executor threadPerTaskExecutor =  new ThreadPerTaskExecutor();
//        threadPerTaskExecutor.execute(new Task());

        Executor serialExecutor = new SerialExecutor(threadPerTaskExecutor);
        serialExecutor.execute(new Task());
    }
}
