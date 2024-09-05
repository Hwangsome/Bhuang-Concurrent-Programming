# 为何说只有 1 种实现线程的方法？


## 在Java thread中，当我调用start方法的时候，run方法是何时调用的
在Java中，当你调用一个线程对象的 `start()` 方法时，它会启动一个新的线程，并且这个新的线程会调用该线程对象的 `run()` 方法。具体过程如下：

1. **调用 `start()` 方法**：当你调用线程对象的 `start()` 方法时，Java 虚拟机（JVM）会为该线程分配资源，并将其放入线程调度器的队列中。

2. **线程进入就绪状态**：调用 `start()` 方法后，线程进入“就绪状态”（Runnable）。在这个状态下，线程已经准备好运行，但实际上还没有开始执行。线程何时从“就绪状态”切换到“运行状态”是由线程调度器决定的，这取决于系统的负载、调度策略等。

3. **调用 `run()` 方法**：一旦线程调度器选择这个线程来执行，它将从“就绪状态”切换到“运行状态”。此时，JVM 会自动调用线程对象的 `run()` 方法。`run()` 方法的具体执行内容由你在类中实现的代码决定。

因此，`run()` 方法的调用时间是由线程调度器决定的，可能会在调用 `start()` 方法之后立即执行，也可能会稍作延迟，具体取决于系统资源和线程调度的情况。

简单来说，`start()` 方法并不会立即执行 `run()` 方法，而是启动一个新线程，让这个新线程去执行 `run()` 方法的代码。


## 为什么说实现 Runnable 接口比继承 Thread 类实现线程要好？
在 Java 中，实现 `Runnable` 接口通常被认为比直接继承 `Thread` 类来实现线程要更好，主要有以下几个原因：

### 1. **Java 的单继承限制**
Java 只允许一个类继承一个父类。如果你继承了 `Thread` 类，那么这个类就无法再继承其他类了。通过实现 `Runnable` 接口，你的类可以同时继承其他类并且仍然可以被线程执行。

### 2. **解耦线程任务与线程控制**
通过实现 `Runnable` 接口，你将任务代码与线程控制分离开来。`Runnable` 接口仅包含 `run()` 方法，它表示线程要执行的任务，而线程的控制逻辑（如启动、暂停等）由 `Thread` 类来处理。这种解耦可以让任务更容易复用和测试。

### 3. **增强灵活性**
实现 `Runnable` 接口的任务对象可以被多个线程对象共享和复用。你可以创建一个 `Runnable` 实现并将它传递给多个 `Thread` 对象，这样多个线程可以执行相同的任务代码。这种灵活性在很多情况下是很有用的。

### 4. **支持更好的设计**
实现 `Runnable` 接口符合面向对象设计原则中的“组合优于继承”原则。通过组合（即将 `Runnable` 作为一个属性传递给 `Thread`），你可以更灵活地设计你的类，并且避免了不必要的类层次结构。

### 5. **简化单任务类**
如果你的类只是为了执行某个任务，那么实现 `Runnable` 接口要比继承 `Thread` 类更加简洁。继承 `Thread` 类会把与线程管理相关的方法也带入到子类中，而这些方法在大多数情况下并不需要。

### 总结
通过实现 `Runnable` 接口，你可以避免 Java 的单继承限制，增强代码的灵活性和可维护性，遵循面向对象设计的最佳实践，并保持任务代码与线程控制的分离。这些优点使得在大多数情况下，使用 `Runnable` 接口比直接继承 `Thread` 类要更好。


# 02 如何正确停止线程？为什么 volatile 标记位的停止方法是错误的？
## 原理介绍
通常情况下，我们不会手动停止一个线程，而是允许线程运行到结束，然后让它自然停止。但是依然会有许多特殊的情况需要我们提前停止线程，比如：用户突然关闭程序，或程序运行出错重启等。
在这种情况下，即将停止的线程在很多业务场景下仍然很有价值。尤其是我们想写一个健壮性很好，能够安全应对各种场景的程序时，正确停止线程就显得格外重要。但是Java 并没有提供简单易用，能够直接安全停止线程的能力。

## 为什么不强制停止？而是通知、协作






# 04 waitnotifynotifyAll 方法的使用注意事项？

