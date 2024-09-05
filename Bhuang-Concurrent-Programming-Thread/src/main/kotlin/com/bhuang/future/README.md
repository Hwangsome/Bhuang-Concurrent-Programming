# Future
`Future` 是 Java 并发编程中非常重要的一个接口，它提供了一种机制，用于表示一个**异步任务的执行结果**。当我们在多线程环境下提交一个任务时，任务的执行可能是异步的，即任务提交后不会立即得到结果。`Future` 提供了一种方式，让我们可以在任务执行完成后获取它的结果，或者检查任务的状态。
创建异步作业时会返回一个 Java Future 对象。此 Future 对象用作异步任务结果的句柄。

### 一、`Future` 接口简介

`Future` 接口位于 `java.util.concurrent` 包中，通常与 `ExecutorService.submit()` 方法一起使用，用来处理**异步任务**的执行结果。

#### 1. `Future` 的常用方法

`Future` 提供了多个方法，用来操作异步任务的执行结果或状态：

```java
public interface Future<V> {
    boolean cancel(boolean mayInterruptIfRunning);  // 尝试取消任务
    boolean isCancelled();                         // 判断任务是否已取消
    boolean isDone();                              // 判断任务是否已完成
    V get() throws InterruptedException, ExecutionException;  // 阻塞并等待任务完成，返回任务结果
    V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException;  // 阻塞等待指定时间，获取任务结果
}
```

- **`cancel()`**：用于取消任务。如果任务已经开始执行，参数 `mayInterruptIfRunning` 决定是否可以中断正在执行的任务。
- **`isCancelled()`**：判断任务是否已取消。
- **`isDone()`**：判断任务是否已经完成，无论是正常完成还是被取消。
- **`get()`**：阻塞当前线程，直到任务执行完成，返回任务的结果。如果任务抛出了异常，`get()` 会抛出 `ExecutionException`。
- **`get(long timeout, TimeUnit unit)`**：与 `get()` 类似，但只会等待指定的时间，如果超时则抛出 `TimeoutException`。

#### 2. `Future` 的泛型

`Future` 是一个泛型接口，它的类型参数 `V` 代表任务的返回结果类型。比如，如果提交的是一个 `Callable<Integer>` 任务，`Future` 的类型参数就是 `Integer`，表示任务返回的结果是一个 `Integer` 类型。

### 二、`Future` 的使用

`Future` 通常与 `ExecutorService` 一起使用。我们通过 `ExecutorService.submit()` 方法提交任务，并返回一个 `Future` 对象。任务执行完成后，可以通过 `Future.get()` 获取任务的结果。

#### 1. **示例代码**

```java
import java.util.concurrent.*;

public class FutureExample {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        // 创建线程池
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // 提交一个 Callable 任务，并获取 Future
        Future<Integer> future = executor.submit(() -> {
            System.out.println("Task is running...");
            Thread.sleep(2000);  // 模拟任务执行时间
            return 42;  // 返回结果
        });

        // 在等待任务完成时，可以做其他事情
        System.out.println("Main thread is doing something else...");

        // 阻塞，直到任务完成并获取结果
        Integer result = future.get();  // 会等待任务完成
        System.out.println("Task result: " + result);

        // 关闭线程池
        executor.shutdown();
    }
}
```

#### 2. **输出结果**：

```
Task is running...
Main thread is doing something else...
Task result: 42
```

在这个例子中，任务执行是异步的，`Future.get()` 方法会阻塞主线程，直到任务完成并返回结果。期间主线程可以做其他工作。

### 三、`Future` 的取消与状态检查

`Future` 提供了 `cancel()` 方法，用于尝试取消任务，并提供了 `isCancelled()` 和 `isDone()` 方法，用于检查任务的状态。

#### 1. **取消任务**

`cancel(boolean mayInterruptIfRunning)` 尝试取消任务。它可以接受一个布尔参数 `mayInterruptIfRunning`，指示是否中断正在运行的任务：
- **`mayInterruptIfRunning = true`**：如果任务正在运行，可以中断任务。
- **`mayInterruptIfRunning = false`**：如果任务已经启动但尚未完成，不会中断任务。

任务只有在还未执行或正在执行时可以被取消，如果任务已经完成，则取消无效。

#### 2. **任务状态检查**

- **`isCancelled()`**：返回 `true` 表示任务已经被取消。
- **`isDone()`**：返回 `true` 表示任务已经完成，无论是正常完成还是被取消。

#### 3. **示例代码：任务取消与状态检查**

```java
import java.util.concurrent.*;

public class FutureCancelExample {
    public static void main(String[] args) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(1);

        // 提交一个任务
        Future<?> future = executor.submit(() -> {
            try {
                System.out.println("Task started...");
                Thread.sleep(5000);  // 模拟任务耗时
                System.out.println("Task completed...");
            } catch (InterruptedException e) {
                System.out.println("Task was interrupted");
            }
        });

        // 等待一段时间后尝试取消任务
        Thread.sleep(1000);
        System.out.println("Attempting to cancel the task...");
        future.cancel(true);  // 尝试取消任务

        // 检查任务状态
        if (future.isCancelled()) {
            System.out.println("Task was cancelled.");
        } else if (future.isDone()) {
            System.out.println("Task completed.");
        }

        // 关闭线程池
        executor.shutdown();
    }
}
```

#### 输出结果：

```
Task started...
Attempting to cancel the task...
Task was interrupted
Task was cancelled.
```

在这个例子中，任务在启动后被主线程取消，任务抛出了 `InterruptedException` 并被取消。

### 四、`Future` 的超时等待

`Future` 提供了带超时的 `get(long timeout, TimeUnit unit)` 方法，用来在指定的时间内等待任务完成，如果任务在指定时间内没有完成，会抛出 `TimeoutException`。

#### 示例代码：带超时的任务等待

```java
import java.util.concurrent.*;

public class FutureTimeoutExample {
    public static void main(String[] args) throws InterruptedException, ExecutionException, TimeoutException {
        ExecutorService executor = Executors.newFixedThreadPool(1);

        // 提交一个任务
        Future<Integer> future = executor.submit(() -> {
            System.out.println("Task started...");
            Thread.sleep(3000);  // 模拟任务耗时
            return 42;
        });

        try {
            // 等待最多 1 秒获取结果，如果任务没有完成，会抛出 TimeoutException
            Integer result = future.get(1, TimeUnit.SECONDS);
            System.out.println("Task result: " + result);
        } catch (TimeoutException e) {
            System.out.println("Task did not complete in the specified time.");
        }

        // 关闭线程池
        executor.shutdown();
    }
}
```

#### 输出结果：

```
Task started...
Task did not complete in the specified time.
```

在这个例子中，任务的执行时间是 3 秒，而我们只等待了 1 秒，因此抛出了 `TimeoutException`。

### 五、`Future` 的实现原理

