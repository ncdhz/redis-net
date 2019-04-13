import com.github.ncdhz.redis.cache.RedisDataCache;
import com.github.ncdhz.redis.net.RedisNet;
import com.github.ncdhz.redis.net.RedisNetConf;
import com.github.ncdhz.redis.net.RedisNetContext;


public class RedisPoolTest {


    public static void main(String[] args){
        RedisNetConf conf = new RedisNetConf()
                .set("redis.net.url","localhost:8001|localhost:9001|116.208.95.229:6379");
        RedisNet redisNet = RedisNetContext.getRedisNet(conf);

        for (int i = 0; i < 1000; i++) {
            redisNet.set("xx"+i,i+"");
        }
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        RedisDataCache.getRedisDataCache1();
        RedisDataCache.getRedisDataCache2();

        redisNet.close();

    }
}
