# execute() & submit() 
`execute()` 和 `submit()` 是 Java 线程池（`Executor` 接口及其实现类）中最常用的两个方法，它们都用于提交任务到线程池执行，但它们之间有一些重要的区别。下面我将详细介绍这两个方法的功能、区别以及使用场景。

### 一、`execute()` 方法

#### 1. **方法定义**

`execute()` 是 `Executor` 接口中定义的方法，用于提交不需要返回结果的任务。它通常用于执行实现了 `Runnable` 接口的任务。

```java
void execute(Runnable command);
```

- 参数 `command` 是一个实现了 `Runnable` 接口的对象，表示要执行的任务。

#### 2. **功能和行为**

- `execute()` 方法提交的任务是无返回值的。因此，无法获得任务执行的结果或捕获任务执行过程中抛出的异常。
- `execute()` 方法的设计目的是用于那些只执行任务而不关心任务结果的场景。
- 如果任务在执行过程中抛出异常，这个异常会被线程池捕获，并由默认的异常处理机制处理（通常是直接打印异常堆栈）。

#### 3. **使用场景**

- 适用于那些不需要返回值的任务，例如日志记录、更新状态、发送通知等。
- 适用于执行 `Runnable` 任务，且不需要知道任务执行是否成功或失败。

#### 4. **示例代码**

```java
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecuteExample {
    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(2);

        Runnable task1 = () -> System.out.println("Task 1 is running");
        Runnable task2 = () -> System.out.println("Task 2 is running");

        // 提交任务
        executor.execute(task1);
        executor.execute(task2);

        // 关闭线程池
        executor.shutdown();
    }
}
```

在这个例子中，`execute()` 方法提交了两个 `Runnable` 任务到线程池，这些任务被线程池中的线程执行。

### 二、`submit()` 方法

#### 1. **方法定义**

`submit()` 是 `ExecutorService` 接口中的方法，它允许提交实现了 `Callable` 或 `Runnable` 的任务，并返回一个 `Future` 对象。

```java
// 提交一个 Runnable 任务
Future<?> submit(Runnable task);

// 提交一个 Callable 任务
<T> Future<T> submit(Callable<T> task);
```

- `Runnable` 任务返回 `Future<?>`，`Future.get()` 返回 `null`（因为 `Runnable` 没有返回值）。
- `Callable` 任务返回 `Future<T>`，`Future.get()` 返回任务执行的结果。

#### 2. **功能和行为**

- `submit()` 方法允许你提交一个任务到线程池，并返回一个 `Future` 对象。`Future` 对象可以用于获取任务的执行结果或在任务执行过程中发生的异常。
- 通过 `Future.get()` 方法，可以阻塞当前线程直到任务执行完成，并返回执行结果（如果任务是 `Callable`）。
- 如果任务抛出异常，异常将会被封装在 `ExecutionException` 中，并在调用 `Future.get()` 时抛出。

#### 3. **使用场景**

- 当需要获取任务的执行结果时使用 `submit()`，例如计算任务的结果或等待任务完成。
- 提交 `Callable` 任务时，必须使用 `submit()` 方法，因为 `Callable` 可以返回结果。
- 可以用于提交 `Runnable` 任务并获取执行的状态或异常信息。

#### 4. **示例代码**

```java
import java.util.concurrent.*;

public class SubmitExample {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // 提交一个 Runnable 任务
        Future<?> future1 = executor.submit(() -> System.out.println("Runnable task is running"));

        // 提交一个 Callable 任务
        Future<Integer> future2 = executor.submit(() -> {
            System.out.println("Callable task is running");
            return 42;
        });

        // 获取 Callable 任务的结果
        Integer result = future2.get(); // 阻塞直到任务完成
        System.out.println("Callable task result: " + result);

        // 关闭线程池
        executor.shutdown();
    }
}
```

在这个例子中：

- `submit()` 方法提交了一个 `Runnable` 任务和一个 `Callable` 任务。
- 通过 `Future.get()` 获取了 `Callable` 任务的结果。

### 三、`execute()` 和 `submit()` 的对比

| 特性                      | `execute()`                                 | `submit()`                                    |
|---------------------------|---------------------------------------------|-----------------------------------------------|
| 返回值                    | 无返回值                                    | 返回 `Future`，可用于获取结果或捕获异常       |
| 任务类型                  | 只能提交 `Runnable` 任务                    | 可以提交 `Runnable` 或 `Callable` 任务        |
| 异常处理                  | 异常直接由线程池处理，通常是打印堆栈信息    | 异常被封装在 `Future.get()` 中的 `ExecutionException` |
| 使用场景                  | 不需要返回结果或处理异常的任务              | 需要返回结果或处理异常的任务                  |

### 四、使用建议

- **使用 `execute()`**：当你不关心任务的返回结果和异常处理时，`execute()` 是更简单直接的选择。
- **使用 `submit()`**：当你需要获取任务的执行结果，或需要处理任务执行过程中的异常时，`submit()` 更加合适。

### 五、实际应用中的考虑

1. **性能开销**：`submit()` 方法比 `execute()` 有额外的性能开销，因为它需要创建 `Future` 对象并管理结果。如果不需要返回值，`execute()` 可能更高效。

2. **异常处理**：使用 `submit()` 时，异常会被封装在 `Future` 中，如果你不调用 `Future.get()`，异常可能会被忽略。使用 `execute()` 时，异常会由线程池处理并通常打印堆栈信息。

3. **线程池管理**：无论是 `execute()` 还是 `submit()`，都应该在任务提交完成后调用 `shutdown()` 方法来关闭线程池，避免资源泄漏。

### 总结

- **`execute()`** 是用于执行不需要返回值的任务的简单方法，适合提交 `Runnable` 任务。
- **`submit()`** 则更为灵活，可以提交 `Runnable` 和 `Callable` 任务，并通过 `Future` 获取结果或处理异常。

这两个方法为不同的任务需求提供了合适的解决方案，在实际开发中选择合适的方法能够更好地满足应用程序的需求。