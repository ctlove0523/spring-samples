Kotlin作为一门编程语言，在其标准库中仅提供了少量的低级别API以供其他库使用协程。和大多数提供协程能力的语言不同，`async` 和`await` 不是Kotlin的关键字甚至也不是标准库的一部分。Kotlin的挂起函数概念对异步操作提供了更加安全的抽象。

`kotlinx.coroutines` 是JetBrains公司开发的丰富的协程库。这个库包含了本文将要介绍的大多数协程高级原语，比如 `launch`, `async` 等。

## 内容列表

- [Basics](https://kotlinlang.org/docs/reference/coroutines/basics.html)
- [Cancellation and Timeouts](https://kotlinlang.org/docs/reference/coroutines/cancellation-and-timeouts.html)
- [Composing Suspending Functions](https://kotlinlang.org/docs/reference/coroutines/composing-suspending-functions.html)
- [Coroutine Context and Dispatchers](https://kotlinlang.org/docs/reference/coroutines/coroutine-context-and-dispatchers.html)
- [Asynchronous Flow](https://kotlinlang.org/docs/reference/coroutines/flow.html)
- [Channels](https://kotlinlang.org/docs/reference/coroutines/channels.html)
- [Exception Handling and Supervision](https://kotlinlang.org/docs/reference/coroutines/exception-handling.html)
- [Shared Mutable State and Concurrency](https://kotlinlang.org/docs/reference/coroutines/shared-mutable-state-and-concurrency.html)
- [Select Expression (experimental)](https://kotlinlang.org/docs/reference/coroutines/select-expression.html)

## 参考

- [Guide to UI programming with coroutines](https://github.com/kotlin/kotlinx.coroutines/blob/master/ui/coroutines-guide-ui.md)
- [Coroutines design document (KEEP)](https://github.com/Kotlin/kotlin-coroutines/blob/master/kotlin-coroutines-informal.md)
- [Full kotlinx.coroutines API reference](https://kotlin.github.io/kotlinx.coroutines)