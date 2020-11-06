## 类

在Kotlin中使用关键字*class*声明一个类：

```kotlin
class Invoice { /*...*/ }
```

类的声明包括类名、类头（类型参数，主构造函数等）和类体（由大括号包围）。类头和类体都是可选，如果一个类没有类体，花括号可以省略。

```kotlin
class Empty
```

### 构造函数

Kotlin中的类可以有一个**主构造函数**，一个或多个**副构造函数**。主构造函数是类头的一部分，位于类名和可选的类型参数之后。

```kotlin
class Person constructor(firstName: String) { /*...*/ }
```

如果主构造函数没有任何注解或可见性修饰符，关键字*constructor*可以省略：

```kotlin
class Person(firstName: String) { /*...*/ }
```

主构造函数不能包含任何代码。初始化代码可以放在初始化块中，初始化块以*init*关键字作为前缀。在实例初始化过程中，初始化块以在代码中出现的顺序执行，并且可以和属性初始化交替执行。

```kotlin
class Person constructor(val name: String, val age: Int) {
    init {
        println("begin init name")
    }
    val address = "begin init $name's address".also(::println)
    init {
        println("begin init age")
    }
}
```

主构造函数中的参数可以被初始化块和属性初始化器使用：

```kotlin
class Customer(name: String) {
    val customerKey = name.toUpperCase()
}
```

主构造函数中声明的属性可以是只读(val)和可变的(var)。



如果主构造函数有注解或可见性修饰符，关键字*constructor*不能省略，而且关键字必须在*constructor*之前。

```kotlin
class Customer public @Inject constructor(name: String) { /*...*/ }
```

#### 副构造函数

一个类可以使用*constructor* 声明副构造函数：

```kotlin
class Person {
    var children: MutableList<Person> = mutableListOf<Person>();
    constructor(parent: Person) {
        parent.children.add(this)
    }
}
```

如果一个类有主构造函数，每个副构造函数必须直接或间接的委托给主构造函数，同一个类内部构造函数的委托使用*this* 关键字：

```kotlin
class Person(val name: String) {
    var children: MutableList<Person> = mutableListOf();

    constructor(name: String, parent: Person) : this(name) {
        parent.children.add(this)
    }
}
```

初始化块中的代码其实是主构造函数的一部分，副构造函数的第一个语句是委托主构造函数，因此初始化中的所有代码和属性初始化代码在副构造函数中的代码之前执行。即使一个类没有主构造函数，委托依然已隐式的方式实现，初始快中的代码依然会被执行：

```kotlin
class Person {
    var name: String

    init {
        println("this code is part of primary constructor")
    }

    constructor(name: String) {
        this.name = name
    }
}
```

如果一个非抽象类没有声明任何构造函数，该类将默认有一个无参的public构造函数。如果不希望类有一个public的构造函数，需要主动声明一个非默认可见性的无参构造函数：

```kotlin
class DontCreateMe private constructor () { /*...*/ }
```

### 创建类的实例

我们像调用普通函数一样调用类的构造函数来创建类的一个实例：

```kotlin
val invoice = Invoice()

val customer = Customer("Joe Smith")
```

Kotlin中创建实例不需要使用*new*，*new*也不是Kotlin的一个关键字。

### 类成员

一个类可以包含以下成员：

- 构造函数和初始化代码块
- 函数
- 属性
- 嵌套类和内部类
- 伴生对象

## 继承

`Any` 是Kotlin中所有类的超类，它是没有声明超类的类的默认超类。

```
class Example // 隐式继承自Any
```

`Any` 有三个方法: `equals()`, `hashCode()` and `toString()`. 因此Kotlin中定义的所有类都有这三个方法。

显示的继承一个类，需要在类头的尾部使用`:` 紧跟被继承的类(只有open的类才能被继承)。

```kotlin
open class Base(p: Int)

class Derived(p: Int) : Base(p)
```

如果派生类有主构造函数，必须使用主构函数的参数初始化基类。如果派生类没有主构造函数，每一个副构造函数必须使用关键字*super* 初始化基类或者委托给其他副构造函数。在这种情况下，不同的副构造函数可以调用基类不同的构造函数。

```kotlin
class MyView : View {
    constructor(ctx: Context) : super(ctx)

    constructor(ctx: Context, attrs: AttributeSet) : super(ctx, attrs)
}
```

### 覆写方法

在Kotlin中，只有open的方法才允许被覆写：

```kotlin
open class Shape {
    open fun draw() { /*...*/ }
    fun fill() { /*...*/ }
}

class Circle() : Shape() {
    override fun draw() { /*...*/ }
}
```

定义父类中被*private*修饰的方法无需使用关键字*override*，*final*的方法无法被覆写（方法默认都是final的）。

