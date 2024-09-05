package com.bhuang.interrupt;

public class InterruptedDemo {
    static class StopThread implements Runnable {
        @Override
        public void run() {
            int count = 0;
            // 在 while 循环体判断语句中，首先通过 Thread.currentThread().isInterrupt() 判断线程是否被中断，
            // 随后检查是否还有工作要做。&& 逻辑表示只有当两个判断条件同时满足的情况下，才会去执行下面的工作。

            // 当主线程调用 interrupt() 后，子线程的中断标志被设置为 true，因此 isInterrupted() 返回 true，!Thread.currentThread().isInterrupted() 返回 false，导致 while 循环条件不再成立。
            //因此，子线程跳出 while 循环，停止执行，从而成功中断。
            while (!Thread.currentThread().isInterrupted() && count < 1000) {
                System.out.println("count = " + count++);
            }
        }
    }

    // 测试线程在 sleep 期间被中断
    static class StopDuringSleep implements Runnable {
        @Override
        public void run() {
            int num = 0;
            // 这里一直在循环检测线程是否被中断，如果线程被中断了，跳出 while 循环，停止执行
            while (!Thread.currentThread().isInterrupted() && num <= 1000) {
                num++;
                try {
                    System.out.println("num = " + num++);
                    // 如果在线程睡眠期间调用了 interrupt() 方法，中断标志被设置为 true，
                    // 并且线程从睡眠状态被唤醒，同时会抛出 InterruptedException 异常
                    Thread.sleep(1000000);
                } catch (InterruptedException e) {
                    // 由于抛出了 InterruptedException，线程的中断状态被清除，即 Thread.currentThread().isInterrupted() 会返回 false，除非你在捕获异常时再次设置中断状态。
                    // 一旦线程被中断并抛出 InterruptedException，程序可以在 catch 块中决定如何处理这个中断：
                    //继续运行：可以选择忽略中断并继续执行。
                    //退出线程：在处理中断后，可以选择让线程安全地终止。
                    //重置中断状态：如果希望在捕获异常后继续保留中断状态，可以在 catch 块中再次调用 Thread.currentThread().interrupt() 来重置中断状态为 true。
                    //e.printStackTrace();
                    // 再次设置中断状态
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    static void testStopThread() throws InterruptedException {
        Thread thread = new Thread(new StopThread());
        thread.start();
        Thread.sleep(5);
        // 我们一旦调用某个线程的 interrupt() 之后，这个线程的中断标记位就会被设置成 true。
        // 每个线程都有这样的标记位，当线程执行时，应该定期检查这个标记位，如果标记位被设置成 true，
        // 就说明有程序想终止该线程
        thread.interrupt();
    }

    static void testStopDuringSleep() throws InterruptedException {
        Thread thread = new Thread(new StopDuringSleep());
        thread.start();
        Thread.sleep(5);
        thread.interrupt();
    }
    public static void main(String[] args) throws InterruptedException {
        // testStopThread();

        testStopDuringSleep();
    }
}