## 为什么在使用 wait() 方法时，必须将其放在 synchronized 保护的同步代码块中
你描述的场景准确地揭示了为什么在使用 `wait()` 方法时，必须将其放在 `synchronized` 保护的同步代码块中。这个设计的好处主要体现在以下几个方面：

### 1. **原子性和线程安全**
- **原子性**：通过将 `wait()` 放在同步代码块中，能够确保“检查条件”和“执行 `wait()`”这两个步骤是原子操作。也就是说，当一个线程检查条件并决定调用 `wait()` 时，其他线程无法在中间插入修改条件的操作，从而避免了“判断-执行”之间的竞态条件。
- **线程安全**：同步代码块（即 `synchronized` 保护的代码块）确保了多个线程对共享资源的操作不会发生冲突。只有持有锁的线程可以访问共享资源，确保了条件判断和 `wait()` 的执行在同一个锁的保护下是线程安全的。

### 2. **避免错失通知（Spurious Wakeups）**
- **防止虚假唤醒**：Java 允许线程在没有 `notify()` 或 `notifyAll()` 调用的情况下被唤醒，这种情况称为“虚假唤醒（Spurious Wakeups）”。为了应对这种情况，使用 `while` 循环而不是 `if` 来检查条件是否满足是必需的。`while` 循环可以确保在被唤醒后，线程会再次检查条件，只有在条件满足的情况下才继续执行后续操作。
- **条件再检查**：如果 `wait()` 不在同步代码块中使用，线程可能会错失其他线程对条件的更新，导致错误的判断和行为。通过将 `wait()` 放在同步代码块中，可以确保线程在醒来后能够重新检查条件，确保操作的正确性。

### 3. **避免死锁和竞态条件**
- **避免竞态条件**：正如你提到的，假设 `wait()` 不在同步代码块中使用，那么在判断条件和执行 `wait()` 之间，另一个线程可能已经改变了共享资源的状态（例如，生产者线程可能已经添加了数据并发出了 `notify()`），但由于 `wait()` 还未执行，线程会错过这次通知，导致程序出现死锁或无限等待的情况。
- **同步锁的保障**：通过将 `wait()`、`notify()`、`notifyAll()` 放在同步代码块中，可以确保这些方法调用的顺序和逻辑符合预期，不会因为竞态条件导致不正确的程序行为。

### 示例代码改进

以下是对你提供的 `BlockingQueue` 示例代码的改进，通过使用 `synchronized` 来确保线程的安全性：

```java
import java.util.LinkedList;
import java.util.Queue;

class BlockingQueue {

    private final Queue<String> buffer = new LinkedList<>();

    public synchronized void give(String data) {
        buffer.add(data);
        notify();  // 唤醒等待线程
    }

    public synchronized String take() throws InterruptedException {
        while (buffer.isEmpty()) {  // 使用 while 循环防止虚假唤醒
            wait();  // 进入等待，并释放锁
        }
        return buffer.remove();
    }
}
```

### 运行逻辑

1. **`give()` 方法**：生产者线程调用 `give()` 方法，将数据添加到 `buffer` 中，并调用 `notify()` 方法通知等待的消费者线程。由于该方法在同步代码块中，保证了生产者在添加数据时，消费者线程无法访问 `buffer`，防止并发问题。

2. **`take()` 方法**：消费者线程调用 `take()` 方法时，会首先检查 `buffer` 是否为空。如果为空，则调用 `wait()` 方法，使自己进入等待状态，并释放锁。由于 `wait()` 方法在同步块中，确保了条件检查和 `wait()` 调用的原子性。

3. **`notify()` 的作用**：当生产者线程调用 `notify()` 后，消费者线程会被唤醒，并在 `wait()` 返回时重新获得锁，继续执行剩余的代码。

### 总结

- **`synchronized` 的必要性**：通过将 `wait()` 和 `notify()` 放在 `synchronized` 代码块中，保证了线程对共享资源的访问是安全的，并避免了竞态条件和死锁问题。
- **`while` 循环的作用**：在 `wait()` 前使用 `while` 而不是 `if`，确保了线程在被唤醒后能够正确地重新检查条件，防止虚假唤醒导致的错误行为。

这种设计有效地保证了生产者-消费者模式中的线程安全性和程序的正确性。

