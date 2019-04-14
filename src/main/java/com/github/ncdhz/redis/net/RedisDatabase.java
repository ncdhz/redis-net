package com.github.ncdhz.redis.net;

public interface RedisDatabase {

    void setHostAndPort(String host,Integer port);

    void setDatabase(Integer database);

    void setPassword(String password);

    String getHost();

    Integer getPort();

    Integer getDatabase();

    String getPassword();

    String[] getHostAndPort();
}
