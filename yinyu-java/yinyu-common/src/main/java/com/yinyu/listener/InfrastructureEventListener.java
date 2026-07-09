package com.yinyu.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yinyu.config.KafkaTopicConfig;
import com.yinyu.event.CacheInvalidationEvent;
import com.yinyu.event.ContentChangedEvent;
import com.yinyu.search.SearchIndexService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "yinyu.kafka", name = "enabled", havingValue = "true")
public class InfrastructureEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(InfrastructureEventListener.class);
    private static final String ACTION_DELETE = "delete";

    private final ObjectMapper objectMapper;
    private final SearchIndexService searchIndexService;
    private final CacheManager cacheManager;

    public InfrastructureEventListener(ObjectMapper objectMapper, SearchIndexService searchIndexService, CacheManager cacheManager) {
        this.objectMapper = objectMapper;
        this.searchIndexService = searchIndexService;
        this.cacheManager = cacheManager;
    }

    @KafkaListener(topics = KafkaTopicConfig.TOPIC_SEARCH_INDEX, groupId = "yinyu-search-index")
    public void handleSearchIndexEvent(String payload) {
        try {
            ContentChangedEvent event = objectMapper.readValue(payload, ContentChangedEvent.class);
            if (ACTION_DELETE.equalsIgnoreCase(event.getAction())) {
                searchIndexService.deleteContent(event.getContentType(), event.getContentId());
                return;
            }
            searchIndexService.syncContent(event.getContentType(), event.getContentId());
        } catch (Exception ex) {
            LOGGER.warn("Handle search index event failed, payload={}", payload, ex);
        }
    }

    @KafkaListener(topics = KafkaTopicConfig.TOPIC_CACHE_INVALIDATION, groupId = "yinyu-cache-invalidation")
    public void handleCacheInvalidationEvent(String payload) {
        try {
            CacheInvalidationEvent event = objectMapper.readValue(payload, CacheInvalidationEvent.class);
            for (String cacheName : event.getCacheNames()) {
                Cache cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    cache.clear();
                }
            }
        } catch (Exception ex) {
            LOGGER.warn("Handle cache invalidation event failed, payload={}", payload, ex);
        }
    }
}
