# redis-net
redis.net.url localhost:8001|localhost:9001
多个数据库用,隔开

redis.database.time.out 2000 数据库默认连接超时时间

redis.database 0|0 默认为0号数据库 用于指定操作的数据库

redis.password pas|pas 默认没有

redis.bad.pool.time 1000 用于检测坏的连接连接池 

redis.good.pool.time 1000 用于检查好的连接池时间

redis.cache.time 1000 表示每秒中会向数据库写一次记录

redis.cache.data.number 5000 缓存中会默认存5000条数据