k3s是经过认证的轻量级Kubernetes发行版本，k3s易于安装，使用内存减半，所有的二进制文件大小小于50mb。k3s适用于以下场景：

* 边缘
* IoT
* CI
* ARM

# K3s是什么?

K3s完全兼容Kubernetes，而且在以下方面进行了增强：

- 使用内嵌的SQLite数据库作为默认的数据存储取代原来的etcd。支持PostfreSQL、MySQL和etcd等外部存储。

- 增加了许多简单但强大的功能，比如本地存储，负载均衡器，Helm控制器和Tracefik ingress控制器。

- 所有Kubernetes控制平面组件的操作都封装在单个二进制文件和进程中。这使K3可以自动化和管理复杂的群集操作，例如分发证书。

- 内嵌的云供应商provider和存储插件被移除。

- 最小化外部依赖（仅依赖一个内核和cgroup挂载），k3s打包依赖以下组件：

  - containerd-行业标准的容器运行时，着重于简单性，健壮性和可移植性。

    ![](../image/architecture.png)

  - [Flannel](https://github.com/coreos/flannel)

  - [CoreDNS](https://coredns.io/)

  - 主机工具 (iptables, socat, etc)

## K3s架构

### 单体架构

一个K3s Server节点并且使用内嵌SQLite 数据库的单体架构如下图所示

![](../image/singleServer.png)

在该模式下所有的代理节点都向同一个k3s Server节点注册，通过K3s Server操作k8s的资源。

### 高可用架构

* 两个以上的K3s Server节点
* 外部数据存储（内嵌SQLite、PostgreSQL 10.7-11.5、MySQL 5.7、MariaDB 10.3.20、etcd 3.3.15、内嵌DQLite）

![](../image/k3s-architecture-ha-server.png)

访问k3s的API需要通过一个load Balancer做负载均衡。

### 代理节点固定地址注册

在高可用架构模式下，代理节点必须使用Kubernetes的API向固定的地址注册，如下图所示：

![](../image/k3s-production-setup.svg)

节点注册成功后会和其中一个k3s Server节点直接建立连接。



### 节点注册工作流程

1、使用k3s agent建立的websocket连接注册，websocket是k3s agent进程的一部分，并又客户端侧的负载均衡器管理

2、节点使用集群密钥和为该节点随机生成的密码向k3s注册（密码存储在**/etc/rancher/node/password**）

3、k3s在**/var/lib/rancher/k3s/server/cred/node-passwd** 存储每个节点的密码，并在节点注册时进行验证。

4、如果节点的**/etc/rancher/node** 目录被删除，需要重新生成密码或者从集群完全移除该节点。

5、可以为节点名附加一个唯一的ID

## 安装

### 安装要求

k3s非常的轻量级，但是有一些下面描述的最基本的要求。不管k3s运行在Docker或者Kubernetes中，每一个运行k3s的节点必须满足下面的最低条件。如果你有更高的要求，则需要提供更多的资源。

#### 先决条件

两个节点的主机名不能相同，如果所有的节点都有相同的主机名，可以**--with-node-id** 选项给主机名增加一个随机的后缀，或者使用**--node-name**  **或$K3S_NODE_NAME** 为添加到集群的每一个节点增加一个唯一的名字。

#### 操作系统

K3几乎可以在任何Linux版本上运行。 但是，K3s已在以下操作系统及其后续非主要版本中进行了测试。

* Ubuntu 16.04 (amd64)
* Ubuntu 18.04 (amd64)

#### 硬件

硬件需求根据部署的规格大小进行扩展，下面是最低的硬件要求：

* 内存: 512MB
* CPU: 1 核

##### 磁盘

k3s的性能依赖于数据库的性能。为了优化性能，在可能的条件下推荐使用SSD。在使用SD卡或eMMC的ARM设备上，磁盘性能会有所不同。

#### 网络

集群节点需要能够访问K3s Server的6443端口。

当使用Flannel VXLAN时，节点之间需要使用UDP 8472端口来彼此访问。节点不能监听任何其他端口。K3使用反向隧道，以便节点与服务器建立外出连接，所有kubelet流量都通过该隧道。如果使用自定义的CNI而不是Flannel，K3s并不需要8472端口。

如果要使用指标服务器，则需要在每个节点上打开端口10250。

| 协议 | 端口  | 来源                       | 描述                            |
| :--- | :---- | :------------------------- | :------------------------------ |
| TCP  | 6443  | K3s server nodes           | Kubernetes API                  |
| UDP  | 8472  | K3s server and agent nodes | Required only for Flannel VXLAN |
| TCP  | 10250 | K3s server and agent nodes | kubelet                         |

#### 大集群

硬件需求依赖于K3s集群的大小，对于生产环境和大集群，推荐使用高可用配置并且使用外部数据库。下面是生产环境推荐使用的外部数据库：

- MySQL
- PostgreSQL
- etcd

**CPU和内存**

下面是不同规模K3s Server节点的最小配置规格：

| 部署大小 | 管理节点数 | VCPUS | RAM   |
| :------- | :--------- | :---- | :---- |
| Small    | 最多 10    | 2     | 4 GB  |
| Medium   | 最多 100   | 4     | 8 GB  |
| Large    | 最多 250   | 8     | 16 GB |
| X-Large  | 最多 500   | 16    | 32 GB |
| XX-Large | 500+       | 32    | 64 GB |

**磁盘** 

K3s集群的性能依赖于数据库的性能。为了确保最佳速度，我们建议始终使用SSD磁盘来备份K3s群集。在云场景，你可能想使用允许最大IOPS的最小磁盘大小。

**网络** 

您应该考虑增加群集CIDR的子网大小，以免Pod的IP耗尽。可以在K3s Server启动时，使用`--cluster-cidr`配置指定CIDR的大小。

**数据库**

K3s支持包括MySQL, PostgreSQL, MariaDB, 和etcd在内的不同数据库。下面是K3s集群大小对应的数据库资源：

| 部署大小 | 管理节点数 | VCPUS | RAM   |
| :------- | :--------- | :---- | :---- |
| Small    | 最多 10    | 1     | 2 GB  |
| Medium   | 最多 100   | 2     | 8 GB  |
| Large    | 最多 250   | 4     | 16 GB |
| X-Large  | 最多 500   | 8     | 32 GB |
| XX-Large | 500+       | 16    | 64 GB |

### 安装选项

#### 安装脚本选项

使用脚本安装k3s最精简的命令:

```sh
curl -sfL https://get.k3s.io | sh -
```

当使用上面的方式安装k3s时，可以使用下面的环境变量配置：

| ENVIRONMENT VARIABLE            | DESCRIPTION                                                  |
| :------------------------------ | :----------------------------------------------------------- |
| `INSTALL_K3S_SKIP_DOWNLOAD`     | If set to true will not download K3s hash or binary.         |
| `INSTALL_K3S_SYMLINK`           | By default will create symlinks for the kubectl, crictl, and ctr binaries if the commands do not already exist in path. If set to ‘skip’ will not create symlinks and ‘force’ will overwrite. |
| `INSTALL_K3S_SKIP_START`        | If set to true will not start K3s service.                   |
| `INSTALL_K3S_VERSION`           | Version of K3s to download from Github. Will attempt to download the latest version if not specified. |
| `INSTALL_K3S_BIN_DIR`           | Directory to install K3s binary, links, and uninstall script to, or use `/usr/local/bin` as the default. |
| `INSTALL_K3S_BIN_DIR_READ_ONLY` | If set to true will not write files to `INSTALL_K3S_BIN_DIR`, forces setting `INSTALL_K3S_SKIP_DOWNLOAD=true`. |
| `INSTALL_K3S_SYSTEMD_DIR`       | Directory to install systemd service and environment files to, or use `/etc/systemd/system` as the default. |
| `INSTALL_K3S_EXEC`              | Command with flags to use for launching K3s in the service. If the command is not specified, and the `K3S_URL` is set, it will default to “agent.” If `K3S_URL` not set, it will default to “server.” For help, refer to [this example.](https://rancher.com/docs/k3s/latest/en/installation/install-options/how-to-flags/#example-b-install-k3s-exec) |
| `INSTALL_K3S_NAME`              | Name of systemd service to create, will default to ‘k3s’ if running k3s as a server and ‘k3s-agent’ if running k3s as an agent. If specified the name will be prefixed with ‘k3s-’. |
| `INSTALL_K3S_TYPE`              | Type of systemd service to create, will default from the K3s exec command if not specified. |

以K3S_开头的环境变量将被保留，以供systemd和openrc服务使用。

在未显式设置exec命令的情况下设置K3S_URL会将命令默认为“代理”。运行代理时，还必须设置K3S_TOKEN。

#### Installing K3s from the Binary

As stated, the installation script is primarily concerned with configuring K3s to run as a service. If you choose to not use the script, you can run K3s simply by downloading the binary from our [release page](https://github.com/rancher/k3s/releases/latest), placing it on your path, and executing it. The K3s binary supports the following commands:

| COMMAND       | DESCRIPTION                                                  |
| :------------ | :----------------------------------------------------------- |
| `k3s server`  | Run the K3s management server, which will also launch Kubernetes control plane components such as the API server, controller-manager, and scheduler. |
| `k3s agent`   | Run the K3s node agent. This will cause K3s to run as a worker node, launching the Kubernetes node services `kubelet` and `kube-proxy`. |
| `k3s kubectl` | Run an embedded [kubectl](https://kubernetes.io/docs/reference/kubectl/overview/) CLI. If the `KUBECONFIG` environment variable is not set, this will automatically attempt to use the config file that is created at `/etc/rancher/k3s/k3s.yaml` when launching a K3s server node. |
| `k3s crictl`  | Run an embedded [crictl](https://github.com/kubernetes-sigs/cri-tools/blob/master/docs/crictl.md). This is a CLI for interacting with Kubernetes’s container runtime interface (CRI). Useful for debugging. |
| `k3s ctr`     | Run an embedded [ctr](https://github.com/projectatomic/containerd/blob/master/docs/cli.md). This is a CLI for containerd, the container daemon used by K3s. Useful for debugging. |
| `k3s help`    | Shows a list of commands or help for one command             |

The `k3s server` and `k3s agent` commands have additional configuration options that can be viewed with `k3s server --help` or `k3s agent --help`.

#### Registration Options for the K3s Server

For details on configuring the K3s server, refer to the [server configuration reference.](https://rancher.com/docs/k3s/latest/en/installation/install-options/server-config)

#### Registration Options for the K3s Agent

For details on configuring the K3s agent, refer to the [agent configuration reference.](https://rancher.com/docs/k3s/latest/en/installation/install-options/agent-config)

### 网络选项

默认K3s使用flannel作为默认的CNI，使用VXLAN作为默认的后端。

#### Flannel Options

flannel的后端默认为VXLAN。通过IPSec或WireGuard选项可以开启加密功能。

如果使用WireGuard作为flannel的后端，这可能需要更多的内核模块。可以从[WireGuard 安装指导](https://www.wireguard.com/install/) 获取详细的细节。WireGuard的安装步骤确保依赖的内核模块最终都安装到操作系统内。需要在每一个节点，包括server和代理节点安装WireGuard。 

| CLI FLAG AND VALUE            | DESCRIPTION                                                  |
| :---------------------------- | :----------------------------------------------------------- |
| `--flannel-backend=vxlan`     | 默认使用VXLAN作为后端                                        |
| `--flannel-backend=ipsec      | 使用IPSEC作为后端以支持网络加密                              |
| `--flannel-backend=host-gw`   | 使用host-gw                                                  |
| `--flannel-backend=wireguard` | 使用WireGuard作为后端以支持网络加密。这可能需要其他内核模块。 |

#### 自定义CNI

Run K3s with `--flannel-backend=none` and install your CNI of choice. IP Forwarding should be enabled for Canal and Calico. Please reference the steps below.

**CANAL**

Visit the [Project Calico Docs](https://docs.projectcalico.org/) website. Follow the steps to install Canal. Modify the Canal YAML so that IP forwarding is allowed in the container_settings section, for example:

```
"container_settings": {
              "allow_ip_forwarding": true
          }
```

Apply the Canal YAML.

Ensure the settings were applied by running the following command on the host:

```
cat /etc/cni/net.d/10-canal.conflist
```

You should see that IP forwarding is set to true.

**CALICO**

Follow the [Calico CNI Plugins Guide](https://docs.projectcalico.org/master/reference/cni-plugin/configuration). Modify the Calico YAML so that IP forwarding is allowed in the container_settings section, for example:

```
"container_settings": {
              "allow_ip_forwarding": true
          }
```

Apply the Calico YAML.

Ensure the settings were applied by running the following command on the host:

```
cat /etc/cni/net.d/10-calico.conflist
```

You should see that IP forwarding is set to true.

### High Availability with an External DB

------

> **NOTE:**Official support for installing Rancher on a Kubernetes cluster was introduced in our v1.0.0 release.

This section describes how to install a high-availability K3s cluster with an external database.

Single server clusters can meet a variety of use cases, but for environments where uptime of the Kubernetes control plane is critical, you can run K3s in an HA configuration. An HA K3s cluster is comprised of:

- Two or more **server nodes** that will serve the Kubernetes API and run other control plane services
- Zero or more **agent nodes** that are designated to run your apps and services
- An **external datastore** (as opposed to the embedded SQLite datastore used in single-server setups)
- A **fixed registration address** that is placed in front of the server nodes to allow agent nodes to register with the cluster

For more details on how these components work together, refer to the [architecture section.](https://rancher.com/docs/k3s/latest/en/architecture/#high-availability-with-an-external-db)

Agents register through the fixed registration address, but after registration they establish a connection directly to one of the server nodes. This is a websocket connection initiated by the `k3s agent` process and it is maintained by a client-side load balancer running as part of the agent process.

#### Installation Outline

Setting up an HA cluster requires the following steps:

1. [Create an external datastore](https://rancher.com/docs/k3s/latest/en/installation/ha/#1-create-an-external-datastore)
2. [Launch server nodes](https://rancher.com/docs/k3s/latest/en/installation/ha/#2-launch-server-nodes)
3. [Configure the fixed registration address](https://rancher.com/docs/k3s/latest/en/installation/ha/#3-configure-the-fixed-registration-address)
4. [Join agent nodes](https://rancher.com/docs/k3s/latest/en/installation/ha/#4-optional-join-agent-nodes)

#### 1. Create an External Datastore

You will first need to create an external datastore for the cluster. See the [Cluster Datastore Options](https://rancher.com/docs/k3s/latest/en/installation/datastore/) documentation for more details.

#### 2. Launch Server Nodes

K3s requires two or more server nodes for this HA configuration. See the [Installation Requirements](https://rancher.com/docs/k3s/latest/en/installation/installation-requirements/) guide for minimum machine requirements.

When running the `k3s server` command on these nodes, you must set the `datastore-endpoint` parameter so that K3s knows how to connect to the external datastore.

For example, a command like the following could be used to install the K3s server with a MySQL database as the external datastore:

```
curl -sfL https://get.k3s.io | sh -s - server \
  --datastore-endpoint="mysql://username:password@tcp(hostname:3306)/database-name"
```

The datastore endpoint format differs based on the database type. For details, refer to the section on [datastore endpoint formats.](https://rancher.com/docs/k3s/latest/en/installation/datastore/#datastore-endpoint-format-and-functionality)

To configure TLS certificates when launching server nodes, refer to the [datastore configuration guide.](https://rancher.com/docs/k3s/latest/en/installation/datastore/#external-datastore-configuration-parameters)

> **NOTE:**The same installation options available to single-server installs are also available for high-availability installs. For more details, see the [Installation and Configuration Options](https://rancher.com/docs/k3s/latest/en/installation/install-options/) documentation.

By default, server nodes will be schedulable and thus your workloads can get launched on them. If you wish to have a dedicated control plane where no user workloads will run, you can use taints. The `node-taint` parameter will allow you to configure nodes with taints, for example `--node-taint k3s-controlplane=true:NoExecute`.

Once you’ve launched the `k3s server` process on all server nodes, ensure that the cluster has come up properly with `k3s kubectl get nodes`. You should see your server nodes in the Ready state.

#### 3. Configure the Fixed Registration Address

Agent nodes need a URL to register against. This can be the IP or hostname of any of the server nodes, but in many cases those may change over time. For example, if you are running your cluster in a cloud that supports scaling groups, you may scale the server node group up and down over time, causing nodes to be created and destroyed and thus having different IPs from the initial set of server nodes. Therefore, you should have a stable endpoint in front of the server nodes that will not change over time. This endpoint can be set up using any number approaches, such as:

- A layer-4 (TCP) load balancer
- Round-robin DNS
- Virtual or elastic IP addresses

This endpoint can also be used for accessing the Kubernetes API. So you can, for example, modify your [kubeconfig](https://kubernetes.io/docs/concepts/configuration/organize-cluster-access-kubeconfig/) file to point to it instead of a specific node. To avoid certificate errors in such a configuration, you should install the server with the `--tls-san YOUR_IP_OR_HOSTNAME_HERE` option. This option adds an additional hostname or IP as a Subject Alternative Name in the TLS cert, and it can be specified multiple times if you would like to access via both the IP and the hostname.

#### 4. Optional: Join Agent Nodes

Because K3s server nodes are schedulable by default, the minimum number of nodes for an HA K3s server cluster is two server nodes and zero agent nodes. To add nodes designated to run your apps and services, join agent nodes to your cluster.

Joining agent nodes in an HA cluster is the same as joining agent nodes in a single server cluster. You just need to specify the URL the agent should register to and the token it should use.

```
K3S_TOKEN=SECRET k3s agent --server https://fixed-registration-address:6443
```



## 集群访问

kubeconfig文件用于配置对Kubernetes集群的访问。 为了访问Kubernetes API（例如使用kubectl）或使用Helm安装应用程序，必须正确设置它。 可以通过导出KUBECONFIG环境变量或为kubectl和helm指定标志来设置kubeconfig。 有关详细信息，请参见以下示例。

使用KUBECONFIG 环境变量：

~~~
export KUBECONFIG=/etc/rancher/k3s/k3s.yaml
kubectl get pods --all-namespaces
helm ls --all-namespaces
~~~

每个命令指定配置文件路径：

~~~
kubectl --kubeconfig /etc/rancher/k3s/k3s.yaml get pods --all-namespaces
helm --kubeconfig /etc/rancher/k3s/k3s.yaml ls --all-namespaces
~~~

## 升级

### 手动升级

**使用安装脚本**

从旧版本升级，直接运行启动脚本

~~~
curl -sfL https://get.k3s.io | sh -
~~~

更新到指定版本

~~~
curl -sfL https://get.k3s.io | INSTALL_K3S_VERSION=vX.Y.Z-rc1 sh -
~~~

**使用二进制文件手动升级*8

1. 下载升级版本的二进制文件
2. 拷贝二进制文件到 `/usr/local/bin/k3s` 或其他自定义路劲
3. 停止旧版本k3s
4. 运行心版本k3s

### 自动升级

[v1.17.4+k3s1](https://github.com/rancher/k3s/releases/tag/v1.17.4%2Bk3s1) 版本开始支持

使用Rancher’s system-upgrade-controller升级k3s集群。

## 卷和存储

### 本地存储

K3s附带了Rancher的Local Path Provisioner，这提供了使用节点本地存储创建持久卷开箱即用的能力。

#### pvc.yaml

```yml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: local-path-pvc
  namespace: default
spec:
  accessModes:
    - ReadWriteOnce
  storageClassName: local-path
  resources:
    requests:
      storage: 2Gi
```

#### pod.yaml

```yml
apiVersion: v1
kind: Pod
metadata:
  name: volume-test
  namespace: default
spec:
  containers:
  - name: volume-test
    image: nginx:stable-alpine
    imagePullPolicy: IfNotPresent
    volumeMounts:
    - name: volv
      mountPath: /data
    ports:
    - containerPort: 80
  volumes:
  - name: volv
    persistentVolumeClaim:
      claimName: local-path-pvc
```

使用kubectl命令创建资源：

```shell
kubectl create -f pvc.yaml
kubectl create -f pod.yaml
```

查询资源：

```
kubectl get pv
kubectl get pvc
```

### 使用Longhorn

## 网络

### 开放端口

6443、8472和10250

### CoreDNS

每个代理启动时都默认部署CoreDNS，禁用CoreDNS可以使用 `--no-deploy coredns`命令。如果不部署CoreDNS你需要自己部署一个集群范围内的DNS.

### Traefik Ingress Controller

Traefik是一个简化微服务部署的现代化的HTTP反向代理和负载均衡器。在设计，部署和运行应用程序时，它简化了网络复杂性。Traefik在Server启动时默认部署，Traefik的默认配置文件在 `/var/lib/rancher/k3s/server/manifests/traefik.yaml` ，对该文件的修改都会自动部署到k8s，效果类似于`kubectl apply`。Traefik使用主机的80，443和8080端口。使用`--no-deploy traefik` 可以关闭Traefik服务。

### Service Load Balancer

K3s提供了一个使用可用主机端口的默认负载均衡器。如果你尝试创建一个监听80端口的负载均衡器，k3s将在集群内寻找80端口可用的主机，如果没有找到则负载均衡器会一直处于pending状态。可以使用`--no-deploy servicelb`关闭创建默认的负载均衡器。如果你需要设置其他的负载均衡器，这是非常有必要的。

## Helm

K3s从*v1.17.0+k3s.1* 版本开始支持Helm3。Helm是Kubernetes首选的软件包管理工具。Helm的charts为Kubernetes YAML提供了模板语法。借助Helm，我们可以创建可配置的部署，而不仅仅是使用静态文件。K3s使用Helm3 无需其他任何配置。

### Deploying Manifests and Helm Charts

 `/var/lib/rancher/k3s/server/manifests` 目录下的所有文件都会被自动部署到Kubernetes，行为和`kubectl apply` 类似。

K3s支持使用CRD控制器安装charts，下面是一个简单的yaml描述文件：

```yaml
apiVersion: helm.cattle.io/v1
kind: HelmChart
metadata:
  name: traefik
  namespace: kube-system
spec:
  chart: stable/traefik
  set:
    rbac.enabled: "true"
    ssl.enabled: "true"
```

`metadata.namespace`的只必须始终是`kube-system`，因为K3s的deploy 控制器只监听这个namespace内的HelmChart资源。`spec.targetNamespace` 可以自定义`namespace`。

> **注意**为了让Helm的控制器知道使用哪个版本的Helm部署chart，需要使用`spec.helmVersion`指定版本。

Also note that besides `set`, you can use `valuesContent` under the `spec` directive. And it’s okay to use both of them:

```yaml
apiVersion: helm.cattle.io/v1
kind: HelmChart
metadata:
  name: grafana
  namespace: kube-system
spec:
  chart: stable/grafana
  targetNamespace: monitoring
  set:
    adminPassword: "NotVerySafePassword"
  valuesContent: |-
    image:
      tag: master
    env:
      GF_EXPLORE_ENABLED: true
    adminUser: admin
    sidecar:
      datasources:
        enabled: true
```

K3s versions `<= v0.5.0` used `k3s.cattle.io` for the API group of HelmCharts. This has been changed to `helm.cattle.io` for later versions.

### Using the Helm CRD

You can deploy a third-party Helm chart using an example like this:

```yaml
apiVersion: helm.cattle.io/v1
kind: HelmChart
metadata:
  name: nginx
  namespace: kube-system
spec:
  chart: nginx
  repo: https://charts.bitnami.com/bitnami
  targetNamespace: default
```

You can install a specific version of a Helm chart using an example like this:

```yaml
apiVersion: helm.cattle.io/v1
kind: HelmChart
metadata:
  name: stable/nginx-ingress
  namespace: kube-system
spec:
  chart: nginx-ingress
  version: 1.24.4
  targetNamespace: default
```