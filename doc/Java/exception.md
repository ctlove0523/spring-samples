### 异常

#### 1、异常匹配

- 抛出异常时，异常处理系统按照代码顺序查找“最近”的处理程序。一旦找到匹配的处理程序，异常处理系统则认为异常得到处理，然后不再继续查找。

- 异常系统查找异常处理程序时并不要求异常同异常处理程序声明的异常完全匹配。子类可以匹配其基类的异常处理程序。

  ```java
  public class ExceptionDemo {
      public static void main(String[] args) {
          try {
              throw new ExceptionB();
          } catch (ExceptionB b) { // 就近匹配该异常处理程序
              System.out.println("exception b");
          } catch (ExceptionA a) {
              System.out.println("exception a");
          }
  
          try {
              throw new ExceptionB();
          } catch (ExceptionA a) { // 匹配基类的异常处理程序
              System.out.println("exception a");
          }
      }
  
  }
  
  class ExceptionA extends Exception {
  }
  
  class ExceptionB extends ExceptionA {
  }
  ```

  上面的程序将输出以下内容：

  ```
  exception b
  exception a
  ```

- 如果处理基类的catch字句放在处理子类的catch字句之前，编译器会报告错误。

  ```java
  try {
  	throw new ExceptionB();
  } catch (ExceptionA a) {
  	System.out.println("exception a");
  } catch (ExceptionB b) { // 编译器报告错误
  	System.out.println("exception ba");
  }
  ```

  编译器会报告：`ExceptionB`已经被捕获处理

#### 14.20.2. `try`-`finally` 和 `try`-`catch`-`finally`的执行

一个包含`finally` 块的`try` 语句，首先执行`try` 语句块：

- 如果try块的执行正常完成，则执行finally块，然后可以选择：
  - 如果`finally` 块正常完成，则`try` 语句块正常完成。
  - 如果`finally` 块因为原因`S`完成，`try` 块也因为原因`S` 完成。
- 如果由于值V的抛出导致try块的执行突然完成，然后可以选择：
  - 如果V的运行时类型与catch语句接受的参数类型匹配，执行匹配的第一个catch块：
    - catch块正常完成，则执行finally块：
      - 如果`finally` 块正常完成，则`try` 语句块正常完成。
      - 如果finally因为任何原因突然完成，try语句也因同样的原因突然完成。
    - 如果catch块因为原因R突然完成，则执行finally块：
      - 如果finally块正常完成，try语句则因为原因R突然完成。
      - 如果finally语句块因为原因S突然完成，try语句因为原因S突然完成（原因R会被丢弃）
  - 如果V的运行时类型没有匹配的catch字句，则执行finally语句块：
    - 如果`finally` 块正常完成，则`try` 块因抛出的V突然完成。
    - 如果finally块因为原因S突然完成，try语句因为原因S突然完成（抛出的V将被丢弃）
- 如果try语句因为其他原因R突然完成，则执行finally语句块：
  - finally块正常完成，try语句因为原因R突然完成
  - 如果finally 语句块因为原因S突然完成，try语句因为原因S突然完成，R被丢弃



**Example 14.20.2-1. Handling An Uncaught Exception With finally**

```
class BlewIt extends Exception {
    BlewIt() { }
    BlewIt(String s) { super(s); }
}
class Test {
    static void blowUp() throws BlewIt {
        throw new NullPointerException();
    }
    public static void main(String[] args) {
        try {
            blowUp();
        } catch (BlewIt b) {
            System.out.println("Caught BlewIt");
        } finally {
            System.out.println("Uncaught Exception");
        }
    }
}
```

This program produces the output:

```
Uncaught Exception
Exception in thread "main" java.lang.NullPointerException
        at Test.blowUp(Test.java:7)
        at Test.main(Test.java:11)
```

The `NullPointerException` (which is a kind of `RuntimeException`) that is thrown by method `blowUp` is not caught by the `try` statement in `main`, because a `NullPointerException` is not assignable to a variable of type `BlewIt`. This causes the `finally` clause to execute, after which the thread executing `main`, which is the only thread of the test program, terminates because of an uncaught exception, which typically results in printing the exception name and a simple backtrace. However, a backtrace is not required by this specification.

The problem with mandating a backtrace is that an exception can be created at one point in the program and thrown at a later one. It is prohibitively expensive to store a stack trace in an exception unless it is actually thrown (in which case the trace may be generated while unwinding the stack). Hence we do not mandate a back trace in every exception.
