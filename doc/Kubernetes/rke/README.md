# Overview of RKE

------

Rancher Kubernetes Engine (RKE) is a CNCF-certified Kubernetes distribution that runs entirely within Docker containers. It works on bare-metal and virtualized servers. RKE solves the problem of installation complexity, a common issue in the Kubernetes community. With RKE, the installation and operation of Kubernetes is both simplified and easily automated, and it’s entirely independent of the operating system and platform you’re running. As long as you can run a supported version of Docker, you can deploy and run Kubernetes with RKE.

---
# Requirements

## 操作系统

### 一般的Linux系统

RKE可以运行在安装了Docker的大多数Linux操作系统。RKE大多数的开发和测试都是基于Ubuntu 16.04版本。但是，某些操作系统具有限制和特定要求。

- SSH user- 用于节点访问的SSH用户必须是该节点上的docker组的成员:

   ```
   usermod -aG docker <user_name>
   ```
   
- 任何node必须禁用Swap
- 必须有以下内核模块
   * `modprobe module_name`
   * `lsmod | grep module_name`
   * `grep module_name /lib/modules/$(uname -r)/modules.builtin`, if it's a built-in module
   * The following bash script

```bash
     for module in br_netfilter ip6_udp_tunnel ip_set ip_set_hash_ip ip_set_hash_net iptable_filter iptable_nat iptable_mangle iptable_raw nf_conntrack_netlink nf_conntrack nf_conntrack_ipv4   nf_defrag_ipv4 nf_nat nf_nat_ipv4 nf_nat_masquerade_ipv4 nfnetlink udp_tunnel veth vxlan x_tables xt_addrtype xt_conntrack xt_comment xt_mark xt_multiport xt_nat xt_recent xt_set  xt_statistic xt_tcpudp;
     do
       if ! lsmod | grep -q $module; then
         echo "module $module is not present";
       fi;
     done
```

| Module name            |      |
| ---------------------- | ---- |
| br_netfilter           |      |
| ip6_udp_tunnel         |      |
| ip_set                 |      |
| ip_set_hash_ip         |      |
| ip_set_hash_net        |      |
| iptable_filter         |      |
| iptable_nat            |      |
| iptable_mangle         |      |
| iptable_raw            |      |
| nf_conntrack_netlink   |      |
| nf_conntrack           |      |
| nf_conntrack_ipv4      |      |
| nf_defrag_ipv4         |      |
| nf_nat                 |      |
| nf_nat_ipv4            |      |
| nf_nat_masquerade_ipv4 |      |
| nfnetlink              |      |
| udp_tunnel             |      |
| veth                   |      |
| vxlan                  |      |
| x_tables               |      |
| xt_addrtype            |      |
| xt_conntrack           |      |
| xt_comment             |      |
| xt_mark                |      |
| xt_multiport           |      |
| xt_nat                 |      |
| xt_recent              |      |
| xt_set                 |      |
| xt_statistic           |      |
| xt_tcpudp              |      |

- 必须应用以下sysctl配置：

```
net.bridge.bridge-nf-call-iptables=1
```

### Red Hat Enterprise Linux (RHEL) / Oracle Enterprise Linux (OEL) / CentOS

