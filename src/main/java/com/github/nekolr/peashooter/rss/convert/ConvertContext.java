package com.github.nekolr.peashooter.rss.convert;

import java.util.List;

public record ConvertContext(Long groupId,
                             String referenceId,
                             String quality,
                             String language,
                             List<Matcher> matchers) {

}