`Future` 本身只是一个接口，它的具体实现由线程池中的 `FutureTask` 类完成。`FutureTask` 是 `Runnable` 和 `Future` 的一个实现类，既可以提交给线程池执行，也可以用于获取执行结果。

`FutureTask` 通过以下步骤实现：
1. 当任务提交时，它会包装成 `FutureTask` 对象。
2. 当任务执行完成时，结果会存储在 `FutureTask` 中。
3. 调用 `Future.get()` 时，`FutureTask` 会返回任务结果或异常。

### 六、总结

- **`Future` 是 Java 提供的异步任务处理机制**，允许你提交一个任务并在未来的某个时刻获取其结果或异常。
- 你可以使用 `Future.get()` 阻塞等待任务完成，也可以使用带超时的 `get(long timeout, TimeUnit unit)` 方法。
- 通过 `Future.cancel()` 可以尝试取消任务，而 `isCancelled()` 和 `isDone()` 可以用来检查任务的状态。
- `Future` 与 `ExecutorService.submit()` 紧密配合，通常用于异步任务的处理。

通过 `Future`，你可以轻松地处理异步任务，进行任务的状态管理、结果获取和异常处理，使得并发编程更加灵活和强大。

# FutureTask
`FutureTask` 是 Java 中 `Runnable` 和 `Future` 的一个实现类，位于 `java.util.concurrent` 包中。它的设计使其既可以作为一个任务提交给线程池或 `Thread` 执行，也可以用来获取任务的结果或处理任务的异常。它是 `Future` 接口的一个重要实现，通常用于异步编程。

### 一、`FutureTask` 的作用

`FutureTask` 可以理解为一个包装类，它可以包装 `Runnable` 或 `Callable` 任务，并提供以下功能：
1. **任务的执行**：`FutureTask` 实现了 `Runnable` 接口，可以作为一个任务被线程执行。
2. **结果的获取**：`FutureTask` 实现了 `Future` 接口，可以通过 `get()` 方法获取任务执行的结果。
3. **任务的取消和状态管理**：`FutureTask` 提供了取消任务、检查任务是否完成或被取消等功能。
4. **异步执行和阻塞等待**：通过 `FutureTask`，你可以异步提交任务，同时能够在任务完成时同步获取结果。

### 二、`FutureTask` 的构造方法

`FutureTask` 有两个构造方法，用来封装 `Runnable` 或 `Callable` 任务。

```java
public class FutureTask<V> implements RunnableFuture<V> {
    // 构造方法1：封装 Callable 任务
    public FutureTask(Callable<V> callable) { /* ... */ }

    // 构造方法2：封装 Runnable 任务，任务的结果可以为 null 或者自定义的值
    public FutureTask(Runnable runnable, V result) { /* ... */ }
}
```

- **封装 `Callable` 任务**：通过 `Callable` 的返回值来获取任务的结果。
- **封装 `Runnable` 任务**：`Runnable` 没有返回值，但 `FutureTask` 允许你传递一个结果，或者任务完成后返回 `null`。

### 三、`FutureTask` 的方法

`FutureTask` 继承了 `Future` 的接口，因此它具有 `Future` 的所有功能，比如获取任务结果、取消任务、检查任务状态等。常用的方法包括：

#### 1. **`run()`** 方法

`FutureTask` 实现了 `Runnable` 接口，所以它必须实现 `run()` 方法。该方法用于执行任务。当 `FutureTask` 被提交给线程池或 `Thread` 时，`run()` 方法会被调用。

```java
public void run() {
    if (state != NEW || !UNSAFE.compareAndSwapInt(this, stateOffset, NEW, COMPLETING))
        return;
    try {
        V result;
        if (callable != null)
            result = callable.call();  // 执行任务
        set(result);  // 设置任务结果
    } catch (Throwable ex) {
        setException(ex);  // 任务执行时发生异常
    }
}
```

- `run()` 方法会执行封装的任务（`Runnable` 或 `Callable`），然后将结果存储起来。
- 当任务执行时如果出现异常，异常会被捕获，并存储到 `FutureTask` 中，稍后可以通过 `Future.get()` 方法捕获到异常。

#### 2. **`get()`** 方法

`FutureTask` 实现了 `Future.get()`，用于获取任务的结果。`get()` 方法会阻塞调用线程，直到任务执行完成并返回结果。

```java
public V get() throws InterruptedException, ExecutionException {
    int s = state;
    if (s <= COMPLETING)
        s = awaitDone(false, 0L);  // 等待任务完成
    return report(s);  // 返回任务结果或抛出异常
}
```

- 如果任务尚未完成，`get()` 方法会阻塞，直到任务完成或抛出异常。
- 如果任务执行过程中抛出了异常，`get()` 会抛出 `ExecutionException`。

#### 3. **`cancel()`** 方法

`cancel(boolean mayInterruptIfRunning)` 用于尝试取消任务。如果任务尚未执行或正在执行时，`cancel()` 会改变任务的状态并尝试中断任务。

```java
public boolean cancel(boolean mayInterruptIfRunning) {
    if (!(state == NEW &&
          UNSAFE.compareAndSwapInt(this, stateOffset, NEW, mayInterruptIfRunning ? INTERRUPTING : CANCELLED)))
        return false;
    if (mayInterruptIfRunning) {
        try {
            Thread t = runner;
            if (t != null)
                t.interrupt();  // 尝试中断正在运行的任务
        } finally {
            UNSAFE.putOrderedInt(this, stateOffset, INTERRUPTED);
        }
    }
    finishCompletion();
    return true;
}
```

- `cancel()` 方法可以中断正在运行的任务（如果 `mayInterruptIfRunning` 为 `true`），否则只能取消未开始的任务。
- 取消任务后，任务的状态会变为已取消，调用 `isCancelled()` 会返回 `true`。

#### 4. **`isDone()` 和 `isCancelled()`**

- **`isDone()`**：返回 `true` 表示任务已经完成，无论任务是正常完成还是被取消。
- **`isCancelled()`**：返回 `true` 表示任务已经被取消。

### 四、`FutureTask` 的工作原理

`FutureTask` 是 `Runnable` 和 `Future` 的组合，它的核心工作流程如下：

1. **任务提交与执行**：`FutureTask` 可以通过 `Thread` 或线程池执行。线程执行时会调用 `run()` 方法，`run()` 方法负责调用 `Callable.call()` 或 `Runnable.run()` 执行任务。

2. **结果存储**：任务执行完成后，结果会被保存到 `FutureTask` 的内部。执行过程中发生的异常也会被捕获并存储。

3. **获取结果或异常**：通过 `get()` 方法，调用方可以获取任务的执行结果。如果任务执行时抛出了异常，`get()` 会抛出 `ExecutionException`，并封装了任务抛出的异常。

4. **任务取消**：任务可以被取消，如果任务还未开始执行，取消操作会成功。如果任务正在执行，可以选择是否尝试中断该任务。

