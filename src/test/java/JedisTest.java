import org.junit.jupiter.api.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class JedisTest {

    private static JedisPool pool = new JedisPool("localhost",3123);

    @Test
    public void confDataTest(){
        new JedisPool();
    }

    @Test
    public void addDataTest(){
        Jedis resource = pool.getResource();
        resource.append("xx1","xx");
        resource.close();
    }

    @Test
    public void getDataTest(){
        Jedis resource = pool.getResource();
        String xx = resource.get("xx1");
        System.out.println(xx);
    }
}
