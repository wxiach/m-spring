# m-spring

![Java](https://img.shields.io/badge/Java-17-007396?logo=openjdk&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-构建-C71A36?logo=apachemaven&logoColor=white)
![ByteBuddy](https://img.shields.io/badge/ByteBuddy-1.15-blue)

> 手写迷你版 Spring 核心容器，以造轮子的方式理解 Spring 底层原理。

1. IoC 容器：`@Component` 组件扫描 + `@Autowired` 依赖注入（字段 / 构造器），支持字段循环依赖
2. Bean 作用域：`@Scope` 指定 singleton（单例）/ prototype（原型）
3. 生命周期回调：`@PostConstruct` / `@PreDestroy` 处理初始化与销毁，`BeanPostProcessor` 提供扩展点
4. AOP 切面：`@Aspect` 声明切面，支持 `@Before` / `@AfterReturning` / `@Around` / `@AfterThrowing` 四种通知
