package com.github.ncdhz.redis.handler;


/**
 * @author majunlong
 */

public enum RedisCommand {
    /**
     * redis 的常用命令
     */
    SET,
    SETNX,
    SETXX,
    GET,
    EXISTS;
}