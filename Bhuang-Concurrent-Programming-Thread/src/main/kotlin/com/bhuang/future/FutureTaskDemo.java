package com.bhuang.future;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

public class FutureTaskDemo {

    static class CallableTask implements Callable<Integer> {
        @Override
        public Integer call() throws Exception {
            return 1;
        }
    }

    static void testFutureTask() {
        // 因为 FutureTask 实现了 RunnableFuture， 而RunnableFuture 接口继承了 Runnable, Future
        // 所以 FutureTask 既可以作为 Runnable 被线程执行，又可以作为 Future 得到 Callable 的返回值。
        // 把 Callable 实例当作 FutureTask 构造函数的参数，生成 FutureTask 的对象，
        // 然后把这个对象当作一个 Runnable 对象，放到线程池中或另起线程去执行，
        // 最后还可以通过 FutureTask 获取任务执行的结果。
        FutureTask<Integer> futureTask = new FutureTask<>(new CallableTask());
        ExecutorService executorService = Executors.newFixedThreadPool(3);

        // new Thread(futureTask).start();

        try {
            executorService.execute(futureTask);
            System.out.println("task运行结果: " + futureTask.get());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } finally {
            executorService.shutdown();
        }
    }

    public static void main(String[] args) {
        testFutureTask();
    }
}