### 五、`FutureTask` 的常见使用场景

#### 1. **用于线程池执行任务**

通常，`FutureTask` 与 `ExecutorService` 一起使用，提交给线程池执行。可以用 `FutureTask` 来包装一个 `Callable` 或 `Runnable` 任务，并通过 `Future` 获取任务的执行结果。

#### 2. **用于延迟或异步任务的执行**

`FutureTask` 可以用于一些异步执行的场景，开发者可以在某个时刻启动任务，然后在需要结果时调用 `get()` 方法来获取任务的执行结果。

#### 3. **用于任务的状态管理和取消**

`FutureTask` 提供了任务取消功能，可以通过 `cancel()` 方法取消任务的执行。

#### 示例代码：

```java
import java.util.concurrent.*;

public class FutureTaskExample {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        // 创建一个 Callable 任务
        Callable<Integer> callableTask = () -> {
            System.out.println("Callable task running");
            Thread.sleep(2000);
            return 123;
        };

        // 创建 FutureTask 并包装 Callable 任务
        FutureTask<Integer> futureTask = new FutureTask<>(callableTask);

        // 使用 Thread 执行任务
        Thread thread = new Thread(futureTask);
        thread.start();

        // 在等待任务完成时，可以执行其他操作
        System.out.println("Main thread doing something else");

        // 阻塞并获取任务结果
        Integer result = futureTask.get();
        System.out.println("Task result: " + result);
    }
}
```

### 六、`FutureTask` 的优点

1. **整合性**：`FutureTask` 将 `Runnable` 和 `Future` 的功能结合在一起，既可以被线程执行，也可以用于获取异步任务的结果。

2. **可取消性**：通过 `FutureTask`，可以灵活地取消任务或者中断任务的执行。

3. **多次结果获取**：任务执行完成后，多次调用 `get()` 都会返回相同的结果。即使 `get()` 方法已经调用过了，再次调用仍然有效。

### 七、`FutureTask` 与 `Future` 的区别

- **`Future` 是接口**，它是异步任务结果的表示，不包含执行逻辑。
- **`FutureTask` 是 `Runnable` 和 `Future` 的具体实现**，它不仅可以代表任务的结果，还可以执行任务。

### 八、总结

`FutureTask` 是 Java 中异步编程的核心工具，它通过包装 `Runnable` 和 `Callable` 任务，提供了异步任务执行、任务结果获取、任务取消等功能。`FutureTask` 可以与 `Thread` 或线程池结合使用，用于处理并发任务。

- `FutureTask` 允许开发者提交一个任务并异步获取任务的执行结果。
- 提供了灵活的任务状态管理和取消功能。
- 是处理异步任务执行、结果获取和异常处理的一个重要工具。

通过 `FutureTask

`，我们可以在多线程编程中实现高效的任务执行和结果处理，非常适合需要延迟计算或异步处理的场景。`

### 总结的关键点：
1. **执行与结果获取**：`FutureTask` 允许异步执行任务，并通过 `get()` 获取结果。即使任务完成后，也可以多次调用 `get()` 来获取同一个结果。
2. **取消和中断**：提供了 `cancel()` 方法，支持任务的取消和中断。如果任务还未执行，可以取消；如果任务正在执行，可以选择是否中断它。
3. **结合线程池或线程使用**：`FutureTask` 可以与线程池或单独的 `Thread` 一起使用，灵活性高，适合异步任务场景。

总体来说，`FutureTask` 在 Java 并发编程中是一个非常有用的工具，能够帮助我们更好地管理异步任务的执行与结果获取，并且提供了便捷的任务取消和状态管理功能。

## FutureTask 的两个身份
FutureTask 既能够作为任务的执行者，又能够作为任务结果的存储和获取机制。

这句话的意思是，**`FutureTask` 既能够作为任务的执行者，又能够作为任务结果的存储和获取机制**。它同时具备了执行任务和管理任务执行结果的双重功能。

### 1. `FutureTask` 可以执行任务

`FutureTask` 实现了 `Runnable` 接口，因此它可以像 `Runnable` 一样，被提交给线程池或单独的 `Thread` 执行，任务会通过 `FutureTask.run()` 方法被执行。

- 当 `FutureTask` 被提交给线程池或者通过 `Thread` 来启动时，任务会在后台异步执行。
- `run()` 方法中，`FutureTask` 内部调用了被包装的 `Callable` 或 `Runnable` 对象的 `call()` 或 `run()` 方法来执行任务。

### 2. `FutureTask` 可以代表任务的结果

`FutureTask` 实现了 `Future` 接口，能够管理并返回任务的执行结果。

- **获取结果**：通过调用 `FutureTask.get()`，你可以阻塞当前线程，直到任务完成并返回结果。
- **处理异常**：如果任务在执行过程中抛出了异常，`FutureTask` 会将该异常封装在 `ExecutionException` 中，当你调用 `get()` 时，这个异常会被抛出。
- **取消任务**：`FutureTask` 提供了 `cancel()` 方法，可以用于取消正在执行的任务。

### 为什么 `FutureTask` 既能执行任务又能代表结果？

因为 `FutureTask` 是 `Runnable` 和 `Future` 的一个组合实现类。

- 作为 **`Runnable`**，它可以被线程执行（即你可以把它传递给 `Thread` 或 `ExecutorService` 来启动任务）。
- 作为 **`Future`**，它可以保存并管理任务的执行结果（即你可以使用 `get()` 方法来获取任务的结果，使用 `cancel()` 来取消任务）。

### 代码示例

下面是一个简单的 `FutureTask` 示例，展示了它如何既执行任务，又返回任务的结果。

```java
import java.util.concurrent.*;

public class FutureTaskExample {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        // 创建一个 Callable 任务
        Callable<Integer> callableTask = () -> {
            System.out.println("Task is running...");
            Thread.sleep(2000); // 模拟任务执行时间
            return 10;
        };

        // 使用 FutureTask 包装 Callable 任务
        FutureTask<Integer> futureTask = new FutureTask<>(callableTask);

        // 使用 Thread 执行 FutureTask（此时 FutureTask 充当一个 Runnable）
        Thread thread = new Thread(futureTask);
        thread.start();

        // 主线程可以在等待任务结果时做其他事情
        System.out.println("Main thread is doing something else...");

