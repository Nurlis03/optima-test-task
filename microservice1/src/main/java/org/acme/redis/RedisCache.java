package org.acme.redis;

import jakarta.enterprise.context.ApplicationScoped;
import redis.clients.jedis.Jedis;

@ApplicationScoped
public class RedisCache {

    private final Jedis jedis;

    public RedisCache() {
        this.jedis = new Jedis("localhost", 6379);
    }

    public void setex(String key, String value) {
        jedis.setex(key, 30, value);
    }

    public String get(String key) {
        return jedis.get(key);
    }
}
