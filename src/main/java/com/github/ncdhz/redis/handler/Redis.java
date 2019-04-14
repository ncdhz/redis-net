package com.github.ncdhz.redis.handler;

public interface Redis {

    String get(String key);

    String set(String key, String value,String nxxx);

    Long exists(String ... keys);

    Boolean exists(String key);
}
