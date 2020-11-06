Kubernetes API是通过HTTP提供的基于资源的（RESTful）编程接口。Kubernetes API支持通过标准的HTTP动词（POST，PUT，PATCH，DELETE，GET）检索，创建，更新和删除主资源，包括支持细粒度授权的附加资源。基于易用性和效率可以使用不同的形式提供资源。Kubernetes API通过“watcher”和一致的lists支持资源状态变更的有效通知，以支持其他组件高效的缓存和同步资源状态。



## 标准API术语

大多数Kubernetes API资源类型都是代表集群某个概念具体实例的对象，比如pod或namespace。有一小部分API的资源类型是虚拟的，代表一种操作而不是资源，比如权限检查。为了支持幂等的创建和检索，所有对象都有一个唯一的名称，如果虚拟资源不可检索或不依赖幂等性，则可以没有唯一的名称。

Kubernetes使用标准的RESTful属于描述API概念：

* 资源类型是URL中使用的名称（pods，namespace，services）
* 所有的资源类型在JSON中都有一种具体的表示形式，这称之为Kind。
* 资源类型的实例列表称为集合
* 资源类型的单个实例称为资源

所有的资源类型只能归属于集群（/apis/GROUP/VERSION/*）或namespace（/api/GROUP/VERSION/namespace/NAMESPACE/\*）内。删除namespace会同时删除namespace内的所有资源，对namespace内资源的访问受namespace的权限控制。下面是Kubernetes检索资源集合和资源的通用URL：

* 集群范围内资
  * `GET /apis/GROUP/VERSION/RESOURCETYPE` 获取资源集合
  * `GET /apis/GROUP/VERSION/RESOURCETYPE/NAME` 获取指定名称的资源
* namespace范围内资
  * `GET /apis/GROUP/VERSION/RESOURCETYPE` - 获取所有namespace内的资源集合
  * `GET /apis/GROUP/VERSION/namespaces/NAMESPACE/RESOURCETYPE` - 获取指定namespace内的资源集合
  * `GET /apis/GROUP/VERSION/namespaces/NAMESPACE/RESOURCETYPE/NAME` - 获取指定资源

namespace本身就是集群范围内的一种资源，因此可以使用 `GET /api/v1/namespaces`获取集群内所有的namespace，并可以使用 `GET /api/v1/namespaces/NAME`获取指定namespace的详细信息。

Kubernetes API使用HTTP的GET表示检索单个资源，使用自定义的list表示检索资源集合。

Kubernetes使用URL中的子path表示资源的一个或多个子资源，URL的形式如下：

- 集群范围内资源： `GET /apis/GROUP/VERSION/RESOURCETYPE/NAME/SUBRESOURCE`
- namespace范围内资源： `GET /apis/GROUP/VERSION/namespaces/NAMESPACE/RESOURCETYPE/NAME/SUBRESOURCE`



## 高效的检测变化

Kubernetes为了支持客户端建立集群当前状态的模型，Kubernetes所有的资源都支持一致的list和称为watch的增量变更通知。Kubernetes的所有资源都有一个标识存储在底层数据的版本：resourceVersion。检索资源集合时，服务端的响应包括一个resourceVersion值，可以使用resourceVersion发起针对服务端的监听。服务的会返回所有在resourceVersion之后的所有变化。这使得客户端可以获取当前的状态并监听所有的变化而不会错过任何更新。如果客户端的watch断链，客户可以基于最近一次服务端返回的resourceVersion发起watch，或者从头开始发起watch（需要再次获取资源集合）。

下面是来官网的一个样例（未做任何修改）:

1. 获取指定namesapce内的所有pods

   ```
   GET /api/v1/namespaces/test/pods
   ---
   200 OK
   Content-Type: application/json
   {
     "kind": "PodList",
     "apiVersion": "v1",
     "metadata": {"resourceVersion":"10245"},
     "items": [...]
   }
   ```

2. 从10245版本开始监听所有的pod创建，删除和更新。

   ```
   GET /api/v1/namespaces/test/pods?watch=1&resourceVersion=10245
   ---
   200 OK
   Transfer-Encoding: chunked
   Content-Type: application/json
   {
     "type": "ADDED",
     "object": {"kind": "Pod", "apiVersion": "v1", "metadata": {"resourceVersion": "10596", ...}, ...}
   }
   {
     "type": "MODIFIED",
     "object": {"kind": "Pod", "apiVersion": "v1", "metadata": {"resourceVersion": "11020", ...}, ...}
   }
   ...
   ```

说明：Kubernetes服务器仅保留有限时间内的变更列表。 默认情况下，Kubernetes使用etcd3集群保留最近5分钟的更改。当watch请求操作由于历史版本不可用导致失败时，客户端必须通过410 Gone状态码识别，清除其本地缓存，执行列表操作以及从该返回的resourceVersion开始watch来处理失败的情况。 对于这种错误处理，多数的客户端都提供了对应的工具。

Kubernetes为了减轻短历史窗的影响，引入了bookmark 事件。这是一种特殊的事件，用于标记对客户端请求的给定resourceVersion的所有更新都已发送。bookmark事件中仅设置resourceVersion。 

## 分块检索大型结果集

支持对于较大的结果集分块返回客户端，这和分页查询类似。
On large clusters, retrieving the collection of some resource types may result in very large responses that can impact the server and client. For instance, a cluster may have tens of thousands of pods, each of which is 1-2kb of encoded JSON. Retrieving all pods across all namespaces may result in a very large response (10-20MB) and consume a large amount of server resources. Starting in Kubernetes 1.9 the server supports the ability to break a single large collection request into many smaller chunks while preserving the consistency of the total request. Each chunk can be returned sequentially which reduces both the total size of the request and allows user-oriented clients to display results incrementally to improve responsiveness.

在大型集群，获取某些类型的资源集合时服务端可能产生大量的数据，这会影响服务端和客户端（网络带宽占用，内存占用等）。例如，一个集群内的pod数量可能有数万个，以JSON编码，每个pod的数据大小约1-2kb。获取所有namespace下的所有pod产生的响应可能约10-20MB，这会消耗大量的服务器资源。Kubernetes从1.9版本开始支持以分块的形式将一个大的集合响应以多个块的形式返回客户端，同时保证整体的一致性。每个块都可以按顺序返回，这既减少了请求的总大小，又使面向用户的客户端可以递增地显示结果以提高响应速度。

To retrieve a single list in chunks, two new parameters `limit` and `continue` are supported on collection requests and a new field `continue` is returned from all list operations in the list `metadata` field. A client should specify the maximum results they wish to receive in each chunk with `limit` and the server will return up to `limit` resources in the result and include a `continue` value if there are more resources in the collection. The client can then pass this `continue` value to the server on the next request to instruct the server to return the next chunk of results. By continuing until the server returns an empty `continue` value the client can consume the full set of results.

为了以块的形式检索单个列表，获取集合的请求支持增加两个新的参数`limit` 和`continue` ，所有的响应在`metadata` 中增加返回`continue` 字段。客户可以通过`limit` 设置每块返回的最大数据量，服务器的返回的响应包含的最大数据量为`limit`，如果还有更多的数据需要返回同时返回`continue`。客户端通过传输continue字段通知服务器返回下一个块的数据。循环往复直到服务器返回所有数据。





Like a watch operation, a `continue` token will expire after a short amount of time (by default 5 minutes) and return a `410 Gone` if more results cannot be returned. In this case, the client will need to start from the beginning or omit the `limit` parameter.

就像watch操作一样，`continue`令牌将在短时间（默认为5分钟）后过期。如果服务器无法返回更多结果，则返回`410 Gone`。 在这种情况下，客户端将需要从头开始或省略`limit`参数。

例如：集群有1253个pod，客户端期望一次返回500个pod，客户端需要三次调用才能获得所有的pod：

1. 获取集群所有的pod，每次获取500个pod：

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

2. 继续向服务端发送请求，获取接下来的500个pod

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

3. 继续向服务器请求，获取剩下的253 个pods.

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

Note that the `resourceVersion` of the list remains constant across each request, indicating the server is showing us a consistent snapshot of the pods. Pods that are created, updated, or deleted after version `10245` would not be shown unless the user makes a list request without the `continue` token. This allows clients to break large requests into smaller chunks and then perform a watch operation on the full set without missing any updates.

三次请求中的`resourceVersion` 都一致，服务保证了数据的一致性。10245版本之后pod的创建、更新和删除不会返回给客户端，除非客户端不使用`continue` 请求。这允许客户端将较大的请求分解为较小的块，然后对整个集合执行监视操作，而不会丢失任何更新。

## 以表的形式接受资源

`kubectl get` is a simple tabular representation of one or more instances of a particular resource type. In the past, clients were required to reproduce the tabular and describe output implemented in `kubectl` to perform simple lists of objects. A few limitations of that approach include non-trivial logic when dealing with certain objects. Additionally, types provided by API aggregation or third party resources are not known at compile time. This means that generic implementations had to be in place for types unrecognized by a client.

`kubectl get` 得到的结果是一种或多种特定资源类型实例的简单表格形式。在之前的版本，客户端被要求重现表格而且要描述`kubectl` 中的输出以实现简单的对象列表。该方法有一些局限性，比如处理特定对象时没有平凡的逻辑。此外，在编译期不知道API聚合或第三方资源的类型。这意味着对于客户端，在遇到无法识别的类型时，需要替换通用实现。

In order to avoid potential limitations as described above, clients may request the Table representation of objects, delegating specific details of printing to the server. The Kubernetes API implements standard HTTP content type negotiation: passing an `Accept` header containing a value of `application/json;as=Table;g=meta.k8s.io;v=v1beta1` with a `GET` call will request that the server return objects in the Table content type.

为了避免上述的潜在限制，客户端可以请求以表的形式表示对象，毕竟表填充的细节委托给服务端。Kubernetes API实现了标准的HTTP内容类型协商：通过GET调用传递包含`application / json; as = Table; g = meta.k8s.io; v = v1beta1`值的消息头，将请求服务器以表格的形式返回对象的内容。

下面是一个简单的例子:

1. 以表的形式查询集群内所有的pods

   ```
   GET /api/v1/pods
   Accept: application/json;as=Table;g=meta.k8s.io;v=v1beta1
   ---
   200 OK
   Content-Type: application/json
   {
       "kind": "Table",
       "apiVersion": "meta.k8s.io/v1beta1",
       ...
       "columnDefinitions": [
           ...
       ]
   }
   ```

For API resource types that do not have a custom Table definition on the server, a default Table response is returned by the server, consisting of the resource’s `name` and `creationTimestamp` fields.

对于服务器上没有自定义表定义的API资源类型，服务器将返回默认的表响应，该响应由资源的名称和creationTimestamp字段组成。

对于在服务端没有自定义表的API资源类型，服务端以默认的形式返回资源（包括`name`和`creationTimestamp` 字段）。

```
    GET /apis/crd.example.com/v1alpha1/namespaces/default/resources
    ---
    200 OK
    Content-Type: application/json
    ...
    {
        "kind": "Table",
        "apiVersion": "meta.k8s.io/v1beta1",
        ...
        "columnDefinitions": [
            {
                "name": "Name",
                "type": "string",
                ...
            },
            {
                "name": "Created At",
                "type": "date",
                ...
            }
        ]
    }
```

Table responses are available beginning in version 1.10 of the kube-apiserver. As such, not all API resource types will support a Table response, specifically when using a client against older clusters. Clients that must work against all resource types, or can potentially deal with older clusters, should specify multiple content types in their `Accept` header to support fallback to non-Tabular JSON:

```
Accept: application/json;as=Table;g=meta.k8s.io;v=v1beta1, application/json
```

kube-apiserver从1.10版本开始支持以表的形式返回响应。尽管如此，但是不是所有类型的API资源都支持以表的形式返回，尤其客户端对接的是老版本集群时。客户端必须可以处理所有的资源类型也能够和老板的集群交互，必须在HTTP的请求头中添加`Accept` 以支持降级到常规的JSON格式：

~~~
Accept: application/json;as=Table;g=meta.k8s.io;v=v1beta1, application/json
~~~



## 支持更改资源表示形式

Kubernetes默认以JSON编码返回结果，但是支持使用Protobuf 编码返回结果。

默认Kubernetes返回的对象以JSON序列化，而且JSON内容的类型为`application/json` 。JSON是Kubernetes API默认的序列化方式。为实现在大规模场景下更好的性能，客户端可以要求Kubernetes以更高效的Protobuf表示返回的对象。Kubernetes API实现了标准的HTTP内容类型协商，`GET` 请求在HTTP头中增加`Accept` 请求头以要求服务端以指定的类型返回对象，但是使用`PUT` 或`POST` 向服务器发送Protobuf表示的对象时，需要增加`Content-Type` 消息头。如果服务端支持客户端传递的类型，在响应头中也会返回`Content-Type` ，否则返回`406 Not acceptable` 以通知客户端类型不支持。

> 可以参考API以获取每个API支持的内容类型。

下面是几个来自官网的样例:

1. 以Protobuf格式查询集群内所有的pods。

   ```
   GET /api/v1/pods
   Accept: application/vnd.kubernetes.protobuf
   ---
   200 OK
   Content-Type: application/vnd.kubernetes.protobuf
   ... binary encoded PodList object
   ```

2. 以Protobuf编码的数据创建pod，但是要求服务端返回JSON编码的响应。

   ```
   POST /api/v1/namespaces/test/pods
   Content-Type: application/vnd.kubernetes.protobuf
   Accept: application/json
   ... binary encoded Pod object
   ---
   200 OK
   Content-Type: application/json
   {
     "kind": "Pod",
     "apiVersion": "v1",
     ...
   }
   ```

并非所有的API资源类型都支持Protobuf，尤其是自定义的资源类型和扩展API。必须处理所有资源类型的客户端应在其Accept标头中指定多种内容类型，以支持回退到JSON：

```
Accept: application/vnd.kubernetes.protobuf, application/json
```

### Protobuf encoding

Kubernetes会使用信封包装器对Protobuf响应包装，包装器以4字节的魔术开头，用于在磁盘或ETCD中表示内容为Protobuf编码（和JSON编码相对），魔术之后Protobuf编码的包装器消息，该消息描述底层对象的编码和类型和对象。

包装后的消息格式如下:

```
A four byte magic number prefix:
  Bytes 0-3: "k8s\x00" [0x6b, 0x38, 0x73, 0x00]

An encoded Protobuf message with the following IDL:
  message Unknown {
    // typeMeta should have the string values for "kind" and "apiVersion" as set on the JSON object
    optional TypeMeta typeMeta = 1;

    // raw will hold the complete serialized object in protobuf. See the protobuf definitions in the client libraries for a given kind.
    optional bytes raw = 2;

    // contentEncoding is encoding used for the raw data. Unspecified means no encoding.
    optional string contentEncoding = 3;

    // contentType is the serialization method used to serialize 'raw'. Unspecified means application/vnd.kubernetes.protobuf and is usually
    // omitted.
    optional string contentType = 4;
  }

  message TypeMeta {
    // apiVersion is the group/version for this type
    optional string apiVersion = 1;
    // kind is the name of the object schema. A protobuf definition should exist for this object.
    optional string kind = 2;
  }
```

客户端如果以pplication/vnd.kubernetes.protobuf格式收到了服务端响应，但是格式的前缀不符合预期，此时客户端应该丢弃响应。将来的版本中可能通过更改前缀的方式进行不兼容的序列化格式改变。



## 资源删除

资源删除阶段：1）完成，2）移除

~~~json
{
  "kind": "ConfigMap",
  "apiVersion": "v1",
  "metadata": {
    "finalizers": {"url.io/neat-finalization", "other-url.io/my-finalizer"},
    "deletionTimestamp": nil,
  }
}
~~~

当客户端第一次删除资源时，`metadata.deletionTimestamp` 字段会被设置为当前时间。一旦`metadata.deletionTimestamp`  字段被设置，外部控制器就可能在任何时间点以任意的顺序执行其清理工作。顺序不会得到保证，因为这会带来 `.metadata.finalizers`阻塞的风险，因为` .metadata.finalizers` 是一个共享字段，任何拥有权限的参与者都可以对其重新排序。如果列表中的清理器按顺序执行，可能导致下面的死锁场景：第一个清理器等待后面清理器发出的信号。不指定清理器执行的顺序，清理器可以以自由的顺序执行，这也不会受清理器列表变化的影响。

## 单资源API

1、GET, CREATE, UPDATE, PATCH, DELETE 和 PROXY仅支持单个资源，这些仅支持单个资源的HTTP 动词不支持在有序列表或无序列表或事务中一起提交多个资源。包括kubectl在内的客户端会解析资源列表并仅发送单资源API请求。

2、LIST和WATCH支持获取多个资源，DELETECOLLECTION 支持删除多个资源。

## 支持Dry-run
**功能状态**: Kubernetes v1.18 stable

修改资源的操作（POST，PUT，PATCH和DELETE）可以在空运行模式下接受请求。空运行模式有助于在典型的请求阶段（许可链，验证，合并冲突）评估请求，但是不会存储任何对象到存储中。请求的响应尽可能接近非空运行响应。 Kubernetes保证空运行模式下的请求将不会持久存储在存储中或有任何其他副作用。

### 创建dry-run请求

通过设置`dryRun` 参数可以出发dry-run。`dryRun` 参数是一个字符串，而且仅支持下面的值（是一个枚举值）：

- `All:`除最后的存储阶段，其他阶段的执行和非dry-run模式一样。权限控制器检查请求是否合法，mutating 控制器修改请求，对`PATCH` 执行合并操作，默认字段，schema校验都会执行。但是变化不会存储到底层的存储系统，最终被存储的对象会返回给客户端，响应也包括正常的状态码。如果请求会出发一个权限控制器而且可能产生副作用，这样的请求将会失败而不是冒险产生副作用。Kubernetes所有内置的权限检查插件都支持dry-run模式。此外，通过将sideEffects字段设置为“ None”，准入webhook可以在其配置对象中声明它们没有副作用。如果Webhook实际上确实有副作用，则应该将sideEffects字段设置为“ NoneOnDryRun”，并且还应该修改该Webhook以了解AdmissionReview中的DryRun字段，并防止对空运行请求的副作用。
- 默认值为空，将会保留默认的修改行为。

下面是一个简单的例子：

```java
POST /api/v1/namespaces/test/pods?dryRun=All
Content-Type: application/json
Accept: application/json
```

响应和非dry-run请求类似，只是有一些字段的值可能存在差异。

### Dry-run 鉴权

dry-run和非dry-run的鉴权是一致的。因此，如果一个用户向发送dry-run请求，用户必须有非dry-run请求的权限。

下面是一个拥有对Deployments执行`PATCH` 操作的RBAC角色：

```yaml
rules:
- apiGroups: ["extensions", "apps"]
  resources: ["deployments"]
  verbs: ["patch"]
```

### 产生的值

对象的某些值通常在持久化对象之前生成。重要的是这些字段值的设置不依赖dry-run，因为这些值在dry-run模式下可能和真实的请求不同。这些字段包括：

- `name`: 如果 设置了`generateName` , `name` 将会设置为一个唯一的名字
- `creationTimestamp`/`deletionTimestamp`: 创建/删除 时间戳
- `UID`: 对象的唯一标识符，随机产生。
- `resourceVersion`: 对象版本
- mutating 权限控制器设置的所有字段
- 对于`Service`资源的Ports或IPs


## Server Side Apply 

**FEATURE STATE:** `Kubernetes v1.16` [beta](https://kubernetes.io/docs/reference/using-api/api-concepts/#)

> **Note:** Starting from Kubernetes v1.18, if you have Server Side Apply enabled then the control plane tracks managed fields for all newly created objects.

### Introduction

Server Side Apply helps users and controllers manage their resources via declarative configurations. It allows them to create and/or modify their objects declaratively, simply by sending their fully specified intent.

服务端应用程序可以帮助用户和管理员通过声明式的配置进行资源管理。支持通过简单发送操作对象的完整意图声明式的创建/修改对象。

A fully specified intent is a partial object that only includes the fields and values for which the user has an opinion. That intent either creates a new object or is [combined](https://kubernetes.io/docs/reference/using-api/api-concepts/#merge-strategy), by the server, with the existing object.

一个操作对象的完整意图是一个部分对象，该对象仅仅包含客户感兴趣的字段和值。这个完整的意图要么是创建一个新的对象，要么是由服务端将其与已有对象合并。

The system supports multiple appliers collaborating on a single object.

系统支持多个应用程序在单个对象上的协作。

This model of specifying intent makes it difficult to remove existing fields. When a field is removed from one’s config and applied, the value will be kept (the system assumes that you don’t care about that value anymore). If an item is removed from a list or a map, it will be removed if no other appliers care about its presence.

这种指定意图的模型使删除现有字段变得困难。当一个字段从系统删除时，系统依然会保存该字段的值（系统知识假设你对字段的值不感兴趣）。如果是从list或map删除一个元素，当没有任何应用关心该元素时，会真正删除该元素。

Changes to an object’s fields are tracked through a “[field management](https://kubernetes.io/docs/reference/using-api/api-concepts/#field-management)“ mechanism. When a field’s value changes, ownership moves from its current manager to the manager making the change. When trying to apply an object, fields that have a different value and are owned by another manager will result in a [conflict](https://kubernetes.io/docs/reference/using-api/api-concepts/#conflicts). This is done in order to signal that the operation might undo another collaborator’s changes. Conflicts can be forced, in which case the value will be overridden, and the ownership will be transferred.

字段管理机制会跟踪对象所有字段的修改。当一个字段的值被修改后，字段的所有权会从之前的manager转移到修改字段值的manager。在操作对象时，如果某个字段拥有一个不同的值而且该值的拥有者是其他manager，这种情况会产生冲突。采用这种机制的主要目的是表明这个操作可能覆盖其他协作应用的修改。冲突可以强制发生，在这种情况下，值将被覆盖，所有权将被转移。

It is meant both as a replacement for the original `kubectl apply` and as a simpler mechanism to write controllers.

可以作为原始`kubectl apply` 替代，也可以作为一种更简单的编写控制器的机制。

### Field Management

Compared to the `last-applied` annotation managed by `kubectl`, Server Side Apply uses a more declarative approach, which tracks a user’s field management, rather than a user’s last applied state. This means that as a side effect of using Server Side Apply, information about which field manager manages each field in an object also becomes available.

For a user to manage a field, in the Server Side Apply sense, means that the user relies on and expects the value of the field not to change. The user who last made an assertion about the value of a field will be recorded as the current field manager. This can be done either by changing the value with `POST`, `PUT`, or non-apply `PATCH`, or by including the field in a config sent to the Server Side Apply endpoint. When using Server-Side Apply, trying to change a field which is managed by someone else will result in a rejected request (if not forced, see [Conflicts](https://kubernetes.io/docs/reference/using-api/api-concepts/#conflicts)).

Field management is stored in a newly introduced `managedFields` field that is part of an object’s [`metadata`](https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.16/#objectmeta-v1-meta).

A simple example of an object created by Server Side Apply could look like this:

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: test-cm
  namespace: default
  labels:
    test-label: test
  managedFields:
  - manager: kubectl
    operation: Apply
    apiVersion: v1
    time: "2010-10-10T0:00:00Z"
    fieldsType: FieldsV1
    fieldsV1:
      f:metadata:
        f:labels:
          f:test-label: {}
      f:data:
        f:key: {}
data:
  key: some value
```

The above object contains a single manager in `metadata.managedFields`. The manager consists of basic information about the managing entity itself, like operation type, api version, and the fields managed by it.

> **Note:** This field is managed by the apiserver and should not be changed by the user.

Nevertheless it is possible to change `metadata.managedFields` through an `Update` operation. Doing so is highly discouraged, but might be a reasonable option to try if, for example, the `managedFields` get into an inconsistent state (which clearly should not happen).

The format of the `managedFields` is described in the [API](https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.16/#fieldsv1-v1-meta).

### Conflicts

A conflict is a special status error that occurs when an `Apply` operation tries to change a field, which another user also claims to manage. This prevents an applier from unintentionally overwriting the value set by another user. When this occurs, the applier has 3 options to resolve the conflicts:

- **Overwrite value, become sole manager:** If overwriting the value was intentional (or if the applier is an automated process like a controller) the applier should set the `force` query parameter to true and make the request again. This forces the operation to succeed, changes the value of the field, and removes the field from all other managers’ entries in managedFields.
- **Don’t overwrite value, give up management claim:** If the applier doesn’t care about the value of the field anymore, they can remove it from their config and make the request again. This leaves the value unchanged, and causes the field to be removed from the applier’s entry in managedFields.
- **Don’t overwrite value, become shared manager:** If the applier still cares about the value of the field, but doesn’t want to overwrite it, they can change the value of the field in their config to match the value of the object on the server, and make the request again. This leaves the value unchanged, and causes the field’s management to be shared by the applier and all other field managers that already claimed to manage it.

### Managers

Managers identify distinct workflows that are modifying the object (especially useful on conflicts!), and can be specified through the `fieldManager` query parameter as part of a modifying request. It is required for the apply endpoint, though kubectl will default it to `kubectl`. For other updates, its default is computed from the user-agent.

### Apply and Update

The two operation types considered by this feature are `Apply` (`PATCH` with content type `application/apply-patch+yaml`) and `Update` (all other operations which modify the object). Both operations update the `managedFields`, but behave a little differently.

> **Note:**
>
> Whether you are submitting JSON data or YAML data, use `application/apply-patch+yaml` as the Content-Type header value.
>
> All JSON documents are valid YAML.

For instance, only the apply operation fails on conflicts while update does not. Also, apply operations are required to identify themselves by providing a `fieldManager` query parameter, while the query parameter is optional for update operations. Finally, when using the apply operation you cannot have `managedFields` in the object that is being applied.

An example object with multiple managers could look like this:

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: test-cm
  namespace: default
  labels:
    test-label: test
  managedFields:
  - manager: kubectl
    operation: Apply
    apiVersion: v1
    fields:
      f:metadata:
        f:labels:
          f:test-label: {}
  - manager: kube-controller-manager
    operation: Update
    apiVersion: v1
    time: '2019-03-30T16:00:00.000Z'
    fields:
      f:data:
        f:key: {}
data:
  key: new value
```

In this example, a second operation was run as an `Update` by the manager called `kube-controller-manager`. The update changed a value in the data field which caused the field’s management to change to the `kube-controller-manager`.

> **Note:** If this update would have been an `Apply` operation, the operation would have failed due to conflicting ownership.

### Merge strategy

The merging strategy, implemented with Server Side Apply, provides a generally more stable object lifecycle. Server Side Apply tries to merge fields based on the fact who manages them instead of overruling just based on values. This way it is intended to make it easier and more stable for multiple actors updating the same object by causing less unexpected interference.

When a user sends a “fully-specified intent” object to the Server Side Apply endpoint, the server merges it with the live object favoring the value in the applied config if it is specified in both places. If the set of items present in the applied config is not a superset of the items applied by the same user last time, each missing item not managed by any other appliers is removed. For more information about how an object’s schema is used to make decisions when merging, see [sigs.k8s.io/structured-merge-diff](https://sigs.k8s.io/structured-merge-diff).

A number of markers were added in Kubernetes 1.16 and 1.17, to allow API developers to describe the merge strategy supported by lists, maps, and structs. These markers can be applied to objects of the respective type, in Go files or OpenAPI specs.

| Golang marker    | OpenAPI extension            | Accepted values                                              | Description                                                  | Introduced in |
| :--------------- | :--------------------------- | :----------------------------------------------------------- | :----------------------------------------------------------- | :------------ |
| `//+listType`    | `x-kubernetes-list-type`     | `atomic`/`set`/`map`                                         | Applicable to lists. `atomic` and `set` apply to lists with scalar elements only. `map` applies to lists of nested types only. If configured as `atomic`, the entire list is replaced during merge; a single manager manages the list as a whole at any one time. If `granular`, different managers can manage entries separately. | 1.16          |
| `//+listMapKeys` | `x-kubernetes-list-map-keys` | Slice of map keys that uniquely identify entries for example `["port", "protocol"]` | Only applicable when `+listType=map`. A slice of strings whose values in combination must uniquely identify list entries. | 1.16          |
| `//+mapType`     | `x-kubernetes-map-type`      | `atomic`/`granular`                                          | Applicable to maps. `atomic` means that the map can only be entirely replaced by a single manager. `granular` means that the map supports separate managers updating individual fields. | 1.17          |
| `//+structType`  | `x-kubernetes-map-type`      | `atomic`/`granular`                                          | Applicable to structs; otherwise same usage and OpenAPI annotation as `//+mapType`. | 1.17          |

### Custom Resources

By default, Server Side Apply treats custom resources as unstructured data. All keys are treated the same as struct fields, and all lists are considered atomic. If the validation field is specified in the Custom Resource Definition, it is used when merging objects of this type.

### Using Server-Side Apply in a controller

As a developer of a controller, you can use server-side apply as a way to simplify the update logic of your controller. The main differences with a read-modify-write and/or patch are the following:

- the applied object must contain all the fields that the controller cares about.
- there are no way to remove fields that haven’t been applied by the controller before (controller can still send a PATCH/UPDATE for these use-cases).
- the object doesn’t have to be read beforehand, `resourceVersion` doesn’t have to be specified.

It is strongly recommended for controllers to always “force” conflicts, since they might not be able to resolve or act on these conflicts.

### Comparison with Client Side Apply

A consequence of the conflict detection and resolution implemented by Server Side Apply is that an applier always has up to date field values in their local state. If they don’t, they get a conflict the next time they apply. Any of the three options to resolve conflicts results in the applied config being an up to date subset of the object on the server’s fields.

This is different from Client Side Apply, where outdated values which have been overwritten by other users are left in an applier’s local config. These values only become accurate when the user updates that specific field, if ever, and an applier has no way of knowing whether their next apply will overwrite other users’ changes.

Another difference is that an applier using Client Side Apply is unable to change the API version they are using, but Server Side Apply supports this use case.

### API Endpoint

With the Server Side Apply feature enabled, the `PATCH` endpoint accepts the additional `application/apply-patch+yaml` content type. Users of Server Side Apply can send partially specified objects as YAML to this endpoint. When applying a configuration, one should always include all the fields that they have an opinion about.

### Clearing ManagedFields

It is possible to strip all managedFields from an object by overwriting them using `MergePatch`, `StrategicMergePatch`, `JSONPatch` or `Update`, so every non-apply operation. This can be done by overwriting the managedFields field with an empty entry. Two examples are:

```json
PATCH /api/v1/namespaces/default/configmaps/example-cm
Content-Type: application/merge-patch+json
Accept: application/json
Data: {"metadata":{"managedFields": [{}]}}
PATCH /api/v1/namespaces/default/configmaps/example-cm
Content-Type: application/json-patch+json
Accept: application/json
Data: [{"op": "replace", "path": "/metadata/managedFields", "value": [{}]}]
```

This will overwrite the managedFields with a list containing a single empty entry that then results in the managedFields being stripped entirely from the object. Note that just setting the managedFields to an empty list will not reset the field. This is on purpose, so managedFields never get stripped by clients not aware of the field.

In cases where the reset operation is combined with changes to other fields than the managedFields, this will result in the managedFields being reset first and the other changes being processed afterwards. As a result the applier takes ownership of any fields updated in the same request.

> **Caution:** Server Side Apply does not correctly track ownership on sub-resources that don’t receive the resource object type. If you are using Server Side Apply with such a sub-resource, the changed fields won’t be tracked.

### Disabling the feature

Server Side Apply is a beta feature, so it is enabled by default. To turn this [feature gate](https://kubernetes.io/docs/reference/command-line-tools-reference/feature-gates) off, you need to include the `--feature-gates ServerSideApply=false` flag when starting `kube-apiserver`. If you have multiple `kube-apiserver` replicas, all should have the same flag setting.

## 资源版本

资源版本是一个字符串，用于标识服务器内部对象的版本。客户端可以使用资源版本检测资源何时被修改，以及在get，list和watch资源时保证数据的一致性。客户端必须透明的处理资源版本，而且传回服务器的版本不能被修改。比如，客户端不能假定资源版本为数字，而且只能比较两个资源版本相等。

### 元数据中的ResourceVersion

客户端可以从包括watch时间中的资源，服务端返回的响应列表等资源中获取资源版本（ResourceVersion）：

[v1.meta/ObjectMeta](https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.16/#objectmeta-v1-meta) -  资源的`metadata.resourceVersion` 字段表示resourceVersion。

[v1.meta/ListMeta](https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.16/#listmeta-v1-meta) -资源集合的 `metadata.resourceVersion` 字段表示resourceVersion。

### ResourceVersion 参数

get，list和watch操作支持`resrouceVersion` 参数。此参数的确切含义因操作和资源版本的值而异。

对于get和list，资源版本的语义如下：

**Get:**

| resourceVersion unset | resourceVersion is `0` | resourceVersion is set but not `0` |
| :-------------------- | :--------------------- | :--------------------------------- |
| Most Recent           | Any                    | Not older than                     |

**List:**

| paging                    | resourceVersion unset | resourceVersion=“0”                           | resourceVersion=“{value other than 0}” |
| :------------------------ | :-------------------- | :-------------------------------------------- | :------------------------------------- |
| limit unset               | Most Recent           | Any                                           | Not older than                         |
| limit=“n”, continue unset | Most Recent           | Any                                           | Exact                                  |
| limit=“n”, continue=”“    | Continue Token, Exact | Invalid, but treated as Continue Token, Exact | Invalid, HTTP `400 Bad Request`        |

get和list语义的含义是：

- **Most Recent:** Return data at the most recent resource version. The returned data must be consistent (i.e. served from etcd via a quorum read).
- **Any:** Return data at any resource version. The newest available resource version is preferred, but strong consistency is not required; data at any resource version may be served. It is possible for the request to return data at a much older resource version that the client has previously observed, particularly in high availabiliy configurations, due to partitions or stale caches. Clients that cannot tolerate this should not use this semantic.
- **Not older than:** Return data at least as new as the provided resource version. The newest available data is preferred, but any data not older than this resource version may be served. Note that this ensures only that the objects returned are no older than they were at the time of the provided resource version. The resource version in the `ObjectMeta` of individual object may be older than the provide resource version so long it is for the latest modification to the object at the time of the provided resource version.
- **Exact:** Return data at the exact resource version provided.
- **Continue Token, Exact:** Return data at the resource version of the initial paginated list call. The returned Continue Tokens are responsible for keeping track of the initially provided resource version for all paginated list calls after the initial paginated list call.

对于watch，资源版本的含义是：

**Watch:**

| resourceVersion unset              | resourceVersion=“0”        | resourceVersion=“{value other than 0}” |
| :--------------------------------- | :------------------------- | :------------------------------------- |
| Get State and Start at Most Recent | Get State and Start at Any | Start at Exact                         |

watch语义的含义如下：

- **Get State and Start at Most Recent:** Start a watch at the most recent resource version, which must be consistent (i.e. served from etcd via a quorum read). To establish initial state, the watch begins with synthetic “Added” events of all resources instances that exist at the starting resource version. All following watch events are for all changes that occurred after the resource version the watch started at.
- **Get State and Start at Any:** Warning: Watches initialize this way may return arbitrarily stale data! Please review this semantic before using it, and favor the other semantics where possible. Start a watch at any resource version, the most recent resource version available is preferred, but not required; any starting resource version is allowed. It is possible for the watch to start at a much older resource version that the client has previously observed, particularly in high availability configurations, due to partitions or stale caches. Clients that cannot tolerate this should not start a watch with this semantic. To establish initial state, the watch begins with synthetic “Added” events for all resources instances that exist at the starting resource version. All following watch events are for all changes that occurred after the resource version the watch started at.
- **Start at Exact:** Start a watch at an exact resource version. The watch events are for all changes after the provided resource version. Unlike “Get State and Start at Most Recent” and “Get State and Start at Any”, the watch is not started with synthetic “Added” events for the provided resource version. The client is assumed to already have the initial state at the starting resource version since the client provided the resource version.

### “410 Gone” 响应

Servers are not required to serve all older resource versions and may return a HTTP `410 (Gone)` status code if a client requests a resourceVersion older than the server has retained. Clients must be able to tolerate `410 (Gone)` responses. See [Efficient detection of changes](https://kubernetes.io/docs/reference/using-api/api-concepts/#efficient-detection-of-changes) for details on how to handle `410 (Gone)` responses when watching resources.

If you request a a resourceVersion outside the applicable limit then, depending on whether a request is served from cache or not, the API server may reply with a `410 Gone` HTTP response.

服务器不会返回所有老版本的资源，如果客户端请求一个比服务端保存的资源版本还老的版本，服务端会返回`410 Gone` 状态码。

如果客户端请求的资源版本超出了限制，这取决于是否从缓存处理请求，API server可能返回一个`410 Gone` 。

### 资源版本不可用

Servers are not required to serve unrecognized resource versions. List and Get requests for unrecognized resource versions may wait briefly for the resource version to become available, should timeout with a `504 (Gateway Timeout)` if the provided resource versions does not become available in a resonable amount of time, and may respond with a `Retry-After` response header indicating how many seconds a client should wait before retrying the request. Currently the kube-apiserver also identifies these responses with a “Too large resource version” message. Watch requests for a unrecognized resource version may wait indefinitely (until the request timeout) for the resource version to become available.

服务器无需为无法识别的资源版本服务。List和Get请求无法识别的资源版本时，可以简单的等待请求的资源版本可用，或者在一个合理的时间内资源依然不可用时返回`504 Gateway Timeout` ，获取在响应中增加`Retry-After` 消息头以告知客户端多少秒之后重试请求。当前版本的kube-apiserver也会将这些响应标记为“过大资源版本”。监听未识别的资源版本可能会一直等待资源可用。
