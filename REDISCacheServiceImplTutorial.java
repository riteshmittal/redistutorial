package com.aem.community.core.services;

import java.util.Optional;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.Designate;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Component(service = REDISCacheService.class, immediate = true)
@Designate(ocd = RedisConfiguration.class)
public class REDISCacheServiceImplTutorial implements REDISCacheServiceTutorial {
	public static final String SEPARATOR_PREFIX = ":";
	private static final String OK = "OK";
	private static JedisPool connectionPool;
	private RedisConfiguration config;
	private String host;
	private int port;
	private int maxPool;

	@Activate
	protected void activate(RedisConfiguration config) {
		this.config = config;
		initializeVars(config);
	}

	private void initializeVars(RedisConfiguration config) {
		host = config.host();
		port = config.port();
		maxPool = config.maxPool();

		JedisPoolConfig poolConfig = new JedisPoolConfig();
		poolConfig.setMaxTotal(maxPool);
		if (Optional.ofNullable(connectionPool).isPresent()) {
			connectionPool.destroy();
		}
		connectionPool = new JedisPool(poolConfig, host, port); // for password use the other other constructor passing
																// password as param
	}

	protected Jedis getConnection() {
		return connectionPool.getResource();
	}

	protected void closeConnection(Jedis connection) {
		if (checkConnection(connection)) {
			connection.close();
		}
	}

	protected String fullKey(String prefix, String key) {
		return prefix + SEPARATOR_PREFIX + key;
	}

	public String get(String prefix, String key) {
		Jedis connection = null;
		try {
			connection = getConnection();
			if (checkConnection(connection)) {
				return connection.get(fullKey(prefix, key));
			}
		} finally {
			closeConnection(connection);
		}
		return null;
	}

	public boolean set(String prefix, String key, String value, Integer ttl) {
		Jedis connection = null;
		try {
			connection = getConnection();
			if (checkConnection(connection)) {
				if (Optional.ofNullable(ttl).isPresent() && ttl.intValue() > 0) {
					return OK.equals(connection.setex(fullKey(prefix, key), ttl.intValue(), value));
				} else {
					return OK.equals(connection.set(fullKey(prefix, key), value));
				}
			}
		} finally {
			closeConnection(connection);
		}
		return false;
	}

	private boolean checkConnection(Jedis connection) {
		return Optional.ofNullable(connection).isPresent();
	}
}