package com.bhuang.threadlocal;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 场景1，ThreadLocal 用作保存每个线程独享的对象，为每个线程都创建一个副本，这样每个线程都可以修改自己所拥有的副本,
 * 而不会影响其他线程的副本，确保了线程安全。
 *
 * 场景2，ThreadLocal 用作每个线程内需要独立保存信息，以便供其他方法更方便地获取该信息的场景。
 * 每个线程获取到的信息可能都是不一样的，前面执行的方法保存了信息后，后续方法可以通过 ThreadLocal 直接获取到，
 * 避免了传参，类似于全局变量的概念。
 */
public class ThreadLocalDemo {


    static class Task implements Runnable {
        private int seconds;

        public Task(int seconds) {
            this.seconds = seconds;
        }

        @Override
        public void run() {
            String date = date(seconds);
            System.out.println(date);
        }

        private String date(int seconds) {
            Date date = new Date(1000L * seconds);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");
            return simpleDateFormat.format(date);

        }
    }


    static class Task2 implements Runnable {
        private int seconds;

        // 其他的没有变化，变化之处就在于，我们把这个 simpleDateFormat 对象给提取了出来，
        // 变成 static 静态变量，需要用的时候直接去获取这个静态对象就可以了

        // 我们有不同的线程，并且线程会执行它们的任务。但是不同的任务所调用的 simpleDateFormat 对象都是同一个，
        // 所以它们所指向的那个对象都是同一个，但是这样一来就会有线程不安全的问题。
        private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");

        public Task2(int seconds) {
            this.seconds = seconds;
        }

        @Override
        public void run() {
            String date = date(seconds);
            System.out.println(date);
        }

        private String date(int seconds) {
            Date date = new Date(1000L * seconds);
            return simpleDateFormat.format(date);
        }
    }

    static class Task3 implements Runnable {
        private Object lock = new Object();
        private int seconds;

        // 其他的没有变化，变化之处就在于，我们把这个 simpleDateFormat 对象给提取了出来，
        // 变成 static 静态变量，需要用的时候直接去获取这个静态对象就可以了

        // 我们有不同的线程，并且线程会执行它们的任务。但是不同的任务所调用的 simpleDateFormat 对象都是同一个，
        // 所以它们所指向的那个对象都是同一个，但是这样一来就会有线程不安全的问题。
        private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");

        public Task3(int seconds) {
            this.seconds = seconds;
        }

        @Override
        public void run() {
            String date = date(seconds);
            System.out.println(date);
        }

        private String date(int seconds) {
            Date date = new Date(1000L * seconds);
            String s = null;
            // 加锁可以解决，多个线程不能同时工作，这样一来，整体的效率就被大大降低了
            synchronized (ThreadLocalDemo.class) {
                s = simpleDateFormat.format(date);
            }
            return s;
        }
    }

    static class Task4 implements Runnable {
        private int seconds;
        public Task4(int seconds) {
            this.seconds = seconds;
        }
        private static final ThreadLocal<SimpleDateFormat> dateFormatThreadLocal = new ThreadLocal<>() {
            @Override
            protected SimpleDateFormat initialValue() {
                return new SimpleDateFormat("mm:ss");
            }
        };

        @Override
        public void run() {
            String date = date(seconds);
            System.out.println(date);
        }

        private String date(int seconds) {
            Date date = new Date(1000L * seconds);
            return dateFormatThreadLocal.get().format(date);
        }
    }

    static void testNoUseThreadLocal() {
        // 程序的运行结果是正确的，打印出 最大的时间 16:40 = 16 * 60 + 40 = 1000s
        // 但是这样做是没有必要的，因为这么多对象 (SimpleDateFormat)的创建是有开销的，
        // 并且在使用完之后的销毁同样是有开销的，而且这么多对象同时存在在内存中也是一种内存的浪费。
        ExecutorService threadPool = Executors.newFixedThreadPool(16);
        try {
            for (int i = 1; i <= 1000; i++) {
                threadPool.submit(new Task(i));
            }
        } finally {
            threadPool.shutdown();
        }
    }

    // 试一下创建一个对象：SimpleDateFormat
    // 测试这个运行结果就出现了线程不安全的问题：比如打印的数字有重复的问题
    static void testNoUseThreadLocalOneObject() {
        ExecutorService threadPool = Executors.newFixedThreadPool(16);
        try {
            for (int i = 1; i <= 100; i++) {
                threadPool.submit(new Task2(i));
            }
        } finally {
            threadPool.shutdown();
        }
    }

    // 在dateFormat 的时候加上锁，使只有一个线程去使用
    static void testNoUseThreadLocalOneObjectWithSync() {
        ExecutorService threadPool = Executors.newFixedThreadPool(16);
        try {
            for (int i = 1; i <= 100; i++) {
                threadPool.submit(new Task3(i));
            }
        } finally {
            threadPool.shutdown();
        }
    }

    static void testThreadLocal() {
        ExecutorService threadPool = Executors.newFixedThreadPool(16);
        try {
            for (int i = 1; i <= 100; i++) {
                threadPool.submit(new Task4(i));
            }
        } finally {
            threadPool.shutdown();
        }
    }

    public static void main(String[] args) {
         // testNoUseThreadLocal();
        // testNoUseThreadLocalOneObject();
        // testNoUseThreadLocalOneObjectWithSync();

        testThreadLocal();
    }
}
