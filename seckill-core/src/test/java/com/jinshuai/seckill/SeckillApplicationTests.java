package com.jinshuai.seckill;

import com.alibaba.fastjson.JSON;
import com.jinshuai.seckill.account.dao.UserDao;
import com.jinshuai.seckill.account.entity.User;
import com.jinshuai.seckill.account.service.UserService;
import com.jinshuai.seckill.controller.SecKillController;
import com.jinshuai.seckill.order.dao.OrderDao;
import com.jinshuai.seckill.order.entity.Order;
import com.jinshuai.seckill.product.dao.ProductDao;
import com.jinshuai.seckill.product.entity.Product;
import com.jinshuai.seckill.service.SecKillService;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SeckillApplicationTests {

    @Autowired
    private ProductDao productDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private SecKillController secKillController;

    @Autowired
    private SecKillService secKillService;

    @Autowired
    private UserService userService;

    @Test
    public void contextLoads() {
        User user = userDao.getUserById(1);
        Product product = productDao.getProductById(1);
        Timestamp ts = new Timestamp(new DateTime().getMillis());
        Order order = new Order(user, product, ts, "");
        orderDao.createOrder(order);
    }

    @Test
    public void createTestSQL() {
        StringBuilder stringBuilder = new StringBuilder();
        String init = "INSERT INTO `user`(username,phone) VALUES ('jinshuai','13622155400');";
        stringBuilder.append(init + "\n");
        for (int i = 0; i < 300000; i++) {
            stringBuilder.append(init + "\n");
        }
        File file = new File("D:/insertUser.sql");
        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(stringBuilder.toString());
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    @Test
    public void createRobot() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 300000; i > 0; i--) {
            String userId = ((int) (Math.random() * 300000)) + ",";
            String productId = ((int) (Math.random() * 10)) % 5 + "";
            stringBuilder.append(userId + productId + "\n");
        }
        File file = new File("D:/robot.txt");
        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(stringBuilder.toString());
            fileWriter.flush();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    @Autowired
    private JedisPool jedisPool;

    @Test
    public void testJedis() {
        Jedis jedis = jedisPool.getResource();
//		String s = jedis.set("name","靳帅");
//		System.out.println(s);
//		s = jedis.set("name","靳帅");
//		System.out.println( "s again = " + s);
        //System.out.println(jedis.get("name"));
        // 将用户Id和商品Id作为集合中唯一元素
//		String itemKey = "itemKey";
//		String isExist = jedis.get(itemKey);
//		jedis.select(0);
//		if (isExist == null) {
//			jedis.set(itemKey,"1");
//			jedis.expire(itemKey,1200);
//		} else if (Integer.valueOf(isExist) > 5) {
//			throw new SecKillException(StatusEnum.FREQUENCY_REQUEST);
//		} else  {
//			jedis.incr(itemKey);
//		}

        for (int i = 0; i < 10; i++) {
            System.out.println(jedis.sismember("orderUUID_Test", String.valueOf(i)));
        }
        for (int i = 0; i < 10; i++) {
            jedis.sadd("orderUUID_Test", String.valueOf(i));
        }


    }

    @Test
    public void testGson() {
        User user = new User();
        user.setId(1);
        Product product = new Product();
        product.setId(1);
        Order order = new Order(user, product, null, null);
        order.setId(1234);
        order.setProduct(product);
        order.setUser(user);
        String jsonStr = JSON.toJSONString(order);
        System.out.println(jsonStr);
        System.out.println(JSON.parseObject(jsonStr, Order.class));
    }

}