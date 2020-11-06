# 数据类

我们经常创建类的目的是作为一个数据容器，在这些类中一些标准和使用函数通常都是从数据中机械的获得。在Kotlin中，这样的类称为*数据类* 并使用`data` 标记。

```kotlin
data class Client(private val name: String, private val postalCode: Int)
```

Kotlin编译器自动从主构造函数声明的属性中生成以下成员：

- `equals()`/`hashCode()` ；
- 格式类似`"Client(name=John, postalCode=42)"`的`toString()`函数；
- `componentN()` 函数，这个和属性声明的顺序相关； 
- `copy()` 函数。

为了保证编译器生成的代码的一致性和有意义，数据类必须满足以下条件：

- 主构造函数至少包含一个参数；
- 主构造函数的参数必须使用`val`或`var` 修饰；
- 数据类不能是abstract, open, sealed or inner；
- Kotlin 1.1版本之前数据类只能实现接口。

此外，考虑到成员继承，成员生成遵循以下规则：

- 如果已经定义 `equals()`, `hashCode()` 或 `toString()` 方法或者超类中有*final*实现，编译器不会生成对应的方法。
- 如果超类型有*open* 的 `componentN()` 函数而且返回兼容的类型，数据类生成的函数会覆写超类型中对应的函数。如果超类型中的方法不允许重写（不兼容的函数签名或final类型的函数），编译器会报错。
- 禁止从已经具有`copy（...）`函数且具有匹配签名的类型派生数据类。
- 不允许提供显式的 `componentN()` 和`copy()` 函数实现。

## 类体中声明的属性

编译器为数据类自动生成的函数只使用了主构造函数中的参数，如果不想一个属性被用在自动生成的函数中，可以在类体中进行声明：

```kotlin
data class Client(val name: String) {
    var postalCode: Int = 123456
}
```

编译器在生成 `toString()`, `equals()`, `hashCode()`, 和`copy()` 实现是只会使用`name` 属性。

```kotlin
val client1 = Client("client")
client1.postalCode = 1
val client2 = Client("client")
client2.postalCode = 2
println(client1 == client2) // true
```

## 拷贝

为了让使用不可变数据类的对象更加容易，Kotlin编译器生成了一个允许*copy*类的实例的方法，并在copy的同时支持修改某些属性的值。创建副本通常是修改实例比较好的选择：副本有着单独的生命周期而且不会影响代码中引用原始实例的位置。

```kotlin
fun copy(name: String = this.name, postalCode: Int = this.postalCode) = Client(name, postalCode)
```

下面是一个使用copy的代码样例：

```kotlin
val client1 = Client("client",12345)
val copyClient = client1.copy(name = "copy")
```

## 数据类和解构声明

编译器生成的Component函数然数据类可以用在解构声明中。

```kotlin
data class Client(val name: String, val postalCode: Int)

val client = strings.Client("client", 12345)
val (name, postalCode) = client
println("name = $name,postal code = $postalCode")// name = client,postal code = 12345
```

## 标准数据类

Kotlin的标准库提供了`Pair` 和`Triple`两个数据类。尽管如此，在大多数场景下，使用命名的数据类是一个更好的设计选择，命名数据类可以让代码更具有可读性。