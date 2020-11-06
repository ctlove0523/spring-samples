[TOC]

## 取消和超时

本章主要介绍协程的取消和超时

### 协程取消

在一个长时间运行的应用中，你可能需要对后台协程细粒度的控制。例如，一个用户可能关闭了一个页面，这个页面启动了一个协程，现在页面的结果已经不再需要，协程也应该关闭。`launch` 函数返回一个`Job`，通过`Job` 可以取消正在运行的协程：

```kotlin
fun main() = runBlocking {
    val job = launch {
        repeat(1000) { i ->
            println("job: I'm sleeping $i ...")
            delay(500L)
        }
    }
    delay(1300L)
    println("main: I'm tired of waiting!")
    job.cancel() // 取消job
    job.join() // 等待job完成
    println("main: Now I can quit.")
}
```

上面代码输出如下:

```
job: I'm sleeping 0 ...
job: I'm sleeping 1 ...
job: I'm sleeping 2 ...
main: I'm tired of waiting!
main: Now I can quit.
```

只要main协程一经调用`job.cancel` 方法，`job`对应的协程不再输出任何内容，因为协程已经被取消。`Job`有一个扩展函数`cancelAndJoin` 组合了`cancel` 和`join` 两个函数。

### 取消需要协作

协程的取消需要协作，一个协程的代码必须和cancelledable协作。`kotlinx.coroutines`包内的所有挂起函数都是可以取消的（cancellable）。挂起函数检查协程是否取消，如果协程取消则抛出`CancellationException `异常。然而，如果一个协程正在工作而且没有检查协程是否取消，则该协程不可被取消，下面是一个简单的例子：

```kotlin
fun main() = runBlocking {
    val startTime = System.currentTimeMillis()
    val job = launch(Dispatchers.Default) {
        var nextPrintTime = startTime
        var i = 0
        while (i < 5) {
            // 每秒打印两次信息
            if (System.currentTimeMillis() >= nextPrintTime) {
                println("job: I'm sleeping ${i++} ...")
                nextPrintTime += 500L
            }
        }
    }
    delay(1300L)
    println("main: I'm tired of waiting!")
    job.cancelAndJoin()
    println("main: Now I can quit.")
}
```

从代码的输出可以看到，协程无法被取消：

~~~
job: I'm sleeping 0 ...
job: I'm sleeping 1 ...
job: I'm sleeping 2 ...
main: I'm tired of waiting!
job: I'm sleeping 3 ...
job: I'm sleeping 4 ...
main: Now I can quit.
~~~

### 标记计算代码可取消

有两种方法可以使计算代码成为可取消的。第一种方法是定时调用一个挂起函数去检查是否已经取消。`yield` 函数非常适合这项工作。另一种方法是显式的检查取消状态。下面使用最后一种方法重构上一节的代码：

将原来代码中的 `while (i < 5)` 替换为`while (isActive)` ：

```kotlin
fun main() = runBlocking {
    val startTime = System.currentTimeMillis()
    val job = launch(Dispatchers.Default) {
        var nextPrintTime = startTime
        var i = 0
        while (isActive) {
            if (System.currentTimeMillis() >= nextPrintTime) {
                println("job: I'm sleeping ${i++} ...")
                nextPrintTime += 500L
            }
        }
    }
    delay(1300L)
    println("main: I'm tired of waiting!")
    job.cancelAndJoin()
    println("main: Now I can quit.")
}
```

重构后代码的输出如下：

~~~
job: I'm sleeping 0 ...
job: I'm sleeping 1 ...
job: I'm sleeping 2 ...
main: I'm tired of waiting!
main: Now I can quit.
~~~

现在代码已经可以被取消。`isActive`是一个扩展属性，可以用来检查协程状态。

### 使用 `finally` 关闭资源

可取消的挂起函数在取消时抛出`CancellationException` ，异常可以使用常规方法处理。例如，`try {...} finally {...}` 表达式处理异常，在协程已经被取消的情况下，Kotlin使用`use` 函数正常执行最后的动作。

```kotlin
fun main() = runBlocking {
    val job = launch {
        try {
            repeat(1000) { i ->
                println("job: I'm sleeping $i ...")
                delay(500L)
            }
        } finally {
            println("job: I'm running finally")
        }
    }
    delay(1300L)
    println("main: I'm tired of waiting!")
    job.cancelAndJoin()
    println("main: Now I can quit.")
}
```