        // 获取任务的结果（阻塞，直到任务完成）
        Integer result = futureTask.get();  // FutureTask 代表任务的结果
        System.out.println("Task result: " + result);
    }
}
```

### 执行过程说明

1. **任务执行**：`FutureTask` 被封装为一个 `Thread`，并在新线程中异步执行任务（`callableTask` 的逻辑）。此时 `FutureTask` 充当了一个 `Runnable`，通过 `run()` 方法执行任务。

2. **结果存储与获取**：任务执行完成后，`FutureTask` 保存任务的返回结果，并通过 `futureTask.get()` 提供给调用者。当调用 `get()` 时，当前线程会阻塞，直到任务完成并返回结果。

### 总结

- **作为任务执行者**：`FutureTask` 可以通过 `run()` 方法执行 `Callable` 或 `Runnable` 任务。它可以被提交给线程池或通过 `Thread` 执行。
- **作为任务结果的表示**：`FutureTask` 通过实现 `Future` 接口，能够管理任务执行的结果、状态以及异常。开发者可以通过 `get()` 获取任务结果，也可以通过 `cancel()` 取消任务。

这就是为什么说 `FutureTask` 既能执行任务，又能代表任务结果的原因。它是 `Runnable` 和 `Future` 的结合体，提供了一种强大而灵活的方式来管理异步任务。


# CompletableFuture
`CompletableFuture` 是 Java 8 中引入的一个强大的类，它不仅实现了 `Future` 接口，还提供了大量的方法，支持异步任务的执行、结果处理、任务依赖组合、流式编程等功能。`CompletableFuture` 是 Java 中异步编程的核心工具之一，能够让开发者更方便地编写复杂的异步任务和依赖任务。

### 一、什么是 `CompletableFuture`

`CompletableFuture` 是 `java.util.concurrent` 包中的类，它提供了更丰富的 API 来处理异步任务。与传统的 `Future` 不同，`CompletableFuture` 支持：
- 异步任务的执行和组合。
- 非阻塞式地处理任务结果。
- 链式处理多个异步任务的依赖关系。
- 结合 `CompletableFuture` 对象，实现多个异步任务的组合。

### 二、`CompletableFuture` 的主要功能

#### 1. **异步任务的执行**

`CompletableFuture` 提供了丰富的工厂方法用于异步执行任务，这些方法可以使用默认的 `ForkJoinPool` 线程池，也可以自定义线程池。常用的异步方法包括：
- **`supplyAsync(Supplier<U> supplier)`**：异步执行任务，并返回结果。
- **`runAsync(Runnable runnable)`**：异步执行任务，不返回结果。

#### 示例代码：使用 `supplyAsync` 异步执行任务

```java
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class CompletableFutureExample {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        // 异步执行任务
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("Executing task asynchronously...");
            return 42;  // 返回结果
        });

        // 阻塞等待结果
        System.out.println("Result: " + future.get());  // 输出结果
    }
}
```

#### 2. **任务完成后的回调**

`CompletableFuture` 支持在任务完成之后执行回调方法，这样可以避免阻塞式等待。它可以在任务完成后自动调用回调函数进行进一步处理。
- **`thenApply(Function<T,R> fn)`**：任务完成后，将结果传递给回调函数，并返回处理后的结果。
- **`thenAccept(Consumer<T> action)`**：任务完成后，使用结果执行回调函数，但不返回新的结果。
- **`thenRun(Runnable action)`**：任务完成后，不使用结果，直接执行一个回调操作。

#### 示例代码：任务完成后的回调

```java
import java.util.concurrent.CompletableFuture;

public class CompletableFutureCallbackExample {
    public static void main(String[] args) {
        CompletableFuture.supplyAsync(() -> {
            System.out.println("Task started...");
            return "Task result";
        }).thenApply(result -> {
            // 处理任务结果
            System.out.println("Processing result: " + result);
            return result.length();  // 返回处理后的结果
        }).thenAccept(length -> {
            // 最终处理
            System.out.println("Result length: " + length);
        });
    }
}
```

#### 3. **组合多个异步任务**

`CompletableFuture` 提供了丰富的 API 来处理多个异步任务的组合，这些组合包括：
- **`thenCombine()`**：当两个 `CompletableFuture` 都完成时，将两个任务的结果组合在一起。
- **`thenCompose()`**：将当前任务的结果作为输入，传递给另一个异步任务，并返回该任务的结果。

#### 示例代码：组合多个异步任务

```java
import java.util.concurrent.CompletableFuture;

public class CompletableFutureCombineExample {
    public static void main(String[] args) {
        CompletableFuture<Integer> future1 = CompletableFuture.supplyAsync(() -> 5);
        CompletableFuture<Integer> future2 = CompletableFuture.supplyAsync(() -> 10);

        // 组合两个异步任务的结果
        CompletableFuture<Integer> combinedFuture = future1.thenCombine(future2, (result1, result2) -> result1 + result2);

        // 输出组合后的结果
        combinedFuture.thenAccept(result -> System.out.println("Combined result: " + result));
    }
}
```

#### 4. **异常处理**

`CompletableFuture` 提供了流式的异常处理机制，允许你在异步任务出错时处理异常，类似于 `try-catch`。
- **`exceptionally(Function<Throwable, ? extends T> fn)`**：在任务出错时，执行指定的异常处理函数。
- **`handle(BiFunction<? super T, Throwable, ? extends U> fn)`**：无论任务是否出错，都会执行指定的回调函数，处理结果或异常。

#### 示例代码：异常处理

```java
import java.util.concurrent.CompletableFuture;

public class CompletableFutureExceptionExample {
    public static void main(String[] args) {
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("Executing task...");
            if (true) throw new RuntimeException("Error occurred");
            return 42;
        });

        // 处理异常
        future.exceptionally(ex -> {
            System.out.println("Handling exception: " + ex.getMessage());
            return -1;  // 返回默认值
        }).thenAccept(result -> {
            System.out.println("Result: " + result);
        });
    }
}
```

#### 5. **等待多个任务完成**

`CompletableFuture` 还提供了等待多个任务完成的方式：
- **`allOf()`**：等待所有任务完成。
- **`anyOf()`**：等待其中一个任务完成。

#### 示例代码：等待多个任务完成

```java
import java.util.concurrent.CompletableFuture;

