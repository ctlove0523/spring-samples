Trebier stack algorithm 是一个使用细粒度并发原语CAS实现的可扩展的栈。1986年 R. Kent Treiber 在其论文《Systems Programming: Coping with Parallelism》首次提出 Trebier stack algorithm。

### 基本原则

Trebier stack algorithm 算法的基本原则是：只有当你知道要添加的条目是自操作以来唯一被添加的条目时，才向栈中添加该条目。这通过使用CAS来实现。当向栈中插入一个条目时，首先获取栈顶条目（原来栈的头），然后将该条目附加到待添加条目之后构成一个新的头，接着比较栈原来的头和栈当前的头，如果两者匹配，则可以将栈原来的头替换为新的头，如果不匹配说明其他线程向栈中添加了新条目，在这种情况下，需要进行重试。

当从栈中弹出一个条目时，在返回栈顶条目之前，必须检查另一个线程自操作开始以来是否添加新的条目，如果插入了新的条目，在这种情况下需要进行重试。

### 正确性

在某些编程语言当中，尤其是那些没有垃圾回收的编程语言，Treiber stack 可能面临ABA问题的风险。当一个进程从堆栈异常条目时（在样例代码pop函数的比较和设置之前），其他进程可以改变堆栈，使得堆栈的头是相同的，但是第二个元素不同。比较和交换（CAS）将堆栈的头部设置为堆栈中旧的第二个元素，混合完整的数据结构。Java运行时提供了更强大的保证（新创建的不混淆的对象引用不可能与任何其他可到达的对象引用相同），因此Java 版本的 Trebier stack 没有ABA问题。

因为有问题的特定事件序列非常的罕见，对诸如ABA类似问题的测试会非常的困难。模型检查是发现此类问题的绝佳方法。参考《Modeling and analysis of communicating Systems》中的练习7.3.3.

### Java实现

~~~Java
public class TrebierStack<E> {
    // 栈顶
    private AtomicReference<Node<E>> top = new AtomicReference<>();

    public void push(E item) {
        Node<E> newHead = new Node<>(item);
        Node<E> oldHead;
        do {
            oldHead = top.get();
            newHead.next = oldHead;
        } while (!top.compareAndSet(oldHead, newHead));
    }

    public E pop() {
        Node<E> oldHead;
        Node<E> newHead;
        do {
            oldHead = top.get();
            if (oldHead == null) {
                return null;
            }
            newHead = oldHead.next;
        } while (!top.compareAndSet(oldHead, newHead));
        return oldHead.item;
    }

    // 堆栈节点类
    private static class Node<E> {
        final E item;
        Node<E> next;

        Node(E item) {
            this.item = item;
        }
    }
}
~~~

基于Java运行时的强有力的保证和Doug Lea大神开发的各种工具，Trebier stack的实现变的如此的简单:smile:

### 过程分析

1. push操作

   初始状态：top指向堆栈的栈顶元素Node1，堆栈内个元素使用`next`引用连接。

   操作过程：使用待插入的元素构建节点Node0，并将Node0的next指针指向Node1，Node0 -> Node1。使用Java的CAS操作将top指向Node0，CAS操作执行成功的条件是：堆栈的栈顶为Node1（自开始执行push操作以来，没有其他线程更改堆栈状态）。

   最终状态：top执行新插入的元素节点Node0

2. pop操作

   初始状态：top指向堆栈的栈顶元素Node0.堆栈内各元素使用`next` 指针连接。

   操作过程：首先获取栈顶元素Node0，如果栈为空则直接返回null；然后获取堆栈的第二个元素Node1，执行CAS操作将top指向Node1，CAS执行成功的条件是：堆栈的栈顶元素为Node0（自执行pop操作以来，没有其他线程更改堆栈状态。）