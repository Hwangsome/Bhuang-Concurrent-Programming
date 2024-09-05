`LockSupport` 是 Java 并发包中的一个工具类，提供了最基础的线程阻塞和唤醒功能。它与传统的 `Object.wait()` 和 `Thread.sleep()` 不同，因为 `LockSupport` 提供了更低级别的、基于许可证的阻塞机制。这使得 `LockSupport` 成为实现高级同步工具（如 `ReentrantLock`、`CountDownLatch`、`Semaphore` 等）的核心组件。

### 一、`LockSupport` 的基本概念

`LockSupport` 的核心机制是基于**许可证（Permit）**的阻塞和唤醒模型。每个线程都有一个与之关联的许可证，它的初始状态是没有许可证的。`LockSupport` 提供的 `park()` 和 `unpark()` 方法分别用于阻塞和唤醒线程。

- **`park()`**：使当前线程进入等待状态（阻塞），直到它被唤醒或中断。阻塞状态的解除依赖于线程是否持有许可证。
- **`unpark(Thread t)`**：唤醒指定线程 `t`，通过为线程 `t` 提供许可证来解除阻塞。如果线程尚未调用 `park()`，`unpark()` 将使其下次调用 `park()` 时立即返回。

### 二、`LockSupport` 的主要方法

#### 1. **`park()`**

```java
public static void park();
```

- `park()` 方法会阻塞当前线程，直到线程获得许可证或被中断。
- 如果线程在调用 `park()` 之前已经被 `unpark()` 授予许可证，`park()` 将立即返回，不会阻塞。

#### 2. **`parkNanos(long nanos)`**

```java
public static void parkNanos(long nanos);
```

- `parkNanos()` 会使线程在指定的纳秒数内阻塞，或者直到线程被唤醒或中断。

#### 3. **`parkUntil(long deadline)`**

```java
public static void parkUntil(long deadline);
```

- `parkUntil()` 会使线程阻塞到指定的时间点（基于系统时间的绝对值），或者直到线程被唤醒或中断。

#### 4. **`unpark(Thread t)`**

```java
public static void unpark(Thread thread);
```

- `unpark()` 方法用于唤醒指定线程 `t`，通过为该线程提供许可证。如果该线程已被阻塞（通过 `park()`），调用 `unpark()` 会使它立即返回。如果线程未被阻塞，许可证将被保存，供以后使用。

### 三、`LockSupport` 的工作原理

`LockSupport` 的阻塞和唤醒是基于许可证模型的，主要机制如下：

1. **许可证模型**：
    - 每个线程有一个隐含的许可证，最初线程没有许可证。
    - `park()` 方法会检查线程是否有许可证，如果没有则阻塞线程。
    - `unpark()` 方法为线程授予许可证，如果线程当前被阻塞，则立即唤醒它。

2. **许可证的消耗**：
    - 一旦 `park()` 成功返回，许可证就被消耗掉了，即线程必须再次调用 `unpark()` 才能解除下一次的阻塞。

3. **FIFO 唤醒**：
    - `unpark()` 机制保证了 FIFO 的唤醒顺序，即最早阻塞的线程优先被唤醒。

### 四、`LockSupport` 使用示例

下面是一个简单的示例，展示如何使用 `LockSupport` 来实现线程的阻塞和唤醒：

```java
public class LockSupportDemo {
    public static void main(String[] args) {
        Thread thread = new Thread(() -> {
            System.out.println("Thread is going to park.");
            LockSupport.park(); // 阻塞当前线程
            System.out.println("Thread is unparked.");
        });

        thread.start();

        try {
            // 主线程等待一段时间，确保子线程已进入 park 状态
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Main thread is going to unpark the thread.");
        LockSupport.unpark(thread); // 唤醒子线程
    }
}
```

### 输出结果

```
Thread is going to park.
Main thread is going to unpark the thread.
Thread is unparked.
```

### 五、与传统方法的比较

#### 1. **与 `Object.wait()` 和 `notify()` 的比较**：
- `LockSupport` 更加灵活，因为它不需要配合 `synchronized` 使用，并且可以对指定的线程进行唤醒。
- `Object.wait()` 必须在同步块或同步方法内调用，而 `LockSupport.park()` 可以在任何地方调用。
- `LockSupport` 提供的许可机制使得 `park()` 和 `unpark()` 不会发生“丢失信号”的问题，即 `unpark()` 可以先于 `park()` 调用，而不会造成线程永远等待。

#### 2. **与 `Thread.sleep()` 的比较**：
- `Thread.sleep()` 是让线程暂停执行一段时间后自动恢复执行，而 `LockSupport.park()` 则是让线程一直阻塞，直到它被显式唤醒或中断。
- `Thread.sleep()` 无法被 `unpark()` 唤醒，而 `park()` 可以。

### 六、实际应用场景

`LockSupport` 通常用于实现更高级的同步工具或优化并发算法。例如：

- **锁和同步器**：`LockSupport` 是实现 `ReentrantLock`、`Semaphore`、`CountDownLatch` 等工具的核心组件之一。
- **线程调度**：用于实现自定义的线程调度逻辑，尤其是在高并发或低延迟要求的场景下。

### 七、注意事项

1. **许可证的使用**：许可证的授予和消耗是一次性的。如果你在调用 `park()` 之前没有调用 `unpark()`，那么 `park()` 会一直阻塞，直到许可证被授予。
2. **中断处理**：虽然 `park()` 可以响应线程中断，但它不会抛出 `InterruptedException`，你需要手动检查线程的中断状态。
3. **公平性**：在使用 `LockSupport` 时，唤醒线程的顺序遵循 FIFO（先入先出）原则。

### 八、总结

`LockSupport` 是一个非常强大和灵活的工具，它提供了底层的线程阻塞和唤醒机制。与传统的 `Object.wait()` 和 `Thread.sleep()` 不同，`LockSupport` 提供了基于许可证的模型，使得它能够更精确地控制线程的阻塞和唤醒。由于其低级别的特性，`LockSupport` 常用于实现更高级的并发结构，是 Java 并发包中不可或缺的一部分。