public class CompletableFutureAllOfExample {
    public static void main(String[] args) throws Exception {
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> "Task 1");
        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> "Task 2");

        // 等待所有任务完成
        CompletableFuture<Void> allOf = CompletableFuture.allOf(future1, future2);

        // 阻塞等待所有任务完成
        allOf.join();

        System.out.println("All tasks completed");
        System.out.println("Task 1 result: " + future1.get());
        System.out.println("Task 2 result: " + future2.get());
    }
}
```

### 三、`CompletableFuture` 的工作流程

1. **异步任务提交**：通过 `supplyAsync()`、`runAsync()` 等方法提交异步任务。这些方法将任务提交给线程池，并立即返回一个 `CompletableFuture` 对象。

2. **任务完成或异常**：异步任务执行完后，`CompletableFuture` 将被标记为“完成”。如果任务正常执行完成，结果会被保存；如果任务抛出异常，异常也会被保存。

3. **获取结果或处理异常**：通过 `thenApply()`、`thenAccept()` 等方法对任务结果进行处理，或者通过 `exceptionally()` 处理异常。你也可以使用 `get()` 方法获取任务的最终结果。

### 四、`CompletableFuture` 的优点

1. **非阻塞异步编程**：与传统的 `Future` 不同，`CompletableFuture` 支持非阻塞式获取结果，可以通过回调函数处理结果，而不是调用 `get()` 阻塞线程。

2. **组合任务**：`CompletableFuture` 提供了丰富的任务组合方法，可以非常灵活地处理多个异步任务之间的依赖关系。

3. **异常处理**：它的异常处理机制使得开发者可以优雅地处理异步任务执行过程中的异常。

4. **流式 API**：`CompletableFuture` 支持链式调用，任务之间的依赖关系可以通过流式 API 清晰地表示，代码更加简洁和易读。

### 五、常见问题

#### 1. **线程池的使用**

默认情况下，`CompletableFuture` 使用 `ForkJoinPool.commonPool()` 作为它的线程池。如果你需要自定义线程池，可以通过传递 `Executor` 参数来指定线程池。

```java
ExecutorService customExecutor = Executors.newFixedThreadPool(10);
CompletableFuture.supplyAsync(() -> "Task", customExecutor);
```

#### 2. **任务的阻塞**

`CompletableFuture.get()` 会阻塞当前线程，直到任务完成。如果不想阻塞，可以使用异步回调函数，如 `thenApply()` 或 `thenAccept()`，来处理任务结果。

### 六、总结

`CompletableFuture` 是 Java 8 引入的一个功能强大的工具，它不仅是对 `Future` 的增强，还是 Java 异步编程的基础工具。它支持：
- 异步任务执行
- 回调机制
- 异常处理
- 多任务组合

这些特性使得 `CompletableFuture` 成为开发高性能、响应式异步应用的重要工具。在使用它时，开发者可以更加灵活地管理任务的依赖关系、异常处理和执行结果。

## Future vs CompletableFuture
![img.png](img%2Fimg.png)
`Future` 和 `CompletableFuture` 都是 Java 中用于处理异步任务的工具，但它们的功能和使用方式有显著的区别。`Future` 是 Java 5 中引入的，主要用于处理异步任务的结果，而 `CompletableFuture` 是 Java 8 中引入的，提供了更强大和灵活的异步编程能力。下面我们将详细比较它们的不同之处。

### 一、`Future` 的特点

`Future` 是 Java 5 中引入的接口，用来表示一个异步计算的结果，它提供了一些基本的方法来操作异步任务。

#### `Future` 的核心功能：
1. **异步任务的结果获取**：你可以提交一个任务给线程池，`submit()` 返回一个 `Future` 对象，通过 `Future.get()` 来获取任务的结果。
2. **取消任务**：`Future` 提供了 `cancel()` 方法，允许取消尚未执行的任务或者中断正在执行的任务。
3. **检查任务状态**：通过 `isDone()` 和 `isCancelled()` 方法可以检查任务是否完成或取消。

#### `Future` 的局限：
1. **`get()` 是阻塞的**：调用 `Future.get()` 会阻塞当前线程，直到任务完成，这意味着调用 `get()` 时无法执行其他操作，除非任务完成。
2. **没有回调机制**：`Future` 没有内置的回调机制，任务完成后不能自动触发后续操作，必须通过阻塞式的 `get()` 方法主动获取结果。
3. **无法组合任务**：`Future` 不支持将多个异步任务组合在一起，也无法处理任务之间的依赖。
4. **异常处理**：`Future` 在 `get()` 时如果任务失败，会抛出 `ExecutionException`，但并不提供灵活的异常处理机制。

#### 示例：使用 `Future` 处理异步任务

```java
import java.util.concurrent.*;

public class FutureExample {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // 提交一个 Callable 任务
        Future<Integer> future = executor.submit(() -> {
            Thread.sleep(2000);  // 模拟耗时操作
            return 42;
        });

        // 在等待任务结果时，可以执行其他操作
        System.out.println("Doing something else...");

        // 获取任务结果（阻塞）
        Integer result = future.get();  // 这会阻塞，直到任务完成
        System.out.println("Task result: " + result);

        // 关闭线程池
        executor.shutdown();
    }
}
```

### 二、`CompletableFuture` 的特点

`CompletableFuture` 是 Java 8 中引入的，功能更为强大，它不仅实现了 `Future` 接口，还提供了许多用于处理异步任务、组合任务、异常处理和回调的便捷方法。

#### `CompletableFuture` 的核心功能：

1. **非阻塞式的异步任务处理**：`CompletableFuture` 提供了 `thenApply()`、`thenAccept()` 等方法，可以在异步任务完成后执行回调函数，而不需要阻塞线程。
2. **任务组合**：`CompletableFuture` 支持将多个异步任务进行组合，如 `thenCompose()`、`thenCombine()`，可以处理复杂的任务依赖。
3. **异常处理**：`CompletableFuture` 提供了流式的异常处理机制，如 `exceptionally()` 和 `handle()`，允许开发者在任务执行出错时优雅地处理异常。
4. **灵活的线程模型**：`CompletableFuture` 可以使用默认的 `ForkJoinPool` 线程池执行任务，也可以传入自定义的 `Executor` 来管理线程。
5. **多任务组合**：`CompletableFuture` 提供了 `allOf()` 和 `anyOf()` 方法，可以等待多个任务全部完成或者其中一个任务完成后继续操作。

#### 示例：使用 `CompletableFuture` 处理异步任务

```java
import java.util.concurrent.*;

