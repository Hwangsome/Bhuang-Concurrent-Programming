package com.bhuang.future;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

/**
 * CompletableFuture 实现了 Future 接口，同时还实现了 CompletionStage 接口
 */
public class CompletableFutureDemo {

    static class Task implements Callable<Integer> {
        @Override
        public Integer call() throws Exception {
            return 1;
        }
    }

    static class SupplierTask implements Supplier<Integer> {
        private Integer number;
        public SupplierTask(Integer number) {
            this.number = number;
        }
        @Override
        public Integer get() {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            // ForkJoinPool.commonPool-worker-1 Supplier value: 1
            System.out.println(Thread.currentThread().getName() +" Supplier value: " + number);
            return number;
        }
    }

    static void testCompletableFuture() {
        CompletableFuture.supplyAsync(new SupplierTask(1))
                // 任务完成后，将结果传递给回调函数，并返回处理后的结果。
                .thenApply(
                        // 一旦感应到这个线程 处理的任务完成了，返回值到这里
                        result -> {
                            // ForkJoinPool.commonPool-worker-1 thenApply result: 10
                            System.out.println(Thread.currentThread().getName() + " Processing result: " + result * 10);
                            return result * 10;
                        }
                )
                // 任务完成后，使用结果执行回调函数，但不返回新的结果。
                // ForkJoinPool.commonPool-worker-1 task运行结果: 10
                .thenAccept(result -> System.out.println(Thread.currentThread().getName() + " task运行结果: " + result));
        System.out.println(Thread.currentThread().getName() +" 主线程做其他事情");
    }

