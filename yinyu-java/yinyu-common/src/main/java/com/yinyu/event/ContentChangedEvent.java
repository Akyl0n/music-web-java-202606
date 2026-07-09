package com.yinyu.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContentChangedEvent {

    private String contentType;

    private Long contentId;

    private String action;

    private LocalDateTime occurredAt;

    public static ContentChangedEvent of(String contentType, Long contentId, String action) {
        return new ContentChangedEvent(contentType, contentId, action, LocalDateTime.now());
    }
}
