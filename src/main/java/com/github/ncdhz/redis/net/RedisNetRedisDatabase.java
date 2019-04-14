package com.github.ncdhz.redis.net;

public class RedisNetRedisDatabase implements RedisDatabase {

    private String host;

    private Integer port;

    private Integer database;

    private String password;

    public RedisNetRedisDatabase(String host, Integer port) {
        this.host = host;
        this.port = port;
    }

    public RedisNetRedisDatabase() {}


    @Override
    public void setHostAndPort(String host, Integer port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public void setDatabase(Integer database) {
        this.database = database;
    }
    @Override
    public void setPassword(String password) {
        this.password = password;
    }
    @Override
    public String getHost() {
        return host;
    }
    @Override
    public Integer getPort() {
        return port;
    }
    @Override
    public Integer getDatabase() {
        return database;
    }
    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String[] getHostAndPort() {
        return new String[]{host,port.toString()};
    }
}
