package com.github.ncdhz.redis.cache;

public interface Redis {

    Object get(String key);

    Object set(String key, Object value);

    void close();
}
