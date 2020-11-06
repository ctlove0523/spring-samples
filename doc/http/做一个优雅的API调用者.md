代码量越少越优雅，实现越简单越优雅，下面介绍如何优雅的实现API调用。

## 1 单资源访问

下面以华为云IoT平台提供的查询订阅API为例说明不同方式调用单资源API的异同。订阅API的详细信息可以从[这里](https://support.huaweicloud.com/api-iothub/iot_06_v5_0076.html)获取。API的简单定义如下：

| 请求方法 | GET                                                  |
| -------- | ---------------------------------------------------- |
| URI      | /v5/iot/{project_id}/subscriptions/{subscription_id} |
| 传输协议 | HTTPS                                                |

### 1.1 API的常规调用方式：

~~~java
// 第一步构建client
OkHttpClient client = new OkHttpClient.Builder()
		.build();

// 第二步构建请求
Request request = new Request.Builder()
		.url("https://iotda.cn-north-4.myhuaweicloud.com//v5/iot/project_id/subscriptions/subscription_id")
		.get()
		.addHeader("X-Auth-Token","token")
		.build();

// 第三步调用请求
Response response = client.newCall(request).execute();

// 第四步处理响应
System.out.println(response);
~~~

### 1.2 API调用的四个步骤：

1、构建一个HTTP/HTTPS client，client可共享。

2、构建HTTP request，不同的API对应不同的request，每个API都需要构建独立的HTTP Request，request在不同API之间不可共享。

3、发送请求并获取响应，这部分逻辑由client负责，应用代码无需处理。

4、响应处理，主要工作就是反序列化，反序列化的代码可以共享。

##### 是否有改进的地方？

* HTTP/HTTPS client的构建不可缺少，步骤一不存在改进的地方（说的是简化代码方面的改进）。

* HTTP/HTTPS Request必不可少，但是构建的方法可以更加优雅，更加简单，是一个改进点。
* 请求发送由client负责，不存在改进的地方。
* 响应处理必不可少，但是响应的反序列化工作可以更简单优雅，减少自研代码量。

找到了优化的地方，就可以行动了，光说不干，用于会止步不前。Wait！Wait！Wait！难道我们是第一个想优化API调用的人吗？当然不是，square已经提供了一个优雅的解决方案，那就是retrofit项目。下面使用retrofit重构上面的API调用，看看retrofit的效果如何？

### 1.3 Retrofit重写API调用

##### 引入retrofit

**MAVEN** 

~~~xml
<dependency>
  <groupId>com.squareup.retrofit2</groupId>
  <artifactId>retrofit</artifactId>
  <version>2.8.1</version>
</dependency>
<dependency>
  <groupId>com.squareup.retrofit2</groupId>
  <artifactId>converter-jackson</artifactId>
  <version>2.8.1</version>
</dependency>
~~~

**GRADLE**（不会GRADLE都无法在社区混了，建议大家学习一下）

~~~kotlin
implementation 'com.squareup.retrofit2:retrofit:2.8.1'
implementation 'com.squareup.retrofit2:converter-jackson:2.8.1'
~~~

##### 定义API接口：

~~~java
public interface IoTSubscriptionService {
    @GET("/v5/iot/{project_id}/subscriptions/{subscription_id}")
    Call<SubscriptionDTO> getSubscription(@Header("X-Auth-Token") String token,
                                          @Path("project_id") String projectId,
                                          @Path("subscription_id") String subscriptionId);
}
~~~

##### 创建`Retrofit` 实例

~~~java
Retrofit retrofit = new Retrofit.Builder()
            .client(HttpsClientFactory.getHttpsClient())
            .addConverterFactory(JacksonConverterFactory.create())
            .baseUrl(ApplicationConf.getEndpoint())
            .build();
~~~

##### 实例化API接口并调用

~~~java
IoTSubscriptionService subService = retrofit.create(IoTSubscriptionService.class);
Call<SubscriptionDTO> response = subService.getSubscription("token", "project_id", "subscriptionId");
~~~

使用Retrofit可以像调用Java的interface一样调用API，是不是有一种RPC调用风格的感觉？

**使用Retrofit和不适用Retrofit调用API的异同之处** 

* 构造HTTP Client的基本工作必不可少。

* 使用Retrofit，可以简化响应的反序列化，Retrofit提供了各种开箱即用的转换器。

* 使用Retrofit可以让URL更短（baseUrl只需要定义一次）。

* 调用风格不同，Retrofit提供一种类似Java Interface的调用风格，这对于开发人员使用更加友好，客户端代码也更加优雅。

Retrofit可以让API的调用更加简单更加优雅，下面详细介绍Retrofit的使用。

### 1.4 Retrofit

Retrofit是类型安全的HTTP client，可用于Android和Java平台。

#### 1.4.1 API声明

Retrofit通过接口方法和方法参数上的注解了解应该如何处理请求。

##### 请求方法

接口中的每个方法都必须有一个HTTP注解，注解包括请求方法和相对URL。Retrofit提供8种注解：`HTTP`, `GET`, `POST`, `PUT`, `PATCH`, `DELETE`, `OPTIONS` 和`HEAD`。资源的相对URL在注解中指定。

```java
@GET("users/list")
```

支持在URL中指定默认查询参数。

```java
@GET("users/list?sort=desc")
```

##### URL 操作

请求的URL可以使用URL上的替换块和方法的参数来动态更新。URL的替换块用`{}` 表示，对应的值必须使用注解`@Path` 指定，而且名字必须相同，这和基于Spring MVC开发API时的使用方法一致。

```java
@GET("group/{id}/users")
Call<List<User>> groupList(@Path("id") int groupId);
```

支持增加单个请求参数：

```java
@GET("group/{id}/users")
Call<List<User>> groupList(@Path("id") int groupId, @Query("sort") String sort);
```

支持通过`Map`添加多个请求参数：

```java
@GET("group/{id}/users")
Call<List<User>> groupList(@Path("id") int groupId, @QueryMap Map<String, String> options);
```

##### 请求体

使用`@Body` 注解可以将一个对象指定为HTTP请求的body。

```java
@POST("users/new")
Call<User> createUser(@Body User user);
```

默认使用`Retrofit` 实例提供的转换器对body进行转换，如果`Retrofit` 没有添加转换器，作为body体对象的类型只能是`Retrofit` 定义的`RequestBody`  类型。

##### FORM ENCODED AND MULTIPART

可以声明方法用来支持发送form-encoded和multipart数据。

当方法上有`@FormUrlEncoded` 注解时，数据会以form-encoded的形式发送。数据的每一个key-value对通过`@Field` 注解指定，其中包括名字和对应的值。

```java
@FormUrlEncoded
@POST("user/edit")
Call<User> updateUser(@Field("first_name") String first, @Field("last_name") String last);
```

方法上有 `@Multipart`  注解时，支持以Multipart 的方式发送数据，数据的每一部分用`@Part` 表示。

```java
@Multipart
@PUT("user/photo")
Call<User> updateUser(@Part("photo") RequestBody photo, @Part("description") RequestBody description);
```

Multipart 的part使用 `Retrofit` 的一个转换器转换，或者实现`RequestBody` 来自定义序列化。

##### 操作header

可以使用 `@Headers`  注解为方法增加静态header值。

```java
@Headers("Cache-Control: max-age=640000")
@GET("widget/list")
Call<List<Widget>> widgetList();
```

~~~java
@Headers({
    "Accept: application/vnd.github.v3.full+json",
    "User-Agent: Retrofit-Sample-App"
})
@GET("users/{username}")
Call<User> getUser(@Path("username") String username);
~~~

> 同名的header不会互相覆盖，而且全部包含在request中。

可以使用`@Header` 注解动态更新request的header，对应的参数必须通过`@Header` 提供。如果参数为null，则该header被忽略，否则调用参数的`toString`方法并替换header。

```java
@GET("user")
Call<User> getUser(@Header("Authorization") String authorization)
```

支持通过`Map` 提供多个header：

```java
@GET("user")
Call<User> getUser(@HeaderMap Map<String, String> headers)
```

每个请求都需要添加的header或请求参数，应该通过OkHttp的拦截器实现，参考[拦截器](https://www.jianshu.com/p/b3ba00969d07).

##### 同步VS. 异步

`Call` 实例支持同步调用和异步调用，每个`Call` 实例只能被调用一次，调用`clone()` 方法可以创建一个继续使用的新的实例。在Android系统，主线程负责回调。在JVM中，执行HTTP请求的线程负责回调。

#### 1.4.2 Retrofit 配置

`Retrofit` 负责将API声明的接口转换为调用对象。默认`Retrofit` 使用系统的默认配置，但是支持自定义。

##### 转换器

默认，Retrofit 只能将HTTP的body序列化为OkHttp的`ResponseBody` 类型，而且`@Body` 只能接受`ResponseBody` 类型的参数。

向Retrofit 添加转换器可以支持其他类型的序列化和反序列化。为了使用方便Retrofit 提供了6个开箱即用的模块，这些模块都是对流行的序列化库的封装。

- [Gson](https://github.com/google/gson): `com.squareup.retrofit2:converter-gson`
- [Jackson](http://wiki.fasterxml.com/JacksonHome): `com.squareup.retrofit2:converter-jackson`
- [Moshi](https://github.com/square/moshi/): `com.squareup.retrofit2:converter-moshi`
- [Protobuf](https://developers.google.com/protocol-buffers/): `com.squareup.retrofit2:converter-protobuf`
- [Wire](https://github.com/square/wire): `com.squareup.retrofit2:converter-wire`
- [Simple XML](http://simple.sourceforge.net/): `com.squareup.retrofit2:converter-simplexml`
- [JAXB](https://docs.oracle.com/javase/tutorial/jaxb/intro/index.html): `com.squareup.retrofit2:converter-jaxb`
- Scalars (primitives, boxed, and String): `com.squareup.retrofit2:converter-scalars`

##### 自定义转换器

如果你需要交互的API使用一种Retrofit不支持的内容格式，比如YAML,txt或自定义格式，或者使用其他的库实现已有的格式，可以自定义一个转换器。如定义转换器非常的简单，只要扩展`Converter.Factory` 类，并将自定义的实例传递给Retrofit即可。



## 2 多资源访问

当检索多个资源时，服务端返回的数据量可能超过客户端的处理能力，而且单次返回的数据量太大会增加客户端和服务器的压力，增加数据传输占用的带宽。分页查询可以有效的降低服务端和客户端的压力，但是编写分页查询的代码十分的枯燥无味，尤其对于SDK的开发者。

### 2.1 分页查询的流程

以获取Kubernetes集群内所有pod为例，说明分页查询的流程，假设集群内共有1253个pods，每次查询服务端最多返回500个pods：

1、查询集群内所有的pods，每次获取500 pods

```
GET /api/v1/pods?limit=500
---
200 OK
Content-Type: application/json
{
  "kind": "PodList",
  "apiVersion": "v1",
  "metadata": {
    "resourceVersion":"10245",
    "continue": "ENCODED_CONTINUE_TOKEN",
    ...
  },
  "items": [...] // returns pods 1-500
}
```

2、继续获取500个pods

```
GET /api/v1/pods?limit=500&continue=ENCODED_CONTINUE_TOKEN
---
200 OK
Content-Type: application/json
{
  "kind": "PodList",
  "apiVersion": "v1",
  "metadata": {
    "resourceVersion":"10245",
    "continue": "ENCODED_CONTINUE_TOKEN_2",
    ...
  },
  "items": [...] // returns pods 501-1000
}
```

3、继续获取余下所有的pod

```
GET /api/v1/pods?limit=500&continue=ENCODED_CONTINUE_TOKEN_2
---
200 OK
Content-Type: application/json
{
  "kind": "PodList",
  "apiVersion": "v1",
  "metadata": {
    "resourceVersion":"10245",
    "continue": "", // continue token is empty because we have reached the end of the list
    ...
  },
  "items": [...] // returns pods 1001-1253
}
```

### 2.2 封装分页查询

从分页查询的流程可以看出，分页查询就是一次一次向服务端发送请求获取数据，直到服务端返回所有的数据。分页查询的流程和Java中的`Iterator` 迭代数据的流程十分的相似。下面考虑将分页查询封装为一个`Iterator` 对象，通过`Iterator` 的`next()` 方法不断获取服务器的数据。由于很难构造一个大的Kubernetes集群，分页查询的封装还是以IoT平台”查询设备列表“这个API为例说明。

~~~java
public interface IoTDeviceService {
    @GET("/v5/iot/{project_id}/devices?limit=50")
    Call<DevicesDTO> queryDeviceList(@Header("X-Auth-Token") String token,
                                     @Path("project_id") String projectId,
                                     @Query("app_id") String appId,
                                     @Query("marker") String marker);
}
~~~

将分页查询封装为`Iterator` ：

~~~java
private static class LazyIterator implements Iterator<Device> {
        private static final Logger log = LoggerFactory.getLogger(LazyIterator.class);
        private IoTDeviceService deviceService = IoTServiceFactory.getIoTDeviceService();
        private String appId;
        private String projectId;
        private String token;
        private List<Device> currentList = new ArrayList<>(50);
        private int pos;
        private String marker;
        private long totalCount = Long.MAX_VALUE;
        private long alreadyCount = 0L;

        LazyIterator(String appId, String projectId, String token) {
            this.appId = appId;
            this.projectId = projectId;
            this.token = token;
        }

        @Override
        public boolean hasNext() {
            return alreadyCount < totalCount;
        }

        @Override
        public Device next() {
            if (!currentList.isEmpty() && pos < currentList.size()) {
                alreadyCount++;
                return currentList.get(pos++);
            } else {
                Call<DevicesDTO> response = deviceService.queryDeviceList(token, projectId, appId, marker);
                try {
                    DevicesDTO devicesDTO = response.execute().body();
                    if (devicesDTO != null) {
                        PageDTO pageDTO = devicesDTO.getPage();
                        this.marker = pageDTO.getMarker();
                        if (totalCount == Long.MAX_VALUE) {
                            this.totalCount = pageDTO.getCount();
                        }
                        List<DeviceDTO> devices = devicesDTO.getDevices();
                        currentList.clear();
                        pos = 0;
                        devices.forEach(deviceDTO -> {
                            Device device = new Device();
                            device.setDeviceId(deviceDTO.getDeviceId());
                            device.setNodeId(deviceDTO.getNodeId());
                            device.setAppId(deviceDTO.getAppId());
                            device.setProductId(deviceDTO.getProductId());
                            currentList.add(device);
                        });
                    }
                } catch (IOException e) {
                    log.error("io exception = {}", e.toString());
                }
            }
            alreadyCount++;
            return currentList.get(pos++);
        }
    }
~~~











