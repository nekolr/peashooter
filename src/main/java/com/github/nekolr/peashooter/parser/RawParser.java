package com.github.nekolr.peashooter.parser;

import com.github.nekolr.peashooter.exception.ParseTitleException;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * reference: <a href="https://github.com/EstrellaXD/Auto_Bangumi/blob/c9c2b28389aac6ac4d778cdc7de1a77ca024b97e/auto_bangumi/parser/analyser/raw_parser.py">Auto_Bangumi Raw Parser</a>
 */
public class RawParser {

    /**
     * 标题正则表达式
     */
    private static final Pattern TITLE_PATTERN = Pattern.compile("(.*)( -? \\d+ |\\[\\d+]|\\[\\d+.?[vV]\\d{1}]|[第]\\d+[话話集]|\\[\\d+.?END])(.*)");

    /**
     * 干扰信息正则表达式
     * 例如：新番、五月番等
     */
    private static final Pattern INTERFERENCE_PATTERN1 = Pattern.compile("新番|月?番");

    /**
     * 干扰信息字符串
     */
    private static final String INTERFERENCE_REGEX2 = "（仅限港澳台地区）";

    /**
     * 季度信息正则表达式
     */
    private static final String SEASON_REGEX = "[Ss]\\d{1,2}|Season \\d{1,2}|[第].[季期]";

    /**
     * 季度信息正则表达式
     */
    private static final Pattern SEASON_PATTERN = Pattern.compile(SEASON_REGEX);

    /**
     * 一段非 ASCII 字符（如中文），后面跟着一个空白字符，再跟着至少 4 个 ASCII 字符（如英文或数字）
     */
    private static final Pattern CHINESE_ENGLISH_PATTERN = Pattern.compile("([^\\x00-\\xff]{1,})(\\s)([\\x00-\\xff]{4,})");

    /**
     * 英文字符正则表达式
     */
    private static final Pattern ENGLISH_PATTERN = Pattern.compile("[aA-zZ]{1}");

    /**
     * 视频分辨率正则表达式
     */
    private static final Pattern RESOLUTION_PATTERN = Pattern.compile("1080|720|2160|4K");

    /**
     * 来源正则表达式
     */
    private static final Pattern SOURCE_PATTERN = Pattern.compile("B-Global|[Bb]aha|[Bb]ilibili|AT-X|Web|CR");

    /**
     * 字幕正则表达式
     */
    private static final Pattern SUBTITLE_PATTERN = Pattern.compile("[简繁日字幕]|CH|BIG5|GB");

    /**
     * 匹配番剧的集数
     */
    private static final Pattern EPISODE_PATTERN = Pattern.compile("\\d+");

    /**
     * 中文数字映射
     */
    private static final Map<String, Integer> CHINESE_NUMBER_MAP = Map.of(
            "一", 1,
            "二", 2,
            "三", 3,
            "四", 4,
            "五", 5,
            "六", 6,
            "七", 7,
            "八", 8,
            "九", 9,
            "十", 10
    );

    /**
     * 预处理标题
     * 去掉首尾空格，替换掉中文括号为英文括号
     */
    private static String preProcess(String title) {
        return title.strip()
                .replaceAll("【", "[")
                .replaceAll("】", "]")
                .replaceAll(INTERFERENCE_REGEX2, "");
    }

    /**
     * 获取发布组
     *
     * @param title 包含发布组信息的标题
     */
    private static String findReleaseGroup(String title) {
        return title.split("[\\[\\]]")[1];
    }

    /**
     * 处理季度信息和番剧名称
     *
     * @param nameSeasonInfo 包含季度信息和番剧名称的字符串
     */
    private static NameSeason processNameAndSeason(String nameSeasonInfo) {
        // 番剧名称和季度名称的组合
        String nameSeason;
        Integer season = null;
        String rawSeason = null;

        Matcher interferenceMatcher = INTERFERENCE_PATTERN1.matcher(nameSeasonInfo);
        if (interferenceMatcher.find()) {
            // 如果标题中包含干扰信息（如新番、几月番等），则去掉字符串开头到“新番”及其后一个字符的所有内容
            nameSeason = nameSeasonInfo.replaceAll(".*新番.", "");
        } else {
            // 去掉标题开头的括号及括号里的内容，即去掉发布组信息
            nameSeason = nameSeasonInfo.replaceAll("^[^]】]*[]】]", "").strip();
        }

        // 将所有的 [ 和 ] 都替换成空格
        nameSeason = nameSeason.replaceAll("[\\[\\]]", " ");

        Matcher seasonMatcher = SEASON_PATTERN.matcher(nameSeason); // 可能会匹配到多个季度信息
        while (seasonMatcher.find()) {
            String seasonStr = seasonMatcher.group();
            rawSeason = seasonStr;

            if (seasonStr.startsWith("Season")) {
                season = Integer.parseInt(seasonStr.substring(6).strip());
                break;
            } else if (seasonStr.startsWith("S") || seasonStr.startsWith("s")) {
                season = Integer.parseInt(seasonStr.substring(1));
                break;
            } else if (seasonStr.startsWith("第")) {
                String maybeChineseNumberSeason = seasonStr.replaceAll("[第季期]", "").strip();
                try {
                    season = Integer.parseInt(maybeChineseNumberSeason);
                } catch (NumberFormatException e) {
                    // 如果转换失败，可能是中文数字
                    season = CHINESE_NUMBER_MAP.get(maybeChineseNumberSeason);
                }
                break;
            }
        }

        if (Objects.isNull(season)) {
            // 如果没有匹配到季度信息，则默认季度为 1
            return new NameSeason(nameSeason, rawSeason, 1);
        } else {
            // 如果匹配到季度信息，则去掉季度信息，返回番剧名称
            String rawName = nameSeason.replaceAll(SEASON_REGEX, "").strip();
            return new NameSeason(rawName, rawSeason, season);
        }
    }

