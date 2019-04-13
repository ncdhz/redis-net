package com.github.ncdhz.redis.cache;

/**
 * @author majunlong
 */

public enum RedisCommand{
    /**
     * redis 的常用命令 set
     */
    SET,

    /**
     *
     */
    GET;

    private DataCache dataCache = RedisDataCache.getDataCache();

    public Object getData(String key){
        return dataCache.get(key);
    }

    public Object addData(String key, Object value) {
        return dataCache.set(key,value,this);
    }

}
