package com.github.ncdhz.redis.util.cache;

import java.util.List;

public interface RedisData {

    Object getData();

    List<List<String[]>> getHostAndUrl();
}
