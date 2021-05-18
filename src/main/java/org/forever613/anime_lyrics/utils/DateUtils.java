package org.forever613.anime_lyrics.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
    private static final Logger logger = LoggerFactory.getLogger("DateUtils");
    private static final DateFormat date_format = new SimpleDateFormat("yyyy-MM-dd");
    private static final DateFormat full_format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private static final DateFormat input1_format = new SimpleDateFormat("yyyy-M-d H:mm");
    private static final DateFormat input2_format = new SimpleDateFormat("yyyy.M.d H:mm");
    private static final DateFormat input3_format = new SimpleDateFormat("yyyy/M/d H:mm");

    public static String format(long milliseconds) {
        return format(new Date(milliseconds));
    }

    public static String format(Date date) {
        if (date == null) return null;
        return full_format.format(date);
    }

    public static String formatDate(long milliseconds) {
        return formatDate(new Date(milliseconds));
    }

    public static String formatDate(Date date) {
        if (date == null) return null;
        return date_format.format(date);
    }

    public static Date fromFormatted(String str) {
        if (str == null) return null;
        try {
            return input1_format.parse(str);
        } catch (ParseException e1) {
            try {
                return input2_format.parse(str);
            } catch (ParseException e2) {
                try {
                    return input3_format.parse(str);
                } catch (ParseException e3) {
                    logger.warn("I met a time object in invalid format: \"{}\"", str);
                    return null;
                }
            }
        }
    }

    public static String format(String str) {
        if (str == null) return null;
        return format(fromFormatted(str));
    }
}
