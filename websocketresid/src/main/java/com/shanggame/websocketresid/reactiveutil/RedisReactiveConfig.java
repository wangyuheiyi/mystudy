package com.shanggame.websocketresid.reactiveutil;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.io.Serializable;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 响应式redis配置类
 * @author admin
 */
@Configuration
public class RedisReactiveConfig {
    @Autowired
    RedisProperties redisPropertie;

    /**
     * 哨兵配置
     * @return
     */
    @Bean
    public RedisSentinelConfiguration redisSentinelConfiguration() {
        RedisSentinelConfiguration sentinelConfig = new RedisSentinelConfiguration();
        sentinelConfig.setMaster(redisPropertie.getSentinel().getMaster());
        Set<RedisNode> sentinels = new HashSet<>();
        for(String redisSentinelNode:redisPropertie.getSentinel().getNodes()){
            String[] item = redisSentinelNode.split(":");
            String ip = item[0].trim();
            String port = item[1].trim();
            sentinels.add(new RedisNode(ip, Integer.parseInt(port)));
        }
        sentinelConfig.setSentinels(sentinels);
        sentinelConfig.setDatabase(redisPropertie.getDatabase());
        return sentinelConfig;
    }

    /**
     * lettuce 连接池配置
     *
     * @return
     */
    @Bean
    public LettucePoolingClientConfiguration lettucePoolConfig() {
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        RedisProperties.Pool lettucePool=redisPropertie.getLettuce().getPool();
        poolConfig.setMaxTotal(lettucePool.getMaxActive());
        poolConfig.setMinIdle(lettucePool.getMinIdle());
        poolConfig.setMaxIdle(lettucePool.getMaxIdle());
        poolConfig.setMaxWaitMillis(lettucePool.getMaxWait().getSeconds());
        poolConfig.setTestOnCreate(false);
        poolConfig.setTestOnReturn(false);
        /** 在获取连接的时候检查有效性, 默认false*/
        poolConfig.setTestOnBorrow(false);
        /** 在空闲时检查有效性, 默认false*/
        poolConfig.setTestWhileIdle(false);
        /** 每次逐出检查时 逐出的最大数目 如果为负数就是 : 1/abs(n), 默认3*/
        poolConfig.setNumTestsPerEvictionRun(3);
        /** //逐出扫描的时间间隔(毫秒) 如果为负数,则不运行逐出线程, 默认-1*/
        poolConfig.setTimeBetweenEvictionRunsMillis(lettucePool.getTimeBetweenEvictionRuns().getSeconds());
        /** 逐出连接的最小空闲时间 默认1800000毫秒(30分钟)
        poolConfig.setMinEvictableIdleTimeMillis(redisPropertie.getMinEvictableIdleTimeMills());*/
        return LettucePoolingClientConfiguration.builder()
                .poolConfig(poolConfig)
                .commandTimeout(redisPropertie.getTimeout())
                .shutdownTimeout(redisPropertie.getLettuce().getShutdownTimeout())
                .build();
    }

    /**
     * lettuce 连接工厂
     *
     * @return
     */
    @Bean
    public LettuceConnectionFactory reactiveRedisConnectionFactory(@Qualifier("lettucePoolConfig") LettucePoolingClientConfiguration lettucePoolConfig,@Qualifier("redisSentinelConfiguration")RedisSentinelConfiguration redisSentinelConfiguration) {
        return   new LettuceConnectionFactory(redisSentinelConfiguration, lettucePoolConfig);
    }

    /**
     * 序列化配置
     * @param reactiveRedisConnectionFactory
     * @return
     */
    @Bean
    public ReactiveRedisTemplate<String, Object> reactiveRedisTemplate(@Qualifier("reactiveRedisConnectionFactory") LettuceConnectionFactory reactiveRedisConnectionFactory)
    {
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance,ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        jackson2JsonRedisSerializer.setObjectMapper(objectMapper);
        RedisSerializer<String> serializer = new StringRedisSerializer();
        RedisSerializationContext serializationContext = RedisSerializationContext
                .<String, Object>newSerializationContext()
                .key(serializer)
                .value(jackson2JsonRedisSerializer)
                .hashKey(serializer)
                .hashValue(jackson2JsonRedisSerializer)
                .build();
        return new ReactiveRedisTemplate<String, Object>(reactiveRedisConnectionFactory,
                serializationContext);
    }
}
