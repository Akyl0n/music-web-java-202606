package com.yinyu.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CacheInvalidationEvent {

    private List<String> cacheNames;

    private LocalDateTime occurredAt;

    public static CacheInvalidationEvent of(List<String> cacheNames) {
        return new CacheInvalidationEvent(cacheNames, LocalDateTime.now());
    }
}
