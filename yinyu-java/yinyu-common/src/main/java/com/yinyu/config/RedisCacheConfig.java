package com.yinyu.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisCacheConfig {

    public static final String CACHE_HOME_PAGE = "home:page";
    public static final String CACHE_RANKING_BOARDS = "ranking:boards";
    public static final String CACHE_RANKING_DETAIL = "ranking:detail";
    public static final String CACHE_DICT_TREE = "dict:tree";
    public static final String CACHE_DICT_TYPES = "dict:types";
    public static final String CACHE_DICT_ITEMS = "dict:items";

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultConfig)
                .withCacheConfiguration(CACHE_HOME_PAGE, defaultConfig.entryTtl(Duration.ofMinutes(10)))
                .withCacheConfiguration(CACHE_RANKING_BOARDS, defaultConfig.entryTtl(Duration.ofMinutes(10)))
                .withCacheConfiguration(CACHE_RANKING_DETAIL, defaultConfig.entryTtl(Duration.ofMinutes(10)))
                .withCacheConfiguration(CACHE_DICT_TREE, defaultConfig.entryTtl(Duration.ofHours(2)))
                .withCacheConfiguration(CACHE_DICT_TYPES, defaultConfig.entryTtl(Duration.ofHours(2)))
                .withCacheConfiguration(CACHE_DICT_ITEMS, defaultConfig.entryTtl(Duration.ofHours(2)))
                .transactionAware()
                .build();
    }
}
