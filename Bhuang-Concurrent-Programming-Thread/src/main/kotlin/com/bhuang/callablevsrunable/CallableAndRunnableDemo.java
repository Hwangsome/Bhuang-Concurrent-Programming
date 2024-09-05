package com.bhuang.callablevsrunable;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class CallableAndRunnableDemo {

    static class Task implements Runnable {
        @Override
        public void run() {
            System.out.println("task is running");
        }
    }

    static class CalculateTask implements Callable<Integer> {
        private Integer a;
        private Integer b;

        public CalculateTask(Integer a, Integer b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public Integer call() throws Exception {
            if (a == null || b == null)
                throw new IllegalArgumentException("a or b is null");
            else
                return a + b;
        }
    }

    static void testRunnable() {
        Thread thread = new Thread(new Task());
        thread.start();
    }

    static void testCallable() {
        CalculateTask calculateTask = new CalculateTask(1, 2);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            // 提交任务并返回 Future
            Future<Integer> future =  executor.submit(calculateTask);
            // 获取 Callable 的执行结果
            Integer result = future.get();  // 阻塞，直到任务完成并返回结果
            System.out.println("Result: " + result);
        } catch (InterruptedException | ExecutionException e) {
            // 捕获异常
            e.printStackTrace();
        } finally {
            // 关闭线程池
            executor.shutdown();
        }
    }
    public static void main(String[] args) {
        testRunnable();
      //  testCallable();
    }
}
