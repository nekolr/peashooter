package com.github.nekolr.peashooter.parser;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RawParserTests {

    private static final String TITLE1 = "[黒ネズミたち] 恋上换装娃娃 Season 2 / Sono Bisque Doll wa Koi wo Suru Season 2 - 16 (ABEMA 1280x720 AVC AAC MP4)";
    private static final String TITLE2 = "【喵萌奶茶屋】★07月新番★[僵尸100 ~变成僵尸前想要完成的100件事~ / Zom 100: Zombie ni Naru made ni Shitai 100 no Koto][10][1080p][简日双语][招募翻译]";
    private static final String TITLE3 = "[ANi] Kakkō no Iinazuke s02 / 杜鹃婚约 第二季 - 04 [1080P][Baha][WEB-DL][AAC AVC][CHT][MP4]";
    private static final String TITLE4 = " [Prejudice-Studio] 盾之勇者成名录 第四季（仅限港澳台地区） Tate no Yuusha no Nariagari S4 - 04 [Bilibili WEB-DL 1080P AVC 8bit AAC MKV][简繁内封] [复制磁连]";
    private static final String TITLE5 = "[ANi] 阴阳回天 Re：Birth Verse - 05 [1080P][Baha][WEB-DL][AAC AVC][CHT][MP4]";
    private static final String TITLE6 = "[黒ネズミたち] 被驱逐出勇者队伍的白魔导师，被S级冒险者捡到 / Yuusha Party wo Tsuihou Sareta Shiro Madoushi - 04 (Baha 1920x1080 AVC AAC MP4)";
    private static final String TITLE7 = "[ANi] 我们不可能成为恋人！绝对不行。（※似乎可行？） - 04 [1080P][Baha][WEB-DL][AAC AVC][CHT][MP4]";
    private static final String TITLE8 = "[ANi] Grand Blue Dreaming / GRAND BLUE 碧蓝之海 2 - 04 [1080P][Baha][WEB-DL][AAC AVC][CHT][MP4]";

    @Test
    void testTitle1() {
        RawParser.Episode result = RawParser.parse(TITLE1);

        assertEquals("Sono Bisque Doll wa Koi wo Suru", result.titleInfo().name());
        assertEquals(2, result.seasonInfo().season());
        assertEquals("黒ネズミたち", result.releaseGroup());
        assertEquals(16, result.episodeInfo().episode());
    }

    @Test
    void testTitle2() {
        RawParser.Episode result = RawParser.parse(TITLE2);

        assertEquals("Zom 100: Zombie ni Naru made ni Shitai 100 no Koto", result.titleInfo().name());
        assertEquals(1, result.seasonInfo().season());
        assertEquals("喵萌奶茶屋", result.releaseGroup());
        assertEquals(10, result.episodeInfo().episode());
    }

    @Test
    void testTitle3() {
        RawParser.Episode result = RawParser.parse(TITLE3);

        assertEquals("Kakkō no Iinazuke", result.titleInfo().name());
        assertEquals(2, result.seasonInfo().season());
        assertEquals("ANi", result.releaseGroup());
        assertEquals(4, result.episodeInfo().episode());
    }

    @Test
    void testTitle4() {
        RawParser.Episode result = RawParser.parse(TITLE4);

        assertEquals("Tate no Yuusha no Nariagari", result.titleInfo().name());
        assertEquals(4, result.seasonInfo().season());
        assertEquals("Prejudice-Studio", result.releaseGroup());
        assertEquals(4, result.episodeInfo().episode());
    }

    @Test
    void testTitle5() {
        RawParser.Episode result = RawParser.parse(TITLE5);

        assertEquals("阴阳回天 Re：Birth Verse", result.titleInfo().name());
        assertEquals(1, result.seasonInfo().season());
        assertEquals("ANi", result.releaseGroup());
        assertEquals(5, result.episodeInfo().episode());
    }

    @Test
    void testTitle6() {
        RawParser.Episode result = RawParser.parse(TITLE6);

        assertEquals("Yuusha Party wo Tsuihou Sareta Shiro Madoushi", result.titleInfo().name());
        assertEquals(1, result.seasonInfo().season());
        assertEquals("黒ネズミたち", result.releaseGroup());
        assertEquals(4, result.episodeInfo().episode());
    }

    @Test
    void testTitle7() {
        RawParser.Episode result = RawParser.parse(TITLE7);

        assertEquals("我们不可能成为恋人！绝对不行。（※似乎可行？）", result.titleInfo().name());
        assertEquals(1, result.seasonInfo().season());
        assertEquals("ANi", result.releaseGroup());
        assertEquals(4, result.episodeInfo().episode());
    }

    @Test
    void testTitle8() {
        RawParser.Episode result = RawParser.parse(TITLE8);

        assertEquals("Grand Blue Dreaming", result.titleInfo().name());
        assertEquals(1, result.seasonInfo().season()); // 这种情况无法确定是第几季，因此默认为 1
        assertEquals("ANi", result.releaseGroup());
        assertEquals(4, result.episodeInfo().episode());
    }
}