    // 测试CompletableFuture.allOf()方法
    static void testCompletableFutureAllofFeature() {
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
            // 某些长时间运行的操作
            System.out.println(Thread.currentThread().getName() + " future1 running");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return  "1" ;
        });

        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
            // 某些长时间运行的操作
            System.out.println(Thread.currentThread().getName() + " future2 running");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return  "2" ;
        });

        CompletableFuture<String> future3 = CompletableFuture.supplyAsync(() -> {
            // 某些长时间运行的操作
            System.out.println(Thread.currentThread().getName() + " future3 running");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return  "3" ;
        });

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(future1, future2, future3);
        allFutures.thenRun(() -> {
            try {
                // 3个任务都完成后，执行这个任务
                System.out.println(Thread.currentThread().getName() + " All tasks are completed");
                String result1 = future1.join();
                String result2 = future2.join();
                String result3 = future3.join();
                System.out.println(Thread.currentThread().getName() + " future1 result: " + result1);
                System.out.println(Thread.currentThread().getName() + " future2 result: " + result2);
                System.out.println(Thread.currentThread().getName() + " future3 result: " + result3);
                System.out.println(Thread.currentThread().getName() + " " + result1 + ", " + result2 + ", " + result3);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    static void  testCompletableFutureHandlerErrorFeature() {
        CompletableFuture.supplyAsync(() -> {
            int  result  =  10 / 0 ; // 导致 ArithmeticException
            return result;
        }).exceptionally(ex -> {
            System.out.println( "发生异常： " + ex.getMessage());
            return  0 ; // 如果发生异常，则返回默认值
        }).thenAccept( result -> {
            System.out.println( "结果： " + result);
        });
    }

    static void  testCompletableFutureHandlerMutilErrorFeature() {
        CompletableFuture<Integer> future1 = CompletableFuture.supplyAsync(() -> {
            // Some long-running operation
            return 10;
        });

        CompletableFuture<Integer> future2 = CompletableFuture.supplyAsync(() -> {
            int result = 10 / 0; // Causes an ArithmeticException
            return result;
        }).exceptionally(ex -> {
            System.out.println("处理 future2 异常: " + ex.getMessage());
            return 0; // 如果有异常，返回默认值 0
        });


        CompletableFuture<Integer> future3 = CompletableFuture.supplyAsync(() -> {
            // Some long-running operation
            return 20/0;
        }).exceptionally(ex -> {
            System.out.println("处理 future3异常: " + ex.getMessage());
            return 0; // 如果有异常，返回默认值 0
        });;

        // allOf() 并不直接返回各个 CompletableFuture 的结果，它只等待所有任务完成。
        // CompletableFuture.allOf() 只等待所有 CompletableFuture 完成，不关心各个任务的结果，也不会返回它们的结果。
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(future1, future2, future3);
        System.out.println("All futures are running");

        allFutures.exceptionally(ex -> {
            System.out.println("Exception occurred: " + ex.getMessage());
            return null; // Default value to return if there's an exception
        }).thenRun(() -> {
            // All futures completed
            int result1 = future1.join();
            int result2 = future2.join();
            int result3 = future3.join();
            System.out.println(result1 + ", " + result2 + ", " + result3);
        });
    }

    /**
     * ForkJoinPool.commonPool-worker-1
     * 默认是在ForkJoinPool 线程池中执行异步任务的
     *
     * 这是因为CompletableFuture.supplyAsync()方法默认使用的线程池是ForkJoinPool.commonPool()，
     *     private static final Executor ASYNC_POOL = USE_COMMON_POOL ?
     *         ForkJoinPool.commonPool() : new ThreadPerTaskExecutor();
     */
    static void testCompletableFutureAsync() {
        CompletableFuture.supplyAsync(() -> {
            // Some long-running operation
            // 做一些异步任务， 这个任务有返回值，比如异步任务是从数据库或者网络获取数据
            System.out.println("CompletableFuture.supplyAsync: " + Thread.currentThread().getName() + " Running");
            return 10;
        }).thenApply(result -> {
            // apply 表示将上一个任务的结果作为参数，传到这个lambda函数中执行一些业务操作
            // Process the result
            System.out.println("Result: " + Thread.currentThread().getName() + " " +result);
            return result * 10;
        }).thenAccept(result -> {
            // Print the result
            // thenAccept 表示接收上一个任务的结果，这里需要对这个结果使用，但是不需要返回值
            System.out.println("Result: " + Thread.currentThread().getName() + " "  +result);
        }).thenRun(() -> {
            // Perform some other operation
            // 任务完成后，不使用结果，直接执行一个回调操作
            System.out.println("Operation completed " + Thread.currentThread().getName() );
        });
    }

    // 在执行异步任务的时候， 你也可以不使用默认的线程池，而是使用自定义的线程池
    // supplyAsync 方法还有一个重载的方法可以传一个自定义的线程池
    static void testCompletableFutureCustomExecutor() {
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        try {
            CompletableFuture.supplyAsync(() -> {
                System.out.println("CompletableFuture.supplyAsync: " + Thread.currentThread().getName() + " Running");
                return 10;
            }, executorService).thenApply(result -> {
                System.out.println("Result: " + Thread.currentThread().getName() + " " +result);
                return result * 10;
            }).thenAccept(result -> {
                System.out.println("Result: " + Thread.currentThread().getName() + " "  +result);
            }).thenRun(() -> {
                System.out.println("Operation completed " + Thread.currentThread().getName());
            });
        } finally {
            executorService.shutdown();
        }
    }

    // thenComposeAsync()是一种CompletableFuture允许您以非阻塞方式将多个异步任务链接在一起的方法。
    // 当有一个CompletableFuture对象返回另一个CompletableFuture对象作为其结果，并且想在第一个任务完成后执行第二个任务时，可以使用此方法。
    static void testCompletableFutureComposeAsync() {
        CompletableFuture.supplyAsync(() -> {
            System.out.println("开始煮热水" + Thread.currentThread().getName() + " Running");
            return "热水";
        }).thenComposeAsync(result -> {
            // Process the result
            return CompletableFuture.supplyAsync(() -> "拿到" + result + " - 沏茶");
        }).thenAccept(result -> {
            // Print the result
            System.out.println("Result: " + result);
        });
    }
    public static void main(String[] args) throws InterruptedException {
       // testCompletableFuture();

        //testCompletableFutureAllofFeature();

        // testCompletableFutureHandlerErrorFeature();
        //testCompletableFutureHandlerMutilErrorFeature();

       // testCompletableFutureAsync();

        //testCompletableFutureCustomExecutor();

        // testCompletableFutureComposeAsync();

        // 主线程这里阻塞住， 防止主线程结束，导致CompletableFuture的线程池被关闭
        Thread.sleep(10000);
    }
}
