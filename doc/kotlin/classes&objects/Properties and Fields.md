# Properties and Fields

## 声明属性

Kotlin中类的属性分为两类：可变属性(*var*)和只读属性(*val*)。

```kotlin
class Address {
    var name: String = "Holmes, Sherlock"
    var street: String = "Baker"
    var city: String = "London"
    var state: String? = null
    var zip: String = "123456"
}
```

直接通过属性的名字来使用属性：

```kotlin
fun copyAddress(address: Address): Address {
    val result = Address() // there's no 'new' keyword in Kotlin
    result.name = address.name // accessors are called
    result.street = address.street
    // ...
    return result
}
```

## Getters and Setters

属性声明的完整语法如下：

```kotlin
var <propertyName>[: <PropertyType>] [= <property_initializer>]
    [<getter>]
    [<setter>]
```

初始化器，getter和setter方法都是可选的，如果属性的类型可以从属性的初始器中推导出，属性的类型也是可以省略的：

使用样例:

```kotlin
var allByDefault: Int? 
var initialized = 1 
```

只读属性的声明和可变属性的声明有两点不同：使用*val* 关键字；没有默认的setter方法。

```kotlin
val simple: Int? 
val inferredType = 1 
```

可以给属性自定义访问器，自定义的访问器每次访问属性时都会被调用。

```kotlin
val isEmpty: Boolean
    get() = this.size == 0
```

Kotlin也允许自定义setter方法，下面是一个自定义setter方法的例子：

```kotlin
var stringRepresentation: String
    get() = this.toString()
    set(value) {
        setDataFromString(value) 
    }
```

按照约定，setter参数的名称为value，但您可以根据需要选择其他名称。

如果可以从getter推断出属性类型，则可以省略属性类型:

```kotlin
val isEmpty get() = this.size == 0  // Boolean类型
```

如果需要改变属性访问器的可见性或者增加注解但是又不想改变默认实现，可以定义一个没有没有body体的访问器：

```kotlin
var setterVisibility: String = "abc"
    private set // the setter is private and has the default implementation

var setterWithAnnotation: Any? = null
    @Inject set // annotate the setter with Inject
```

### Backing Fields

Fields cannot be declared directly in Kotlin classes. However, when a property needs a backing field, Kotlin provides it automatically. This backing field can be referenced in the accessors using the `field` identifier:

```
var counter = 0 // Note: the initializer assigns the backing field directly
    set(value) {
        if (value >= 0) field = value
    }
```

The `field` identifier can only be used in the accessors of the property.

A backing field will be generated for a property if it uses the default implementation of at least one of the accessors, or if a custom accessor references it through the `field` identifier.

For example, in the following case there will be no backing field:

```
val isEmpty: Boolean
    get() = this.size == 0
```

### Backing Properties

If you want to do something that does not fit into this "implicit backing field" scheme, you can always fall back to having a *backing property*:

```
private var _table: Map<String, Int>? = null
public val table: Map<String, Int>
    get() {
        if (_table == null) {
            _table = HashMap() // Type parameters are inferred
        }
        return _table ?: throw AssertionError("Set to null by another thread")
    }
```

> **On the JVM**: The access to private properties with default getters and setters is optimized so no function call overhead is introduced in this case.

## Compile-Time Constants

Properties the value of which is known at compile time can be marked as *compile time constants* using the *const* modifier. Such properties need to fulfil the following requirements:

- Top-level, or member of an [*object* declaration](https://kotlinlang.org/docs/reference/object-declarations.html#object-declarations) or [a *companion object*](https://kotlinlang.org/docs/reference/object-declarations.html#companion-objects).
- Initialized with a value of type `String` or a primitive type
- No custom getter

Such properties can be used in annotations:

```
const val SUBSYSTEM_DEPRECATED: String = "This subsystem is deprecated"

@Deprecated(SUBSYSTEM_DEPRECATED) fun foo() { ... }
```

## Late-Initialized Properties and Variables

Normally, properties declared as having a non-null type must be initialized in the constructor. However, fairly often this is not convenient. For example, properties can be initialized through dependency injection, or in the setup method of a unit test. In this case, you cannot supply a non-null initializer in the constructor, but you still want to avoid null checks when referencing the property inside the body of a class.

To handle this case, you can mark the property with the `lateinit` modifier:

```
public class MyTest {
    lateinit var subject: TestSubject

    @SetUp fun setup() {
        subject = TestSubject()
    }

    @Test fun test() {
        subject.method()  // dereference directly
    }
}
```

The modifier can be used on `var` properties declared inside the body of a class (not in the primary constructor, and only when the property does not have a custom getter or setter) and, since Kotlin 1.2, for top-level properties and local variables. The type of the property or variable must be non-null, and it must not be a primitive type.

Accessing a `lateinit` property before it has been initialized throws a special exception that clearly identifies the property being accessed and the fact that it hasn't been initialized.

### Checking whether a lateinit var is initialized (since 1.2)

To check whether a `lateinit var` has already been initialized, use `.isInitialized` on the [reference to that property](https://kotlinlang.org/docs/reference/reflection.html#property-references):

```
if (foo::bar.isInitialized) {
    println(foo.bar)
}
```

This check is only available for the properties that are lexically accessible, i.e. declared in the same type or in one of the outer types, or at top level in the same file.

## Overriding Properties

See [Overriding Properties](https://kotlinlang.org/docs/reference/classes.html#overriding-properties)

## Delegated Properties

The most common kind of properties simply reads from (and maybe writes to) a backing field. On the other hand, with custom getters and setters one can implement any behaviour of a property. Somewhere in between, there are certain common patterns of how a property may work. A few examples: lazy values, reading from a map by a given key, accessing a database, notifying listener on access, etc.

Such common behaviours can be implemented as libraries using [*delegated properties*](https://kotlinlang.org/docs/reference/delegated-properties.html).