`join`和 `cancelAndJoin` 都要等待最后的动作执行完成，因此上面的代码的输出结果如下：

```
job: I'm sleeping 0 ...
job: I'm sleeping 1 ...
job: I'm sleeping 2 ...
main: I'm tired of waiting!
job: I'm running finally
main: Now I can quit.
```

### 执行不可取消的代码块

Any attempt to use a suspending function in the `finally` block of the previous example causes [CancellationException](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-cancellation-exception/index.html), because the coroutine running this code is cancelled. Usually, this is not a problem, since all well-behaving closing operations (closing a file, cancelling a job, or closing any kind of a communication channel) are usually non-blocking and do not involve any suspending functions. However, in the rare case when you need to suspend in a cancelled coroutine you can wrap the corresponding code in `withContext(NonCancellable) {...}` using [withContext](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/with-context.html) function and [NonCancellable](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-non-cancellable.html) context as the following example shows:

在之前的例子中，任何一个在`finally` 块使用挂起函数的尝试都会导致`CancellationException`，因为执行代码的协程已经被取消。通常来说这并不是一个问题，因为所有使用良好的关闭操作（关闭文件，取消Job，关闭通信通道）都是非阻塞的而且不需要使用挂起函数。但是，在极少数情况下，你需要在一个已经被取消的协程中暂停，这种场景可以使用`withContext(NonCancellable)` 封装要执行的代码。

```kotlin
fun main() = runBlocking {
    val job = launch {
        try {
            repeat(1000) { i ->
                println("job: I'm sleeping $i ...")
                delay(500L)
            }
        } finally {
            withContext(NonCancellable) {
                println("job: I'm running finally")
                delay(1000L)
                println("job: And I've just delayed for 1 sec because I'm non-cancellable")
            }
        }
    }
    delay(1300L)
    println("main: I'm tired of waiting!")
    job.cancelAndJoin()
    println("main: Now I can quit.")
}
```

上面代码的输出如下：

~~~
job: I'm sleeping 0 ...
job: I'm sleeping 1 ...
job: I'm sleeping 2 ...
main: I'm tired of waiting!
job: I'm running finally
job: And I've just delayed for 1 sec because I'm non-cancellable
main: Now I can quit.
~~~

### 超时

取消一个正在执行的协程的主要原因是执行超时。尽管可以通过获取协程返回的Job，并通过另外一个协程调用`cancel` 方法可以取消协程，但是Kotlin提供了`withTimeOut`函数可以自动的执行取消。下面是一个非常简单的样例：

```kotlin
fun main() = runBlocking {
    withTimeout(1300L) {
        repeat(1000) { i ->
            println("I'm sleeping $i ...")
            delay(500L)
        }
    }
}
```

代码输出如下:

```
I'm sleeping 0 ...
I'm sleeping 1 ...
I'm sleeping 2 ...
Exception in thread "main" kotlinx.coroutines.TimeoutCancellationException: Timed out waiting for 1300 ms
	at kotlinx.coroutines.TimeoutKt.TimeoutCancellationException(Timeout.kt:116)
	at kotlinx.coroutines.TimeoutCoroutine.run(Timeout.kt:86)
	at kotlinx.coroutines.EventLoopImplBase$DelayedRunnableTask.run(EventLoop.common.kt:492)
	at kotlinx.coroutines.EventLoopImplBase.processNextEvent(EventLoop.common.kt:271)
	at kotlinx.coroutines.DefaultExecutor.run(DefaultExecutor.kt:68)
	at java.lang.Thread.run(Thread.java:748)
```

`withTimeout` 函数会抛出`CancellationException`的子异常`TimeoutCancellationException`。在之前的代码中我们并没有看到console打印异常堆栈，这是因为`CancellationException`异常时一个导致协程完成的正常原因。但是在这个例子中，在main函数中使用了`withTimeout` 函数。

既然取消只是一个异常，因此所有资源都可以以常用的方式关闭。可以使用`try {...} catch (e: TimeoutCancellationException) {...}`包装代码来执行一些额外的操作或者使用`withTimeoutOrNull` ：

```kotlin
fun main() = runBlocking {
    val result = withTimeoutOrNull(1300L) {
        repeat(1000) { i ->
            println("I'm sleeping $i ...")
            delay(500L)
        }
        "Done" 
    }
    println("Result is $result")
}
```

上面代码的输出如下：

```
I'm sleeping 0 ...
I'm sleeping 1 ...
I'm sleeping 2 ...
Result is null
```