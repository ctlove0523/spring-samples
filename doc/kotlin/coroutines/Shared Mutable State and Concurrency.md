**Table of contents**

- Shared mutable state and concurrency
  - [The problem](https://kotlinlang.org/docs/reference/coroutines/shared-mutable-state-and-concurrency.html#the-problem)
  - [Volatiles are of no help](https://kotlinlang.org/docs/reference/coroutines/shared-mutable-state-and-concurrency.html#volatiles-are-of-no-help)
  - [Thread-safe data structures](https://kotlinlang.org/docs/reference/coroutines/shared-mutable-state-and-concurrency.html#thread-safe-data-structures)
  - [Thread confinement fine-grained](https://kotlinlang.org/docs/reference/coroutines/shared-mutable-state-and-concurrency.html#thread-confinement-fine-grained)
  - [Thread confinement coarse-grained](https://kotlinlang.org/docs/reference/coroutines/shared-mutable-state-and-concurrency.html#thread-confinement-coarse-grained)
  - [Mutual exclusion](https://kotlinlang.org/docs/reference/coroutines/shared-mutable-state-and-concurrency.html#mutual-exclusion)
  - [Actors](https://kotlinlang.org/docs/reference/coroutines/shared-mutable-state-and-concurrency.html#actors)

## Shared mutable state and concurrency

Coroutines can be executed concurrently using a multi-threaded dispatcher like the [Dispatchers.Default](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-dispatchers/-default.html). It presents all the usual concurrency problems. The main problem being synchronization of access to **shared mutable state**. Some solutions to this problem in the land of coroutines are similar to the solutions in the multi-threaded world, but others are unique.

### The problem

Let us launch a hundred coroutines all doing the same action thousand times. We'll also measure their completion time for further comparisons:

```
suspend fun massiveRun(action: suspend () -> Unit) {
    val n = 100  // number of coroutines to launch
    val k = 1000 // times an action is repeated by each coroutine
    val time = measureTimeMillis {
        coroutineScope { // scope for coroutines 
            repeat(n) {
                launch {
                    repeat(k) { action() }
                }
            }
        }
    }
    println("Completed ${n * k} actions in $time ms")    
}
```

We start with a very simple action that increments a shared mutable variable using multi-threaded [Dispatchers.Default](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-dispatchers/-default.html).

```
var counter = 0

fun main() = runBlocking {
    withContext(Dispatchers.Default) {
        massiveRun {
            counter++
        }
    }
    println("Counter = $counter")
}
```

Target platform: JVMRunning on kotlin v. 1.3.61

> You can get full code [here](https://github.com/kotlin/kotlinx.coroutines/blob/master/kotlinx-coroutines-core/jvm/test/guide/example-sync-01.kt).

What does it print at the end? It is highly unlikely to ever print "Counter = 100000", because a hundred coroutines increment the `counter` concurrently from multiple threads without any synchronization.

### Volatiles are of no help

There is common misconception that making a variable `volatile` solves concurrency problem. Let us try it:

```
@Volatile // in Kotlin `volatile` is an annotation 
var counter = 0

fun main() = runBlocking {
    withContext(Dispatchers.Default) {
        massiveRun {
            counter++
        }
    }
    println("Counter = $counter")
}
```

Target platform: JVMRunning on kotlin v. 1.3.61

> You can get full code [here](https://github.com/kotlin/kotlinx.coroutines/blob/master/kotlinx-coroutines-core/jvm/test/guide/example-sync-02.kt).

This code works slower, but we still don't get "Counter = 100000" at the end, because volatile variables guarantee linearizable (this is a technical term for "atomic") reads and writes to the corresponding variable, but do not provide atomicity of larger actions (increment in our case).

### Thread-safe data structures

The general solution that works both for threads and for coroutines is to use a thread-safe (aka synchronized, linearizable, or atomic) data structure that provides all the necessarily synchronization for the corresponding operations that needs to be performed on a shared state. In the case of a simple counter we can use `AtomicInteger` class which has atomic `incrementAndGet` operations:

```
var counter = AtomicInteger()

fun main() = runBlocking {
    withContext(Dispatchers.Default) {
        massiveRun {
            counter.incrementAndGet()
        }
    }
    println("Counter = $counter")
}
```

Target platform: JVMRunning on kotlin v. 1.3.61

> You can get full code [here](https://github.com/kotlin/kotlinx.coroutines/blob/master/kotlinx-coroutines-core/jvm/test/guide/example-sync-03.kt).

This is the fastest solution for this particular problem. It works for plain counters, collections, queues and other standard data structures and basic operations on them. However, it does not easily scale to complex state or to complex operations that do not have ready-to-use thread-safe implementations.

### Thread confinement fine-grained

*Thread confinement* is an approach to the problem of shared mutable state where all access to the particular shared state is confined to a single thread. It is typically used in UI applications, where all UI state is confined to the single event-dispatch/application thread. It is easy to apply with coroutines by using a
single-threaded context.

```
val counterContext = newSingleThreadContext("CounterContext")
var counter = 0

fun main() = runBlocking {
    withContext(Dispatchers.Default) {
        massiveRun {
            // confine each increment to a single-threaded context
            withContext(counterContext) {
                counter++
            }
        }
    }
    println("Counter = $counter")
}
```

Target platform: JVMRunning on kotlin v. 1.3.61

> You can get full code [here](https://github.com/kotlin/kotlinx.coroutines/blob/master/kotlinx-coroutines-core/jvm/test/guide/example-sync-04.kt).

This code works very slowly, because it does *fine-grained* thread-confinement. Each individual increment switches from multi-threaded [Dispatchers.Default](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-dispatchers/-default.html) context to the single-threaded context using [withContext(counterContext)](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/with-context.html) block.

### Thread confinement coarse-grained

In practice, thread confinement is performed in large chunks, e.g. big pieces of state-updating business logic are confined to the single thread. The following example does it like that, running each coroutine in the single-threaded context to start with.

```
val counterContext = newSingleThreadContext("CounterContext")
var counter = 0

fun main() = runBlocking {
    // confine everything to a single-threaded context
    withContext(counterContext) {
        massiveRun {
            counter++
        }
    }
    println("Counter = $counter")
}
```

Target platform: JVMRunning on kotlin v. 1.3.61

> You can get full code [here](https://github.com/kotlin/kotlinx.coroutines/blob/master/kotlinx-coroutines-core/jvm/test/guide/example-sync-05.kt).

This now works much faster and produces correct result.

### Mutual exclusion

Mutual exclusion solution to the problem is to protect all modifications of the shared state with a *critical section* that is never executed concurrently. In a blocking world you'd typically use `synchronized` or `ReentrantLock` for that. Coroutine's alternative is called [Mutex](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.sync/-mutex/index.html). It has [lock](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.sync/-mutex/lock.html) and [unlock](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.sync/-mutex/unlock.html) functions to delimit a critical section. The key difference is that `Mutex.lock()` is a suspending function. It does not block a thread.

There is also [withLock](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.sync/with-lock.html) extension function that conveniently represents `mutex.lock(); try { ... } finally { mutex.unlock() }` pattern:

```
val mutex = Mutex()
var counter = 0

fun main() = runBlocking {
    withContext(Dispatchers.Default) {
        massiveRun {
            // protect each increment with lock
            mutex.withLock {
                counter++
            }
        }
    }
    println("Counter = $counter")
}
```

Target platform: JVMRunning on kotlin v. 1.3.61

> You can get full code [here](https://github.com/kotlin/kotlinx.coroutines/blob/master/kotlinx-coroutines-core/jvm/test/guide/example-sync-06.kt).

The locking in this example is fine-grained, so it pays the price. However, it is a good choice for some situations where you absolutely must modify some shared state periodically, but there is no natural thread that this state is confined to.

### Actors

An [actor](https://en.wikipedia.org/wiki/Actor_model) is an entity made up of a combination of a coroutine, the state that is confined and encapsulated into this coroutine, and a channel to communicate with other coroutines. A simple actor can be written as a function, but an actor with a complex state is better suited for a class.

There is an [actor](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.channels/actor.html) coroutine builder that conveniently combines actor's mailbox channel into its scope to receive messages from and combines the send channel into the resulting job object, so that a single reference to the actor can be carried around as its handle.

The first step of using an actor is to define a class of messages that an actor is going to process. Kotlin's [sealed classes](https://kotlinlang.org/docs/reference/sealed-classes.html) are well suited for that purpose. We define `CounterMsg` sealed class with `IncCounter` message to increment a counter and `GetCounter` message to get its value. The later needs to send a response. A [CompletableDeferred](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-completable-deferred/index.html) communication primitive, that represents a single value that will be known (communicated) in the future, is used here for that purpose.

```
// Message types for counterActor
sealed class CounterMsg
object IncCounter : CounterMsg() // one-way message to increment counter
class GetCounter(val response: CompletableDeferred<Int>) : CounterMsg() // a request with reply
```

Then we define a function that launches an actor using an [actor](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.channels/actor.html) coroutine builder:

```
// This function launches a new counter actor
fun CoroutineScope.counterActor() = actor<CounterMsg> {
    var counter = 0 // actor state
    for (msg in channel) { // iterate over incoming messages
        when (msg) {
            is IncCounter -> counter++
            is GetCounter -> msg.response.complete(counter)
        }
    }
}
```

The main code is straightforward:

```
fun main() = runBlocking<Unit> {
    val counter = counterActor() // create the actor
    withContext(Dispatchers.Default) {
        massiveRun {
            counter.send(IncCounter)
        }
    }
    // send a message to get a counter value from an actor
    val response = CompletableDeferred<Int>()
    counter.send(GetCounter(response))
    println("Counter = ${response.await()}")
    counter.close() // shutdown the actor
}
```

Target platform: JVMRunning on kotlin v. 1.3.61

> You can get full code [here](https://github.com/kotlin/kotlinx.coroutines/blob/master/kotlinx-coroutines-core/jvm/test/guide/example-sync-07.kt).

It does not matter (for correctness) what context the actor itself is executed in. An actor is a coroutine and a coroutine is executed sequentially, so confinement of the state to the specific coroutine works as a solution to the problem of shared mutable state. Indeed, actors may modify their own private state, but can only affect each other through messages (avoiding the need for any locks).

Actor is more efficient than locking under load, because in this case it always has work to do and it does not have to switch to a different context at all.

> Note that an [actor](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.channels/actor.html) coroutine builder is a dual of [produce](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.channels/produce.html) coroutine builder. An actor is associated with the channel that it receives messages from, while a producer is associated with the channel that it sends elements to.