## 通过将 wait() 放在同步代码块中，可以确保线程在醒来后能够重新检查条件，确保操作的正确性。检查什么条件？
在使用 `wait()` 的典型场景中，线程会等待某个特定条件的满足。这个条件通常与共享资源的状态有关，例如队列是否为空、缓存是否已满、某个标志位是否设置等等。通过将 `wait()` 放在同步代码块中，可以确保线程在被唤醒后能够重新检查这些条件，以确定是否可以继续执行后续操作。

### 具体的条件

条件是指线程在继续执行前需要确认的某些状态。这些状态的检查通常通过一个布尔表达式来实现，典型的条件包括：

1. **缓冲区是否为空/已满**：
    - 在生产者-消费者模型中，消费者线程在从缓冲区中获取数据之前，通常需要检查缓冲区是否为空。
    - 生产者线程在向缓冲区中添加数据之前，可能需要检查缓冲区是否已满。

2. **任务队列是否有任务**：
    - 在一个任务处理系统中，工作线程可能会等待，直到任务队列中有任务可以处理。

3. **某个标志位是否被设置**：
    - 某些情况下，线程可能会等待一个标志位的设置，例如等待某个操作完成的信号。

### 示例：生产者-消费者模型中的条件

假设我们有一个简单的生产者-消费者模型，其中消费者从队列中取数据，生产者向队列中添加数据。

```java
import java.util.LinkedList;
import java.util.Queue;

class BlockingQueue {
    private final Queue<String> buffer = new LinkedList<>();
    private final int capacity = 10;

    public synchronized void give(String data) throws InterruptedException {
        while (buffer.size() == capacity) {
            wait();  // 等待直到缓冲区有空间
        }
        buffer.add(data);
        notifyAll();  // 唤醒所有等待的线程
    }

    public synchronized String take() throws InterruptedException {
        while (buffer.isEmpty()) {
            wait();  // 等待直到缓冲区有数据
        }
        String result = buffer.remove();
        notifyAll();  // 唤醒所有等待的线程
        return result;
    }
}
```

### 线程检查的条件

- **消费者线程的条件**：`while (buffer.isEmpty())`
    - 消费者在 `take()` 方法中调用 `wait()` 之前，检查队列（缓冲区）是否为空。
    - 只有在缓冲区非空时，消费者才会继续执行并从缓冲区中取出数据。
    - 如果缓冲区为空，消费者线程会调用 `wait()`，等待生产者线程向缓冲区中添加数据，并在 `notifyAll()` 后重新检查 `buffer.isEmpty()` 的条件。

- **生产者线程的条件**：`while (buffer.size() == capacity)`
    - 生产者在 `give()` 方法中调用 `wait()` 之前，检查缓冲区是否已满（`buffer.size() == capacity`）。
    - 只有在缓冲区未满时，生产者才会继续执行并将数据添加到缓冲区中。
    - 如果缓冲区已满，生产者线程会调用 `wait()`，等待消费者线程从缓冲区中取出数据，并在 `notifyAll()` 后重新检查 `buffer.size() == capacity` 的条件。

### 为什么需要重新检查条件？

1. **防止虚假唤醒（Spurious Wakeups）**：
    - 虚假唤醒指的是线程在没有任何 `notify` 或 `notifyAll` 调用的情况下被唤醒。为了应对这种情况，即使线程被唤醒，它也应该重新检查条件是否满足。

2. **竞争条件的处理**：
    - 在多线程环境中，多个线程可能会竞争同一资源。例如，多个消费者线程可能被同时唤醒，但只有一个线程能够首先获取数据。为了防止其他线程在没有满足条件的情况下继续执行，每个线程在被唤醒后都应重新检查条件。

3. **确保程序的正确性**：
    - 通过重新检查条件，可以确保线程在执行操作之前，资源的状态是符合预期的。这种机制防止了由于条件不满足而引起的错误操作。

### 总结

当线程被唤醒时，重新检查条件是为了确保共享资源的状态符合预期，并且线程可以安全地继续执行其任务。这个检查条件的过程保证了线程在多线程环境下的正确性和安全性。


## wait & notify
`wait()` 方法是 Java 中线程间通信的一部分，用于让当前线程进入等待状态，直到被其他线程唤醒。它通常与 `notify()` 或 `notifyAll()` 方法配合使用，以实现线程之间的协作。下面是 `wait()` 方法的详细作用和原理。

### `wait()` 方法的作用

