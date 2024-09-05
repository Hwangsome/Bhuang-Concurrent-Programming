`CountDownLatch`、`CyclicBarrier` 和 `Semaphore` 是 Java 并发包中用于控制线程协作的三个重要工具类。它们各自解决了不同的并发控制问题，广泛应用于多线程编程中。下面我将详细讲解它们的工作原理、使用场景及示例代码。

### 一、`CountDownLatch`

#### 1. **概述**
`CountDownLatch` 是一个同步工具类，它允许一个或多个线程等待，直到一组操作完成。它的核心机制是一个计数器，线程可以减少这个计数器的值，计数器到达零时，所有等待的线程将被唤醒继续执行。

#### 2. **工作原理**
- `CountDownLatch` 初始化时会设置一个计数器，表示需要等待的事件数量。
- 每当一个事件完成时，通过调用 `countDown()` 方法减少计数器。
- 线程调用 `await()` 方法进入等待状态，直到计数器变为零。计数器变为零时，所有等待的线程会被唤醒。

#### 3. **典型使用场景**
- **启动多个线程**：主线程等待一组任务完成，然后继续执行。
- **并行任务的结果汇总**：等待多个线程完成各自的任务，然后汇总结果。

#### 4. **示例代码**

```java
import java.util.concurrent.CountDownLatch;

public class CountDownLatchExample {
    public static void main(String[] args) throws InterruptedException {
        int taskCount = 3;
        CountDownLatch latch = new CountDownLatch(taskCount);

        for (int i = 0; i < taskCount; i++) {
            new Thread(new Worker(latch)).start();
        }

        // 等待所有工作线程完成
        latch.await();
        System.out.println("All tasks completed. Main thread proceeds.");
    }

    static class Worker implements Runnable {
        private final CountDownLatch latch;

        Worker(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void run() {
            try {
                System.out.println(Thread.currentThread().getName() + " is working.");
                Thread.sleep(2000); // 模拟工作
                System.out.println(Thread.currentThread().getName() + " completed.");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                latch.countDown(); // 完成工作后减少计数器
            }
        }
    }
}
```

### 二、`CyclicBarrier`

#### 1. **概述**
`CyclicBarrier` 是一个同步工具类，它允许一组线程互相等待，直到所有线程都到达一个共同的屏障点。`CyclicBarrier` 可以被重复使用（即它是循环的），这与 `CountDownLatch` 只能使用一次不同。

#### 2. **工作原理**
- `CyclicBarrier` 初始化时指定参与的线程数量（即屏障点的参与者）。
- 每个线程调用 `await()` 方法等待其他线程。所有线程到达屏障点时，屏障被解除，所有线程继续执行。
- 可以为 `CyclicBarrier` 设置一个 `Runnable` 任务，当所有线程到达屏障点时，首先执行这个任务。

#### 3. **典型使用场景**
- **多阶段任务**：将一个任务分为多个阶段，每个阶段各线程并行执行，所有线程完成一个阶段后再开始下一个阶段。
- **分布式计算**：多个子任务需要在某个阶段同步，然后继续后续计算。

#### 4. **示例代码**

```java
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class CyclicBarrierExample {
    public static void main(String[] args) {
        int taskCount = 3;
        CyclicBarrier barrier = new CyclicBarrier(taskCount, () -> {
            System.out.println("All threads reached the barrier. Proceeding to the next step.");
        });

        for (int i = 0; i < taskCount; i++) {
            new Thread(new Worker(barrier)).start();
        }
    }

    static class Worker implements Runnable {
        private final CyclicBarrier barrier;

        Worker(CyclicBarrier barrier) {
            this.barrier = barrier;
        }

        @Override
        public void run() {
            try {
                System.out.println(Thread.currentThread().getName() + " is working.");
                Thread.sleep(2000); // 模拟工作
                System.out.println(Thread.currentThread().getName() + " reached the barrier.");
                barrier.await(); // 等待其他线程到达屏障点
                System.out.println(Thread.currentThread().getName() + " continues after the barrier.");
            } catch (InterruptedException | BrokenBarrierException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
```

### 三、`Semaphore`

#### 1. **概述**
`Semaphore` 是一个计数信号量，用于控制同时访问某个特定资源的线程数量。它通过一个计数器来控制对共享资源的访问权限。线程通过 `acquire()` 获取许可，如果许可数量足够，则可以继续执行；否则，线程会被阻塞，直到有许可可用。

#### 2. **工作原理**
- `Semaphore` 初始化时设置许可的数量。
- 线程调用 `acquire()` 方法请求获取许可，如果当前有许可可用，则许可数减一，线程继续执行；否则，线程被阻塞，直到有许可可用。
- 线程执行完任务后，通过 `release()` 方法释放许可，许可数加一，可能会唤醒等待的线程。

#### 3. **典型使用场景**
- **资源池管理**：限制同时访问某个资源的线程数量，如数据库连接池、限流器等。
- **限流控制**：控制并发请求的数量，以保护系统免受过载。

#### 4. **示例代码**

```java
import java.util.concurrent.Semaphore;

public class SemaphoreExample {
    public static void main(String[] args) {
        int permits = 3;
        Semaphore semaphore = new Semaphore(permits);

        for (int i = 0; i < 5; i++) {
            new Thread(new Worker(semaphore)).start();
        }
    }

    static class Worker implements Runnable {
        private final Semaphore semaphore;

        Worker(Semaphore semaphore) {
            this.semaphore = semaphore;
        }

        @Override
        public void run() {
            try {
                semaphore.acquire(); // 获取许可
                System.out.println(Thread.currentThread().getName() + " acquired a permit.");
                Thread.sleep(2000); // 模拟工作
                System.out.println(Thread.currentThread().getName() + " releasing the permit.");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                semaphore.release(); // 释放许可
            }
        }
    }
}
```

### 四、总结

- **`CountDownLatch`**：用于让一个或多个线程等待其他线程完成，类似于一个“一次性”的门闩，计数器归零时门闩打开，所有等待的线程被唤醒继续执行。
- **`CyclicBarrier`**：用于让一组线程互相等待，直到所有线程都达到一个共同的屏障点，然后所有线程一起继续执行。与 `CountDownLatch` 不同，`CyclicBarrier` 可以被重复使用。
- **`Semaphore`**：用于控制同时访问某个资源的线程数量，常用于限流或控制资源访问的场景。

这些工具类在多线程编程中提供了强大的同步机制，使得线程之间的协作变得更加容易和安全。在实际开发中，根据具体的并发控制需求选择合适的工具类可以大大简化并发编程的复杂度。