public class CompletableFutureExample {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        // 使用 CompletableFuture 执行异步任务
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("Task is running...");
            return 42;
        });

        // 非阻塞地处理任务结果
        future.thenAccept(result -> System.out.println("Result: " + result));

        // 让主线程等待异步任务完成
        Thread.sleep(1000);  // 模拟其他操作
    }
}
```

### 三、`Future` 与 `CompletableFuture` 的区别

| 特性                    | `Future`                                     | `CompletableFuture`                           |
|-------------------------|----------------------------------------------|----------------------------------------------|
| **异步结果获取**         | 通过 `get()` 阻塞式获取结果                  | 支持非阻塞式回调机制，如 `thenApply()`、`thenAccept()` |
| **任务组合**             | 不支持任务组合                               | 支持任务的组合和依赖，例如 `thenCompose()`、`thenCombine()` |
| **任务取消**             | 通过 `cancel()` 取消任务                     | 通过 `cancel()` 取消任务                     |
| **异常处理**             | 通过 `ExecutionException` 捕获异常           | 提供流式异常处理，如 `exceptionally()` 和 `handle()` |
| **多任务处理**           | 不支持                                       | 支持多任务并行处理，如 `allOf()` 和 `anyOf()` |
| **线程模型**             | 任务执行时通常通过线程池                     | 支持使用默认线程池或自定义线程池             |
| **回调机制**             | 无内置回调机制                               | 提供丰富的回调方法                           |
| **异步处理模式**         | 阻塞，手动检查任务状态                       | 非阻塞，链式流处理                           |

### 四、详细对比分析

#### 1. **异步结果获取**

- **`Future`**：调用 `get()` 方法会阻塞当前线程，直到任务完成。如果你想继续做其他事情，你必须手动检查任务是否完成 (`isDone()`)，或者用带超时的 `get(long timeout, TimeUnit unit)` 来避免长时间阻塞。

- **`CompletableFuture`**：提供了丰富的回调函数，允许你在任务完成后自动处理结果，避免手动轮询或阻塞。例如，`thenApply()` 可以在任务完成后，立即对结果进行进一步处理，`thenAccept()` 可以接收结果并执行副作用操作，而不阻塞主线程。

#### 2. **任务组合**

- **`Future`**：无法组合多个任务，如果你有多个异步任务，需要手动等待每个任务的完成，处理比较复杂。

- **`CompletableFuture`**：支持将多个任务进行组合处理，像 `thenCombine()`、`thenCompose()` 允许你将任务的结果作为输入，传递给下一个任务，流畅地管理复杂的任务依赖和组合。

#### 3. **异常处理**

- **`Future`**：如果任务抛出异常，`get()` 会抛出 `ExecutionException`，需要手动捕获并处理。

- **`CompletableFuture`**：提供了 `exceptionally()` 和 `handle()` 方法，用来优雅地处理任务执行中的异常。这些方法允许你在流式操作中直接处理异常，使代码更加简洁。

#### 4. **多任务处理**

- **`Future`**：不支持多个任务的组合或并行处理。你需要手动管理多个 `Future` 对象，编写复杂的逻辑来等待所有任务完成。

- **`CompletableFuture`**：提供了 `allOf()` 和 `anyOf()` 方法，可以轻松处理多个异步任务。例如，`allOf()` 会等待所有任务完成，`anyOf()` 则会在任意一个任务完成后返回。

#### 5. **回调机制**

- **`Future`**：没有回调机制。你必须手动检查任务是否完成，或者阻塞等待任务完成后，才能继续进行下一步操作。

- **`CompletableFuture`**：提供了丰富的回调方法，例如 `thenApply()`、`thenAccept()`、`thenRun()` 等，这些方法允许你在任务完成时立即执行下一步操作，避免繁琐的阻塞式编程。

#### 6. **线程模型**

- **`Future`**：通常使用 `ExecutorService` 提交任务，并依赖线程池来执行任务。

- **`CompletableFuture`**：同样可以使用 `ExecutorService`，但默认使用 `ForkJoinPool.commonPool()` 来执行任务。此外，还可以传递自定义的 `Executor` 来更灵活地控制任务的执行线程。

### 五、什么时候使用 `Future`，什么时候使用 `CompletableFuture`？

- **使用 `Future`**：
    - 如果你的任务只涉及基本的异步结果获取，并且你可以接受阻塞式获取结果的方式。
    - 如果你不需要处理任务之间的依赖，也不需要组合多个异步任务。

- **使用 `CompletableFuture`**：
    - 如果你的应用需要

更复杂的异步处理，比如任务组合、依赖管理、非阻塞结果处理或流式的操作，`CompletableFuture` 是更合适的选择。
- 当你需要处理多个并行任务，等待所有任务完成，或者处理任意一个任务的结果时。
- 如果你想在任务完成后立即触发回调操作，而不希望阻塞主线程。
- 当你需要优雅地处理异步任务执行中的异常，`CompletableFuture` 的流式异常处理机制提供了很大的灵活性。

### 六、总结

- **`Future`** 是 Java 5 中提供的基础工具，适用于简单的异步任务执行，但由于它的阻塞特性和缺乏组合与回调机制，它的使用在复杂场景下会显得不够灵活。

- **`CompletableFuture`** 则是更强大的异步任务处理工具，提供了非阻塞的回调、任务组合、流式编程和异常处理等功能，适用于更复杂的异步任务场景。如果你有多任务组合、依赖管理或更复杂的异常处理需求，`CompletableFuture` 将是更好的选择。

使用 `CompletableFuture` 可以让异步编程变得更加高效和简洁，尤其是在构建高性能、响应式的应用程序时，它是非常有用的工具。

## join() vs get()
`CompletableFuture` 的 `join()` 方法是 `CompletableFuture` 提供的一个非常重要且常用的方法，它与 `get()` 方法功能类似，都是用于获取异步任务的结果。但是，`join()` 和 `get()` 之间有一些显著的区别，特别是在异常处理方面。

### 一、`join()` 方法的功能

`join()` 方法的作用是**等待异步任务完成并返回结果**。如果任务已经完成，`join()` 会立即返回结果；如果任务尚未完成，`join()` 会阻塞当前线程，直到任务执行完毕，并返回任务的结果。

`join()` 主要用于在等待异步任务完成时，提供非检查异常的处理方式，而不是像 `get()` 那样抛出受检查的异常。

#### `join()` 的方法签名：
```java
public T join()
```
- **返回值**：返回任务的结果，如果任务执行成功，返回异步任务的结果值。
- **异常处理**：`join()` 不会抛出 `InterruptedException` 或 `ExecutionException`，而是抛出**未检查异常**（`CompletionException`），从而简化异常处理流程。

### 二、`join()` 与 `get()` 的区别

#### 1. **异常处理方式**

- **`get()`**：
  - `get()` 方法会抛出 `InterruptedException`（如果任务在等待时被中断）或 `ExecutionException`（如果任务执行时发生了异常）。
  - 因为这些是检查异常（Checked Exceptions），你在调用 `get()` 时，必须显式地捕获这些异常或抛出。

- **`join()`**：
  - `join()` 方法不会抛出 `InterruptedException` 或 `ExecutionException`。
  - 如果任务在执行过程中发生异常，`join()` 会将异常封装在 `CompletionException` 中，这是一个未检查异常（Unchecked Exception），因此不强制要求进行异常处理。

这种差异使得 `join()` 在编写流式异步处理代码时更为简洁，因为你不必显式处理受检查异常。

#### 2. **返回结果的方式**

- **`get()`**：在等待任务完成时，`get()` 可能会抛出检查异常并导致调用方的代码中断。
- **`join()`**：`join()` 更加流畅，不需要显式处理检查异常，它只会抛出未检查异常，使得代码更加简洁。

### 三、`join()` 的使用场景

`join()` 通常用于以下场景：

1. **简化异步任务的结果获取**：如果你不想在代码中显式处理异常，尤其是 `InterruptedException` 或 `ExecutionException`，`join()` 提供了一种更简洁的方式。

2. **流式异步编程**：在链式调用 `CompletableFuture` 时，`join()` 非常适合用来组合多个异步任务的结果，因为它不会破坏方法链，也不会要求处理检查异常。

3. **异常处理简化**：`join()` 适合那些不需要处理线程中断和任务执行异常的场景，简化了异步编程中的异常处理。

### 四、代码示例

#### 示例 1：使用 `join()` 获取异步任务的结果

```java
import java.util.concurrent.CompletableFuture;

