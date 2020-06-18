package com.santander.efx;

import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;

public class BaseIntegrationTest extends BaseTest {

	@Autowired 
	private RedisTemplate< String, String > template;
	
	@Before
	public void setUp() {
		template.execute(new RedisCallback<Void>() {
			@Override
			public Void doInRedis(RedisConnection connection) throws DataAccessException {
				connection.flushDb();
				return null;
			}
		});
	}
	
}