1. **释放锁并进入等待状态**：
    - 当一个线程调用 `wait()` 方法时，它会释放当前持有的对象锁（也称为监视器锁），并进入该对象的等待队列。
    - 线程进入等待状态后，直到另一个线程在同一个对象上调用 `notify()` 或 `notifyAll()` 方法，它才会被唤醒。

2. **等待条件的改变**：
    - `wait()` 方法通常用于在某个条件不满足时，暂停线程的执行，直到条件发生变化。
    - 例如，在生产者-消费者模型中，消费者线程在缓冲区为空时调用 `wait()`，等待生产者添加数据后再被唤醒。

3. **防止忙等待（Busy Waiting）**：
    - `wait()` 的使用可以避免忙等待，即线程不停地检查条件，浪费 CPU 资源。相反，`wait()` 让线程进入等待状态，直到条件满足时再被唤醒，从而提高了效率。

4. **恢复执行**：
    - 当一个线程被 `notify()` 或 `notifyAll()` 方法唤醒后，它会重新尝试获取对象锁。如果成功获取锁，线程会继续从 `wait()` 方法之后的代码执行。

### `wait()` 方法的工作机制

1. **线程必须持有对象锁**：
    - `wait()` 方法必须在同步方法或同步块中调用，因为它要求调用线程已经持有对象的锁。如果线程在没有持有锁的情况下调用 `wait()`，会抛出 `IllegalMonitorStateException`。

2. **释放锁和进入等待**：
    - 当线程调用 `wait()` 方法时，它会释放当前持有的锁，并将自己放入该对象的等待队列中，同时暂停执行。

3. **唤醒线程**：
    - 另一个线程可以调用 `notify()` 或 `notifyAll()` 方法，唤醒在该对象上等待的线程。`notify()` 唤醒一个线程，而 `notifyAll()` 唤醒所有等待线程。
    - 被唤醒的线程在返回 `wait()` 之后会重新尝试获取锁。只有在获取锁后，它才能继续执行后续代码。

4. **重新检查条件**：
    - 通常，`wait()` 会与 `while` 循环一起使用，以确保线程被唤醒后重新检查条件是否满足（这也是为了防止虚假唤醒）。

### 示例代码

```java
class SharedResource {
    private boolean condition = false;

    public synchronized void awaitCondition() throws InterruptedException {
        while (!condition) {
            wait();  // 进入等待状态，释放锁
        }
        // 条件满足后继续执行
        System.out.println("Condition met, proceeding...");
    }

    public synchronized void signalCondition() {
        condition = true;
        notify();  // 唤醒等待的线程
    }
}

public class WaitNotifyExample {
    public static void main(String[] args) throws InterruptedException {
        SharedResource sharedResource = new SharedResource();

        Thread waitingThread = new Thread(() -> {
            try {
                sharedResource.awaitCondition();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        Thread signalingThread = new Thread(() -> {
            try {
                Thread.sleep(1000);  // 模拟一些工作
                sharedResource.signalCondition();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        waitingThread.start();
        signalingThread.start();

        waitingThread.join();
        signalingThread.join();
    }
}
```

### 解释

- `awaitCondition()` 方法使用 `wait()` 方法在条件不满足时进入等待状态。
- `signalCondition()` 方法改变条件并调用 `notify()` 唤醒等待的线程。
- 这个例子展示了一个线程如何等待条件的满足，而另一个线程如何通知并唤醒它。

### 总结

`wait()` 方法是 Java 中线程间通信的重要工具，允许线程在某个条件未满足时进入等待状态，并在条件改变时被唤醒。这种机制有效地防止了忙等待，提高了多线程程序的效率和性能。


## 当线程调用 wait() 方法时，涉及两个关键操作：释放对象锁（监视器锁） 和 进入对象的等待队列。
当线程调用 `wait()` 方法时，涉及两个关键操作：**释放对象锁（监视器锁）** 和 **进入对象的等待队列**。为了更好地理解这个过程，下面将详细解释这两个操作。

### 1. **对象锁（监视器锁）**
- **对象锁**：在Java中，每个对象都有一个隐式的锁（也称为监视器锁），这个锁用于控制对同步代码块或同步方法的访问。只有持有这个锁的线程才能进入相应的同步代码块。
- **获取锁**：当一个线程进入一个 `synchronized` 方法或 `synchronized` 块时，它会尝试获取这个对象的锁。如果成功获取，线程就可以执行这个同步代码块中的代码，其他尝试进入相同同步代码块的线程则必须等待，直到这个锁被释放。
- **释放锁**：当线程退出 `synchronized` 代码块时，它会释放这个对象的锁，允许其他线程获取锁并进入同步代码块。

