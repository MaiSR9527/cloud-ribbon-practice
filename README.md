文章首发：[Spring Cloud Ribbon入门和实战](https://www.maishuren.top/archives/springcloudribbon%E5%85%A5%E9%97%A8%E5%92%8C%E5%AE%9E%E6%88%98)

# Ribbon是什么

Ribbon是一个客户端负载均衡器，它赋予了应用一些支配HTTP与TCP行为的能力，这里的负载均衡是客户端的负载均衡，也有人称为后端负载均衡是进程内负载均衡的一种。Ribbon是SpringCloud生态里的不可缺少的组件，有了它，是个服务的横向扩展更加方便了。此外想Feign和Zuul默认是集成了Ribbon。

Ribbon是Neflix开源的一个组件，目前Ribbon早已进入维护状态，但是就目前的情况来看，Spring Cloud Netflix的一些组件还是可以使用。

Spring Cloud Loadbalancer是Spring Cloud社区开源的组件，目的是替代进入维护状态的Ribbon，但是Loadbalancer还是有很长的一段路要走。

# Ribbon入门

由于客户端负载均衡需要从注册中心获取服务列表，所以需要集成注册中心。

## 创建父级工程cloud-ribbon-practice

```xml
	<parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.2.3.RELEASE</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
        <java.version>1.8</java.version>
        <spring.cloud-version>Hoxton.SR3</spring.cloud-version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring.cloud-version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
```

## 创建注册中心cloud-eureka-server

```xml
<dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
        </dependency>
    </dependencies>
```

配置文件application.xml

```yaml
server:
  port: 8761
eureka:
  instance:
    hostname: localhost
  client:
    registerWithEureka: false
    fetchRegistry: false
    serviceUrl:
      defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka/
```

启动类

```java
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
```

## 创建源服务工程cloud-ribbon-hello-b1、cloud-ribbon-hello-b2、cloud-ribbon-hello-b3

```xml
	<dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
    </dependencies>
```

配置文件application.yml

b1

```yaml
spring:
  application:
    name: ribbon-service-b
server:
  port: 7777
eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true
```

b2

```yaml
spring:
  application:
    name: ribbon-service-b
server:
  port: 7778
eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true
```

b3

```yaml
spring:
  application:
    name: ribbon-service-b
server:
  port: 7779
eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true
```

b1、b2、b3的controller

```java
@RestController
public class OrderController {

    @Value("${server.port}")
    private Integer port;

    @Value("${spring.application.name}")
    private String name;

    @GetMapping("/test")
    public String add() {
        return "this service name is " + name + " and port is " + port;
    }

}
```

b1、b2、b3的启动类

```java
@SpringBootApplication
@EnableDiscoveryClient
public class ServiceB1Application {
    public static void main(String[] args) {
        SpringApplication.run(ServiceB1Application.class, args);
    }
}

@SpringBootApplication
@EnableDiscoveryClient
public class ServiceB2Application {
    public static void main(String[] args) {
        SpringApplication.run(ServiceB2Application.class, args);
    }
}

@SpringBootApplication
@EnableDiscoveryClient
public class ServiceB3Application {
    public static void main(String[] args) {
        SpringApplication.run(ServiceB3Application.class, args);
    }
}
```

## 创建服务调用者cloud-ribbon-hello-a

`spring-cloud-starter-netflix-eureka-client`已经集成了ribbon。不需要额外引入，直接使用即可。

![](http://cdn.jsdelivr.net/gh/MaiSR9527/blog-pic/springcloud/ribbon-04.png)

```xml
	<dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
    </dependencies>
```

配置文件application.yml

```yaml
spring:
  application:
    name: ribbon-hello-a
server:
  port: 7776
eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true
```

启动类，RestTemplate使用了`@LoadBalanced`，这样RestTemplate就开启了ribbon的负载均衡了。

```java
@SpringBootApplication
@EnableDiscoveryClient
public class ServiceAApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceAApplication.class, args);
    }

    @LoadBalanced
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

controller

```java
@RestController
@RequestMapping("ribbon")
public class TestController {

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/test")
    public String test(){
        String body = restTemplate.getForEntity("http://RIBBON-SERVICE-B/test", String.class).getBody();
        return body;
    }

}
```

测试，使用postman访问几次：http://localhost:7776/ribbon/test。下图可以看出，默认是的负载均衡算法是轮询。

![](http://cdn.jsdelivr.net/gh/MaiSR9527/blog-pic/springcloud/ribbon-01.png)

![](http://cdn.jsdelivr.net/gh/MaiSR9527/blog-pic/springcloud/ribbon-02.png)

![](http://cdn.jsdelivr.net/gh/MaiSR9527/blog-pic/springcloud/ribbon-03.png)

# Ribbon实战

从上一节可以看到，开启负载均衡只需通过`@LoadBalanced`注解即可。负载均衡中又很多的负载均衡策略，如轮询（Round Robin）、权重（Weight）、ip_hash等。这些丰富的策略让我们在构建应用的时候，有很多选择的余地，可以根据实际的业务场景选择最合适的策略。

在Ribbon中一共提供了7中负载均衡策略：

| 策略类                       | 命名             | 描述                                                         |
| ---------------------------- | ---------------- | ------------------------------------------------------------ |
| RandomRule                   | 随机策略         | 随机选择Server                                               |
| RoundRobinRule               | 轮询策略         | 按顺序选择Server                                             |
| RetryRule                    | 重试策略         | 在一个配置时间段内当选择Server不成功，则一直尝试选择一个可用的Server |
| BestAvailableRule            | 最低并发策略     | 卓哥考察Server，如果Server断路器打开，则忽略，再选择其中并发连接最低的Server |
| AvailabilityFilteringRule    | 可用过滤策略     | 过滤一直连接失败并标记为circuit tripped的Server，过滤掉那些高并发连接的Server(active connections超过配置的阈值) |
| ~~ResponseTimeWeightedRule~~ | 响应时间加权策略 | 已经被弃用，作用同WeightedResponseTimeRule                   |
| WeightedResponseTimeRule     | 响应时间加权策略 | 根据Server的响应时间分配权重，响应时间越长，权重越低，被选中的概率就越低。响应时间越短，权重越高，被选择到的概率越高 |
| ZoneAvoidanceRule            | 区域权衡策略     | 综合判断Server所在区域的性能和Server的可用性轮询选择Server，并且判断一个AWS Zone的运行性能是否可用，剔除不可用的Zone中的所有Server |

在上面的入门案例中Ribbon的默认负载均衡策略是轮询策略，

## Ribbon自定义配置负载均衡策略

### 全局配置

使用Ribbon时配置全局的负载均衡策略，需要加一个配置类。改配置类需要被`@ComponentScan`扫描到才能全局生效。

```java
@Configuration
public class GlobalRuleConfig {
    @Bean
    public IRule ribbonRule() {
        return new RandomRule();
    }
}
```

上面配置了随机的策略，多次访问http://localhost:7776/ribbon/test。就可看到Server的选择就变成了随机了。同样了可以选择其他的Ribbon已经实现的策略，也可以自定义负载均衡策略。

### 基于`@RibbonClient`或`@RibbonClients`注解的配置

配置类，注意：**编写自定义配置类，需要特别注意的是官方文档明确给出了警告：这个自定义配置类不能放在@ComponentScan所扫描的包以及其子包下(即不能放在主启动类所在的包及其子包下，因此我们需要新建一个包来放该配置类)，否则我们自定义的这个配置类就会被所有的Ribbon客户端所共享，也就达不到特殊化定制的目的了**

```java
@Configuration
public class AnnoRuleConfig {
    @Bean
    public IRule ribbonRule() {
        return new RandomRule();
    }
}
```

启动类的配置

```java
@SpringBootApplication
@EnableDiscoveryClient
@RibbonClient(name = "ribbon-service-b", configuration = AnnoRuleConfig.class)
public class ServiceAApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceAApplication.class, args);
    }

    @LoadBalanced
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

