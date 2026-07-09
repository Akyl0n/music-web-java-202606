package com.yinyu.service;

import com.yinyu.config.KafkaTopicConfig;
import com.yinyu.event.CacheInvalidationEvent;
import com.yinyu.event.ContentChangedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InfrastructureEventPublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(InfrastructureEventPublisher.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final boolean kafkaEnabled;

    public InfrastructureEventPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${yinyu.kafka.enabled:false}") boolean kafkaEnabled
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.kafkaEnabled = kafkaEnabled;
    }

    public void publishContentChanged(String contentType, Long contentId, String action) {
        ContentChangedEvent event = ContentChangedEvent.of(contentType, contentId, action);
        send(KafkaTopicConfig.TOPIC_SEARCH_INDEX, contentType + ":" + contentId, event);
    }

    public void publishCacheInvalidation(List<String> cacheNames) {
        if (cacheNames == null || cacheNames.isEmpty()) {
            return;
        }
        CacheInvalidationEvent event = CacheInvalidationEvent.of(cacheNames);
        send(KafkaTopicConfig.TOPIC_CACHE_INVALIDATION, "cache", event);
    }

    private void send(String topic, String key, Object event) {
        if (!kafkaEnabled) {
            return;
        }
        try {
            kafkaTemplate.send(topic, key, objectMapper.writeValueAsString(event));
        } catch (Exception ex) {
            LOGGER.warn("Kafka event publish failed, topic={}, key={}", topic, key, ex);
        }
    }
}
