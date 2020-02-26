### 什么是Filter

Filter是request/response执行过滤任务的对象，资源可以是一个servlet或者静态资源。Filter在`doFilter` 方法中执行过滤逻辑。每个Filter都有一个FilterConfig对象，可以从FilterConfig对象获取初始化参数和ServletContext引用。

### Filter能干什么

![](https://github.com/ctlove0523/spring-samples/blob/master/filter/filter%20chain.png)

Filter工作在客户端和Servlet之间，可以对客户端request以及服务器的response进行处理，基于此Filter可以用于实现以下功能：

* 鉴权，在请求到达真正的资源前判断请求是否有操作资源的权限。
* 审计日志，记录每一个请求方便对操作审计。
* 数据压缩，响应返回客户端前压缩以降低网络消耗。
* Token解析或校验，校验header中携带的token是否合法，或者从token中解析内容。
  

### 定义Filter

定义一个Filter需要`javax.servlet.Filter` 接口，`Filter` 有如下三个方法：

* `void init(FilterConfig filterConfig)` ：Web容器会调用该方法以表明Filter已经可以使用。Filter实例化后，Web容器会调用`init()` 方法，而且保证只调用一次。Filter必须在执行过滤任务之前完成初始化。如果Filter的`init()` 方法抛出异常或超时，Web容器不会将该Filter投入使用。
* `void doFilter(ServletRequest request, ServletResponse response,FilterChain chain)` ：每次客户端请求资源，请求/响应经过过滤链时，Web容器会调用该方法对请求和响应执行过滤任务。通过`FilterChain` 可以将请求/响应传递给过滤链上的下一个Filter。`chan.doFilter()` 前面的代码用于过滤request，之后的代码过滤response，这和AOP的思想是一致的。
* `void destroy()` ：Web容器调用该方法表明该Filter不再提供服务，request/response也不会经过该Filter。通过该方法Filter可以清理使用的资源比如内存、文件句柄、线程等。 

#### 第一个Filter

下面定义一个简单的Filter，在`init()` 方法中初始化Filter的名字，在`doFilter` 方法中简单的记录Filter被调用，`destroy()` 简单的记录方法被调用。

**FirstFilter：** 

```java
public class FirstFilter implements Filter {
    private String filterName;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        if (filterConfig != null) {
            this.filterName = filterConfig.getFilterName();
        }
        System.out.println(filterName + " init finished");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        System.out.println(filterName + " filter request");
        filterChain.doFilter(servletRequest, servletResponse);
        System.out.println(filterName + " filter response");
    }


    @Override
    public void destroy() {
        System.out.println(filterName + " begin to destroy");
    }
}
```

####  注册Filter

在完成Filter的创建后，还需要将Filter注册到Web容器（添加到Filter chain）才能对request/response进行过滤。在Spring Boot中注册Filter非常简单，下面是一个简单注册Filter的样例：

~~~java
@Configuration
public class FilterConfigure {
    
    @Bean
    public FilterRegistrationBean<Filter> registrationBean() {
        FilterRegistrationBean<Filter> filter = new FilterRegistrationBean<>();
        filter.setFilter(new FirstFilter());
        filter.setName("first filter");
        return filter;
    }
}
~~~

> 其中注解`@Configuration` 和 `@Bean` 是必须的。

#### 测试Filter

启动Spring Boot并调用一个测试接口，测试接口可从[这里](https://github.com/ctlove0523/spring-samples/tree/master/filter)获取。

~~~shell
curl -i http://localhost:5230/api/filter
~~~

应用的输出如下：

~~~
first filter filter request
first filter filter response
~~~

从输出中可以看出Filter完成了初始化，Filter的名字是“first filter”。

#### Filter在Filter chain中的顺序

如果定义了多个Filter，并期望request/response可以按照设定的顺序依次经过各个Filter（例如：request需要先经过鉴权Filter，鉴权通过后再进入参数校验Filter等），这种情况如何保证Filter的执行顺序呢？在注册Filter的时候可以给每个Filter设置一个数字表示的order，值越小Filter在chain中的位置越靠前。为了严重Filter的执行顺序，我们定义第二个Filter：SecondFilter，源码可从[这里](https://github.com/ctlove0523/spring-samples/tree/master/filter)获取。然后将两个Filter添加到Web容器中：

~~~java
@Configuration
public class FilterConfigure {

    @Bean
    public FilterRegistrationBean<Filter> registrationBean() {
        FilterRegistrationBean<Filter> filter = new FilterRegistrationBean<>();
        filter.setFilter(new FirstFilter());
        filter.setName("first filter");
        filter.setOrder(1);
        return filter;
    }

    @Bean
    public FilterRegistrationBean<Filter> registerSecondBean() {
        FilterRegistrationBean<Filter> filter = new FilterRegistrationBean<>();
        filter.setFilter(new SecondFilter());
        filter.setName("second filter");
        filter.setOrder(2);
        return filter;
    }
}
~~~

启动Spring Boot并调用一个测试接口，测试接口可从[这里](https://github.com/ctlove0523/spring-samples/tree/master/filter)获取。

~~~shell
curl -i http://localhost:5230/api/filter
~~~

应用的输出如下：

~~~
first filter filter request
second filter filter request
second filter filter response
first filter filter response
~~~

request依次经过first filter -> second filter，response依次经过second filter - > first filter，Filter的执行顺序满足我们的期望。

### 样例

通过上面学习的知识，我们实现一个鉴权的Filter。假设客户端访问资源时需要在请求的header中携带两个参数：user和password（生成环境携带账号密码是十分危险的，应该考虑基于Token的鉴权），如果有一个参数没有携带则返回客户端错误的请求（400 Bad Request），如果user和password不匹配或系统不存在用户则返回无权访问 （403 Forbidden）。客户端每成功一次，系统都会记录用户的访问次数。

Filter的实现如下：

~~~java
@Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        // filter request
        System.out.println(filterName + " filter request");
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String userName = httpServletRequest.getHeader("user");
        String password = httpServletRequest.getHeader("password");
        boolean userMatch = userService.userMatch(userName, password);
        if (userName == null || password == null) {
            HttpServletResponse httpServletResponse = (HttpServletResponse) response;
            httpServletResponse.setStatus(HttpStatus.BAD_REQUEST.value());
            return;
        }
        if (!userMatch) {
            HttpServletResponse httpServletResponse = (HttpServletResponse) response;
            httpServletResponse.setStatus(HttpStatus.FORBIDDEN.value());
            return;
        }

        chain.doFilter(request, response);

        // filter response
        System.out.println(filterName + " filter response");
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        if (httpServletResponse.getStatus() == HttpStatus.OK.value()) {
            userService.view(userName);
        }
    }
~~~

过滤步骤：

* 从request中获取user和password
* 检查user和password是为空，为空则直接返回
* 检查user和password是否和系统用户匹配，不匹配则直接返回
* 调用下一个Filter
* 检查response是否成功，成功则记录客户端访问。

完整的源码可以从[这里](https://github.com/ctlove0523/spring-samples/tree/master/filter)获取。