通过`@RibbonClient`指定某个服务的负载均衡策略，其他没有被指定的，就是用默认的负载均衡策略。该注解可以把其他的配置类作为另外一个IOC容器导入到应用中，相当于加载了两个完全不相干的Spring的beans配置文件，此时应用中会有两个IOC容器。

```java
@RibbonClient(name = "RIBBON-SERVICE-B", configuration = AnnoRuleConfig.class)
```

也可以使用一下的方式，指定多个服务的负载均衡策略

```java
@RibbonClients(value = {
		@RibbonClient(name = "RIBBON-SERVICE-B", configuration = AnnoRuleConfig.class),
		@RibbonClient(name = "RIBBON-SERVICE-C", configuration = AnnoRuleConfig.class)
})
```

### 基于配置文件

下面对服务`ribbon-service-b`的负载均衡策略使用

```yaml
RIBBON-SERVICE-B:
  ribbon:
    NFLoadBalancerRuleClassName: com.netflix.loadbalancer.RandomRule
```

## Ribbon超时与重试

使用HTTP发起请求难免会发生问题，在F版开始Ribbon的重试机制默认是开启的，需要添加对超时时间与重试策略的配置。列入下面`ribbon-service-b`服务的配置

```yaml
RIBBON-SERVICE-B:
  ribbon:
    NFLoadBalancerRuleClassName: com.netflix.loadbalancer.RandomRule
    ConnectTimeout: 3000
    ReadTimeout: 60000
    MaxAutoRetries: 3 #对第一次请求的服务的重试次数
    MaxAutoRetriesNextServer: 1 #要重试的下一个服务的最大数量（不包括第一个服务）
    OkToRetryOnAllOperations: true
```