```kotlin
open class Rectangle() : Shape() {
    final override fun draw() { /*...*/ }
}
```

### 覆写属性

Kotlin中覆写属性和覆写方法一样，基类中定义的熟悉可以在子类中使用*override*重新声明。父类中的每一个属性可以通过两种方法覆写：属性+初始化器和属性+`get`方法。

```kotlin
open class Shape {
    open val vertexCount: Int = 0
}

class Rectangle : Shape() {
    override val vertexCount = 4
}
```

可以通过`var` 属性覆写`val` 属性，反之不行。`val` 属性声明了一个`get`方法，在子类中使用`var` 属性覆写，增加了一个`set`方法，反过来则是不行的。

```kotlin
interface Shape {
    val vertexCount: Int
}

class Rectangle(override val vertexCount: Int = 4) : Shape // Always has 4 vertices

class Polygon : Shape {
    override var vertexCount: Int = 0  // Can be set to any number later
}
```

### 派生类初始化顺序

在构建派生类实例时，首先需要完成基类的初始化，然后执行派生类的初始化逻辑。

```kotlin
open class Base(val name: String) {
    init {
        println("init base")
    }

    open val size: Int = name.length.also { println("Initializing size in Base: $it") }

}

class Derived(name: String, val lastName: String)
    : Base(name.capitalize().also { println("Argument for Base: $it") }) {

    init { println("Initializing Derived") }

    override val size: Int =
        (super.size + lastName.length).also { println("Initializing size in Derived: $it") }
}

fun main(args: Array<String>) {
    val derivedObject = Derived("derived class","test object")
}
```

运行上面的代码，输出结果如下：

~~~
Argument for Base: Derived class
init base
Initializing size in Base: 13
Initializing Derived
Initializing size in Derived: 24
~~~

在基类的构造函数执行时，派生类中声明或覆写的属性还没有完成初始化，因此如果在基类的初始化逻辑中使用了任何这些未初始化的属性会导致不正确的行为或运行时失败。在设计基类时，应避免在构造函数、属性初始化器和*init*块中使用*open*的成员。

### 调用超类实现

派生类可以使用关键字*super* 调用超类的函数和属性访问器实现：

```kotlin
open class Rectangle {
    open fun draw() { println("Drawing a rectangle") }
    val borderColor: String get() = "black"
}

class FilledRectangle : Rectangle() {
    override fun draw() {
        super.draw()
        println("Filling the rectangle")
    }

    val fillColor: String get() = super.borderColor
}
```

内部类调用外部类的超类的函数或属性访问器实现可以使用`super@Outer`：

```kotlin
class FilledRectangle: Rectangle() {
    fun draw() { /* ... */ }
    val borderColor: String get() = "black"
    
    inner class Filler {
        fun fill() { /* ... */ }
        fun drawAndFill() {
            super@FilledRectangle.draw() // Calls Rectangle's implementation of draw()
            fill()
            println("Drawn a filled rectangle with color ${super@FilledRectangle.borderColor}") // Uses Rectangle's implementation of borderColor's get()
        }
    }
}
```

### 覆写规则

如果派生类继承的类或实现的接口有重复的方法，派生类必须提供自己的实现，否则会有编译错误。如果派生类需要调用超类或接口的实现，可以使用`super<>` 并在`<>` 指定调用的类型即可。

```kotlin
open class Rectangle {
    open fun draw() { /* ... */ }
}

interface Polygon {
    fun draw() { /* ... */ } // interface members are 'open' by default
}

class Square() : Rectangle(), Polygon {
    // The compiler requires draw() to be overridden:
    override fun draw() {
        super<Rectangle>.draw() // call to Rectangle.draw()
        super<Polygon>.draw() // call to Polygon.draw()
    }
}
```

## 抽象类

Kotlin和Java一样抽象类都无法实例化，抽象类中的成员也无需提供实现，抽象类和抽象方法默认都是*open* 。并且可以使用抽象方法覆写一个非抽象方法。

```kotlin
open class Polygon {
    open fun draw() {}
}

abstract class Rectangle : Polygon() {
    override abstract fun draw()
}
```

## 伴生对象

如果你需要一个可以在没有类实例的情况下调用但是需要访问类内的函数，可以将其写成那个类中的对象声明成员。在类中定义的对象之一可以使用一个特殊的关键字来标记：companion。这样声明的对象获得了直接通过容器类名称访问对象方法和属性的能力，不再需要显式的指明对象的名称。

~~~kotlin
class CompanionClass {
    companion object {
        fun info() = "I'm a companion object"
    }
}

fun main(args: Array<String>) {
    println(CompanionClass.info())
}
~~~

