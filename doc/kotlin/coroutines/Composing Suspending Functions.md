[TOC]

## 组合挂起函数

本章我们将介绍多种组合挂起函数的方法。

### 1 默认有序

假设我们定义了两个非常有用的函数，比如执行远程服务调用或计算。为了模拟两个函数都是耗时操作，在例子中我们使用delay进行模拟。

```kotlin
suspend fun remoteServiceCall(): Int {
    delay(1000L)
    return 13
}

suspend fun computation(): Int {
    delay(1000L)
    return 29
}
```

如果想按照`remoteServiceCall` -> `computation` 的顺序调用两个函数并计算两个函数返回结果的和，我们应该怎么做？在实践中，我们可以这样做，使用第一个函数的结果来决定是否需要调用第二个函数或决定如何调用第二个函数。

我们按顺序调用函数，因为协程中的代码和普通的代码一样默认都是按顺序执行。下面的代码通过统计函数的执行时间来证明挂起函数按顺序执行：

```kotlin
fun main() = runBlocking {
    val time = measureTimeMillis {
        val one = remoteServiceCall()
        val two = computation()
        println("The answer is ${one + two}")
    }
    println("Completed in $time ms")
}
```

上面代码的一种可能结果如下：

```
The answer is 42
Completed in 2007 ms
```

### 2 使用 async 实现并发

如果`remoteServiceCall` 和`computation` 之间没有依赖关系，为了更快的得到结果，可以使用async实现两个函数的并发调用。从概念上将`async` 和`launch` 很像。async启动一个独立的协程（轻量级线程）以和其他所有协程并发工作。两者之间的主要区别是，`launch`返回一个`Job`，并且不返回任何计算结果；`async`返回一个`Deferred`，一个代表计算结果的轻量级非阻塞future。可以使用`.await()` 来获取结果，`Deferred`自身是一个`Job`，如果需要可以取消。

```kotlin
fun main() = runBlocking {
    val time = measureTimeMillis {
        val one = async { remoteServiceCall() }
        val two = async { computation() }
        println("The answer is ${one.await() + two.await()}")
    }
    println("Completed in $time ms")
}
```

上面代码的一种可能结果如下：

```
The answer is 42
Completed in 1019 ms
```

和顺序执行相比，并发执行的速度快了一倍。请注意使用协程进行并发操作也是显式的。

### 3 异步懒启动

通过设置`async` 的`start` 参数的值为`CoroutineStart.LAZY` 可以实现协程的懒启动，只有通过`await`方法获取结果或者调用对应`Job`的stat函数时才启动协程。

```kotlin
fun main() = runBlocking {
    val time = measureTimeMillis {
        val one = async(start = CoroutineStart.LAZY) { remoteServiceCall() }
        val two = async(start = CoroutineStart.LAZY) { computation() }
        one.start()
        two.start()
        println("The answer is ${one.await() + two.await()}")
    }
    println("Completed in $time ms")
}
```

上面代码的一种可能结果如下：

```
there are two routines,but not stared
The answer is 42
Completed in 1215 ms
```

上面的例子首先创建了两个协程，但是这些协程还没有启动。但是我们有控制协程的方法，通过调用`start` 方法来启动已经创建的两个协程，然后调用`await` 方法获取计算结果。如果只是在`println` 函数中调用`await` 方法但是不调用`start` 方法，这会导致函数按顺序调用，因为`await`函数启动协程并等待计算结果，这不是用来实现协程懒启动的方法。在调用挂起函数计算的场景， `async(start = CoroutineStart.LAZY)` 优于使用`lazy` 函数。

### 4 异步风格函数

使用`async` 协程构建器+`GlobalScope` 引用可以定义异步风格的函数。为了强调函数的异步性，可以将Async作为函数名的后缀，以提醒使用者计算是异步的，需要显式的获取异步计算结果。

```kotlin
fun remoteServiceCallAsync() = GlobalScope.async {
    remoteServiceCall()
}

fun computationAsync() = GlobalScope.async {
    computation()
}
```

`remoteServiceCallAsync()` 和`computationAsync()` 两个函数并不是挂起函数，它们可以在任何地方被调用 。但是，使用它们始终意味着它们与调用代码的异步执行。

在非协程调用异步函数:

```kotlin
fun main() {
    val time = measureTimeMillis {
        val one = remoteServiceCallAsync()
        val two = computationAsync()
        runBlocking {
            println("The answer is ${one.await() + two.await()}")
        }
    }
    println("Completed in $time ms")
}
```

上面的方法在Kotlin中不是推荐的方法，下面让我们简单分析一下原因。考虑一下，如果`val one = somethingUsefulOneAsync()`和 `one.await()` 之间的代码有逻辑错误，程序抛出异常，正在执行的操作被终止会发生什么？一般来说，一个全局的错误处理器可以捕获异常，记录日志并向开发者报告错误，但是程序不能继续执行其他操作。但是，`remoteServiceCallAsync()`仍然在后台运行，即使调用函数的操作已经终止。下面将要介绍的结构化并发不存在这个问题，是Kotlin更加推荐使用的方式。

### 5 使用async实现结构化并发

以第二节的实例为例，提取一个并发执行`remoteServiceCall()`和`computation()`函数并返回两个函数和的函数。由于`async` 协程构建器是`CoroutineScope`的一个扩展，我们需要将它包含在范围内，这就是`coroutineScope`函数提供的功能：

```kotlin
suspend fun concurrentSum(): Int = coroutineScope {
    val one = async { remoteServiceCall() }
    val two = async { computation() }
    one.await() + two.await()
}
```

使用这种方式，如果在`concurrentSum` 内部发生错误抛出异常，在其作用域中启动的所有协程都会被取消。

```kotlin
fun main() = runBlocking {
    val time = measureTimeMillis {
        println("The answer is ${concurrentSum()}")
    }
    println("Completed in $time ms")
}
```

上面代码的一种可能结果如下：

```
The answer is 42
Completed in 1017 ms
```

取消总是通过协程层次结构传播：

```kotlin
fun main() = runBlocking<Unit> {
    try {
        failedConcurrentSum()
    } catch(e: ArithmeticException) {
        println("Computation failed with ArithmeticException")
    }
}

suspend fun failedConcurrentSum(): Int = coroutineScope {
    val one = async<Int> {
        try {
            delay(Long.MAX_VALUE)
            42
        } finally {
            println("First child was cancelled")
        }
    }
    val two = async<Int> {
        println("Second child throws an exception")
        throw ArithmeticException()
    }
    one.await() + two.await()
}
```

上面代码的一种可能结果如下：

```
Second child throws an exception
First child was cancelled
Computation failed with ArithmeticException
```