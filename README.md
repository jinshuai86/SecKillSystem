# SecKillSystem
秒杀系统设计思路与实现

## 瓶颈
- 数据库交互

## 服务端设计思路
- 秒杀流程
  - 查库存 
  - 更新库存
  - 订单入队列
  - 创建订单到数据库
  - 返回响应
- 一般都是读多(**读库存**)写少(**创建订单**)的操作，尽量将请求拦截在系统上游。对于查询库存操作通过缓存实现，减少数据库操作

# 进度
## Finished
- [x] 通过Redis实现缓存，每次查询库存都从Redis进行查找(注意缓存雪崩和缓存穿透)
- [x] 通过Redis复制以及哨兵机制实现了缓存高可用
- [x] 使用RocketMQ作为消息队列进行流量削峰
- [x] SpringBoot(SSM)作为整个项目的框架，**个人感觉**相比SpringDataJPA，MyBatis更灵活一些
- [x] MySQL持久化数据，分别采用乐观锁、悲观锁进行并发控制并通过JMeter进行性能测试。
- [x] 结合MySQL主从复制特性，在应用层通过AOP实现了读写分离

## TODO
- [ ] 分布式集群(比如MySQL、Redis、~~SpringBoot集群、Nginx进行负载均衡~~)
- [ ] 解决MySQL主从复制带来的数据不一致性问题
- [ ] 每次只放指定数量的请求到消息队列，等处理完毕再重新拉请求入队列。