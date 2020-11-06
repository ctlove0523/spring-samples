**Table of contents**

- Channels
  - [Channel basics](https://kotlinlang.org/docs/reference/coroutines/channels.html#channel-basics)
  - [Closing and iteration over channels](https://kotlinlang.org/docs/reference/coroutines/channels.html#closing-and-iteration-over-channels)
  - [Building channel producers](https://kotlinlang.org/docs/reference/coroutines/channels.html#building-channel-producers)
  - [Pipelines](https://kotlinlang.org/docs/reference/coroutines/channels.html#pipelines)
  - [Prime numbers with pipeline](https://kotlinlang.org/docs/reference/coroutines/channels.html#prime-numbers-with-pipeline)
  - [Fan-out](https://kotlinlang.org/docs/reference/coroutines/channels.html#fan-out)
  - [Fan-in](https://kotlinlang.org/docs/reference/coroutines/channels.html#fan-in)
  - [Buffered channels](https://kotlinlang.org/docs/reference/coroutines/channels.html#buffered-channels)
  - [Channels are fair](https://kotlinlang.org/docs/reference/coroutines/channels.html#channels-are-fair)
  - [Ticker channels](https://kotlinlang.org/docs/reference/coroutines/channels.html#ticker-channels)

## Channels

Deferred 值为在协程之间传递单个值提供了方便的方法，在协程之间传递流是不二的选择。

### Channel 基础

在概念上`Channel`和 `BlockingQueue` 十分的类似。两者之间最大的不同时，`BlockingQueue` 的`put` 和 `take` 会阻塞，但是对应`Channel` 的 `send` 和 `receive` 会挂起。

```kotlin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val channel = Channel<Int>()
    launch {
        for (x in 1..5) channel.send(x * x)
        channel.close();
    }
    for (i in channel) {
        println(i)
    }
    println("Done!")
}
```

输出:

```
1
4
9
16
25
Done!
```

> 如果不调用`channel.close()` 方法，main线程会一直被阻塞，输出也不会打印Done!。

### 关闭和遍历channels

和队列不同，一个channel可以关闭以表明不会有更多的元素。对于接收端使用`for` 循环遍历channel非常的简单。从概念上讲，channel的关闭像是给channel发送了一个特殊的关闭令牌。一旦接收到关闭令牌循环会立即结束，因此无法保证在关闭之前发送的数据都已经被接收。

### 构建channel 生产者

一个协程生产一系列元素是一种十分常见的模式，这也是生产-消费者模式的一部分，而该模式常用于并发开发中。您可以将这样的生产者抽象为以channel为参数的函数，但这与必须从函数返回结果的常识相反。在生产端Kotlin提供了非常方面的`produce` 构建器，在消费端提供了`consumeEach` 用来代替`for` 循环。

```kotlin
@ExperimentalCoroutinesApi
fun CoroutineScope.produceSquares(): ReceiveChannel<Int> = produce {
    for (x in 1..5) send(x * x)
}

@ExperimentalCoroutinesApi
fun main() = runBlocking {
    val squares = produceSquares()
    squares.consumeEach { println(it) }
    println("Done!")
}
```

### Pipelines

pipeline是一种模式，其中有一个协程生产一个数据流（流可能是无限的），有一个或多个协程消费流做一些处理并生产心的结果，下面是一个简单的例子用来对流中的数字变换为平方：

The main code starts and connects the whole pipeline:

```kotlin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.runBlocking

@ExperimentalCoroutinesApi
fun CoroutineScope.produceNumbers() = produce<Int> {
    var x = 1
    while (true) send(x++) // infinite stream of integers starting from 1
}

@ExperimentalCoroutinesApi
fun CoroutineScope.square(numbers: ReceiveChannel<Int>): ReceiveChannel<Int> = produce {
    for (x in numbers) send(x * x)
}

@ExperimentalCoroutinesApi
fun main() = runBlocking {
    val numbers = produceNumbers()
    val squares = square(numbers)
    repeat(5) {
        println(squares.receive())
    }
    println("Done!")
    coroutineContext.cancelChildren()
}
```

> 所有函数都定义为`CoroutineScope`的扩展函数，因此可以依靠结构化并发来确保应用程序中没有持久运行的全局协程。

### 使用pipeline计算素数

下面我们使用pipeline来计算素数，首先我们需要一个无穷数字的队列：

```kotlin
@ExperimentalCoroutinesApi
fun CoroutineScope.numbersFrom(start: Int) = produce {
    var x = start
    while (true) send(x++)
}
```

以下pipeline阶段过滤输入的数字流，移除所有可被给定质数整除的数字：

```kotlin
@ExperimentalCoroutinesApi
fun CoroutineScope.filter(numbers: ReceiveChannel<Int>, prime: Int) = produce {
    for (x in numbers) if (x % prime != 0) send(x)
}
```

下面的代码产生前10个素数：

```kotlin
fun main() = runBlocking<Unit> {
    var cur = numbersFrom(2)
    repeat(10) {
        val prime = cur.receive()
        println(prime)
        cur = filter(cur, prime)
    }
    coroutineContext.cancelChildren()
}
```

输出：

```
2
3
5
7
11
13
17
19
23
29
```

但是，pipeline使用channel可以充分利用CPU多核能力。实际上，pipeline确实会调用一些挂起函数（比如异步调用远程服务），这些pipeline不能使用`sequence/iterator` 构建，因为和`produce` 不同，他们不允许被任意挂起，相反`produce` 是完全异步的。

### Fan-out

多个协程可能会从同一个channel读取数据。下面是一个每0.1秒产生一个数字的producer：

```kotlin
fun CoroutineScope.produceNumbers() = produce<Int> {
    var x = 1
    while (true) {
        send(x++)
        delay(100)
    }
}
```

处理channel中数据的processor：

```kotlin
fun CoroutineScope.launchProcessor(id: Int, channel: ReceiveChannel<Int>) = launch {
    for (msg in channel) {
        println("Processor #$id received $msg")
    }    
}
```

启动多个processor：

```kotlin
val producer = produceNumbers()
repeat(5) { launchProcessor(it, producer) }
delay(950)
producer.cancel()
```

一种可能的输出如下：

```
Processor #0 received 1
Processor #0 received 2
Processor #1 received 3
Processor #2 received 4
Processor #3 received 5
Processor #4 received 6
Processor #0 received 7
Processor #1 received 8
Processor #2 received 9
Processor #3 received 10
```

请注意，取消生产者协程将关闭其通道，从而最终终止在通道上的迭代。另外，注意如何使用`for` 循环显式的在channel上执行遍历操作。不同于`consumeEach` ，`for` 循环模式可以在多个协程中安全使用。如果一个处理协程失败，其他协程依然可以继续处理channel。

### Fan-in

多个协程可能向同一个channel发送数据。

```kotlin
suspend fun sendString(channel: SendChannel<String>, s: String, time: Long) {
    while (true) {
        delay(time)
        channel.send(s)
    }
}
```

启动多个协程向channel写入数据：

```kotlin
val channel = Channel<String>()
launch { sendString(channel, "foo", 200L) }
launch { sendString(channel, "BAR!", 500L) }
repeat(6) {
	println(channel.receive())
}
coroutineContext.cancelChildren()
```

输出：

```
foo
foo
BAR!
foo
foo
BAR!
```

### 缓冲 channels

无缓冲channel在生产者和消费者处理速度相当时能够很好的工作，如果生产者和消费者处理速度不同，速度快的一方就会被挂起。可以使用`Channel()` 和 `produce` 方法创建带有缓冲的channel。有缓冲的channel在挂起生产者之前允许发送多个数据。

下面是一个简单的样例：

```kotlin
fun main() = runBlocking<Unit> {
    val channel = Channel<Int>(4)
    val sender = launch {
        repeat(10) {
            channel.send(it)
            println("Sending $it")
        }
    }
    delay(1000)
    sender.cancel()
}
```

输出：

```
Sending 0
Sending 1
Sending 2
Sending 3
```

### Channels 是公平的

channel的send和receive操作和在多协程中调用的顺序保持一致，channel遵循先进先出的顺序。有时候channel看上去是不公平的，这与使用的执行器相关。

### Ticker channels

Ticker channel是一种特殊的会和channel，Ticker channel自最后一次消费起每间隔给定的延迟产生一个`Unit`。Ticker channel单独使用没有太大的意义，但是在构建复杂的基于时间，时间窗的操作时非常有用。Ticker channel可以用于select中来表示一个tick动作。可以使用`ticker` 工厂方法创建Ticker channel，并使用`ReceiveChannel.cancel` 方法关闭Ticker channel。