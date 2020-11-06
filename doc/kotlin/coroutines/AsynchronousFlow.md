[TOC]



## 异步流

Suspending functions asynchronously returns a single value, but how can we return multiple asynchronously computed values? This is where Kotlin Flows come in.

挂起函数异步返回一个值，但是如何我们才能获得多个异步计算的值？Kotlin的Flow就是专门用来解决这个问题。

### 表示多个值

在Kotlin中可以使用collection表示多个值。

```kotlin
fun foo(): List<Int> = listOf(1, 2, 3)

fun main() {
    foo().forEach { println(it) }
}
```

#### Sequences

如果每一个值的计算都销毁非常多的CPU时间，可以使用`Sequence ` 表示多个值。

```kotlin
fun foo(): Sequence<Int> = sequence {
    for (i in 1..3) {
        Thread.sleep(100)
        yield(i)
    }
}

fun main() {
    foo().forEach { value -> println(value) }
}
```

#### 挂起函数

但是，上面的计算代码会阻塞主线程，如果使用异步代码计算，这样即可以返回计算结果又不会阻塞线程。

```kotlin
suspend fun foo(): List<Int> {
    delay(1000)
    return listOf(1, 2, 3)
}

fun main() = runBlocking<Unit> {
    foo().forEach { value -> println(value) }
}
```

#### Flows

使用`List` 作为返回类型，这意味着需要一次返回所有的值。为了表示异步计算的值流，我们可以使用Flow类型，就像在同步计算中使用Sequence类型：

```kotlin
fun foo(): Flow<Int> = flow {
    for (i in 1..3) {
        delay(100)
        emit(i)
    }
}

fun main() = runBlocking<Unit> {
    launch {
        for (k in 1..3) {
            println("I'm not blocked $k")
            delay(100)
        }
    }
    foo().collect { value -> println(value) }
}
```

输出：

```
I'm not blocked 1
1
I'm not blocked 2
2
I'm not blocked 3
3
```

使用`Flow`和其他的区别：

- 使用`flow` 函数构建一个`Flow` 类型的值。
- `flow { ...}` 构建器内的代码可以被挂起。
- `foo()` 函数不再需要使用`suspend` 修饰符表明可以被挂起。
- 使用`emit` 函数返回值。
- 使用`collect` 函数从flow中收集值。

> 如果使用`Thead.sleep`函数替换`delay` ，main线程会被阻塞。

### ‘冷’ Flows

------

Flows是类似于sequences的‘冷’ 流：`flow` 构建器内的代码不会被执行直到需要从flow获取元素为止。下面的代码很容易证明这一点：

```kotlin
fun foo(): Flow<Int> = flow {
    println("Flow started")
    for (i in 1..3) {
        delay(100)
        emit(i)
    }
}

fun main() = runBlocking<Unit> {
    println("Calling foo...")
    val flow = foo()
    println("Calling collect...")
    flow.collect { value -> println(value) }
    println("Calling collect again...")
    flow.collect { value -> println(value) }
}
```

输出：

```
Calling foo...
Calling collect...
Flow started
1
2
3
Calling collect again...
Flow started
1
2
3
```

这是`foo()` 函数不需要使用`suspend` 修饰符的主要原因，`foo()` 函数会立即返回。每次从flow收集元素的时候，flow会每次都启动。

### 取消 Flow

Flow遵从协程取消的一般约定。但是，flow没有引入其他的取消点（方法）。Flow的取消是完全透明的。当flow在一个可取消的挂起函数中时flow才能够被取消，否则流是不可取消。

下面的代码展示了如何在`withTimeoutOrNull` 方法内取消flow：

```kotlin
fun foo(): Flow<Int> = flow {
    for (i in 1..3) {
        delay(100)
        println("Emitting $i")
        emit(i)
    }
}

fun main() = runBlocking<Unit> {
    withTimeoutOrNull(250) {
        foo().collect { value -> println(value) }
    }
    println("Done")
}
```

只能从`foo()` 函数获取到两个值：

```
Emitting 1
1
Emitting 2
2
Done
```

### Flow 构建器

------

除了`flow { ... }` 构建器之外，还有下面两个构建器用于方便的构建flows：

- 产生固定数量元素的`flowOf`。
- 将各种collection和sequence转换为flow的扩展函数`.asFlow()` 。

So, the example that prints the numbers from 1 to 3 from a flow can be written as:

```
// Convert an integer range to a flow
(1..3).asFlow().collect { value -> println(value) }
```

### flow的中间操作

------

就像collection和sequence一样，可以对flow进行变换。对上游flow执行中间 操作会生成一个新的flow。这些操作都是‘冷’操作，就像flow自身一样。调用中间操作无需一定是挂起函数，中间操作会迅速的返回经过转换的flow。

