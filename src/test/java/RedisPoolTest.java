import com.github.ncdhz.redis.net.RedisContext;
import com.github.ncdhz.redis.net.RedisNetConf;
import com.github.ncdhz.redis.net.RedisNetContext;
import com.github.ncdhz.redis.net.RedisThreadPool;


public class RedisPoolTest {

    public static void main(String[] args) throws InterruptedException {
        RedisNetConf conf = new RedisNetConf();
        conf.set("redis.net.url","localhost:8001|localhost:9001")
                .set("redis.database","1|2");
        RedisThreadPool redisThreadPool = new RedisThreadPool(100);
        for (int i = 0; i < 100; i++) {
            redisThreadPool.execute(()->{
                RedisContext redisNet = RedisNetContext.getRedisNet(conf);
                Boolean xx1 = redisNet.exists("xx1");
                System.out.println(xx1);

                Long exists = redisNet.exists("xx1", "xx2");
                System.out.println(exists);

                redisNet.close();
            });
        }
    }
}
