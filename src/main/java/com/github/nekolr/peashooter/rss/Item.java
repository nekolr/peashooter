package com.github.nekolr.peashooter.rss;

import java.util.Date;

/**
 * Rss Item Element
 */
public record Item(String title, String link, Date pubDate, String guid, Enclosure enclosure) {

}