这些基本操作都有非常熟悉的名字，比如`map`和`filter` 。和sequence中对应函数的最大不同是这些函数中的代码可以调用挂起函数。

```kotlin
suspend fun performRequest(request: Int): String {
    delay(1000)
    return "response $request"
}

fun main() = runBlocking<Unit> {
    (1..3).asFlow()
            .map { request -> performRequest(request) }
            .collect { response -> println(response) }
}
```

输出：

```
response 1
response 2
response 3
```

#### 变换操作

------

在flow所有的操作之中，最常用的是一个称为`transform` 的操作。他可以用来替代简单的`map` 和`filter` 操作，也可以实现更加复杂的变换操作。使用`transform` 操作符，可以产生任意值任意次数。

下面是一个简单的样例：

```kotlin
suspend fun performRequest(request: Int): String {
    delay(1000)
    return "response $request"
}

fun main() = runBlocking<Unit> {
    (1..3).asFlow()
        .transform { request ->
            emit("Making request $request") 
            emit(performRequest(request)) 
        }
        .collect { response -> println(response) }
}
```

代码输出：

```
Making request 1
response 1
Making request 2
response 2
Making request 3
response 3
```

#### 限制大小运算

像`take` 这样限制大小的中间运算，可以在结果大小达到限制时关闭flow。在协程中往往通过抛出异常来执行取消操作，因此在取消场景，所有的资源操作函数都可以正常工作。

```kotlin
fun numbers(): Flow<Int> = flow {
    try {
        emit(1)
        emit(2)
        println("This line will not execute")
        emit(3)
    } finally {
        println("Finally in numbers")
    }
}

fun main() = runBlocking<Unit> {
    numbers()
            .take(2)
            .collect { value -> println(value) }
}
```



代码的输出清晰的阐明了`flow {...}`函数的执行：

```
1
2
Finally in numbers
```

### 终止操作

flow的终止操作会挂起函数并从flow中收集数据。`collect` 是最常用的一个终止操作，但是还有很多其他的终止操作，这些操作可以让数据的收集变的更加方便。

- 将flow转换成其他collection，比如`toList` 和`toSet` 。
- 获取flow第一个值的`first` 和包装flow至少有一个值的`single`。
- reduce操作：`reduce` 和 `fold`。

一个求和的简单样例：

~~~kotlin
@ExperimentalCoroutinesApi
fun main() = runBlocking<Unit> {
    val sum = (1..5).asFlow()
            .map { it * it }
            .reduce { a, b -> a + b }
    println(sum)
}
~~~

### Flows are sequential

除非使用对多个流进行操作的特殊运算符，否则flow的每个操作都依次按顺序执行。所有的操作都在调用终止操作的协程内运行，默认不会启动新的协程。flow中的每一个值都会被中间操作处理然后发送至终止操作。

下面是一个过滤偶数并变为字符串的样例：

```kotlin
fun main() = runBlocking<Unit> {
    (1..5).asFlow()
            .filter {
                println("Filter $it")
                it % 2 == 0
            }
            .map {
                println("Map $it")
                "string $it"
            }.collect {
                println("Collect $it")
            }
}
```

输出：

```
Filter 1
Filter 2
Map 2
Collect string 2
Filter 3
Filter 4
Map 4
Collect string 4
Filter 5
```

### Flow context

Collection of a flow always happens in the context of the calling coroutine. For example, if there is a `foo` flow, then the following code runs in the context specified by the author of this code, regardless of the implementation details of the `foo` flow:

```
withContext(context) {
    foo.collect { value ->
        println(value) // run in the specified context 
    }
}
```

This property of a flow is called *context preservation*.

So, by default, code in the `flow { ... }` builder runs in the context that is provided by a collector of the corresponding flow. For example, consider the implementation of `foo` that prints the thread it is called on and emits three numbers:

```
fun foo(): Flow<Int> = flow {
    log("Started foo flow")
    for (i in 1..3) {
        emit(i)
    }
}  

fun main() = runBlocking<Unit> {
    foo().collect { value -> log("Collected $value") } 
}            
```

Target platform: JVMRunning on kotlin v. 1.3.61