### 2. **调用 `wait()` 时发生的事情**

当线程在一个同步代码块或同步方法中调用 `wait()` 方法时，会发生以下步骤：

#### a) **释放当前持有的对象锁**
- 在调用 `wait()` 方法时，线程会主动释放它持有的对象锁。这个释放锁的操作意味着其他等待这个锁的线程现在可以尝试获取锁并进入同步代码块。
- 释放锁是为了让其他线程能够执行与这个对象相关的操作（例如，生产者线程添加数据，消费者线程消费数据），从而可能改变程序的状态，使得条件得以满足。

#### b) **进入该对象的等待队列**
- 释放锁后，调用 `wait()` 的线程不会立即继续执行，而是进入这个对象的等待队列。等待队列是与每个对象关联的一个内部数据结构，保存了所有在这个对象上调用了 `wait()` 并正在等待的线程。
- 在进入等待队列后，线程的状态会从 "运行中" 或 "可运行" 变为 "等待" 状态，这意味着线程在等待某个条件满足时不会占用CPU资源。

### 3. **等待队列和唤醒机制**
- 当线程处于等待队列中时，它不会继续执行，除非有其他线程在同一个对象上调用 `notify()` 或 `notifyAll()` 方法。
- **`notify()`**：唤醒等待队列中的一个线程。如果有多个线程在等待，哪个线程被唤醒是由JVM决定的。
- **`notifyAll()`**：唤醒等待队列中的所有线程。

被唤醒的线程会尝试重新获取之前释放的对象锁。如果它成功获取锁，那么线程会从 `wait()` 方法返回，并继续执行 `wait()` 之后的代码。如果它无法立即获取锁，则线程会继续等待，直到锁可用为止。

### 4. **一个例子来说明这一过程**

```java
class Example {
    public synchronized void exampleMethod() throws InterruptedException {
        System.out.println("Thread " + Thread.currentThread().getName() + " is waiting.");
        wait();  // 释放锁并进入等待队列
        System.out.println("Thread " + Thread.currentThread().getName() + " is resumed.");
    }

    public synchronized void resumeThread() {
        notify();  // 唤醒一个等待中的线程
        System.out.println("A thread is notified.");
    }
}

public class WaitNotifyDemo {
    public static void main(String[] args) throws InterruptedException {
        Example example = new Example();

        Thread thread1 = new Thread(() -> {
            try {
                example.exampleMethod();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        Thread thread2 = new Thread(() -> {
            try {
                example.exampleMethod();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        thread1.start();
        thread2.start();

        Thread.sleep(1000);  // 确保 thread1 和 thread2 已经在 wait() 中等待

        example.resumeThread();  // 唤醒一个线程
    }
}
```

### 解释

1. `thread1` 和 `thread2` 先后调用 `exampleMethod()`，它们都会进入 `wait()`，并释放锁。此时，它们进入 `Example` 对象的等待队列。
2. 当 `resumeThread()` 方法被调用时，`notify()` 唤醒等待队列中的一个线程（`thread1` 或 `thread2` 其中之一）。
3. 被唤醒的线程尝试重新获取锁，并继续执行 `wait()` 之后的代码，打印 `"Thread X is resumed."`。

### 总结

- **释放锁**：调用 `wait()` 时，线程会释放它持有的对象锁，允许其他线程执行。
- **进入等待队列**：线程进入对象的等待队列，等待 `notify()` 或 `notifyAll()` 方法的唤醒。
- **线程通信**：通过这种机制，可以在多线程环境中安全、有效地进行线程间的通信和协作。

## 什么是对象的等待队列
对象的等待队列（Wait Queue）是与每个Java对象关联的一个内部数据结构，用于存储在该对象上调用了 `wait()` 方法并进入等待状态的线程。等待队列在Java的多线程同步机制中起着关键作用，特别是在使用 `wait()`、`notify()` 和 `notifyAll()` 进行线程间通信时。

### 具体作用