public class CompletableFutureJoinExample {
    public static void main(String[] args) {
        // 创建异步任务
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000);  // 模拟任务执行
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "Task Result";
        });

        // 使用 join() 等待任务完成并获取结果
        String result = future.join();  // 非阻塞式获取结果，不抛出检查异常
        System.out.println("Result: " + result);
    }
}
```

#### 输出结果：

```
Result: Task Result
```

在这个例子中，我们使用 `join()` 方法等待异步任务执行完成，并直接返回结果。与 `get()` 方法不同的是，这里不需要显式捕获或处理异常。

#### 示例 2：处理异常

如果任务执行过程中发生了异常，`join()` 会抛出 `CompletionException`，而不会抛出 `ExecutionException`。

```java
import java.util.concurrent.CompletableFuture;

public class CompletableFutureJoinExceptionExample {
    public static void main(String[] args) {
        // 创建异步任务，模拟异常发生
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            throw new RuntimeException("Task failed!");
        });

        try {
            // 使用 join() 获取结果，会抛出 CompletionException 包装的异常
            Integer result = future.join();
            System.out.println("Result: " + result);
        } catch (Exception e) {
            // 捕获 CompletionException
            System.out.println("Exception occurred: " + e.getCause().getMessage());
        }
    }
}
```

#### 输出结果：

```
Exception occurred: Task failed!
```

在这个例子中，任务抛出了 `RuntimeException`，`join()` 会将异常封装在 `CompletionException` 中，并抛出。在捕获异常时，你可以通过 `getCause()` 方法获取任务执行时抛出的真实异常。

### 五、`join()` 的常见用法

1. **串行任务**：在多个异步任务之间，使用 `join()` 来获取前一个任务的结果并传递给下一个任务。

   ```java
   CompletableFuture<Integer> future1 = CompletableFuture.supplyAsync(() -> 10);
   CompletableFuture<Integer> future2 = future1.thenApply(result -> result * 2);
   
   // 使用 join() 获取最终结果
   Integer finalResult = future2.join();
   System.out.println("Final Result: " + finalResult);
   ```

2. **等待多个任务的完成**：`join()` 可以与 `CompletableFuture.allOf()` 或 `anyOf()` 结合使用，等待多个任务完成后获取结果。

   ```java
   CompletableFuture<Void> allOf = CompletableFuture.allOf(future1, future2);
   
   // 使用 join() 等待所有任务完成
   allOf.join();
   System.out.println("All tasks completed");
   ```

### 六、`join()` 的优点

1. **简化代码**：由于 `join()` 不抛出检查异常，因此代码中不必显式捕获 `InterruptedException` 和 `ExecutionException`，使得代码更加简洁流畅。

2. **与流式编程结合良好**：`join()` 可以与 `thenApply()`、`thenAccept()` 等链式方法良好结合，适合流式异步编程，避免异常处理的冗余代码。

3. **更灵活的异常处理**：`join()` 的异常处理机制将所有异常封装为未检查异常（`CompletionException`），简化了异常处理逻辑。

### 七、`join()` 的局限性

- **潜在的未检查异常**：虽然 `join()` 简化了代码，但未检查异常的处理可能导致开发者忽略了潜在的问题。开发者需要清楚地了解任务执行过程中可能抛出的异常。
- **阻塞行为**：虽然 `join()` 更简洁，但它仍然是一个阻塞操作，因此在高并发场景中仍需谨慎使用，避免因为任务等待而导致主线程长时间阻塞。

### 八、总结

`CompletableFuture.join()` 是 `CompletableFuture` 提供的一个简化异步任务结果获取的工具，与 `get()` 类似，但它的异常处理更加简洁。使用 `join()` 时，你不必显式捕获检查异常，这让代码更干净简洁，非常适合用于流式异步编程和组合多个任务。

使用 `join()` 时需要注意的是它的阻塞特性，以及它会抛出未检查异常（`CompletionException`），开发者需要确保对潜在异常进行正确处理。总体来说，`join()` 在简化异步任务处理方面非常有用，尤其是在复杂的异步工作流中。

# CompletionStage
`CompletionStage` 是 Java 中用于表示**异步计算**的一个接口，最早在 Java 8 中引入，它定义了如何将多个异步任务组合在一起，允许你在一个异步任务完成后，继续进行后续的处理。`CompletionStage` 是一个**异步计算的流程控制接口**，其实现类 `CompletableFuture` 提供了非常丰富的功能用于异步编程。

### 一、`CompletionStage` 是什么？

`CompletionStage` 本质上表示一个异步任务的阶段（Stage），它定义了多个方法，允许开发者在该阶段完成时，继续执行其他操作或处理下一个阶段。一个 `CompletionStage` 可以链式组合多个异步任务，形成一个任务的流水线，定义每个任务的执行顺序和依赖关系。

**核心点**：
- **非阻塞**：所有操作都是非阻塞的。
- **流式编程**：支持链式调用来处理多个依赖的任务。
- **任务组合**：可以通过 `thenApply()`、`thenCompose()`、`thenCombine()` 等方法将多个异步任务组合起来。

`CompletionStage` 本身是一个接口，`CompletableFuture` 是它的常用实现类。`CompletionStage` 提供了大量方法来描述任务的顺序依赖关系，例如当一个任务完成后执行另一个任务，或者当两个任务都完成后进行某个合并操作。

### 二、`CompletionStage` 常见方法

`CompletionStage` 提供了许多方法来定义任务的处理流程，这些方法主要分为两类：
1. **转换（Transformation）**：当一个任务完成时，对其结果进行转换。
2. **组合（Combining）**：当多个任务都完成时，将它们的结果组合。

#### 1. 转换方法

这些方法用于在一个异步任务完成后，对其结果进行处理或转换：

- **`thenApply(Function<T, R> fn)`**：当任务完成后，使用结果并返回新的值（可以理解为同步转换）。

  ```java
  CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> "Hello")
      .thenApply(result -> result + " World"); // 将 "Hello" 转换为 "Hello World"
  ```

- **`thenApplyAsync(Function<T, R> fn)`**：与 `thenApply` 类似，但转换操作在另一个线程中执行。

- **`thenAccept(Consumer<T> action)`**：当任务完成后，使用其结果进行操作，但不返回值。

  ```java
  CompletableFuture.supplyAsync(() -> "Result")
      .thenAccept(result -> System.out.println("Result: " + result)); // 输出 "Result: Result"
  ```

- **`thenRun(Runnable action)`**：任务完成后不使用其结果，只执行一个动作。

  ```java
  CompletableFuture.supplyAsync(() -> "Task Done")
      .thenRun(() -> System.out.println("Task completed!"));
  ```

#### 2. 组合方法

这些方法用于将多个异步任务的结果进行组合，或者执行依赖于其他异步任务的操作：

- **`thenCompose(Function<T, CompletionStage<U>> fn)`**：用于将一个任务的结果，作为下一个异步任务的输入，并返回新的 `CompletionStage`。它可以创建一个依赖于前一个任务结果的异步链条。

  ```java
  CompletableFuture.supplyAsync(() -> "Hello")
      .thenCompose(result -> CompletableFuture.supplyAsync(() -> result + " World"))
      .thenAccept(finalResult -> System.out.println(finalResult));  // 输出 "Hello World"
  ```

- **`thenCombine(CompletionStage<U> other, BiFunction<T, U, R> fn)`**：当两个 `CompletionStage` 都完成时，将它们的结果合并。

  ```java
  CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> "Hello");
  CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> "World");

  future1.thenCombine(future2, (result1, result2) -> result1 + " " + result2)
         .thenAccept(finalResult -> System.out.println(finalResult)); // 输出 "Hello World"
  ```

- **`allOf(CompletionStage<?>... stages)`**：等待所有传入的 `CompletionStage` 完成，返回一个新的 `CompletionStage`。它不会直接返回结果，但可以用 `join()` 获取每个阶段的结果。

  ```java
  CompletableFuture<Void> allFuture = CompletableFuture.allOf(future1, future2);
  ```

- **`anyOf(CompletionStage<?>... stages)`**：当任意一个 `CompletionStage` 完成时，返回一个新的 `CompletionStage`。

  ```java
  CompletableFuture<Object> anyFuture = CompletableFuture.anyOf(future1, future2);
  ```

### 三、`CompletionStage` 异常处理

异步任务可能会在执行过程中抛出异常，`CompletionStage` 提供了多种方法来处理这些异常：

- **`exceptionally(Function<Throwable, ? extends T> fn)`**：当任务抛出异常时，使用 `fn` 函数处理异常并返回一个默认值。

  ```java
  CompletableFuture.supplyAsync(() -> { throw new RuntimeException("Error!"); })
      .exceptionally(ex -> {
          System.out.println("Handled exception: " + ex.getMessage());
          return "Default Value";  // 返回默认值
      });
  ```

- **`handle(BiFunction<? super T, Throwable, ? extends U> fn)`**：无论任务是正常完成还是抛出异常，都会执行 `fn`。`fn` 函数接收任务的结果或异常，可以用于返回不同的值。

  ```java
  CompletableFuture.supplyAsync(() -> { throw new RuntimeException("Error!"); })
      .handle((result, ex) -> {
          if (ex != null) {
              System.out.println("Handled exception: " + ex.getMessage());
              return "Recovered Value";
          } else {
              return result;
          }
      });
  ```

- **`whenComplete(BiConsumer<? super T, ? super Throwable> action)`**：在任务完成或抛出异常时执行某些操作，任务的结果和异常都会传递给 `action`，但不改变任务结果。

  ```java
  CompletableFuture.supplyAsync(() -> "Hello")
      .whenComplete((result, ex) -> {
          if (ex != null) {
              System.out.println("Exception occurred: " + ex.getMessage());
          } else {
              System.out.println("Completed with result: " + result);
          }
      });
  ```

### 四、`CompletionStage` 的关键概念

#### 1. **异步任务的流式编程**
`CompletionStage` 的设计思想是以流式方式来构建异步任务的依赖链。每个异步任务可以是前一个任务的输出，并且可以根据不同的场景决定下一步的操作。

#### 2. **异步与同步操作**
在 `CompletionStage` 中，`thenApply()` 和 `thenApplyAsync()` 的区别在于：
- `thenApply()` 是同步的，当前任务完成后，立即执行下一个任务，通常在同一线程内执行。
- `thenApplyAsync()` 是异步的，当前任务完成后，新的任务会提交到线程池中执行。

类似的，其他方法如 `thenCompose()` 和 `thenCombine()` 也有对应的异步版本。

#### 3. **异常处理**
`CompletionStage` 提供了灵活的异常处理机制。通过 `exceptionally()` 和 `handle()`，你可以对不同的异常情况作出适当的反应，确保整个异步链条不会因为某个阶段的异常而终止。

### 五、代码示例：组合异步任务并处理异常

以下示例展示了如何使用 `CompletionStage` 来组合多个异步任务，并处理可能的异常：

```java
import java.util.concurrent.CompletableFuture;

