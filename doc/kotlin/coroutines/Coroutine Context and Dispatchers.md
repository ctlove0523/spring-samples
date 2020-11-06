[TOC]

## 协程上下文和调度器

协程始终在由Kotlin标准库中定义的CoroutineContext类型的值表示的某些上下文中执行。协程上下文由一组各种元素组成。协程上下文的主要元素包括Job和dispatcher。

### 调度器和线程

------

协程上下文包含一个协程调度器，调度器负责决定哪个线程用来执行协程。协程调度器可以将协程的执行限定在特定线程，调度到一个线程池或让协程的执行不受限制。

所有的协程构建器（比如`launch`和`async`）都有一个可选的参数`CoroutineContext`，该参数可用于为新协程和其他上下文元素显式指定调度器。

Try the following example:

```kotlin
@ObsoleteCoroutinesApi
fun main() = runBlocking<Unit> {
    launch {
        println("main runBlocking      : I'm working in thread ${Thread.currentThread().name}")
    }
    launch(Dispatchers.Unconfined) {
        println("Unconfined            : I'm working in thread ${Thread.currentThread().name}")
    }
    launch(Dispatchers.Default) {
        println("Default               : I'm working in thread ${Thread.currentThread().name}")
    }
    launch(newSingleThreadContext("MyOwnThread")) {
        // will get its own new thread
        println("newSingleThreadContext: I'm working in thread ${Thread.currentThread().name}")
    }
}
```

上面代码的输出如下（顺序可能不同）:

```
Unconfined            : I'm working in thread main
Default               : I'm working in thread DefaultDispatcher-worker-1
newSingleThreadContext: I'm working in thread MyOwnThread
main runBlocking      : I'm working in thread main
```

当不使用参数使用`launch { ... }`时，会从启动协程的`CoroutineScope`中继承上下文。在上面的例子中，`launch { ... }` 继承main中`runBlocking` 协程的上下文。`Dispatchers.Unconfined`是一种特殊的调度器，它似乎也运行在主线程中，但实际上，这是一种不同的机制，稍后将进行说明。`Dispatchers.Default` 是`GlobalScope`中启动协程时使用的默认调度器，使用共享的后台线程池，因此`launch（Dispatchers.Default）{...}`与`GlobalScope.launch {...}`使用相同的调度程序。 `newSingleThreadContext`创建一个线程用于协程的运行。专用线程是非常昂贵的资源。在实际的应用程序中，必须在不再需要时使用close函数将其释放，或者将其存储在顶级变量中，然后在整个应用程序中重复使用。

### Unconfined vs confined dispatcher

------

`Dispatchers.Unconfined`协程调度器在调用的线程内启动协程，但是只在遇到第一个挂起点时才启动协程。挂起后，它将在线程中恢复协程，该协程完全由所调用的挂起函数确定。不受限的调度器适用于既不占用CPU也不更新受限于特定线程的任何共享数据（如UI）的协程。

On the other side, the dispatcher is inherited from the outer [CoroutineScope](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-coroutine-scope/index.html) by default. The default dispatcher for the [runBlocking](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/run-blocking.html) coroutine, in particular, is confined to the invoker thread, so inheriting it has the effect of confining execution to this thread with predictable FIFO scheduling.

另一方面，`Dispatchers.Unconfined` 调度器默认继承自外部`CoroutineScope`。`runBlocking` 协程默认的调度器限制在调用线程内，继承的控制器相当于以先进先出的顺序在调用线程上调度。

```kotlin
fun main() = runBlocking<Unit> {
    launch(Dispatchers.Unconfined) {
        // 无限定 -- 再main线程中
        println("Unconfined      : I'm working in thread ${Thread.currentThread().name}")
        delay(500)
        println("Unconfined      : After delay in thread ${Thread.currentThread().name}")
    }
    launch {
        // 继承 main runBlocking coroutine的上下文
        println("main runBlocking: I'm working in thread ${Thread.currentThread().name}")
        delay(1000)
        println("main runBlocking: After delay in thread ${Thread.currentThread().name}")
    }
}
```