1. **线程等待管理**：
    - 当一个线程在某个对象上调用 `wait()` 方法时，该线程会释放对象的锁并进入该对象的等待队列。这个线程不会继续执行，直到被唤醒为止。

2. **线程唤醒管理**：
    - 当另一个线程调用该对象的 `notify()` 或 `notifyAll()` 方法时，会从该对象的等待队列中唤醒一个或多个等待的线程。被唤醒的线程会尝试重新获得该对象的锁，如果成功获取锁，它将继续执行 `wait()` 方法后面的代码。

### 如何使用等待队列

等待队列是JVM实现的一部分，作为开发者，不能直接访问或操作等待队列，但可以通过 `wait()`、`notify()` 和 `notifyAll()` 方法间接影响它。以下是这些方法如何与等待队列交互：

1. **`wait()` 方法**：
    - 当一个线程在同步代码块中调用 `wait()` 方法时，线程会释放对象的锁，并进入该对象的等待队列。线程将保持等待状态，直到被其他线程唤醒。

2. **`notify()` 方法**：
    - `notify()` 方法用于唤醒在该对象的等待队列中等待的一个线程。被唤醒的线程会尝试重新获取对象的锁，继续执行 `wait()` 后面的代码。
    - 如果等待队列中有多个线程，JVM会随机选择一个线程唤醒。

3. **`notifyAll()` 方法**：
    - `notifyAll()` 方法用于唤醒在该对象的等待队列中等待的所有线程。所有被唤醒的线程会依次尝试获取对象的锁，只有成功获取锁的线程才能继续执行。

### 示例代码解释

以下代码展示了对象的等待队列是如何通过 `wait()` 和 `notify()` 方法来管理线程的：

```java
class Example {
    public synchronized void exampleMethod() throws InterruptedException {
        System.out.println("Thread " + Thread.currentThread().getName() + " is waiting.");
        wait();  // 进入等待队列，释放锁
        System.out.println("Thread " + Thread.currentThread().getName() + " is resumed.");
    }

    public synchronized void resumeThread() {
        notify();  // 唤醒等待队列中的一个线程
        System.out.println("A thread is notified.");
    }
}

public class WaitNotifyDemo {
    public static void main(String[] args) throws InterruptedException {
        Example example = new Example();

        Thread thread1 = new Thread(() -> {
            try {
                example.exampleMethod();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        Thread thread2 = new Thread(() -> {
            try {
                example.exampleMethod();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        thread1.start();
        thread2.start();

        Thread.sleep(1000);  // 确保 thread1 和 thread2 已经进入等待队列

        example.resumeThread();  // 唤醒等待队列中的一个线程
    }
}
```

### 解释

1. **等待队列的形成**：
    - `thread1` 和 `thread2` 先后调用 `exampleMethod()`，进入 `wait()` 方法。这时，它们被放入 `Example` 对象的等待队列，并释放了该对象的锁。

2. **线程唤醒**：
    - 当主线程调用 `resumeThread()` 时，`notify()` 方法唤醒等待队列中的一个线程（`thread1` 或 `thread2` 之一）。
    - 被唤醒的线程从等待队列中移除，并尝试重新获取锁。如果获取成功，继续执行 `wait()` 后的代码。

3. **锁竞争**：
    - 如果有多个线程被 `notifyAll()` 唤醒，它们都会尝试获取锁，但只有一个线程可以成功获取锁并继续执行，其余的线程仍需等待锁的释放。

### 总结

- **等待队列的本质**：等待队列是Java对象的内部机制，用于管理调用 `wait()` 方法后进入等待状态的线程。
- **作用**：它确保了线程在某个条件不满足时可以安全地进入等待状态，并在条件满足时被正确唤醒。
- **使用场景**：等待队列主要用于实现线程间的协调和通信，尤其是在生产者-消费者模型、资源池管理等场景中。

# 05 有哪几种实现生产者消费者模式的方法？
## 前置知识
在Java中，`Condition` 是一种更高级的线程同步工具，通常与 `Lock` 接口一起使用。`Condition` 提供了与 `wait()`、`notify()` 和 `notifyAll()` 类似的功能，但它更加灵活和强大。`Condition` 的使用主要依赖于 `Lock`，而不是传统的 `synchronized` 块。

### `Condition` 的概述