也可以全局配置

```yaml
ribbon:
  ConnectTimeout: 3000
  ReadTimeout: 60000
  MaxAutoRetries: 3 #对第一次请求的服务的重试次数
  MaxAutoRetriesNextServer: 1 #要重试的下一个服务的最大数量（不包括第一个服务）
  OkToRetryOnAllOperations: true
```

一般Ribbon都是搭配OpenFeign这类Http客户端或者其他RPC使用。因为这样去调用远程服务会更加优雅和方便。而OpenFeign默认是继承了Ribbon，对于Ribbon的超时时间配置也是很简单。

对于网络抖动这些可以使用`spring-retry`，spring-retry是spring提供的一个基于spring的重试框架，非常好用。

## Ribbon饥饿加载

Ribbon在进行客户端负载均衡的时候，并不是启动时就加载上下文，而是在实际请求的时候采取创建。因为要加载上下文的原因，在第一次调用时可能会很慢，甚至导致超时。所以我们可以指定Ribbon客户端开启立即加载(饥饿加载)，在应用启动的时候就立即加载所有配置项的应用程序上下文。

```yaml
ribbon:
  eager-load:
    clients: ribbon-service-b, ribbon-service-order
    enabled: true
```



## 自定义Ribbon客户端

在Ribbon的1.2.0版本之后，就可以使用配置文件来定制Ribbon客户端，其实实质就是使用配置文件来指定一些默认加载类，从而更改Ribbon客户端的行为，并且使用这种方式优先级最高，优先级高于使用注解`@RibbonClient`指定的配置和源码中加载的相关的Bean。看下表：

| 配置项                                            | 说明                         |
| ------------------------------------------------- | ---------------------------- |
| <clientName>.ribbon.NFLoadBalancerClassName       | 指定ILoadBalancer的实现类    |
| <clientName>.ribbon.NFLoadBalancerRuleClassName   | 指定IRule的实现类            |
| <clientName>.ribbon.NFLoadBalancerPingClassName   | 指定IPing的实现类            |
| <clientName>.ribbon.NiWSServerListClassName       | 指定ServerList的实现类       |
| <clientName>.ribbon.NIWSServerListFilterClassName | 指定ServerListFilter的实现类 |

例如：这里使用的实现的RIbbon提供的实现

```yaml
RIBBON-SERVICE-B:
  ribbon:
    NFLoadBalancerRuleClassName: com.netflix.loadbalancer.RandomRule
    NiWSServerListClassName: com.netflix.loadbalancer.ConfigurationBasedServerList
```



## Ribbon脱离Eureka使用