产生如下输出:

```
Unconfined      : I'm working in thread main
main runBlocking: I'm working in thread main
Unconfined      : After delay in thread kotlinx.coroutines.DefaultExecutor
main runBlocking: After delay in thread main
```

继承自 `runBlocking {...}` 上下文件的协程仍然在`main`线程中执行，经过`delay` 函数的调用后，会重用默认的executor执行。

> 无限制调度器是一种高级机制，在不需要协程进行分派以便以后执行的场景中非常有用，否则会产生不良的副作用，因为协程中的某些操作必须立即执行。 但是不应在通用代码中使用。



### 调试协程和线程

------

协程可以在一个线程中被挂起，然后在另外一个线程中恢复。即使使用单线程调度器，搞清楚何时何地协程做了哪些工作依然十分的困难。一种常用的调试手段就是在每一个日志打印语句中都加上线程的名字。使用协程时，线程名并不能提供太多上下文信息，`kotlinx.coroutines`提供了方法以方便协程的调试（JVM参数：Dkotlinx.coroutines.debug）。

使用 `-Dkotlinx.coroutines.debug` JVM 参数运行下面一段简单程序：

```kotlin
@ObsoleteCoroutinesApi
fun log(msg: String) = println("[${Thread.currentThread().name}] $msg")

@ObsoleteCoroutinesApi
fun main() = runBlocking<Unit> {
    val a = async {
        log("I'm computing a piece of the answer")
        6
    }
    val b = async {
        log("I'm computing another piece of the answer")
        7
    }
    log("The answer is ${a.await() * b.await()}")
}
```

输出如下：

```
[main @coroutine#2] I'm computing a piece of the answer
[main @coroutine#3] I'm computing another piece of the answer
[main @coroutine#1] The answer is 42
```

### 上下文切换

使用debug参数执行下面的代码：

```kotlin
fun main() = runBlocking<Unit> {
    newSingleThreadContext("Ctx1").use { ctx1 ->
        newSingleThreadContext("Ctx2").use { ctx2 ->
            runBlocking(ctx1) {
                log("Started in ctx1")
                withContext(ctx2) {
                    log("Working in ctx2")
                }
                log("Back to ctx1")
            }
        }
    }
}
```

上面的代码的说明了一些新的技术。一个是`runBlocking` 可以和一个显式的上下文一起使用，另一个是通过`withContext` 函数可以切换协程的上下文，代码的输出也说明了这一点：