- **灵活的条件等待**：`Condition` 对象可以让线程等待特定的条件，并提供一种机制来通知等待条件的线程。与 `Object` 的内置监视器方法（`wait()`、`notify()`、`notifyAll()`）相比，`Condition` 提供了更多的灵活性，尤其是在复杂的多线程环境中。

- **与 `Lock` 配合使用**：`Condition` 通常与 `Lock` 接口一起使用，而不是与 `synchronized` 块一起使用。通过 `Lock` 创建 `Condition` 对象，然后线程可以在 `Condition` 上等待或被唤醒。

### `Condition` 的常用方法

- **`await()`**：相当于 `wait()`，使当前线程等待，直到收到信号或被中断。
- **`signal()`**：相当于 `notify()`，唤醒一个等待在该 `Condition` 上的线程。
- **`signalAll()`**：相当于 `notifyAll()`，唤醒所有等待在该 `Condition` 上的线程。

### `Condition` 的使用场景

`Condition` 通常用于需要更灵活的线程同步场景，例如在同一锁上需要多个条件队列的情况。每个 `Condition` 对象可以有一个独立的等待队列，这比 `synchronized` 和 `wait/notify` 的单一等待队列更为灵活。

### 示例代码

以下是一个使用 `Condition` 实现的生产者-消费者模型的示例：

```java
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.LinkedList;
import java.util.Queue;

class BlockingQueueWithCondition {
    private final Queue<String> buffer = new LinkedList<>();
    private final int capacity;
    private final Lock lock = new ReentrantLock();
    private final Condition notFull = lock.newCondition();
    private final Condition notEmpty = lock.newCondition();

    public BlockingQueueWithCondition(int capacity) {
        this.capacity = capacity;
    }

    public void give(String data) throws InterruptedException {
        lock.lock();
        try {
            while (buffer.size() == capacity) {
                notFull.await();  // 缓冲区满，等待消费者取走数据
            }
            buffer.add(data);
            System.out.println("Produced: " + data);
            notEmpty.signal();  // 唤醒等待在 notEmpty 上的消费者
        } finally {
            lock.unlock();
        }
    }

    public String take() throws InterruptedException {
        lock.lock();
        try {
            while (buffer.isEmpty()) {
                notEmpty.await();  // 缓冲区空，等待生产者添加数据
            }
            String data = buffer.remove();
            System.out.println("Consumed: " + data);
            notFull.signal();  // 唤醒等待在 notFull 上的生产者
            return data;
        } finally {
            lock.unlock();
        }
    }
}

class Producer implements Runnable {
    private final BlockingQueueWithCondition queue;

    public Producer(BlockingQueueWithCondition queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        try {
            for (int i = 1; i <= 10; i++) {
                queue.give("Data-" + i);
                Thread.sleep(100);  // 模拟生产时间
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

class Consumer implements Runnable {
    private final BlockingQueueWithCondition queue;

    public Consumer(BlockingQueueWithCondition queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        try {
            for (int i = 1; i <= 10; i++) {
                queue.take();
                Thread.sleep(150);  // 模拟消费时间
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

public class ConditionExample {
    public static void main(String[] args) throws InterruptedException {
        BlockingQueueWithCondition queue = new BlockingQueueWithCondition(5);  // 缓冲区容量为5

        Thread producerThread = new Thread(new Producer(queue));
        Thread consumerThread = new Thread(new Consumer(queue));

        producerThread.start();
        consumerThread.start();

        producerThread.join();
        consumerThread.join();
    }
}
```

### 运行结果分析

当你运行 `ConditionExample` 时，你会看到类似的输出：

```
Produced: Data-1
Consumed: Data-1
Produced: Data-2
Consumed: Data-2
Produced: Data-3
Produced: Data-4
Consumed: Data-3
Produced: Data-5
Produced: Data-6
Consumed: Data-4
Produced: Data-7
Consumed: Data-5
Produced: Data-8
Consumed: Data-6
Produced: Data-9
Consumed: Data-7
Produced: Data-10
Consumed: Data-8
Consumed: Data-9
Consumed: Data-10
```

### 解释

1. **创建条件对象**：在 `BlockingQueueWithCondition` 中，我们使用 `lock.newCondition()` 创建了两个条件对象 `notFull` 和 `notEmpty`。这两个条件对象分别用于处理缓冲区满和缓冲区空的情况。

