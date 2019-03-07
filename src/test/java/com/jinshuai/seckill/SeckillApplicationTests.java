package com.jinshuai.seckill;

import com.alibaba.fastjson.JSON;
import com.jinshuai.seckill.controller.SecKillController;
import com.jinshuai.seckill.dao.ISecKillDao;
import com.jinshuai.seckill.entity.Order;
import com.jinshuai.seckill.entity.Product;
import com.jinshuai.seckill.entity.User;
import com.jinshuai.seckill.enums.StatusEnum;
import com.jinshuai.seckill.exception.SecKillException;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SeckillApplicationTests {

	@Autowired
	private ISecKillDao secKillDao;

	@Autowired
	private SecKillController secKillController;

	@Test
	public void contextLoads() {
		User user = secKillDao.getUserById(1);
		Product product = secKillDao.getProductById(1);
		Timestamp ts = new Timestamp(new DateTime().getMillis());
		Order order = new Order(user,product,ts,"");
		secKillDao.createOrder(order);
	}

	@Test
	public void createTestSQL() {
		StringBuilder stringBuilder = new StringBuilder();
		String init = "INSERT INTO `user`(username,phone) VALUES ('jinshuai','13622155400');";
		stringBuilder.append(init + "\n");
		for (int i = 0; i < 1000; i++) {
			stringBuilder.append(init+"\n");
		}
		File file = new File("F:/insertUser.sql");
		try (FileWriter fileWriter = new FileWriter(file)){
            fileWriter.write(stringBuilder.toString());
        } catch (IOException e1) {
            e1.printStackTrace();
        }
	}

	@Test
	public void createRobot() {
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 1000; i > 0; i--) {
			String userId = ((int)(Math.random() * 1000)) % 943 + ",";
			String productId = ((int)(Math.random() * 10)) % 5 + "";
			stringBuilder.append(userId + productId + "\n");
		}
		File file = new File("F:/robot.txt");
		try (FileWriter fileWriter = new FileWriter(file)){
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
		String itemKey = "itemKey";
		String isExist = jedis.get(itemKey);
		jedis.select(0);
		if (isExist == null) {
			jedis.set(itemKey,"1");
			jedis.expire(itemKey,1200);
		} else if (Integer.valueOf(isExist) > 5) {
			throw new SecKillException(StatusEnum.FREQUENCY_REQUEST);
		} else  {
			jedis.incr(itemKey);
		}
	}

	@Test
	public void testDao() {
		System.out.println(AopUtils.isAopProxy(secKillController));
		System.out.println(AopUtils.isAopProxy(secKillDao));
		List<Product> productList = secKillDao.getAllProducts();
		productList.forEach(System.out::println);
	}

	@Test
	public void testGson() {
		User user = new User();
		user.setId(1);
		Product product = new Product();
		product.setId(1);
		Order order = new Order(user,product,null,null);
		order.setId(1234);
		order.setProduct(product);
		order.setUser(user);
		String jsonStr = JSON.toJSONString(order);
		System.out.println(jsonStr);
		System.out.println(JSON.parseObject(jsonStr,Order.class));
	}

}