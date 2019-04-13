import com.github.ncdhz.redis.cache.RedisDataCache;
import com.github.ncdhz.redis.net.RedisNet;
import com.github.ncdhz.redis.net.RedisNetConf;

import java.util.LinkedHashMap;

public class RedisPoolTest {


    public static void main(String[] args){
        RedisNetConf conf = new RedisNetConf()
                .set("redis.net.url","localhost:8001|localhost:9001|116.208.95.229:6379");
        RedisNet redisNet = conf.getRedisNet();

        for (int i = 0; i < 1000; i++) {
            redisNet.set("xx"+i,i+"");
        }
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LinkedHashMap<String, Object> redisDataCache2 = RedisDataCache.getRedisDataCache2();
        RedisDataCache.getRedisDataCache1();

        redisNet.close();

    }
}
