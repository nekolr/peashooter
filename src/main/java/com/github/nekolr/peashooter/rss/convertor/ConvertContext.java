package com.github.nekolr.peashooter.rss.convertor;

import lombok.Builder;

import java.util.List;

@Builder
public record ConvertContext(Long groupId,
                             String referenceId,
                             String quality,
                             String language,
                             List<Matcher> matchers) {

}
