[TOC]

### java学习之路(经验总结)
#### 问题

 - [ ] 适配器模式、装饰模式、外观模式、代理模式四者的区别具体使用场景？
 - [ ] 命令模式不太明白，只是将调用链加长，没理解它的真谛，需要研究下；
 - [ ] 责任链模式和观察者模式有些相似
 - [ ] 状态模式和策略模式的区别和差异
#### 可以读的书
1、深入理解java虚拟机：JVM高级特性与最佳实践
2、HotSpot实战 （c/c++语言写的看有一定难度）
3、java并发编程实战（偏向于介绍并发、锁之类的讲的太过详细）
4、Java多线程编程核心技术（多线程）
5、effective java中文版
6、深入分析java web技术内幕（web相关的技术讲的比较广，深入重点）
7、大型网站技术架构 核心原理与案例分析（主要介绍web架构的分布式使用方式和原理）

#### java设计模式
##### 创建型模式
###### 工厂方法 FactoryMethod
定义一个用于创建对象的接口，让子类决定实例化哪一个类，FactoryMethod使一个类的实例化延迟到其子类。
![74e27999bd1880fcf73d9e7d77746c5b.png](en-resource://database/1120:1)


###### 抽象工厂 abstract factory
提供一个创建一系列相关或相互依赖对象的接口，而无需指定他们具体的类；
**自己理解**: 抽象工厂相当于在工厂方法的基础上抽象了一层，封装多个工厂方法，将多个对象或方法绑定在一起对外提供使用；
![7b0461b337e489b940da42442d0378be.png](en-resource://database/1118:1)

###### 建造者模式 builder
将一个复杂对象的构造与它的表示分离，使得同样的构建过程可以创建不同的表示；
**自己理解：** 将对象的结构构造进行分解成多个方法，根据不同需要封装不等的方法到建造者类中，对外提供；


1、Builder：给出一个抽象接口，以规范产品对象的各个组成成分的建造。这个接口规定要实现复杂对象的哪些部分的创建，并不涉及具体的对象部件的创建。
2、ConcreteBuilder：实现Builder接口，针对不同的商业逻辑，具体化复杂对象的各部分的创建。 在建造过程完成后，提供产品的实例。
3、Director：调用具体建造者来创建复杂对象的各个部分，在指导者中不涉及具体产品的信息，只负责保证对象各部分完整创建或按某种顺序创建。
4、Product：要创建的复杂对象。
![af5063bfae211434dcad3e976cee7f5e.png](en-resource://database/1122:1)


###### 单态模式 singleton
确保一个类只允许创建一个实例，只提供一个访问它的全局对象句柄；

###### 原型模式 prototope
用原型实例指定创建对象的种类，并且通过拷贝这些原型创建新的对象；
**自己理解：** 通过实现Clongable接口，使原型提供克隆自身的接口；它的优点在于原型属性修改后可以保存修改后的原型内容，不用重新初始化再进行修改；


1、客户(Client)角色：客户类提出创建对象的请求。　　
2、抽象原型(Prototype)角色：这是一个抽象角色，通常由一个Java接口或Java抽象类实现。此角色给出所有的具体原型类所需的接口。　　
3、具体原型（Concrete Prototype）角色：被复制的对象。此角色需要实现抽象的原型角色所要求的接口。
  
![f4415b73af80e2835e852f3655267c99.png](en-resource://database/1128:1)

------
##### 结构型模式
###### 适配器模式 adapter
将一个类的接口转换成客户希望的另外一个接口，adapter模式使得原本由于单个接口无法满足的功能，通过组合适配这些功能来提供，使这些功能可以一起工作；
1、Target: 目标类，目标抽象类定义客户所需的接口，可以是一个抽象类，接口或者是具体类。在类适配器中只能够是接口！因为JAVA只支持单继承但可以多实现！
2、Adapter:适配器类，它通过继承一个目标类，调用另一个接口。使其产生练习！Adapter是适配器模式的核心类！在类适配器中，适配器类通过继承适配者类和实现目标类，通过继承实现的方式使其产生联系。在对象适配器中，通过继承目标类关联适配者使其产生联系。
3、Adaptee:适配者类，它一般来说是定义的一个接口，然后有客户端所需要的方法。
![97bb08492811510bc33681f408982de2.png](en-resource://database/1126:1)


###### 桥接模式 bridge
将抽象部分与它的实现部分分离，使它们都可以独立地变化；
桥梁模式是对象的结构模式，又称为柄体（Handle and Body）模式或接口(Interface)模式，桥梁模式的用意是“将抽象化(Abstraction)与实现化（Implementation）脱耦，使得二者可以独立地变化；”


1、抽象化(Abstraction)角色：抽象化给出的定义，并保存一个对实现化对象的引用。　　
2、修正抽象化(RefinedAbstraction)角色：扩展抽象化角色，改变和修正父类对抽象化的定义。　　
3、实现化(Implementor)角色：这个角色给出实现化角色的接口，但不给出具体的实现。必须指出的是，这个接口不一定和抽象化角色的接口定义相同，实际上，这两个接口可以非常不一样。实现化角色应当只给出底层操作，而抽象化角色应当只给出基于底层操作的更高一层的操作。　　
4、具体实现化(ConcreteImplementor)角色：这个角色给出实现化角色接口的具体实现。
![057c7734eee1fc4b7f0cccb90418c41c.png](en-resource://database/1124:1)

###### 组合模式 composite
将对象组合成树形结构以表示“部分-整体”的层次结构，“composite”使得用户对单个对象和组合对象的使用具有一致性；
1、component (抽象构件：容器)：它可以是接口或者抽象类，为叶子构建和子容器构建对象声明接口，在该角色中可以包含所有子类共有的行为的实现和声明。在抽象构建中定义了访问及管理它的子构件的方法，如增加子构件，删除子构件，获取子构件等。
2、leaf(叶子构建)：叶子构建可以说就是各种类型的文件！叶子构建没有子构件。它实现了抽象构建中的定义的行为。对于那些访问子容器，删除子容器，增加子容器的就报错。
3、compsite(子容器构建)：它在组合模式中表示容器节点对象，容器结点是子节点，可以是子容器，也可以是叶子构建，它提供一个集合来存储子节点。
![3821aeeef6504094e8d508f1e8b1fa3b.png](en-resource://database/1130:1)


###### 装饰模式 decorator
动态地给一个对象添加一些额外的职责，就增加功能来说，Decorator模式相比生成子类更加灵活;
1、Component 定义一个对象接口，可以给这些对象动态地添加职责；
2、ConcreteComponent 定义一个对象，可以给这个对象添加一些职责；
3、Decorator 维持一个指向Component对象的指针，并定义一个与Component接口一致的接口；
4、ConcreateDecorator向组件添加职责；
![edfe29fe60d62f2ae30cf2dc98cf733d.png](en-resource://database/1132:1)


###### 外观模式 Facade
为子系统中的一组接口提供一个一致的界面，Facade模式定义了一个高层接口，这个接口使得这与子系统更加容易使用；
Facade：知道哪些子系统类负责处理请求，将客户的请求代理给适当的子系统对象；
Subsystemclasses:实现子系统的功能, 处理由Facade对象指派的任务, 没有facade的任何相关信息；即没有指向Facade的指针;
![114ba727da55c8c22941eb365fe5e122.png](en-resource://database/1134:1)

###### 享元模式 FlyWeight
运用共享技术有效地支持大量细粒度的对象；

1、抽象享元(Flyweight)角色 ：给出一个抽象接口，以规定出所有具体享元角色需要实现的方法；
2、具体享元(ConcreteFlyweight)角色：实现抽象享元角色所规定出的接口。如果有内蕴状态的话，必须负责为内蕴状态提供存储空间；
3、享元工厂(FlyweightFactory)角色 ：本角色负责创建和管理享元角色。本角色必须保证享元对象可以被系统适当地共享。当一个客户端对象调用一个享元对象的时候，享元工厂角色会检查系统中是否已经有一个符合要求的享元对象。如果已经有了，享元工厂角色就应当提供这个已有的享元对象；如果系统中没有一个适当的享元对象的话，享元工厂角色就应当创建一个合适的享元对象。
![28b81be2e0f0519528627acfc0e12e28.png](en-resource://database/1136:1)

###### 代理模式 Proxy
为其他对象提供一种代理以控制对这个对象的访问；
代理模式是指客户端并不直接调用实际的对象，而是通过调用代理，来间接的调用实际的对象；
**代理方式 适用性**
1、远程代理（RemoteProxy）为一个对象在不同的地址空间提供局部代表；
2、虚代理（VirtualProxy）根据需要创建开销很大的对象；
3、保护代理（ProtectionProxy）控制对原始对象的访问；
4、智能指引（SmartReference）取代了简单的指针，它在访问对象时执行一些附加操作；
**角色**
1、Proxy 保存一个引用使得代理可以访问实体。若RealSubject和Subject的接口相同，Proxy会引用Subject，提供一个与Subject的接口相同的接口，这样代理就可以用来替代实体，控制对实体的提取，并可能负责创建和删除它，其他功能依赖于代理的类型;
2、RemoteProxy负责对请求及其参数进行编码，并向不同地址空间中的实体发送已编码的请求；
3、VirtualProxy可以缓存实体的附加信息，以便延迟对它的访问；
4、ProtectionProxy检查调用者是否具有实现一个请求所必需的访问权限；
5、Subject:定义RealSubject和Proxy的共用接口，这样就在任何使用RealSubject的地方都可以使用Proxy；
6、RealSubject定义Proxy所代表的实体；
![ddea3a5ee9ba01328da82215d3a5851c.png](en-resource://database/1138:1)

------
##### 行为型模式
###### 责任链模式 chain of responsibility
使多个对象都有机会处理请求，从而避免请求的发送者和接收者之间的耦合关系，将这些对象连成链，并随着这条链传递该请求，直到有一个对象处理它为止；
这一模式的想法是给多个对象处理一个请求的机会，从而解耦发送者和接收者；
**角色**
1、抽象处理者（Handler）:定义出一个处理请求的接口，抽象方法handleRequest()规范子类处理请求的操作；
2、具体处理者（ConcreateHandler）：具体处理者接到请求后，可以选择将请求处理掉，或者将请求传给下一个处理者；
3、客户端（Client）：设置不同的具体处理者的层级；
![aa567df73d00049104e219e0ff286009.png](en-resource://database/1140:1)

###### 命令模式 command
将一个请求封装为一个对象，从而使你可用不同的请求对客户进行参数化，对请求排队或记录请求日志，已经支持可撤销的操作；
请求以命令的形式包裹在对象中，并传给调用对象。调用对象寻找可以处理该命令的合适的对象，并把该命令传给相应的对象，该对象执行命令。
**角色**
1、Command：声明执行操作的接口；
2、ConcreateCommand：将一个接收者对象绑定于一个动作，调用接收者相应的操作，以实现Execute；
3、Client：创建一个具体命令对象并设定它的接收者；
4、Invoker：要求改命令执行这个请求；
5、Receiver：知道如何实现与执行一个请求相关的操作，任何类都可能作为一个接收者；
![31338e6712ad24d9c61c89181d5ad0ef.png](en-resource://database/1142:1)


###### 解释器模式 interpreter
给定一个语言，定义它的文法的一种表示，并定义一个解释器，这个解释器使用该表示来解释语言中的句子；
解释器模式（Interpreter Pattern）提供了评估语言的语法或表达式的方式，它属于行为性模式，这种模式实现了一个表达式接口，该接口解释一个特定的上下文，这种模式被用于在sql解析、符号处理引导等；
**角色**
1、抽象表达式（Abstract Expression）角色：定义解释器的接口，约定解释器的解释操作，主要包括解释方法interpret();
2、终结符表达式（Terminal Expression）角色：是抽象表达式的子类，用来实现文法中与终结符相关的操作，文法中的每一个终结符都有一个具体终结表达式与之相对应；
3、非终结表达式（Nonterminal Expression）角色：也是抽象表达式的子类，用来实现文法中欧冠与非终结符相关的操作，文法中的每条规则都对应于一个非终结符表达式；
4、环境（Context）角色：通常包含各个解释器需要的数据或是公共的功能，一般用来传递被所有解释器共享的数据，后面的解释器可以从这里获取这些值；
5、客户端（Client）：主要任务是将需要分析的句子或表达式转换成使用解释器对象描述的抽象语法树，然后调用解释器的解释语法，当然也可以通过环境角色间接访问解释器的解释方法；
![216ee93a8029e4b13b2bbdfb26c3c1f4.png](en-resource://database/1146:1)

###### 迭代器模式 iterator
迭代器模式（Iterator Pattern）是java 和.net编程环境中非常常用的设计模式，这种模式用于顺序访问集合对象的元素，不需要知道集合对象的底层表示，迭代器模式属于行为型模式；
**角色**
1、迭代器角色(Iterator)：定义遍历元素所需要的方法，一般来说会有这么三个方法，取得下一个元素的方法next()，判断是否遍历结束的方法hasNext()，移出当前对象的方法remove()；
2、具体迭代器角色（Concreate Iterator）：实现迭代器接口中定义的方法，完成集合迭代；
3、容器角色（Aggregate）：一般是一个接口，提供一个Iterator()方法，例如java中的Collection接口，List接口，Set接口等；
4、具体容器角色（ConcreateAggregate）：就是抽象容器的具体实现类，比如List接口的有序列表实现ArrayList，List接口的链表实现LinkList，Set接口的哈希列表的实现HashSet等；
![be88e734f22865e4ee17117a89d7cebd.png](en-resource://database/1148:1)

###### 中介者模式 mediator
用一个中介对象来封装一系列的对象交互，中介者使各对象不需要显式地相互引用，从而使其耦合松散，而且可以独立地改变它们之间的交互；
**角色**
1、Mediator 中介者定义一个接口用于与各同事（Colleague）对象通信；
2、ConcreateMediator具体中介者通过协调各同事对象实现协作行为，了解并维护它的各个同事；
3、Colleagueclass每一个同事类都知道它的中介者对象，每一个同事对象在需与其他的同事通信的时候与它的中介者通信；
![cfab012b29b31bcd9ebd034e6c793727.png](en-resource://database/1150:1)


###### 备忘录模式 memento
在不破坏封装性的前提下，捕获一个对象的内部状态，并在该对象之外保存这个状态，这样以后就可将该对象恢复到原先保存的状态；
**角色**
1、Memento备忘录存储原发器对象的内部状态；
2、Originator原发器创建一个备忘录，用以记录当前时刻的内部状态，使用备忘录恢复内部状态；
3、Caretaker负责保存好备忘录，不能对备忘录的内部进行操作或检查；
![34bdb32a1c51f75848159c928016b5aa.png](en-resource://database/1152:1)

###### 观察者模式 observer
定义对象间的一种一对多的依赖关系，当一个对象的状态发生改变时，所有依赖于它的对象都得到通知并被自动更新；
**角色**
1、Subject:目标知道他的观察者，可以有任意多个观察者观察同一个目标提供注册和删除观察者对象的接口；
2、Observer：观察者，为那些在目标发生改变时需获得通知的对象定义一个更新接口；
3、ConcreateSubject:具体目标，将有关状态存入各个ConcreateObserver对象，当它的状态发生改变时，向它的各个观察者发出通知；
4、ConcreateObserver：具体观察者，维护一个指向ConcreateSubject对象的引用，存储有关状态，这些状态应与目标的状态保持一致，实现Observer的更新接口，使自身状态与目标的状态保持一致；

###### 状态模式 state
定义对象间的一种一对多的依赖关系，当一个对象的状态发生改变时，所有依赖于它的对象都得到通知并被自动更新；
当一个对象内在状态改变时允许其改变行为，这个对象看起来像改变了其类；
**角色**
1、Context：它就是那个含有状态的对象，它可以处理一些请求，这些请求最终产生的响应会与状态相关；
2、State：状态接口，它定义了每一个状态的行为集合，这些行为会在Context中得以使用；
3、ConcreateState：具体状态，实现相关行为的具体状态类；
![055c1279f1d3614f07c54ccb460ffa30.png](en-resource://database/1154:1)

###### 策略模式 strategy
定义一系列的算法，把他们一个个封装起来，并且使它们可相互替换，本模式使得算法可独立于使用它的客户而变化；
策略模式也叫政策模式，指的是对象具备某个行为，但是在不同场景中该行为有不同的实现算法，比如一个人的交税比率与他的工资有关，不同的工资水平对应不同的税率；
**角色**
1、Context（上下文角色）：用来操作策略的上下文环境，屏蔽高层模块对策略，算法的直接访问，封装可能存在的变化；
2、Strategy（抽象策略角色）：规定策略或算法的行为；
3、ConcreateStrategy（具体策略角色）：具体的策略或算法实现
![0d21a756992350c6338150d55f7039a0.png](en-resource://database/1156:1)


###### 模板方法 templateMethod
定义一个操作中的算法的骨架，再将一些步骤延迟到子类中；templateMethod使得子类可以不改变一个算法的结构即可重定义该算法的某些特定步骤；
使用了Java的继承机制，在抽象类中定义一个模板方法，该方法引用了若干个抽象方法，或具体方法；
**角色**
1、AbstractClass（抽象父类）：实现了模板方法，定义了算法的骨架；
2、ConcreateClass（具体类）：实现抽象类中的抽象方法，即不同的对象的具体实现细节；
![ff5deff5b67611ef291b6027f6f3d24c.png](en-resource://database/1160:1)

###### 访问者模式 visitor
访问：遍历就是访问的一般形式，单独读取一个元素进行相应的处理也叫做访问，读取到想要查看的内容+对其进行处理就叫做访问；
将访问操作独立出来变成一个新的类，当我们需要增加访问操作的时候，直接增加新的类，原来的代码不需要进行任何的改变，如果可以这样做，那么我们的程序就是好的程序，因为可以扩展，符合开闭原则，而访问者模式就是实现这个的，使得使用不同的访问方式都可以对某些元素进行访问；
**角色**
1、Visitor：表示访问者的抽象类，用于声明对数据结构中的元素访问的visit方法；
2、ConcreateVisitor：标识具体的访问者，继承Visitor并对其声明的抽象方法提供具体实现；
3、Element：表示元素的抽象类，即访问者实际要访问的对象，Element角色需要对访问者提供一个开发的接口，即accept方法，该方法的参数就是Visitor角色；
4、ConcreateElement：表示具体的元素，提供accept方法的实现；
5、ObjectStructure：负责处理Element元素的集合，即表示数据结构的类；
![2654782bb62580c81ac6ab9491423729.png](en-resource://database/1162:1)

#### 数据结构
##### 栈
##### 队列
##### 链表
##### 数组
##### 哈希表
##### bitmap
##### 树-二叉树
##### 树-字典树
##### 树-二叉平衡树（B树）
##### 树-排序树
##### 树-B+树
##### 树-B-树
##### 树-红黑树

#### 算法
![6c1924a3f6059d8d8234d613a3083c40.png](en-resource://database/1190:1)

##### 排序-冒泡
##### 排序-快速
##### 排序-插入
##### 排序-选择
##### 排序-堆排序
##### 排序-桶排序
##### 排序-基数排序
##### 全排列
##### 贪心算法
##### KMP算法
##### hash算法
##### Raft算法


#### 高级技术
##### rest
##### rpc
##### rmi
##### SOA
##### SOAP
##### ioc
控制反转（Inversion of Control，英文缩写为IOC）是一个重要的面向对象编程的法则用来削减计算机程序的耦合问题，也是轻量级的Spring框架的核心；分为两种类型：依赖注入（Dependency injection 简称DI）和依赖查找(Dependency lookup)。所谓依赖注入就是由ioc容器在运行期间，动态地将某种依赖关系注入到对象之中，下面我们先介绍依赖注入；
![8a5f92387bbd17760d3c31b6f857a66a.png](en-resource://database/1186:1)
**具体代码后面补上**

##### jms
##### jndi
##### soa
##### spi
SPI全称是Service Provider Interface基于接口提供服务，是Java提供的一套用来被第三方实现或者扩展的API，它可用来启用框架扩展和替换的组件；
实际上是“基于接口的编程+策略模式+配置文件”组合实现的动态加载机制；
**使用场景**
适用于根据时间使用需要，启用、扩展、或者替换框架的实现策略；
适用于那种多个项目使用到同一接口实现的场景；
比较常见的使用例子有：
- 数据库驱动加载接口实现类，加载jdbc加载不同类型数据库的驱动；
- 日志门面接口实现类加载，SLF4J加载不同提供商的日志实现类；
- Spring中大量使用了SPI，比如：对servlet3.0规范对ServletContainerInitializer的实现、自动类型转换Type Conversion SPI（Converter SPI、Formatter SPI）等；
- Dubbo中也大量使用SPI的方式实现架构的扩展，不过它对Java提供的原生SPI做了封装，允许用户扩展实现Filter接口；

**使用步骤**
- 步骤1：定义好一个接口，并提供该接口的多个实现；
```java
public interface IShout {
    void shout();
}
public class Cat implements IShout {
    @Override
    public void shout() {
        System.out.println("miao miao");
    }
}
public class Dog implements IShout {
    @Override
    public void shout() {
        System.out.println("wang wang");
    }
}
```
- 步骤2：在 src/main/resources/ 下建立 /META-INF/services 目录，在目录下新增配置文件，文件名是上面接口的全路径（org.foo.demo.IShout），内容为要实现的类全路径（
org.foo.demo.animal.Dog和org.foo.demo.animal.Cat，每行一个类）
- 步骤3：使用ServiceLoader来加载配置文件中指定的实现；
```java
public class SPIMain {
    public static void main(String[] args) {
        ServiceLoader<IShout> shouts = ServiceLoader.load(IShout.class);
        for (IShout s : shouts) {
            s.shout();
        }
    }
}
```

**ServiceLoader内部构成**
- loader(ClassLoader类型，类加载器)
- acc(AccessControlContext类型，访问控制器)
- providers(LinkedHashMap<String,S>类型，用于缓存加载成功的类)
- lookupIterator(实现迭代器功能)

**优点**
使用Java SPI机制的优势是实现解耦，使得第三方服务模块的装配控制的逻辑与调用者的业务代码分离，而不是耦合在一起，应用程序可以根据实际业务情况启用架构扩展或替换框架组件；
**缺点**
虽然ServiceLoader也算是使用的延迟加载，但是基本职能通过遍历全部获取，也就是接口的实现类全部加载并实例化一遍，如果你并不想用某些实现类，它也被加载并实例化了这就造成了浪费，获取某个实现类的方式不够灵活，只能通过Iterator形式获取，不能根据某个参数来获取对应的实现类。
多个并发线程使用ServiceLoader类的实例是不安全的；

##### jdwp
##### webservice
##### wsdl
##### socket
###### nio
###### aio
###### bio

#### 技术架构

##### web容器架构
###### spring core
###### spring mvc
###### spring boot
###### spring cloud
###### mybatis

##### 分布式协调工具
###### zookeeper

##### 搜索架构
###### solr
###### lucene
##### elasticSearch

##### 分布式计算架构
###### hadoop
###### hive
###### presto
###### spark
###### flink

##### 日志收集工具
###### flume
###### kafka
###### scribe
###### chukwa

##### olap工具
**OLTP**
OLTP，也叫联机事务处理（Online Transaction Processing），表示事务性非常高的系统，一般都是高可用的在线系统，以小的事务以及小的查询为主，评估其系统的时候，一般看其每秒执行的Transaction以及Execute SQL的数量。在这样的系统中，单个数据库每秒处理的Transaction往往超过几百个，或者是几千个，Select 语句的执行量每秒几千甚至几万个。典型的OLTP系统有电子商务系统、银行、证券等，如美国eBay的业务数据库，就是很典型的OLTP数据库。

-----
**OLAP**
OLAP，也叫联机分析处理（Online Analytical Processing）系统，有的时候也叫DSS决策支持系统，就是我们说的数据仓库。在这样的系统中，语句的执行量不是考核标准，因为一条语句的执行时间可能会非常长，读取的数据也非常多。所以，在这样的系统中，考核的标准往往是磁盘子系统的吞吐量（带宽），如能达到多少MB/s的流量。

###### zeppelin
###### kylin
###### druid 
###### TiDB
**背景：**
随着业务量的增长，数据也会成倍增长，可能会达到TB级，传统的单机数据库提供的服务，在系统的可扩展、性价比方面已经不再适用，比如Mysql数据库，缺点是没法做到水平扩展，Mysql要想能做到水平扩展，唯一的方法就是在业务层分库分表或者使用中间层、mycat等方案，但是这些方案也有很大局限性，执行计划不是最优，分布式事务，跨节点join，扩容复杂等；
**简介：** PingCAP开发，TiDB是一个开源分布式NewSQL数据库，实现了自动的水平伸缩，强一致性的分布式事务，基于Raft算法的多副本复制等重要NewSQL特性，TiDB结合了RDBMS和NoSQL的优点，部署简单，在线弹性扩容和异步表结构变更不影响业务，真正的异地多活及自动故障恢复保障数据安全，同事兼容MySQL协议，使迁移使用成本降到极低；
TiDB的设计目标是使用100%的OLTP场景和80%的OLAP场景。
**优点：** 
- SQL支持（TiDB与Mysql兼容友好）
- 水平线性弹性扩展
- 分布式事务（具体如何支持的需要了解  TODO?）
- 跨数据中心数据强一致性保证
- 故障自恢复的高可用

**架构：**
官网地址：https://pingcap.com/
![0577cac9e683c7c642a248e98e090359.png](en-resource://database/1188:1)
**TiDB Server：**
TiDB Server 负责接收 SQL 请求，处理 SQL 相关的逻辑，并通过 PD 找到存储计算所需数据的 TiKV 地址，与 TiKV 交互获取数据，最终返回结果。 TiDB Server 是无状态的，其本身并不存储数据，只负责计算，可以无限水平扩展，可以通过负载均衡组件（如LVS、HAProxy 或 F5）对外提供统一的接入地址。
**PD Server：**
Placement Driver (简称 PD) 是整个集群的管理模块，其主要工作有三个： 一是存储集群的元信息（某个 Key 存储在哪个 TiKV 节点）；二是对 TiKV 集群进行调度和负载均衡（如数据的迁移、Raft group leader 的迁移等）；三是分配全局唯一且递增的事务 ID。PD 是一个集群，需要部署奇数个节点，一般线上推荐至少部署 3 个节点。
**TiKV Server：**
TiKV Server 负责存储数据，从外部看 TiKV 是一个分布式的提供事务的 Key-Value 存储引擎。存储数据的基本单位是 Region（区域），每个 Region 负责存储一个 Key Range （从 StartKey 到 EndKey 的左闭右开区间）的数据，每个 TiKV 节点会负责多个 Region 。TiKV 使用 Raft 协议做复制，保持数据的一致性和容灾。副本以 Region 为单位进行管理，不同节点上的多个 Region 构成一个 Raft Group，互为副本。数据在多个 TiKV 之间的负载均衡由 PD 调度，这里也是以 Region 为单位进行调度。
**应用场景：**
- MySQL分片与合并，通过Syncer（PingCAP开发的）工具可以吧TiDB作为一个MySQL Slave，将TiDB作为现有数据库从库接在主MySQL库的后方，在这一层可以跨库、跨业务的将数据收集到一起，进行实时sql查询，以多主一从的方式实现；
- 直接替换MySQL，不做过多描述，就是简单直接的替换；
- 数据仓库，PingCAP开源了TiSpark工具是Spark的插件，用户可以将Mysql、通过Spark读取的数据统一收集过来，进行关联查询；
- 作为其他系统的模块，TiDB可以对外单独提供API接口如ACID Transcation（跨行事务）和Raw API（单行事务），用户可以根据自身的需求选择这两个接口来使用；也可以用来替换Redis来存储一些中间数据等场景；

###### clickHouse
官网地址：https://www.yandex.com/
https://clickhouse.yandex/docs/zh/development/architecture/
**简介：** 
clickhouse是俄罗斯的Yandex（做搜索的）开源的一套针对数据仓库场景的多维数据存储与检索工具，它通过针对性的设计力图解决海量多维度数据的查询性能问题，主要包含以下功能：
- 深度列存储
- 向量化查询执行（Vectorized Query Execution）
- 数据压缩
- 并行和分布式查询
- 实时数据注入
- 跨数据中心的备份
- 磁盘上的数据访问局部性（Locality of reference）
- 类SQL支持
- 局部和分布式的join
- 可插入式的纬度表（dimension table）
- 预估查询处理
- 支持IPV6数据格式
- 网站和应用分析
通过系统service的方式启动服务，可以通过clickhouse-client 命令或HTTP API的方式(增加、修改操作只能用POST方法，其他操作可以用GET方法)进行数据访问或操作；
clilckHouse是一个列导向数据库，是原生的向量化执行引擎，和Hadoop生态不同，它采用的是Local attached storage作为存储，它的系统在生产环境中可以应用到比较大的规模，因为他的线性扩展能力和可靠性保障能够原生支持shard+replication这种解决方案；

里面分为以下部分 ： TODO？
- 列（chunks）
- 数据域（Fieldis ）
- 抽象渗漏法则（IColumn）
- 数据类型（IDataType）
- 数据块（block）
- 数据块流（IBlockInputStream）
- 格式（BlockInputStreamFromRowInputStream 和BlockOutputStreamFromRowOutputStream ）
- I/O
- 表
- 解析器（AST）
- 中断器
- 函数
- 聚合函数
- 引擎（engine） https://www.jianshu.com/p/dbca0f5ededb
- 服务器
- 分布式查询执行
- 合并数（MergeTree）


**优点：** 
- 开源的列存储数据库管理系统
- 支持线性扩展
- 简单方便
- 高可靠性
- 容错（支持多主机异步复制，可以跨多个数据中心部署，单个节点或整个数据中心的停机时间不会影响系统的读写可用性）

clickhouse执行速度比较快，只因为快是因为以下三方面因素：
- 它的数据剪枝能力比较强，分区剪枝在执行层，而存储格式用局部数据表示，就可以更细粒度地做一些数据的剪枝。它的引擎在实际使用中应用了一种现在比较流行的 LSM 方式
- 它对整个资源的垂直整合能力做得比较好，并发 MPP+ SMP 这种执行方式可以很充分地利用机器的集成资源。它的实现又做了很多性能相关的优化，它的一个简单的汇聚操作有很多不同的版本，会根据不同 Key 的组合方式有不同的实现。对于高级的计算指令，数据解压时，它也有少量使用;
- 我当时选择它的一个原因，ClickHouse 是一套完全由 C++ 模板 Code 写出来的实现，代码还是比较优雅的。

**缺点：**
不支持事务，需要依赖外部事务机制；

**应用场景：** 
适用于OLAP实时场景；
https://www.infoq.cn/article/NTwo*yR2ujwLMP8WCXOE 侧重应用场景
http://www.clickhouse.com.cn/topic/5a3df7b02141c2917483557c  侧重将技术底层
https://www.zouyesheng.com/clickhouse.html#toc9 侧重讲特定场景下计算引擎的设置


###### Waterdrop
**收集数据：**
- Waterdrop是一个非常易用，高性能，能够应对海量数据的实时数据处理产品，它构建在Spark之上。Waterdrop拥有着非常丰富的插件，支持从Kafka、HDFS、Kudu中读取数据，进行各种各样的数据处理，并将结果写入ClickHouse、Elasticsearch或者Kafka中。
https://blog.csdn.net/huochen1994/article/details/83827587
- 内部kafka 引擎方式接入
```
CREATE TABLE tkafka (timestamp UInt64,level String,message String) ENGINE = Kafka SETTINGS kafka_broker_list = ‘192.168.1.198:9092’,kafka_topic_list = ‘test2’,kafka_group_name = ‘group1’,kafka_format = ‘JSONEachRow’,kafka_row_delimiter = ‘\n’,kafka_num_consumers = 1;
```
- Hangout

###### flinkx
底层基于flink引擎的数据同步工具
**优点**
* 使用flink API方式提交作业无需单独进程启动减少资源占用
* 作业调度机制比较简单，效率高
* 数据源完全基于flink source和sink，效率高
* 使用配置简单入门快

**缺点**
* 仅使用于数据同步流程，因为调度时使用单条线性流程，所以不适合合并和关联等一对多或多对一操作，
* 不支持非格式化数据

###### Tableau
数据分析工具，司内信息流部门在用


##### 开源调度工具
###### Luigi python
###### Azkaban (linkdin)java
###### apache oozie java
###### apache airflow (airbnb) python

#### 多线程
继承Thread类或实现Runnable接口，实现类中run方法就是多线程执行的逻辑代码，通过start方法将线程启动。
##### interrupt 和 stop
两个方法都是停止线程的方法，区别如下：
**stop**
对运行中的线程进行立即停止，停止时线程中在运行的代码会进行跳过立即停止，比较暴力，不建议使用；

**interrupt**
interrupt和sleep方法进行公用时会抛Illgal*Exception异常；
推荐使用方式，执行该方法会向线程发送停止标识，线程内通过interrupted 或 isInterrupted方法进行标识判断是否需要结束线程，如果为true时执行结束操作，结束操作有如下两种方式可实现：
* return 通过该方法可直接跳过return后面的代码直接结束
* throw new Illgal**Exception 通过抛异常方式结束线程，推荐使用方式，因为抛异常可在调用线程处进行异常捕获

##### interrupted 和 isInterrupted
* interrupted： 静态方法，线程执行interrupt后返回true，同时清除标识状态再次调用返回false；
* isInterrupted： 非静态方法，线程执行interrupt后返回true，可多次调用；

##### suspend 和 resume
挂起线程 恢复线程挂起

##### join、wait、sleep、yield
* join：线程阻塞,可通过该方法可实现线程的顺序执行；
* wait：Thread类中把线程从running状态转化为非runnable状态有一个方法就是wait方法。wait方法是线程的等待状态；wait会释放锁，线程中的下一步操作会被挂起，通过notify或notifyAll来唤醒线程
* sleep：暂停线程，本地native方法，非java封装方法,让出cpu给其他线程，但是不释放锁资源和监控状态
* yield：使线程暂停并允许执行其他线程,不会释放锁

看书笔记记录
wait notify等待通知机制
pipinputstream pipoutputstream 消息通道
threadlocal线程本地模式
join以及和sleep的区别
synchronized
volatile可见性 非原子性
automic类
常量池进行锁容易形成死锁
suspend与resume的缺陷是独占 不同步
yield方法释放cpu
线程守护和线程优先级

#### 锁

#### jvm底层
##### jvm结构
##### 内存
##### 垃圾收集算法
##### 逃逸分析

##### UML类图
类节点主要分三部分从上到下依次是类名、属性、方法（前面+表示public，-表示private，#表示protected）；
![bcf9728f7f9b1e98a13f113b138bcb6f.png](en-resource://database/1184:1)


1、依赖：虚线箭头指向依赖，是一种使用的关系，即一个类的实现需要另一个类的协助，所以要尽量不使用双向的互相依赖；
![fbbdbe850e54348c2fad020647124bb9.png](en-resource://database/1176:1)

2、关联：实线箭头指向关联，是一种拥有的关系，它使一个类知道另一个类的属性和方法，如老师与学生，丈夫与妻子关联可以是双向的，也可以是单向的，双向是两个箭头单向是一个箭头；
![19aaa746f9e9ee5b77f685c2c4f5ab6b.png](en-resource://database/1178:1)

3、实现：虚线三角指向接口，是一种类与接口的关系，表示类是接口所有特征和行为的实现；
![3f64e53746183cc06bec3c6b6a73fb3e.png](en-resource://database/1174:1)

4、泛化：实线三角指向父类，是一种继承关系，表示一般与特殊的关系，它指定了子类如何特化父类的所有特征和行为；
![bb78d1883955ed6b9a285264d3190dd2.png](en-resource://database/1172:1)

5、聚合：空心菱形能分离而独立存在，是整体与部分的关系，且部分可以离开整体而单独存在，如车和轮胎是整体和部分的关系，轮胎离开车可以单独存在，聚合关系是关联关系的一种，是强的关联关系，关联和聚合在语法上无法区分必须考察具体的逻辑关系；
![f6836266c36a29831134856ff9094a99.png](en-resource://database/1180:1)

6、组合：实心菱形精密关联不可分，是整体与部分的关系，但部分不能离开整体而单独存在，如公司和部门是整体和部分的关系，没有公司就没有部门，组合关系是也是一种关联，是比聚合关系还要强的关系，它要求普通的聚合关系中代表整体的对象负责代表部分的对象的生命周期；
![088b4cf2d6621a1e7c74504874c4a8b5.png](en-resource://database/1182:1)


