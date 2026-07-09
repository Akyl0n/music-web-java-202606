package com.yinyu.search;

import java.util.List;

public record SearchPage(List<Long> ids, long total) {
}
