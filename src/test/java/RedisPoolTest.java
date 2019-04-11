import com.github.ncdhz.redis.net.RedisNet;
import com.github.ncdhz.redis.net.RedisNetConf;

public class RedisPoolTest {

    public static void main(String[] args){
        RedisNetConf conf = new RedisNetConf()
                .set("redis.net.url","localhost:8001|localhost:9001");
        RedisNet redisNet = conf.getRedisNet();
    }
}