If using Red Hat Enterprise Linux, Oracle Enterprise Linux or CentOS, you cannot use the `root` user as [SSH user]({{<baseurl>}}/rke/latest/en/config-options/nodes/#ssh-user) due to [Bugzilla 1527565](https://bugzilla.redhat.com/show_bug.cgi?id=1527565). Please follow the instructions below how to setup Docker correctly, based on the way you installed Docker on the node.

#### Using upstream Docker
If you are using upstream Docker, the package name is `docker-ce` or `docker-ee`. You can check the installed package by executing:

```
rpm -q docker-ce
```

When using the upstream Docker packages, please follow [Manage Docker as a non-root user](https://docs.docker.com/install/linux/linux-postinstall/#manage-docker-as-a-non-root-user).

#### Using RHEL/CentOS packaged Docker
If you are using the Docker package supplied by Red Hat / CentOS, the package name is `docker`. You can check the installed package by executing:

```
rpm -q docker
```

If you are using the Docker package supplied by Red Hat / CentOS, the `dockerroot` group is automatically added to the system. You will need to edit (or create) `/etc/docker/daemon.json` to include the following:

```
{
    "group": "dockerroot"
}
```

Restart Docker after editing or creating the file. After restarting Docker, you can check the group permission of the Docker socket (`/var/run/docker.sock`), which should show `dockerroot` as group:

```
srw-rw----. 1 root dockerroot 0 Jul  4 09:57 /var/run/docker.sock
```

Add the SSH user you want to use to this group, this can't be the `root` user.

```
usermod -aG dockerroot <user_name>
```

To verify that the user is correctly configured, log out of the node and login with your SSH user, and execute `docker ps`:

```
ssh <user_name>@node
$ docker ps
CONTAINER ID        IMAGE               COMMAND             CREATED             STATUS              PORTS               NAMES
```

### Red Hat Atomic

Before trying to use RKE with Red Hat Atomic nodes, there are a couple of updates to the OS that need to occur in order to get RKE working.

#### OpenSSH version

By default, Atomic hosts ship with OpenSSH 6.4, which doesn't support SSH tunneling, which is a core RKE requirement. If you upgrade to the latest version of OpenSSH supported by Atomic, it will correct the SSH issue.

#### Creating a Docker Group

By default, Atomic hosts do not come with a Docker group. You can update the ownership of the Docker socket by enabling the specific user in order to launch RKE.

```
# chown <user> /var/run/docker.sock
```

## 软件

This section describes the requirements for Docker, Kubernetes, and SSH.

### OpenSSH

In order to SSH into each node, OpenSSH 7.0+ must be installed on each node.

### Kubernetes

Refer to the [RKE release notes](https://github.com/rancher/rke/releases) for the supported versions of Kubernetes.

### Docker

Each Kubernetes version supports different Docker versions. The Kubernetes release notes contain the [current list](https://kubernetes.io/docs/setup/release/notes/#dependencies) of validated Docker versions.

### Installing Docker

You can either follow the [Docker installation](https://docs.docker.com/install/) instructions or use one of Rancher's [install scripts](https://github.com/rancher/install-docker) to install Docker. For RHEL, please see [How to install Docker on Red Hat Enterprise Linux 7](https://access.redhat.com/solutions/3727511).

| Docker Version | Install Script                                               |      |
| -------------- | ------------------------------------------------------------ | ---- |
| 18.09.2        | <code>curl https://releases.rancher.com/install-docker/18.09.2.sh &#124; sh</code> |      |
| 18.06.2        | <code>curl https://releases.rancher.com/install-docker/18.06.2.sh &#124; sh</code> |      |
| 17.03.2        | <code>curl https://releases.rancher.com/install-docker/17.03.2.sh &#124; sh</code> |      |

### Checking the Installed Docker Version

Confirm that a Kubernetes supported version of Docker is installed on your machine, by running `docker version --format '{{.Server.Version}}'`.

```
docker version --format '{{.Server.Version}}'
17.03.2-ce
```

## Ports
{{< ports-rke-nodes >}}
{{< requirements_ports_rke >}}

If you are using an external firewall, make sure you have this port opened between the machine you are using to run `rke` and the nodes that you are going to use in the cluster.


### Opening port TCP/6443 using `iptables`

```
# Open TCP/6443 for all
iptables -A INPUT -p tcp --dport 6443 -j ACCEPT

# Open TCP/6443 for one specific IP
iptables -A INPUT -p tcp -s your_ip_here --dport 6443 -j ACCEPT
```

### Opening port TCP/6443 using `firewalld`

```
# Open TCP/6443 for all
firewall-cmd --zone=public --add-port=6443/tcp --permanent
firewall-cmd --reload

# Open TCP/6443 for one specific IP
firewall-cmd --permanent --zone=public --add-rich-rule='
  rule family="ipv4"
  source address="your_ip_here/32"
  port protocol="tcp" port="6443" accept'
firewall-cmd --reload
```

## SSH Server Configuration

Your SSH server system-wide configuration file, located at `/etc/ssh/sshd_config`, must include this line that allows TCP forwarding:

```
AllowTcpForwarding yes
```