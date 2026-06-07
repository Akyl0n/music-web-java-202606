package com.yinyu.entity.enums;

import lombok.Getter;

@Getter
public enum UserActionTypeEnum {

    LIKE_SONG("like_song"),
    FAVORITE_PLAYLIST("favorite_playlist"),
    PLAY_SONG("play_song");

    private final String code;

    UserActionTypeEnum(String code) {
        this.code = code;
    }
}
