[TOC]

## 协程基础

在本章我们主要介绍协程的基本概念。

### 第一个协程程序

Run the following code:

```kotlin
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun main(args: Array<String>) {
    GlobalScope.launch { //后台启动一个协程并继续
        delay(1000L) // 非阻塞延迟1秒 (默认单位为毫秒)
        println("${Thread.currentThread().name} thread output: World!") // 1秒延迟后打印信息
    }
    println("${Thread.currentThread().name} thread output: Hello,") // 协程延迟时main线程继续执行
    Thread.sleep(2000L) // 阻塞main线程2秒保证JVM允许。
}
```

上面的代码将输出如下内容:

```
main thread output: Hello,
DefaultDispatcher-worker-2 thread output: World!
```

实际上，协程是轻量级的线程。在某些`CoroutineScope`上下文中使用协程构建器启动一个协程。在上面的代码中，我们在`GlobalScope` 上下文内启动了一个新的协程，这意味着协程的生命周期受整个应用的生命周期限制。

上面代码的一种替代实现如下：

~~~kotlin
import kotlin.concurrent.thread

fun main(args: Array<String>) {
    thread {
        Thread.sleep(1000L)
        println("${Thread.currentThread().name} thread output: World!")
    }
    println("${Thread.currentThread().name} thread output: Hello,")
    Thread.sleep(2000L)
}
~~~

> **注意挂起函数只能由协程或其他挂起函数调用，因为挂起函数不能阻塞线程**。

### 桥接阻塞和非阻塞世界

在第一个例子中我们混合使用了非阻塞的`delay(...)` 和阻塞的`Thread.sleep(...)` 函数。这样很容易忘记哪个函数是阻塞函数，哪个函数是非阻塞函数。可以使用runBlocking协程构建器显式的表明阻塞，就像下面的代码一样：

```kotlin
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) {
    GlobalScope.launch {
        delay(1000L)
        println("${Thread.currentThread().name} thread output: World!")
    }
    println("${Thread.currentThread().name} thread output: Hello,")
    runBlocking {
        delay(2000L)
    }
}
```

除去打印world!的协程名字不同外（每次运行结果可能都不同）其他的输出和上面的输出一致，但是代码只使用了非阻塞的delay函数。main线程调用`runBlocking` 被阻塞，直到`runBlocking` 内的协程执行完成。

可以使用`runBlocking`包裹main函数的执行来重构：

```kotlin
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) = runBlocking<Unit> {
    GlobalScope.launch {
        delay(1000L)
        println("${Thread.currentThread().name} thread output: World!")
    }
    println("${Thread.currentThread().name} thread output: Hello,")
    delay(2000L)
}
```

上面这种方式也常用于单元测试：

```kotlin
class MyTest {
    @Test
    fun testMySuspendingFunction() = runBlocking<Unit> {
        // here we can use suspending functions using any assertion style that we like
    }
}
```

### 等待任务完成

显式等待后台任务完成是一种更好的编码选择：

```kotlin
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) = runBlocking<Unit> {
    val job = GlobalScope.launch {
        delay(1000L)
        println("World!")
    }
    println("Hello,")
    job.join()
}
```

### 结构化并发

在实际使用协程时仍然有很多事情值得思考。当使用`GlobalScope.launch` 加载一个协程时，实际上我们创建了一个生命周期受应用生命周期限制的顶层协程。尽管协程非常的轻量级，但是协程运行依然消耗内存资源。如果忘记了依然在运行中协程的引用，如果协程中的代码被挂住，或者系统加载了过多的协程，系统内存将会被耗光。保存所有协程的引用并调用协程的join方法是非常容易出错的。

在代码中使用结构化并发是一个更好的解决方案。什么是结构化并发，说白了就是在不同的范围内加载协程，而不是在全局范围内加载协程。

使用结构化并发重构上面的代码：

```kotlin
import kotlinx.coroutines.*

fun main() = runBlocking { // 这是协程的scope，我们称为main scope
    launch { // 在main scope内加载一个新的协程
        delay(1000L)
        println("World!")
    }
    println("Hello,")
}
```

> 如果一个协程的scope内还有协程没有执行完成，该协程不会退出。

### Scope 构建器

In addition to the coroutine scope provided by different builders, it is possible to declare your own scope using [coroutineScope](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/coroutine-scope.html) builder. It creates a coroutine scope and does not complete until all launched children complete.

除去了标准库提供的各种协程scope构建器，Kotlin运行使用`coroutineScope` 自定义协程scope。`runBlocking` 和`coroutineScope` 十分的类似，两者都会等待协程内的代码或子协程执行完成。两者的根本区别为：`runBlocking` 阻塞当前线程，`coroutineScope` 释放底层的线程。因此`runBlocking` 是一个普通的函数，而`coroutineScope` 是一个挂起函数。

下面的样例，很好的说明了两者的区别:

```kotlin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


fun main() = runBlocking {
    launch {
        delay(200L)
        println("Task from runBlocking")
    }

    coroutineScope { // 自定义scope
        launch {
            delay(500L)
            println("Task from nested launch")
        }

        delay(100L)
        println("Task from coroutine scope")
    }

    println("Coroutine scope is over")
}
```

上面代码的输出结果如下：

~~~
Task from coroutine scope
Task from runBlocking
Task from nested launch
Coroutine scope is over
~~~

### 协程非常的轻量

JVM使用`-Xmx20M -Xms20M` 分配20M的heap内存，并允许下面的代码加载10000个协程，程序可以正常运行：

```kotlin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


fun main() = runBlocking {
    repeat(100_00) { // launch a lot of coroutines
        launch {
            delay(1000L)
            println(".")
        }
    }
}
```

创建同等数量的线程，可能会导致内存益处错误，由此可见协程占用的资源还是相对较少，协程更加轻量。

### 全局协程更像守护线程

下面的代码启动了一个长时间运行的协程，该协程在主协程退出后返回：

```kotlin
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


fun main() = runBlocking {
    GlobalScope.launch {
        repeat(1000) { i ->
            println("I'm sleeping $i ...")
            delay(500L)
        }
    }
    delay(1300L)
}
```

上面的代码将打印三行并退出：

```
I'm sleeping 0 ...
I'm sleeping 1 ...
I'm sleeping 2 ...
```
