package com.github.ncdhz.redis.util.cache;

import com.github.ncdhz.redis.handler.RedisCommand;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author majunlong
 */
public class RedisNetData implements RedisData {

    private Object data;

    private RedisCommand command;

    private List<List<String[]>> hostAndUrl;

    public RedisNetData(Object data, RedisCommand command, List<List<String[]>> hostAndUrl) {
        this.data = data;
        this.command = command;
        this.hostAndUrl = new CopyOnWriteArrayList<>(hostAndUrl);
    }

    @Override
    public Object getData() {
        return data;
    }




    @Override
    public List<List<String[]>> getHostAndUrl() {
        return hostAndUrl;
    }
}
