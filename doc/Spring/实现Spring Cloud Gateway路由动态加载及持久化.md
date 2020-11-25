​                                                                                                                                                                          

## 实现Spring Cloud Gateway路由动态加载及持久化



Spring Cloud Gateway提供了添加、删除和查询路由的[API](https://docs.spring.io/spring-cloud-gateway/docs/2.2.5.RELEASE/reference/html/#actuator-api)，通过API添加的路由默认存储在内存之中。应用重启时，通过API添加的路由会丢失，进而导致应用的功能受损。如果应用有多个实例，Spring Cloud Gateway也没有提供路由同步机制，多个实例之间的路由信息不一致，影响正常的业务。由于Spring Cloud Gateway默认实现在可靠性和一致性方面存在不足，进而无法直接部署在生产环境，为此需要自定义实现路由的存储和同步机制。

#### Spring Cloud Gateway的路由加载机制

在Spring Cloud Gateway中一个RouteDefinition对象定义一个路由。Spring Cloud Gateway启动时首先加载所有的RouteDefinition，然后生成路由并讲路由加载到内中（有缓存机制，主要目的是提供路由匹配的效率）。Spring Cloud Gateway可以从四个数据源加载RouteDefinition：配置文件，Fluent Route API，RouteDefinitionRepository和DiscoveryClient（Spring Cloud定义的服务发现接口）。从路由的加载机制可以看出，能够实现动态添加、删除、修改路由的方式就是自定义实现RouteDefinitionRepository，从而可以保证在Spring Cloud Gateway启动时加载之前已经添加过的路由。

> 应用通过代码，配置文件以及基于服务发现添加的路由无法被修改

#### Spring Cloud Gateway的路由刷新通知机制。

Spring Cloud Gateway定义了一个RouteRefreshListener用来监听路由变化事件RefreshRoutesEvent，因此在添加、删除、更新路由后只需要发布一个RefreshRoutesEvent事件以让所有存储路由的组件更新路由即可。

#### 动态路由实现

##### 实现机制

![image-20201115191642769](C:\Users\chentong\AppData\Roaming\Typora\typora-user-images\image-20201115191642769.png)

实现步骤：

* 第一步：客户端发送路由创建、删除、修改的请求到一个SCG1应用。
* 第二步：SCG1将路由信息写入到持久存储
* 第三步：SCG1通知内部组件加载新路由
* 第四步：SCG1通知SCG2路由有刷新
* 第五步：SCG2通知内部组件加载新路由









~~~java


@Component
@Slf4j
public class StorageRouteDefinitionRepository implements RouteDefinitionRepository, ApplicationEventPublisherAware {
    // 删除、添加路由时发送事件通知，让CachingRouteDefinitionLocator加载缓存
    private ApplicationEventPublisher publisher;

    @Autowired
    private DbRouteDefinitionRepository repository;

    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {
        List<RouteDefinitionPO> routeDefinitionPOS = repository.findAll();
        if (routeDefinitionPOS.isEmpty()) {
            return Flux.empty();
        }

        return Flux.fromIterable(routeDefinitionPOS)
                .map(routeDefinitionPo -> {
                    RouteDefinition definition = new RouteDefinition();
                    definition.setId(routeDefinitionPo.getRouteId());
                    definition.setFilters(routeDefinitionPo.getFilters());
                    definition.setMetadata(routeDefinitionPo.getMetadata());
                    definition.setOrder(routeDefinitionPo.getRouteOrder());
                    definition.setPredicates(routeDefinitionPo.getPredicates());
                    definition.setUri(UriComponentsBuilder.fromUriString(routeDefinitionPo.getUri()).build().toUri());
                    return definition;
                }).onErrorStop();
    }

    @Override
    public Mono<Void> save(Mono<RouteDefinition> route) {
        return route.flatMap((Function<RouteDefinition, Mono<? extends Void>>) routeDefinition -> {
            RouteDefinitionPO po = new RouteDefinitionPO();
            po.setRouteId(routeDefinition.getId());
            po.setFilters(routeDefinition.getFilters());
            po.setMetadata(routeDefinition.getMetadata());
            po.setPredicates(routeDefinition.getPredicates());
            po.setRouteOrder(routeDefinition.getOrder());
            po.setUri(routeDefinition.getUri().toString());
            repository.save(po);

            publisher.publishEvent(new RefreshRoutesEvent(this));
            return Mono.empty();
        }).doOnError(throwable -> log.error("exception ", throwable));
    }

    @Override
    public Mono<Void> delete(Mono<String> routeId) {
        return routeId.flatMap(s -> {
            repository.deleteByRouteId(s);
            publisher.publishEvent(new RefreshRoutesEvent(this));
            return Mono.empty();
        });
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.publisher = applicationEventPublisher;
    }
}
~~~

在容器加载了上面的Bean之后，Spring Cloud Gateway的API存储Route时就会调用我们自定义的save方法，实现Route的持久化存储。

### 修改路由

Spring Cloud Gateway支持添加、查询、删除路由，但是没有提供API修改路由，比如修改URI、增加删除Filter等，为了方便使用，我们下面提供修改路由的API。修改路由的API实现思路十分简单：首先修改数据库中存储的路由信息，然后发送事件通知刷新路由。下面代码是一个简单的实现：



* 第一步：自定义Endpoint。其中id不能为gateway，否则应用会因为Bean冲突而无法启动

  ~~~java
  @RestControllerEndpoint(id = "self-gateway")
  @Slf4j
  public class SelfGatewayControllerEndpoint implements ApplicationEventPublisherAware {
      private ApplicationEventPublisher publisher;
  
      @Autowired
      private DbRouteDefinitionRepository repository;
  
      @PutMapping("/routes/{id}")
      public Mono<ResponseEntity<Object>> modifyRoute(@PathVariable String id,
                                                      @RequestBody RouteDefinition routeDefinition) {
          // 省略实现细节，具体的代码可以从Github获取
          return Mono.emtpy();
      }
  
      @Override
      public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
          this.publisher = applicationEventPublisher;
      }
  }
  ~~~

* 第二步：将自定义的Endpoint注册到Spring Context

  ~~~java
  @Configuration
  public class GatewayConfig {
      @Bean
      @ConditionalOnAvailableEndpoint
      public SelfGatewayControllerEndpoint routeController() {
          return new SelfGatewayControllerEndpoint();
      }
  }
  ~~~

* 调用API修改Route

  ~~~curl
  curl -i -XPUT http://ip:port/actuator/self-gateway/routes/{id}
  ~~~

### 源码地址

文中的源代码可以从[Github](https://github.com/ctlove0523/spring-samples/tree/master/spring-cloud-gateway)获取。