package com.yinyu.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@ConditionalOnProperty(prefix = "yinyu.kafka", name = "enabled", havingValue = "true")
public class KafkaTopicConfig {

    public static final String TOPIC_SEARCH_INDEX = "yinyu.search.index";
    public static final String TOPIC_CACHE_INVALIDATION = "yinyu.cache.invalidation";

    @Bean
    public NewTopic searchIndexTopic() {
        return TopicBuilder.name(TOPIC_SEARCH_INDEX).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic cacheInvalidationTopic() {
        return TopicBuilder.name(TOPIC_CACHE_INVALIDATION).partitions(3).replicas(1).build();
    }
}
