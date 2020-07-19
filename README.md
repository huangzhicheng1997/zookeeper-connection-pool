# zookeeper-connection-pool
由于TCP协议的带宽的局限性，所以可以使用多个客户端连接来充分利用网络带宽。<br>
此项目主要是用来管理Zookeeper连接，连接的负载均衡。在zookeeper原生java客户端的基础上，抽象出ZookeeperConnection，以及ZookeeperConntionPool。<br>
用户只需在yml配置文件中配置最大连接数，以及序列化器即可使用，目前序列化器提供MessagePack以及Kryo两种方式。<br>
<br>
实现的功能：<br>
1.递归创建节点<br>
2.多个client，并发执行，以连接池形式提供资源管理<br>
3.多个client的负载均衡（引用计数法获取负载最小的客户端程序）<br>
4.提供高效序列化方式<br>
5.集成zookeeper分布式锁<br>


