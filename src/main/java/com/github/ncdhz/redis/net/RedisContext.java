package com.github.ncdhz.redis.net;

/**
 * redis的操作接口
 * @author majunlong
 */
public interface RedisContext {
    /**
     * 添加数据到redis中
     * @param key 数据的name
     * @param value 数据的具体类容
     * @return 返回插入数据的值
     */
    String set(String key, String value);

    /**
     * 存储数据到redis中，并制定过期时间和当Key存在时是否覆盖。
     * @param key 数据的key
     * @param value 数据的值
     * @param nxxx 只能是nx或者xx nx表示当数据库指定key的数据存在时不插入数据
     *            xx表示不管数据库指定key数据存不存在都插入数据（也就是存在时相当于更新数据）
     * @param expx expx的值只能取EX或者PX，代表数据过期时间的单位，EX代表秒，PX代表毫秒
     * @param time 过期时间，单位是expx所代表的单位
     * @return
     */
    String set(String key, String value, String nxxx, String expx, long time);

    String set(String key, String value, String expx, long time);

    String set(String key, String value, String nxxx);

    String set(byte[] key, byte[] value);

    String set(byte[] key, byte[] value, byte[] nxxx);

    String set(byte[] key, byte[] value, byte[] nxxx, byte[] expx, long time);

    String set(byte[] key, byte[] value, byte[] nxxx, byte[] expx,
                      final int time);

    /**
     * 通过key获取value
     */
    String get(String key);

    byte[] get(byte[] key);

    /**
     * 关闭所有redis连接
     */
    void close();
    /**
     * 判断key是否在数据库中存在
     * @param key 数据的键
     */
    Boolean exists(String key);

    /**
     * 判断有多少key在数据库中存在
     * @param keys 数据键的集合
     * @return 返回存在的个数
     */
    Long exists(String ... keys);

    /**
     * 判断key是否在数据库中存在
     * @param key 数据的键
     */
    Boolean exists(byte[] key);

    /**
     * 判断有多少key在数据库中存在
     * @param keys 数据键的集合
     * @return 返回存在的个数
     */
    Long exists(byte[] ... keys);

}