2. **生产者等待条件**：生产者在缓冲区满时调用 `notFull.await()` 进入等待状态，直到有空间可以添加数据。然后调用 `notEmpty.signal()` 唤醒等待的消费者。

3. **消费者等待条件**：消费者在缓冲区空时调用 `notEmpty.await()` 进入等待状态，直到缓冲区有数据可取。然后调用 `notFull.signal()` 唤醒等待的生产者。

4. **灵活的同步机制**：`Condition` 提供了比 `wait()` 和 `notify()` 更加灵活的线程同步机制，允许在同一 `Lock` 上定义多个条件对象，分别管理不同的等待队列。

### 总结

- **`Condition` 的优点**：相比传统的 `synchronized` 块和 `wait/notify`，`Condition` 提供了更强大的功能，特别是在需要多个条件变量的复杂场景中。

- **灵活的等待和通知机制**：通过 `Condition`，可以更灵活地控制线程何时等待、何时被唤醒，从而精确地管理并发任务。

- **与 `Lock` 配合使用**：`Condition` 的使用必须与 `Lock`（通常是 `ReentrantLock`）配合，以替代 `synchronized` 和内置的监视器方法。


# 06 一共有哪 3 类线程安全问题？
在多线程编程中，线程安全问题通常可以归类为以下三大类：

### 1. **竞态条件（Race Conditions）**

**概念**: 竞态条件是指多个线程在不正确的同步下访问共享资源时，由于线程执行的顺序不确定，导致程序的行为无法预测和控制。换句话说，程序的输出或状态取决于线程的执行顺序。

**例子**: 假设两个线程同时对一个共享变量进行递增操作，如果没有正确的同步措施，最终结果可能会比预期的少，因为可能存在以下的执行顺序：

- 线程 A 读取共享变量值 10。
- 线程 B 读取共享变量值 10。
- 线程 A 将共享变量值加 1 并写回，结果是 11。
- 线程 B 将共享变量值加 1 并写回，结果是 11（而不是 12，因为线程 B 读取的是线程 A 修改前的值）。

**防范措施**:
- 使用 `synchronized` 块、`Lock` 或者 `Atomic` 类等机制，确保共享资源的访问是线程安全的。

### 2. **死锁（Deadlock）**

**概念**: 死锁发生在两个或多个线程相互等待对方释放资源时，导致它们永久阻塞。每个线程都持有一个资源，并且在等待其他线程释放它们需要的资源，形成一个闭环。

**例子**: 假设线程 A 持有资源 X，并且等待资源 Y，而线程 B 持有资源 Y，并且等待资源 X。由于两个线程都在等待对方释放资源，这种情况就会导致死锁。

**防范措施**:
- 避免嵌套锁定（减少锁的嵌套使用）。
- 遵循一致的锁定顺序，确保所有线程以相同的顺序请求锁。
- 使用锁的超时机制（如 `tryLock()`）避免无限等待。
- 使用 `Deadlock Detector` 等工具检测和解决死锁问题。

### 3. **内存可见性问题（Memory Visibility Issues）**

**概念**: 内存可见性问题指的是一个线程对共享变量的修改，另一个线程无法立即看到，导致程序行为异常。Java 中的线程在访问共享变量时，可能会从缓存中读取，而不是从主存中读取，导致内存不可见性问题。

**例子**: 假设线程 A 修改了某个共享变量的值，但线程 B 并未及时看到这个修改，仍然使用旧值进行计算。即使在不发生竞态条件的情况下，程序也可能表现出错误的行为。

**防范措施**:
- 使用 `volatile` 关键字，确保共享变量在多个线程之间的可见性。
- 使用 `synchronized`、`Lock` 等机制，不仅保证原子性，还能确保内存可见性。
- 使用高层次的并发工具，如 `ConcurrentHashMap`、`CopyOnWriteArrayList`，它们在内部处理了同步和可见性问题。

### 总结

1. **竞态条件**: 主要问题是多个线程竞争访问共享资源，导致结果不确定或错误。通过同步机制来防止。
2. **死锁**: 主要问题是线程间相互等待资源，导致线程永久阻塞。通过设计避免嵌套锁定或使用超时机制等方法来防止。
3. **内存可见性问题**: 主要问题是线程间共享数据的修改无法及时被其他线程看到。通过 `volatile`、同步机制等来确保可见性。



75 为什么需要 AQS？AQS 的作用和重要性是什么？
