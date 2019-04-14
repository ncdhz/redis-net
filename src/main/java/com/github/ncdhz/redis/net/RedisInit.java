package com.github.ncdhz.redis.net;

/**
 * @author majunlong
 */
public interface RedisInit {

    /**
     * 初始化数据的密码
     * @return 初始化类
     */
    RedisInit initPassword();

    /**
     * 初始化选择哪个数据库
     * @return 初始化类
     */
    RedisInit initDatabase();


    /**
     * 初始化数据库的路径
     * @return 初始化类
     */
    RedisInit initNetUrl();
}