> You can get the full code from [here](https://github.com/kotlin/kotlinx.coroutines/blob/master/kotlinx-coroutines-core/jvm/test/guide/example-flow-13.kt).

Running this code produces:

```
[main @coroutine#1] Started foo flow
[main @coroutine#1] Collected 1
[main @coroutine#1] Collected 2
[main @coroutine#1] Collected 3
```

Since `foo().collect` is called from the main thread, the body of `foo`'s flow is also called in the main thread. This is the perfect default for fast-running or asynchronous code that does not care about the execution context and does not block the caller.

#### Wrong emission withContext

However, the long-running CPU-consuming code might need to be executed in the context of [Dispatchers.Default](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-dispatchers/-default.html) and UI-updating code might need to be executed in the context of [Dispatchers.Main](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-dispatchers/-main.html). Usually, [withContext](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/with-context.html) is used to change the context in the code using Kotlin coroutines, but code in the `flow { ... }` builder has to honor the context preservation property and is not allowed to [emit](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/-flow-collector/emit.html) from a different context.

Try running the following code:

```
fun foo(): Flow<Int> = flow {
    // The WRONG way to change context for CPU-consuming code in flow builder
    kotlinx.coroutines.withContext(Dispatchers.Default) {
        for (i in 1..3) {
            Thread.sleep(100) // pretend we are computing it in CPU-consuming way
            emit(i) // emit next value
        }
    }
}

fun main() = runBlocking<Unit> {
    foo().collect { value -> println(value) } 
}            
```

Target platform: JVMRunning on kotlin v. 1.3.61

> You can get the full code from [here](https://github.com/kotlin/kotlinx.coroutines/blob/master/kotlinx-coroutines-core/jvm/test/guide/example-flow-14.kt).

This code produces the following exception:

```
Exception in thread "main" java.lang.IllegalStateException: Flow invariant is violated:
        Flow was collected in [CoroutineId(1), "coroutine#1":BlockingCoroutine{Active}@5511c7f8, BlockingEventLoop@2eac3323],
        but emission happened in [CoroutineId(1), "coroutine#1":DispatchedCoroutine{Active}@2dae0000, DefaultDispatcher].
        Please refer to 'flow' documentation or use 'flowOn' instead
    at ...
```

#### flowOn operator

The exception refers to the [flowOn](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/flow-on.html) function that shall be used to change the context of the flow emission. The correct way to change the context of a flow is shown in the example below, which also prints the names of the corresponding threads to show how it all works:

```
fun foo(): Flow<Int> = flow {
    for (i in 1..3) {
        Thread.sleep(100) // pretend we are computing it in CPU-consuming way
        log("Emitting $i")
        emit(i) // emit next value
    }
}.flowOn(Dispatchers.Default) // RIGHT way to change context for CPU-consuming code in flow builder

fun main() = runBlocking<Unit> {
    foo().collect { value ->
        log("Collected $value") 
    } 
}            
```

Target platform: JVMRunning on kotlin v. 1.3.61

> You can get the full code from [here](https://github.com/kotlin/kotlinx.coroutines/blob/master/kotlinx-coroutines-core/jvm/test/guide/example-flow-15.kt).

Notice how `flow { ... }` works in the background thread, while collection happens in the main thread:

Another thing to observe here is that the [flowOn](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/flow-on.html) operator has changed the default sequential nature of the flow. Now collection happens in one coroutine ("coroutine#1") and emission happens in another coroutine ("coroutine#2") that is running in another thread concurrently with the collecting coroutine. The [flowOn](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/flow-on.html) operator creates another coroutine for an upstream flow when it has to change the [CoroutineDispatcher](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-coroutine-dispatcher/index.html) in its context.

### Buffering

Running different parts of a flow in different coroutines can be helpful from the standpoint of the overall time it takes to collect the flow, especially when long-running asynchronous operations are involved. For example, consider a case when the emission by `foo()` flow is slow, taking 100 ms to produce an element; and collector is also slow, taking 300 ms to process an element. Let's see how long it takes to collect such a flow with three numbers:

```
fun foo(): Flow<Int> = flow {
    for (i in 1..3) {
        delay(100) // pretend we are asynchronously waiting 100 ms
        emit(i) // emit next value
    }
}

fun main() = runBlocking<Unit> { 
    val time = measureTimeMillis {
        foo().collect { value -> 
            delay(300) // pretend we are processing it for 300 ms
            println(value) 
        } 
    }   
    println("Collected in $time ms")
}
```

Target platform: JVMRunning on kotlin v. 1.3.61

> You can get the full code from [here](https://github.com/kotlin/kotlinx.coroutines/blob/master/kotlinx-coroutines-core/jvm/test/guide/example-flow-16.kt).

It produces something like this, with the whole collection taking around 1200 ms (three numbers, 400 ms for each):

```
1
2
3
Collected in 1220 ms
```

We can use a [buffer](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/buffer.html) operator on a flow to run emitting code of `foo()` concurrently with collecting code, as opposed to running them sequentially:

```
val time = measureTimeMillis {
    foo()
        .buffer() // buffer emissions, don't wait
        .collect { value -> 
            delay(300) // pretend we are processing it for 300 ms
            println(value) 
        } 
}   
println("Collected in $time ms")
```

Target platform: JVMRunning on kotlin v. 1.3.61

> You can get the full code from [here](https://github.com/kotlin/kotlinx.coroutines/blob/master/kotlinx-coroutines-core/jvm/test/guide/example-flow-17.kt).

It produces the same numbers just faster, as we have effectively created a processing pipeline, having to only wait 100 ms for the first number and then spending only 300 ms to process each number. This way it takes around 1000 ms to run:

```
1
2
3
Collected in 1071 ms
```

> Note that the [flowOn](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/flow-on.html) operator uses the same buffering mechanism when it has to change a [CoroutineDispatcher](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-coroutine-dispatcher/index.html), but here we explicitly request buffering without changing the execution context.

#### Conflation

When a flow represents partial results of the operation or operation status updates, it may not be necessary to process each value, but instead, only most recent ones. In this case, the [conflate](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/conflate.html) operator can be used to skip intermediate values when a collector is too slow to process them. Building on the previous example:

```
val time = measureTimeMillis {
    foo()
        .conflate() // conflate emissions, don't process each one
        .collect { value -> 
            delay(300) // pretend we are processing it for 300 ms
            println(value) 
        } 
}   
println("Collected in $time ms")
```

Target platform: JVMRunning on kotlin v. 1.3.61

> You can get the full code from [here](https://github.com/kotlin/kotlinx.coroutines/blob/master/kotlinx-coroutines-core/jvm/test/guide/example-flow-18.kt).

We see that while the first number was still being processed the second, and third were already produced, so the second one was *conflated* and only the most recent (the third one) was delivered to the collector:

```
1
3
Collected in 758 ms
```

#### Processing the latest value

Conflation is one way to speed up processing when both the emitter and collector are slow. It does it by dropping emitted values. The other way is to cancel a slow collector and restart it every time a new value is emitted. There is a family of `xxxLatest` operators that perform the same essential logic of a `xxx` operator, but cancel the code in their block on a new value. Let's try changing [conflate](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/conflate.html) to [collectLatest](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/collect-latest.html) in the previous example:

```
val time = measureTimeMillis {
    foo()
        .collectLatest { value -> // cancel & restart on the latest value
            println("Collecting $value") 
            delay(300) // pretend we are processing it for 300 ms
            println("Done $value") 
        } 
}   
println("Collected in $time ms")
```

Target platform: JVMRunning on kotlin v. 1.3.61

> You can get the full code from [here](https://github.com/kotlin/kotlinx.coroutines/blob/master/kotlinx-coroutines-core/jvm/test/guide/example-flow-19.kt).

Since the body of [collectLatest](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/collect-latest.html) takes 300 ms, but new values are emitted every 100 ms, we see that the block is run on every value, but completes only for the last value:

```
Collecting 1
Collecting 2
Collecting 3
Done 3
Collected in 741 ms
```

### Composing multiple flows

There are lots of ways to compose multiple flows.

#### Zip

Just like the [Sequence.zip](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.sequences/zip.html) extension function in the Kotlin standard library, flows have a [zip](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/zip.html) operator that combines the corresponding values of two flows:

```

val nums = (1..3).asFlow() // numbers 1..3
val strs = flowOf("one", "two", "three") // strings 
nums.zip(strs) { a, b -> "$a -> $b" } // compose a single string
    .collect { println(it) } // collect and print
```

Target platform: JVMRunning on kotlin v. 1.3.61

> You can get the full code from [here](https://github.com/kotlin/kotlinx.coroutines/blob/master/kotlinx-coroutines-core/jvm/test/guide/example-flow-20.kt).

This example prints:

```
1 -> one
2 -> two
3 -> three
```

#### Combine

When flow represents the most recent value of a variable or operation (see also the related section on [conflation](https://kotlinlang.org/docs/reference/coroutines/flow.html#conflation)), it might be needed to perform a computation that depends on the most recent values of the corresponding flows and to recompute it whenever any of the upstream flows emit a value. The corresponding family of operators is called [combine](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/combine.html).

For example, if the numbers in the previous example update every 300ms, but strings update every 400 ms, then zipping them using the [zip](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/zip.html) operator will still produce the same result, albeit results that are printed every 400 ms:

> We use a [onEach](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/on-each.html) intermediate operator in this example to delay each element and make the code that emits sample flows more declarative and shorter.

```

val nums = (1..3).asFlow().onEach { delay(300) } // numbers 1..3 every 300 ms
val strs = flowOf("one", "two", "three").onEach { delay(400) } // strings every 400 ms
val startTime = System.currentTimeMillis() // remember the start time 
nums.zip(strs) { a, b -> "$a -> $b" } // compose a single string with "zip"
    .collect { value -> // collect and print 
        println("$value at ${System.currentTimeMillis() - startTime} ms from start") 
    } 
```

Target platform: JVMRunning on kotlin v. 1.3.61

> You can get the full code from [here](https://github.com/kotlin/kotlinx.coroutines/blob/master/kotlinx-coroutines-core/jvm/test/guide/example-flow-21.kt).

However, when using a [combine](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/combine.html) operator here instead of a [zip](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/zip.html):

```

val nums = (1..3).asFlow().onEach { delay(300) } // numbers 1..3 every 300 ms
val strs = flowOf("one", "two", "three").onEach { delay(400) } // strings every 400 ms          
val startTime = System.currentTimeMillis() // remember the start time 
nums.combine(strs) { a, b -> "$a -> $b" } // compose a single string with "combine"
    .collect { value -> // collect and print 
        println("$value at ${System.currentTimeMillis() - startTime} ms from start") 
    } 
```

Target platform: JVMRunning on kotlin v. 1.3.61

> You can get the full code from [here](https://github.com/kotlin/kotlinx.coroutines/blob/master/kotlinx-coroutines-core/jvm/test/guide/example-flow-22.kt).

We get quite a different output, where a line is printed at each emission from either `nums` or `strs` flows:

```
1 -> one at 452 ms from start
2 -> one at 651 ms from start
2 -> two at 854 ms from start
3 -> two at 952 ms from start
3 -> three at 1256 ms from start
```

### Flattening flows

Flows represent asynchronously received sequences of values, so it is quite easy to get in a situation where each value triggers a request for another sequence of values. For example, we can have the following function that returns a flow of two strings 500 ms apart:

```
fun requestFlow(i: Int): Flow<String> = flow {
    emit("$i: First") 
    delay(500) // wait 500 ms
    emit("$i: Second")    
}
```

Now if we have a flow of three integers and call `requestFlow` for each of them like this:

```
(1..3).asFlow().map { requestFlow(it) }
```

Then we end up with a flow of flows (`Flow>`) that needs to be *flattened* into a single flow for further processing. Collections and sequences have [flatten](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.sequences/flatten.html) and [flatMap](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.sequences/flat-map.html) operators for this. However, due the asynchronous nature of flows they call for different *modes* of flattening, as such, there is a family of flattening operators on flows.

#### flatMapConcat

Concatenating mode is implemented by [flatMapConcat](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/flat-map-concat.html) and [flattenConcat](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/flatten-concat.html) operators. They are the most direct analogues of the corresponding sequence operators. They wait for the inner flow to complete before starting to collect the next one as the following example shows:

```
val startTime = System.currentTimeMillis() // remember the start time 
(1..3).asFlow().onEach { delay(100) } // a number every 100 ms 
    .flatMapConcat { requestFlow(it) }                                                                           
    .collect { value -> // collect and print 
        println("$value at ${System.currentTimeMillis() - startTime} ms from start") 
    } 
```

Target platform: JVMRunning on kotlin v. 1.3.61

> You can get the full code from [here](https://github.com/kotlin/kotlinx.coroutines/blob/master/kotlinx-coroutines-core/jvm/test/guide/example-flow-23.kt).

The sequential nature of [flatMapConcat](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/flat-map-concat.html) is clearly seen in the output:

```
1: First at 121 ms from start
1: Second at 622 ms from start
2: First at 727 ms from start
2: Second at 1227 ms from start
3: First at 1328 ms from start
3: Second at 1829 ms from start
```

#### flatMapMerge

Another flattening mode is to concurrently collect all the incoming flows and merge their values into a single flow so that values are emitted as soon as possible. It is implemented by [flatMapMerge](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/flat-map-merge.html) and [flattenMerge](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/flatten-merge.html) operators. They both accept an optional `concurrency` parameter that limits the number of concurrent flows that are collected at the same time (it is equal to [DEFAULT_CONCURRENCY](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/-d-e-f-a-u-l-t_-c-o-n-c-u-r-r-e-n-c-y.html) by default).

```
val startTime = System.currentTimeMillis() // remember the start time 
(1..3).asFlow().onEach { delay(100) } // a number every 100 ms 
    .flatMapMerge { requestFlow(it) }                                                                           
    .collect { value -> // collect and print 
        println("$value at ${System.currentTimeMillis() - startTime} ms from start") 
    } 
```

Target platform: JVMRunning on kotlin v. 1.3.61

> You can get the full code from [here](https://github.com/kotlin/kotlinx.coroutines/blob/master/kotlinx-coroutines-core/jvm/test/guide/example-flow-24.kt).

The concurrent nature of [flatMapMerge](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/flat-map-merge.html) is obvious:

```
1: First at 136 ms from start
2: First at 231 ms from start
3: First at 333 ms from start
1: Second at 639 ms from start
2: Second at 732 ms from start
3: Second at 833 ms from start
```

> Note that the [flatMapMerge](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/flat-map-merge.html) calls its block of code (`{ requestFlow(it) }` in this example) sequentially, but collects the resulting flows concurrently, it is the equivalent of performing a sequential `map { requestFlow(it) }` first and then calling [flattenMerge](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/flatten-merge.html) on the result.

#### flatMapLatest

In a similar way to the [collectLatest](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/collect-latest.html) operator, that was shown in ["Processing the latest value"](https://kotlinlang.org/docs/reference/coroutines/flow.html#processing-the-latest-value) section, there is the corresponding "Latest" flattening mode where a collection of the previous flow is cancelled as soon as new flow is emitted. It is implemented by the [flatMapLatest](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/flat-map-latest.html) operator.

```
val startTime = System.currentTimeMillis() // remember the start time 
(1..3).asFlow().onEach { delay(100) } // a number every 100 ms 
    .flatMapLatest { requestFlow(it) }                                                                           
    .collect { value -> // collect and print 
        println("$value at ${System.currentTimeMillis() - startTime} ms from start") 
    } 
```

Target platform: JVMRunning on kotlin v. 1.3.61

> You can get the full code from [here](https://github.com/kotlin/kotlinx.coroutines/blob/master/kotlinx-coroutines-core/jvm/test/guide/example-flow-25.kt).

The output here in this example is a good demonstration of how [flatMapLatest](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/flat-map-latest.html) works:

```
1: First at 142 ms from start
2: First at 322 ms from start
3: First at 425 ms from start
3: Second at 931 ms from start
```

> Note that [flatMapLatest](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/flat-map-latest.html) cancels all the code in its block (`{ requestFlow(it) }` in this example) on a new value. It makes no difference in this particular example, because the call to `requestFlow` itself is fast, not-suspending, and cannot be cancelled. However, it would show up if we were to use suspending functions like `delay` in there.

### Flow exceptions

Flow collection can complete with an exception when an emitter or code inside the operators throw an exception. There are several ways to handle these exceptions.

#### Collector try and catch

A collector can use Kotlin's [`try/catch`](https://kotlinlang.org/docs/reference/exceptions.html) block to handle exceptions:

```
fun foo(): Flow<Int> = flow {
    for (i in 1..3) {
        println("Emitting $i")
        emit(i) // emit next value
    }
}

fun main() = runBlocking<Unit> {
    try {
        foo().collect { value ->         
            println(value)
            check(value <= 1) { "Collected $value" }
        }
    } catch (e: Throwable) {
        println("Caught $e")
    } 
}            
```

Target platform: JVMRunning on kotlin v. 1.3.61

> You can get the full code from [here](https://github.com/kotlin/kotlinx.coroutines/blob/master/kotlinx-coroutines-core/jvm/test/guide/example-flow-26.kt).

This code successfully catches an exception in [collect](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/collect.html) terminal operator and, as we see, no more values are emitted after that:

```
Emitting 1
1
Emitting 2
2
Caught java.lang.IllegalStateException: Collected 2
```

#### Everything is caught

The previous example actually catches any exception happening in the emitter or in any intermediate or terminal operators. For example, let's change the code so that emitted values are [mapped](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/map.html) to strings, but the corresponding code produces an exception:

```
fun foo(): Flow<String> = 
    flow {
        for (i in 1..3) {
            println("Emitting $i")
            emit(i) // emit next value
        }
    }
    .map { value ->
        check(value <= 1) { "Crashed on $value" }                 
        "string $value"
    }

fun main() = runBlocking<Unit> {
    try {
        foo().collect { value -> println(value) }
    } catch (e: Throwable) {
        println("Caught $e")
    } 
}            
```

Target platform: JVMRunning on kotlin v. 1.3.61

> You can get the full code from [here](https://github.com/kotlin/kotlinx.coroutines/blob/master/kotlinx-coroutines-core/jvm/test/guide/example-flow-27.kt).

This exception is still caught and collection is stopped:

```
Emitting 1
string 1
Emitting 2
Caught java.lang.IllegalStateException: Crashed on 2
```

### Exception transparency

But how can code of the emitter encapsulate its exception handling behavior?

Flows must be *transparent to exceptions* and it is a violation of the exception transparency to [emit](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/-flow-collector/emit.html) values in the `flow { ... }` builder from inside of a `try/catch` block. This guarantees that a collector throwing an exception can always catch it using `try/catch` as in the previous example.

The emitter can use a [catch](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/catch.html) operator that preserves this exception transparency and allows encapsulation of its exception handling. The body of the `catch` operator can analyze an exception and react to it in different ways depending on which exception was caught:

- Exceptions can be rethrown using `throw`.
- Exceptions can be turned into emission of values using [emit](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/-flow-collector/emit.html) from the body of [catch](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/catch.html).
- Exceptions can be ignored, logged, or processed by some other code.

For example, let us emit the text on catching an exception:

```
foo()
    .catch { e -> emit("Caught $e") } // emit on exception
    .collect { value -> println(value) }
```

Target platform: JVMRunning on kotlin v. 1.3.61

> You can get the full code from [here](https://github.com/kotlin/kotlinx.coroutines/blob/master/kotlinx-coroutines-core/jvm/test/guide/example-flow-28.kt).

The output of the example is the same, even though we do not have `try/catch` around the code anymore.

#### Transparent catch

The [catch](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/catch.html) intermediate operator, honoring exception transparency, catches only upstream exceptions (that is an exception from all the operators above `catch`, but not below it). If the block in `collect { ... }` (placed below `catch`) throws an exception then it escapes:

```
fun foo(): Flow<Int> = flow {
    for (i in 1..3) {
        println("Emitting $i")
        emit(i)
    }
}

fun main() = runBlocking<Unit> {
    foo()
        .catch { e -> println("Caught $e") } // does not catch downstream exceptions
        .collect { value ->
            check(value <= 1) { "Collected $value" }                 
            println(value) 
        }
}            
```

Target platform: JVMRunning on kotlin v. 1.3.61

> You can get the full code from [here](https://github.com/kotlin/kotlinx.coroutines/blob/master/kotlinx-coroutines-core/jvm/test/guide/example-flow-29.kt).

A "Caught …" message is not printed despite there being a `catch` operator:

#### Catching declaratively

We can combine the declarative nature of the [catch](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/catch.html) operator with a desire to handle all the exceptions, by moving the body of the [collect](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/collect.html) operator into [onEach](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/on-each.html) and putting it before the `catch` operator. Collection of this flow must be triggered by a call to `collect()` without parameters:

```
foo()
    .onEach { value ->
        check(value <= 1) { "Collected $value" }                 
        println(value) 
    }
    .catch { e -> println("Caught $e") }
    .collect()
```

Target platform: JVMRunning on kotlin v. 1.3.61

> You can get the full code from [here](https://github.com/kotlin/kotlinx.coroutines/blob/master/kotlinx-coroutines-core/jvm/test/guide/example-flow-30.kt).

Now we can see that a "Caught …" message is printed and so we can catch all the exceptions without explicitly using a `try/catch` block:

### Flow completion

When flow collection completes (normally or exceptionally) it may need to execute an action. As you may have already noticed, it can be done in two ways: imperative or declarative.

#### Imperative finally block

In addition to `try`/`catch`, a collector can also use a `finally` block to execute an action upon `collect` completion.

```
fun foo(): Flow<Int> = (1..3).asFlow()

fun main() = runBlocking<Unit> {
    try {
        foo().collect { value -> println(value) }
    } finally {
        println("Done")
    }
}            
```

Target platform: JVMRunning on kotlin v. 1.3.61

> You can get the full code from [here](https://github.com/kotlin/kotlinx.coroutines/blob/master/kotlinx-coroutines-core/jvm/test/guide/example-flow-31.kt).

This code prints three numbers produced by the `foo()` flow followed by a "Done" string:

```
1
2
3
Done
```

#### Declarative handling

For the declarative approach, flow has [onCompletion](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/on-completion.html) intermediate operator that is invoked when the flow has completely collected.

The previous example can be rewritten using an [onCompletion](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/on-completion.html) operator and produces the same output:

```
foo()
    .onCompletion { println("Done") }
    .collect { value -> println(value) }
```

Target platform: JVMRunning on kotlin v. 1.3.61

> You can get the full code from [here](https://github.com/kotlin/kotlinx.coroutines/blob/master/kotlinx-coroutines-core/jvm/test/guide/example-flow-32.kt).

The key advantage of [onCompletion](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/on-completion.html) is a nullable `Throwable` parameter of the lambda that can be used to determine whether the flow collection was completed normally or exceptionally. In the following example the `foo()` flow throws an exception after emitting the number 1:

```
fun foo(): Flow<Int> = flow {
    emit(1)
    throw RuntimeException()
}

fun main() = runBlocking<Unit> {
    foo()
        .onCompletion { cause -> if (cause != null) println("Flow completed exceptionally") }
        .catch { cause -> println("Caught exception") }
        .collect { value -> println(value) }
}            
```

Target platform: JVMRunning on kotlin v. 1.3.61

> You can get the full code from [here](https://github.com/kotlin/kotlinx.coroutines/blob/master/kotlinx-coroutines-core/jvm/test/guide/example-flow-33.kt).

As you may expect, it prints:

```
1
Flow completed exceptionally
Caught exception
```

The [onCompletion](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/on-completion.html) operator, unlike [catch](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/catch.html), does not handle the exception. As we can see from the above example code, the exception still flows downstream. It will be delivered to further `onCompletion` operators and can be handled with a `catch` operator.

#### Upstream exceptions only

Just like the [catch](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/catch.html) operator, [onCompletion](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/on-completion.html) only sees exceptions coming from upstream and does not see downstream exceptions. For example, run the following code:

```
fun foo(): Flow<Int> = (1..3).asFlow()

fun main() = runBlocking<Unit> {
    foo()
        .onCompletion { cause -> println("Flow completed with $cause") }
        .collect { value ->
            check(value <= 1) { "Collected $value" }                 
            println(value) 
        }
}
```

Target platform: JVMRunning on kotlin v. 1.3.61

> You can get the full code from [here](https://github.com/kotlin/kotlinx.coroutines/blob/master/kotlinx-coroutines-core/jvm/test/guide/example-flow-34.kt).

We can see the completion cause is null, yet collection failed with exception:

```
1
Flow completed with null
Exception in thread "main" java.lang.IllegalStateException: Collected 2
```

### Imperative versus declarative

Now we know how to collect flow, and handle its completion and exceptions in both imperative and declarative ways. The natural question here is, which approach is preferred and why? As a library, we do not advocate for any particular approach and believe that both options are valid and should be selected according to your own preferences and code style.

### Launching flow

It is easy to use flows to represent asynchronous events that are coming from some source. In this case, we need an analogue of the `addEventListener` function that registers a piece of code with a reaction for incoming events and continues further work. The [onEach](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/on-each.html) operator can serve this role. However, `onEach` is an intermediate operator. We also need a terminal operator to collect the flow. Otherwise, just calling `onEach` has no effect.

If we use the [collect](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/collect.html) terminal operator after `onEach`, then the code after it will wait until the flow is collected:

```
// Imitate a flow of events
fun events(): Flow<Int> = (1..3).asFlow().onEach { delay(100) }

fun main() = runBlocking<Unit> {
    events()
        .onEach { event -> println("Event: $event") }
        .collect() // <--- Collecting the flow waits
    println("Done")
}            
```

Target platform: JVMRunning on kotlin v. 1.3.61

> You can get the full code from [here](https://github.com/kotlin/kotlinx.coroutines/blob/master/kotlinx-coroutines-core/jvm/test/guide/example-flow-35.kt).

As you can see, it prints:

```
Event: 1
Event: 2
Event: 3
Done
```

The [launchIn](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/launch-in.html) terminal operator comes in handy here. By replacing `collect` with `launchIn` we can launch a collection of the flow in a separate coroutine, so that execution of further code immediately continues:

```
fun main() = runBlocking<Unit> {
    events()
        .onEach { event -> println("Event: $event") }
        .launchIn(this) // <--- Launching the flow in a separate coroutine
    println("Done")
}            
```

Target platform: JVMRunning on kotlin v. 1.3.61

> You can get the full code from [here](https://github.com/kotlin/kotlinx.coroutines/blob/master/kotlinx-coroutines-core/jvm/test/guide/example-flow-36.kt).

It prints:

```
Done
Event: 1
Event: 2
Event: 3
```

The required parameter to `launchIn` must specify a [CoroutineScope](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-coroutine-scope/index.html) in which the coroutine to collect the flow is launched. In the above example this scope comes from the [runBlocking](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/run-blocking.html) coroutine builder, so while the flow is running, this [runBlocking](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/run-blocking.html) scope waits for completion of its child coroutine and keeps the main function from returning and terminating this example.

In actual applications a scope will come from an entity with a limited lifetime. As soon as the lifetime of this entity is terminated the corresponding scope is cancelled, cancelling the collection of the corresponding flow. This way the pair of `onEach { ... }.launchIn(scope)` works like the `addEventListener`. However, there is no need for the corresponding `removeEventListener` function, as cancellation and structured concurrency serve this purpose.

Note that [launchIn](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/launch-in.html) also returns a [Job](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-job/index.html), which can be used to [cancel](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-job/cancel.html) the corresponding flow collection coroutine only without cancelling the whole scope or to [join](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-job/join.html) it.

### Flow and Reactive Streams

For those who are familiar with [Reactive Streams](https://www.reactive-streams.org/) or reactive frameworks such as RxJava and project Reactor, design of the Flow may look very familiar.

Indeed, its design was inspired by Reactive Streams and its various implementations. But Flow main goal is to have as simple design as possible, be Kotlin and suspension friendly and respect structured concurrency. Achieving this goal would be impossible without reactive pioneers and their tremendous work. You can read the complete story in [Reactive Streams and Kotlin Flows](https://medium.com/@elizarov/reactive-streams-and-kotlin-flows-bfd12772cda4) article.

While being different, conceptually, Flow *is* a reactive stream and it is possible to convert it to the reactive (spec and TCK compliant) Publisher and vice versa. Such converters are provided by `kotlinx.coroutines` out-of-the-box and can be found in corresponding reactive modules (`kotlinx-coroutines-reactive` for Reactive Streams, `kotlinx-coroutines-reactor` for Project Reactor and `kotlinx-coroutines-rx2` for RxJava2). Integration modules include conversions from and to `Flow`, integration with Reactor's `Context` and suspension-friendly ways to work with various reactive entities.