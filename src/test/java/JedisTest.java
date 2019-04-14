import org.junit.jupiter.api.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class JedisTest {

    private static JedisPool pool = new JedisPool(new JedisPoolConfig(),"localhost",8001,2000,null,2);

    @Test
    public void confDataTest(){
        new JedisPool();
        new JedisPool();
    }

    @Test
    public void addDataTest(){
        Jedis resource = pool.getResource();
        resource.set("xx1","xx111");
        resource.close();
        pool.close();
    }

    @Test
    public void getDataTest(){
        Jedis resource = pool.getResource();
        String xx = resource.get("xx1");
        System.out.println(xx);
        resource.close();
        pool.close();
    }

    @Test
    public void pingTest(){
        Jedis resource = pool.getResource();
        String ping = resource.ping();
        System.out.println(ping);
        resource.close();
        pool.close();
    }
    public static void main(String[] args){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Jedis resource = pool.getResource();
                System.out.println(resource);
            }
        }).start();
    }
}