~~~
[Ctx1 @coroutine#2] Started in ctx1
[Ctx2 @coroutine#2] Working in ctx2
[Ctx1 @coroutine#2] Back to ctx1
~~~



> `use` 函数可以自动释放`newSingleThreadContext` 创建的线程。

### 上下文中的Job

协程的Job是上下文的一部分，可以使用 `coroutineContext[Job]` 表达式获取：

```kotlin
println("My job is ${coroutineContext[Job]}")
```

debug模式下输出如下（数值可能不同，但是格式一致）：

```
My job is "coroutine#1":BlockingCoroutine{Active}@6d311334
```

### 子协程

在`CoroutineScope` 内协程A启动协程B，那么协程B就继承了协程A的上下文，协程B的Job是协程A的Job的一个子Job。当父协程被取消是，所有的子协程会递归的被取消。有一种情况例外，就是在`GlobalScope` 内启动的新协程的Job没有父Job。因此新协程不依赖于启动它的scope而且操作都是独立的。

```kotlin
fun main() = runBlocking<Unit> {
    val request = launch {
        GlobalScope.launch {
            println("job1: I run in GlobalScope and execute independently!")
            delay(1000)
            println("job1: I am not affected by cancellation of the request")
        }
        // and the other inherits the parent context
        launch {
            delay(100)
            println("job2: I am a child of the request coroutine")
            delay(1000)
            println("job2: I will not execute this line if my parent request is cancelled")
        }
    }
    delay(500)
    request.cancel()
    delay(1000)
    println("main: Who has survived request cancellation?")
}
```

上面代码的输出如下：

```
job1: I run in GlobalScope and execute independently!
job2: I am a child of the request coroutine
job1: I am not affected by cancellation of the request
main: Who has survived request cancellation?
```

### 父协程的责任

------

等待子协程完成是父协程的责任。父协程无需显式跟踪所有的子协程，也不需要使用`Job.join` 等待子协程完成。

```kotlin
fun main() = runBlocking<Unit> {
    val request = launch {
        repeat(3) { i ->
            launch {
                delay((i + 1) * 200L)
                println("Coroutine $i is done")
            }
        }
        println("request: I'm done and I don't explicitly join my children that are still active")
    }
    request.join()
    println("Now processing of the request is complete")
}
```

输出结果可能如下：

```
request: I'm done and I don't explicitly join my children that are still active
Coroutine 0 is done
Coroutine 1 is done
Coroutine 2 is done
Now processing of the request is complete
```

### 协程命名

------

通常来说协程的自动编号已经能够满足要求，但是当协程负责处理一些特殊请求或者在后台执行某些任务时，协程有用一个和功能相关的名字会更加方便调试。`CoroutineName` 上下文元素和线程名提供相同的能力，包括执行协程的线程的名字（debug模式下）。

下面是一个简单的样例：

```kotlin
fun main() = runBlocking<Unit> {
    log("Started main coroutine")
    val v1 = async(CoroutineName("v1coroutine")) {
        delay(500)
        log("Computing v1")
        252
    }
    val v2 = async(CoroutineName("v2coroutine")) {
        delay(1000)
        log("Computing v2")
        6
    }
    log("The answer for v1 / v2 = ${v1.await() / v2.await()}")
}
```

使用debug参数启动JVM，代码输出如下：

```
[main @coroutine#1] Started main coroutine
[main @v1coroutine#2] Computing v1
[main @v2coroutine#3] Computing v2
[main @coroutine#1] The answer for v1 / v2 = 42
```

### 组合上下文元素

------

有时候需要在协程的上下文定义多个元素，Kotlin提供了`+` 操作符来支持。

```kotlin
fun main() = runBlocking<Unit> {
    launch(Dispatchers.Default + CoroutineName("test")) {
        println("I'm working in thread ${Thread.currentThread().name}")
    }
}
```

使用参数 `-Dkotlinx.coroutines.debug` 启动JVM，输出如下：

```
I'm working in thread DefaultDispatcher-worker-1 @test#2
```

### 线程局部数据

------

拥有在协程之间传递一些线程局部数据的能力有时候会带来极大的便利。但是，因为协程没有和任何特定的线程绑定，如果通过手动的方式实现可能会出现很多样本代码。Kotlin提供了扩展函数`asContextElement`用来实现类似的功能，它创建一个额外的上下文元素用来保存`ThreadLocal`的值，并在协程切换上文时携带数据。 

下面的代码很容易证明这一点：

```kotlin
val threadLocal = ThreadLocal<String?>()

fun main() = runBlocking<Unit> {
    threadLocal.set("main")
    println("Pre-main, current thread: ${Thread.currentThread()}, thread local value: '${threadLocal.get()}'")
    val job = launch(Dispatchers.Default + threadLocal.asContextElement(value = "launch")) {
        println("Launch start, current thread: ${Thread.currentThread()}, thread local value: '${threadLocal.get()}'")
        yield()
        println("After yield, current thread: ${Thread.currentThread()}, thread local value: '${threadLocal.get()}'")
    }
    job.join()
    println("Post-main, current thread: ${Thread.currentThread()}, thread local value: '${threadLocal.get()}'")
}
```

输出如下:

```
Pre-main, current thread: Thread[main @coroutine#1,5,main], thread local value: 'main'
Launch start, current thread: Thread[DefaultDispatcher-worker-1 @coroutine#2,5,main], thread local value: 'launch'
After yield, current thread: Thread[DefaultDispatcher-worker-2 @coroutine#2,5,main], thread local value: 'launch'
Post-main, current thread: Thread[main @coroutine#1,5,main], thread local value: 'main'
```