在默认的情况下，Ribbon客户端需要从Eureka注册中心读取服务注册信息列表，来达到一种动态负载均衡的功能。当使用的注册中心是公共的注册中心，例如：社区公益Eureka(http://eureka.springcloud.cn)，所以就不要从Eureka中读取服务列表了。而是从Ribbon客户端自行指定原服务地址，让Rabbon脱离使用。配置如下：

```yaml
ribbon: 
  eureka:
    enabled: false
#RIBBON-SERVICE-B服务的获取地址
RIBBON-SERVICE-B:
  ribbon:
    listOfServers: http://localhost:8088/
```

# Ribbon进阶

## 核心工作原理

Ribbon的核心接口：

| 接口                      | 描述                                                      | 默认实现                       |
| ------------------------- | --------------------------------------------------------- | ------------------------------ |
| IClientConfig             | 定义Ribbon中管理配置的接口                                | DefaultClientConfigImpl        |
| IRule                     | 定义Ribbon中负载均衡策略的接口                            | ZoneAdvoidanceRule             |
| IPing                     | 定义定期Ping服务检查可用性的接口                          | DummyPing                      |
| ServerList\<Server>       | 定义获取服务列表方法的接口                                | ConfigurationBasedServerList   |
| ServerListFilter\<Server> | 定义特定期望获取服务列表方法的接口                        | ZonePreferenceServerListFilter |
| ILoadBalancer             | 定义负载均衡选择服务的核心方法的接口                      | ZoneAwareLoadBalancer          |
| ServerListUpdater         | 为DynamicServerListLoadBalancer定义动态更新服务列表的接口 | PollingServerListUpdater       |

Ribbon完全是基于这些接口上建立起来的，是Ribbon的核心。了解这些核心的类的功能对于理解Ribbon的原理和扩展很有利。

在之前的例子中，使用Ribbon负载均衡都是通过在RestTemplate的Bean上添加注解`@LoadBalanced`，使得RestTemplate拥有了负载均衡的能力。

**LoadBalanced源码：**

```java

/**
 * Annotation to mark a RestTemplate or WebClient bean to be configured to use a
 * LoadBalancerClient.
 * @author Spencer Gibb
 */
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Qualifier
public @interface LoadBalanced {
}
```

在注释中可以看到：**该注解标记在RestTemplate或者其他的WebClient的Bean上，来使用LoadBalancerClient**。

**LoadBalancerClient：**该接口扩展自ServiceInstanceChooser

```java
public interface LoadBalancerClient extends ServiceInstanceChooser {

	/**
	 * Executes request using a ServiceInstance from the LoadBalancer for the specified
	 * service.
	 * @param serviceId The service ID to look up the LoadBalancer.
	 * @param request Allows implementations to execute pre and post actions, such as
	 * incrementing metrics.
	 * @param <T> type of the response
	 * @throws IOException in case of IO issues.
	 * @return The result of the LoadBalancerRequest callback on the selected
	 * ServiceInstance.
	 */
	<T> T execute(String serviceId, LoadBalancerRequest<T> request) throws IOException;

	/**
	 * Executes request using a ServiceInstance from the LoadBalancer for the specified
	 * service.
	 * @param serviceId The service ID to look up the LoadBalancer.
	 * @param serviceInstance The service to execute the request to.
	 * @param request Allows implementations to execute pre and post actions, such as
	 * incrementing metrics.
	 * @param <T> type of the response
	 * @throws IOException in case of IO issues.
	 * @return The result of the LoadBalancerRequest callback on the selected
	 * ServiceInstance.
	 */
	<T> T execute(String serviceId, ServiceInstance serviceInstance,
			LoadBalancerRequest<T> request) throws IOException;

	/**
	 * Creates a proper URI with a real host and port for systems to utilize. Some systems
	 * use a URI with the logical service name as the host, such as
	 * http://myservice/path/to/service. This will replace the service name with the
	 * host:port from the ServiceInstance.
	 * @param instance service instance to reconstruct the URI
	 * @param original A URI with the host as a logical service name.
	 * @return A reconstructed URI.
	 */
	URI reconstructURI(ServiceInstance instance, URI original);
}
```

**ServiceInstanceChooser：**

```java
public interface ServiceInstanceChooser {

	/**
	 * Chooses a ServiceInstance from the LoadBalancer for the specified service.
	 * @param serviceId The service ID to look up the LoadBalancer.
	 * @return A ServiceInstance that matches the serviceId.
	 */
	ServiceInstance choose(String serviceId);
}
```

* ServiceInstance choose(String serviceId)：根据ServiceId，结合负载均衡器选择一个服务实例
* <T> T execute(String serviceId, LoadBalancerRequest<T> request)：使用来自LoadBalancer的ServiceInstance为指定的服务执行请求
* <T> T execute(String serviceId, ServiceInstance serviceInstance,
  			LoadBalancerRequest<T> request)：使用来自LoadBalancer的ServiceInstance为指定的服务执行请求，是上一个方法的重载，在实现类中可以看到它们的关系，就是前一个方法的细节实现、
* URI reconstructURI(ServiceInstance instance, URI original)：使用注解ip和port构建特定的URL以供Ribbon内部使用。Ribbon使用具有逻辑服务名称的URL作为host，例如：http://service-b/order/add。

从这些方法的功能可以知道这两个接口的重要性了。这两个接口的同一包下有一个类`LoadBalancerAutoConfiguration`。`LoadBalancerAutoConfiguration`在`org.springframework.cloud.client.loadbalancer`包下，在`spring-cloud-commons`里面。该自动配置类正式Ribbon的核心配置类。

```java
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(RestTemplate.class)
@ConditionalOnBean(LoadBalancerClient.class)
@EnableConfigurationProperties(LoadBalancerRetryProperties.class)
public class LoadBalancerAutoConfiguration {

	@LoadBalanced
	@Autowired(required = false)
	private List<RestTemplate> restTemplates = Collections.emptyList();

	@Autowired(required = false)
	private List<LoadBalancerRequestTransformer> transformers = Collections.emptyList();

	@Bean
	public SmartInitializingSingleton loadBalancedRestTemplateInitializerDeprecated(
			final ObjectProvider<List<RestTemplateCustomizer>> restTemplateCustomizers) {
		return () -> restTemplateCustomizers.ifAvailable(customizers -> {
			for (RestTemplate restTemplate : LoadBalancerAutoConfiguration.this.restTemplates) {
				for (RestTemplateCustomizer customizer : customizers) {
					customizer.customize(restTemplate);
				}
			}
		});
	}

	@Bean
	@ConditionalOnMissingBean
	public LoadBalancerRequestFactory loadBalancerRequestFactory(
			LoadBalancerClient loadBalancerClient) {
		return new LoadBalancerRequestFactory(loadBalancerClient, this.transformers);
	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnMissingClass("org.springframework.retry.support.RetryTemplate")
	static class LoadBalancerInterceptorConfig {

		@Bean
		public LoadBalancerInterceptor ribbonInterceptor(
				LoadBalancerClient loadBalancerClient,
				LoadBalancerRequestFactory requestFactory) {
			return new LoadBalancerInterceptor(loadBalancerClient, requestFactory);
		}

		@Bean
		@ConditionalOnMissingBean
		public RestTemplateCustomizer restTemplateCustomizer(
				final LoadBalancerInterceptor loadBalancerInterceptor) {
			return restTemplate -> {
				List<ClientHttpRequestInterceptor> list = new ArrayList<>(
						restTemplate.getInterceptors());
				list.add(loadBalancerInterceptor);
				restTemplate.setInterceptors(list);
			};
		}

	}

	/**
	 * Auto configuration for retry mechanism.
	 */
	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass(RetryTemplate.class)
	public static class RetryAutoConfiguration {

		@Bean
		@ConditionalOnMissingBean
		public LoadBalancedRetryFactory loadBalancedRetryFactory() {
			return new LoadBalancedRetryFactory() {
			};
		}

	}

	/**
	 * Auto configuration for retry intercepting mechanism.
	 */
	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass(RetryTemplate.class)
	public static class RetryInterceptorAutoConfiguration {

		@Bean
		@ConditionalOnMissingBean
		public RetryLoadBalancerInterceptor ribbonInterceptor(
				LoadBalancerClient loadBalancerClient,
				LoadBalancerRetryProperties properties,
				LoadBalancerRequestFactory requestFactory,
				LoadBalancedRetryFactory loadBalancedRetryFactory) {
			return new RetryLoadBalancerInterceptor(loadBalancerClient, properties,
					requestFactory, loadBalancedRetryFactory);
		}

		@Bean
		@ConditionalOnMissingBean
		public RestTemplateCustomizer restTemplateCustomizer(
				final RetryLoadBalancerInterceptor loadBalancerInterceptor) {
			return restTemplate -> {
				List<ClientHttpRequestInterceptor> list = new ArrayList<>(
						restTemplate.getInterceptors());
				list.add(loadBalancerInterceptor);
				restTemplate.setInterceptors(list);
			};
		}

	}

}
```

这就清晰起来了，他的配置加载时机是当前工程环境必须有RestTemplate的实例和必须初始化了LoadBalancerClient的实现类。

```java
@ConditionalOnClass(RestTemplate.class)
@ConditionalOnBean(LoadBalancerClient.class)
```

LoadBalancerRequestFactory：用于创建LoadBalancerRequest给LoadBalancerInterceptor使用。

LoadBalancerInterceptorConfig：维护了LoadBalancerInterceptor与RestTemplateCustomizer的实例。

* LoadBalancerInterceptor：拦截每一次的HTTP请求，将请求绑定金Ribbon的负载均衡的生命周期。
* RestTemplateCustomizer：为每一个Restemplate绑定LoadBalancerInterceptor拦截器。

LoadBalancerInterceptor的作用已经和贴近答案了。

```java
public class LoadBalancerInterceptor implements ClientHttpRequestInterceptor {

	private LoadBalancerClient loadBalancer;

	private LoadBalancerRequestFactory requestFactory;

	public LoadBalancerInterceptor(LoadBalancerClient loadBalancer,
			LoadBalancerRequestFactory requestFactory) {
		this.loadBalancer = loadBalancer;
		this.requestFactory = requestFactory;
	}

	public LoadBalancerInterceptor(LoadBalancerClient loadBalancer) {
		// for backwards compatibility
		this(loadBalancer, new LoadBalancerRequestFactory(loadBalancer));
	}

	@Override
	public ClientHttpResponse intercept(final HttpRequest request, final byte[] body,
			final ClientHttpRequestExecution execution) throws IOException {
		final URI originalUri = request.getURI();
		String serviceName = originalUri.getHost();
		Assert.state(serviceName != null,
				"Request URI does not contain a valid hostname: " + originalUri);
		return this.loadBalancer.execute(serviceName,
				this.requestFactory.createRequest(request, body, execution));
	}
}
```

在源码可以看到它是通过ClientHttpRequestInterceptor实现每次对HTTP请求的拦截，ClientHttpRequestInterceptor类是Spring中维护的请求拦截器，实现它的intercept方法就可以使得请求进入方法内，从而Ribbon就可以做一些自己的处理了。

在使用RestTemplate请求服务时使用的URI：http://serviceName/path/to/service，通过getHost拿到服务名称serviceName。LoadBalancer有两个：RibbonLoadBalancerClient和BlockingLoadBalancerClient。这里只说RibbonLoadBalancerClient。

LoadBalancerInterceptor中的intercept方法，最终调用的是RibbonLoadBalancerClient的execute方法：

```java
	public <T> T execute(String serviceId, LoadBalancerRequest<T> request, Object hint) throws IOException {
        // 拿到负载均衡器的实现
        ILoadBalancer loadBalancer = this.getLoadBalancer(serviceId);
        // 拿到具体的Server
        Server server = this.getServer(loadBalancer, hint);
        if (server == null) {
            throw new IllegalStateException("No instances available for " + serviceId);
        } else {
            RibbonLoadBalancerClient.RibbonServer ribbonServer = new RibbonLoadBalancerClient.RibbonServer(serviceId, server, this.isSecure(server, serviceId), this.serverIntrospector(serviceId).getMetadata(server));
            return this.execute(serviceId, (ServiceInstance)ribbonServer, (LoadBalancerRequest)request);
        }
    }
	protected Server getServer(ILoadBalancer loadBalancer, Object hint) {
        return loadBalancer == null ? null : loadBalancer.chooseServer(hint != null ? hint : "default");
    }
```

对于chooseServer是接口ILoadBalancer的方法，这里就先看一下其中的一个实现BaseLoadBalancer

```java
	public Server chooseServer(Object key) {
        if (this.counter == null) {
            this.counter = this.createCounter();
        }

        this.counter.increment();
        if (this.rule == null) {
            return null;
        } else {
            try {
                return this.rule.choose(key);
            } catch (Exception var3) {
                logger.warn("LoadBalancer [{}]:  Error choosing server for key {}", new Object[]{this.name, key, var3});
                return null;
            }
        }
    }
```

最后是通过：`rule.choose(key)`拿到Server，而rule就是IRule。

在`RibbonClientConfiguration`中初始化了上面表格提到几个核心类

1. 初始化ribbonRule: **ZoneAvoidanceRule**
2. 初始化ribbonPing：**DummyPing**
3. 初始化ribbonServerList：**ConfigurationBasedServerList**
4. 初始化ServerListUpdater：**new PollingServerListUpdater(config)**
5. 初始化ILoadBalancer：**ZoneAwareLoadBalancer**
6. 初始化ribbonServerListFilter：**ZonePreferenceServerListFilter**
7. 初始化ribbonLoadBalancerContext：**RibbonLoadBalancerContext**
8. 初始化serverIntrospector：**DefaultServerIntrospector**

**关于BlockingLoadBalancerClient：**

Spring Cloud Hoxton.RELEASE 版本发布之后，新增了一个新的负载均衡器实现`BlockingLoadBalancerClient`。它是第一个包含`阻塞式`和`非阻塞式`负载均衡器客户端实现的版本，作为已进入维护模式的Netflix Ribbon的替代方案。

如果想在 `RestTemplate`使用新的 `BlockingLoadBalancerClient`, 需要增加 `spring-cloud-loadbalancer` 的依赖，否则默认使用`RibbonLoadBalancerClient`。