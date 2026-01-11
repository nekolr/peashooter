package com.github.nekolr.peashooter.rss.convertor;

import lombok.Builder;

import java.util.List;

/**
 * 进行转换时的上下文
 *
 * @param groupId     分组 id
 * @param referenceId 剧集 id
 * @param quality     质量
 * @param language    语言
 * @param matchers    匹配器列表
 */
@Builder
public record ConvertContext(Long groupId,
                             String referenceId,
                             String quality,
                             String language,
                             List<Matcher> matchers) {

}