public class CompletionStageExample {
    public static void main(String[] args) {
        // 异步任务1：返回一个值
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
            System.out.println("Task 1: Fetching user data");
            return "User1";
        });

        // 异步任务2：基于任务1的结果执行
        CompletableFuture<String> future2 = future1.thenCompose(user -> {
            System.out.println("Task 2: Fetching order for " + user);
            return CompletableFuture.supplyAsync(() -> "Order123");
        });

        // 异步任务3：任务1与任务2完成后合并结果
        CompletableFuture<String> future3 = future1.thenCombine(future2, (user, order) -> {
            System.out.println("Task 3: Combining user and order");
            return user + " has order: " + order;
        });

        // 最终任务：处理结果或异常
        future3.handle((result, ex) -> {
            if (ex != null) {
                System.out.println("Exception occurred: " + ex.getMessage());
                return "Failed to complete tasks";
            } else {
                System.out.println("Final result: " + result);
                return result;
            }


```java
            }
        });

        // 阻塞主线程以等待结果
        future3.join();  // 确保主线程等待所有异步任务执行完成
    }
}
```

### 代码解释：

1. **异步任务1 (`future1`)**：通过 `supplyAsync()` 异步获取用户数据，返回一个 `User` 的 ID。

2. **异步任务2 (`future2`)**：基于 `future1` 的结果（即 `User1`），异步获取该用户的订单数据。

3. **异步任务3 (`future3`)**：使用 `thenCombine()` 组合 `future1` 和 `future2` 的结果，生成最终的输出。

4. **结果处理 (`handle()`)**：无论异步任务成功或失败，`handle()` 都会处理结果。如果有异常发生，它会捕获异常并提供一个默认值；如果任务成功，它会返回最终的结果。

5. **`join()`**：用于等待所有异步任务完成并获取最终结果。在实际生产中，`join()` 可能会被其他非阻塞方式替代。

### 六、总结

- `CompletionStage` 是 Java 并发编程中非常强大和灵活的接口，支持流式异步编程。
- 它允许我们使用多个异步任务，并在任务完成后进行转换、组合以及异常处理。
- 通过 `CompletionStage`，你可以编写非阻塞、并行化的代码，以提高应用程序的响应能力和性能。

通常情况下，`CompletableFuture` 是 `CompletionStage` 的具体实现，并提供了更多用于创建、组合和执行异步任务的便捷方法。