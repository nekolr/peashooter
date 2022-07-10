package com.github.nekolr.peashooter.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {

    public static Date plus(Date start, int seconds) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);

        calendar.add(Calendar.SECOND, seconds);
        return calendar.getTime();
    }

    public static Date parse(String datetime) {
        LocalDateTime l = LocalDateTime.parse(datetime, DateTimeFormatter.ISO_DATE_TIME);
        return Date.from(l.atZone(ZoneId.systemDefault()).toInstant());
    }

}