    private static Name findName(String rawName) {
        rawName = rawName.strip();

        // 使用 /
        // 使用两个连续的空格或者制表符
        // 使用 - 后面跟两个空格或者制表符
        String[] splitNames = rawName.split("/|\\s{2}|-\\s{2}");

        // 移除空字符串
        splitNames = removeBlank(splitNames);

        if (splitNames.length == 1) {
            if (rawName.contains("_")) {
                splitNames = rawName.split("_");
            } else if (rawName.contains(" - ")) {
                splitNames = rawName.split("-");
            }
        }

        if (splitNames.length == 1) {
            Matcher chenMatcher = CHINESE_ENGLISH_PATTERN.matcher(rawName);
            if (chenMatcher.find()) {
                // 如果匹配到中文和英文的组合，则返回英文部分
                return new Name(chenMatcher.group(3).strip(), splitNames);
            }
        }

        // 最长的英文长度
        long maxLength = 0;
        // 最长英文的索引
        int maxIndex = 0;

        for (int i = 0; i < splitNames.length; i++) {
            String name = splitNames[i];
            Matcher englishMatcher = ENGLISH_PATTERN.matcher(name);
            long len = englishMatcher.results().count();
            if (len > maxLength) {
                maxLength = len;
                maxIndex = i;
            }
        }

        return new Name(splitNames[maxIndex].strip(), splitNames);
    }

    private static Tag findTags(String otherInfo) {
        String source = null;
        String subtitle = null;
        String resolution = null;
        String[] othersInfo = otherInfo.replaceAll("[\\[\\]()（）]", " ").split(" ");
        for (String info : othersInfo) {
            if (!info.isBlank()) {
                if (SOURCE_PATTERN.matcher(info).find()) {
                    source = info;
                } else if (RESOLUTION_PATTERN.matcher(info).find()) {
                    resolution = info;
                } else if (SUBTITLE_PATTERN.matcher(info).find()) {
                    subtitle = info;
                }
            }
        }

        return new Tag(resolution, source, subtitle);
    }

    /**
     * 移除字符串数组中的空字符串和 null 值
     */
    private static String[] removeBlank(String[] arr) {
        return Arrays.stream(arr)
                .filter(Objects::nonNull)
                .filter(s -> !s.isBlank())
                .toArray(String[]::new);
    }

    /**
     * 解析标题
     */
    public static Episode parse(String title) {
        // 预处理标题
        String preProcessedTitle = preProcess(title);
        // 获取发布组
        String releaseGroup = findReleaseGroup(preProcessedTitle);

        Matcher titleMatcher = TITLE_PATTERN.matcher(preProcessedTitle);
        if (titleMatcher.find()) {
            String nameSeasonInfo = titleMatcher.group(1).strip();
            String episodeInfo = titleMatcher.group(2).strip();
            String otherInfo = titleMatcher.group(3).strip();
            // 获取番剧名称和季度信息
            NameSeason nameSeason = processNameAndSeason(nameSeasonInfo);
            // 获取番剧名称
            Name name = findName(nameSeason.rawName);
            // 获取其他信息中的标签
            Tag tag = findTags(otherInfo);

            Matcher episodeMatcher = EPISODE_PATTERN.matcher(episodeInfo);
            if (episodeMatcher.find()) {
                String rawEpisode = episodeMatcher.group();
                int episode = Integer.parseInt(rawEpisode);
                return new Episode(new EpisodeInfo(rawEpisode, episode),
                        new TitleInfo(nameSeason.rawName, name.name, name.splitNames),
                        new SeasonInfo(nameSeason.rawSeason, nameSeason.season),
                        releaseGroup, tag.resolution, tag.source, tag.subtitle);
            }
        }

        throw new ParseTitleException("无法解析标题: " + title);
    }

    /**
     * 番剧名称和季度信息的组合
     *
     * @param rawName   番剧的原始名称
     * @param rawSeason 季度的原始字符串
     * @param season    季度
     */
    private record NameSeason(String rawName, String rawSeason, int season) {

    }

    /**
     * 番剧名称和分割后的名称数组
     *
     * @param name       番剧名称
     * @param splitNames 分割后的名称数组
     */
    private record Name(String name, String[] splitNames) {

    }

    /**
     * 番剧标签信息
     *
     * @param resolution 分辨率
     * @param source     来源
     * @param subtitle   字幕信息
     */
    private record Tag(String resolution, String source, String subtitle) {

    }

    /**
     * 季度信息
     *
     * @param rawSeason 原始季度字符串
     * @param season    处理后的季度
     */
    public record SeasonInfo(String rawSeason, int season) {

    }

    /**
     * 番剧集数信息
     *
     * @param rawEpisode 原始集数字符串
     * @param episode    处理后的集数
     */
    public record EpisodeInfo(String rawEpisode, int episode) {

    }

    /**
     * 番剧标题信息
     *
     * @param rawName    原始名称
     * @param name       处理后的名称
     * @param splitNames 分割后的名称数组
     */
    public record TitleInfo(String rawName, String name, String[] splitNames) {

        @Override
        public String toString() {
            return "TitleInfo{" +
                    "rawName='" + rawName + '\'' +
                    ", name='" + name + '\'' +
                    ", splitNames=" + Arrays.toString(splitNames) +
                    '}';
        }
    }

    /**
     * 番剧解析结果
     */
    public record Episode(EpisodeInfo episodeInfo, TitleInfo titleInfo, SeasonInfo seasonInfo, String releaseGroup, String resolution, String source, String subtitle) {

    }

}
