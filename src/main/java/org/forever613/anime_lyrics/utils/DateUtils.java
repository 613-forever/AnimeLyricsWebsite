/*
 * This file is part of AnimeLyricsWebsite.
 * Copyright (C) 2021 613_forever
 *
 * AnimeLyricsWebsite is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * AnimeLyricsWebsite is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with AnimeLyricsWebsite. If not, see <https://www.gnu.org/licenses/>.
 */

package org.forever613.anime_lyrics.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class DateUtils {
    private static final Logger logger = LoggerFactory.getLogger("DateUtils");
    private static final DateFormat date_format = new SimpleDateFormat("yyyy-MM-dd");
    private static final DateFormat full_format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private static final DateTimeFormatter input1_format = DateTimeFormatter.ofPattern("yyyy-M-d H:mm");
    private static final DateTimeFormatter input2_format = DateTimeFormatter.ofPattern("yyyy.M.d H:mm");
    private static final DateTimeFormatter input3_format = DateTimeFormatter.ofPattern("yyyy/M/d H:mm");

    public static ZonedDateTime fromMilli(long milliseconds) {
        // 用不到毫秒部分，这里都扔掉
        return ZonedDateTime.ofInstant(Instant.ofEpochSecond(milliseconds / 1000), ZoneId.systemDefault());
    }

    public static String format(long milliseconds) {
        return format(fromMilli(milliseconds));
    }

    @Deprecated
    public static String format(Date date) {
        if (date == null) return null;
        return full_format.format(date);
    }

    public static String format(ZonedDateTime date) {
        return DateTimeFormatter.ISO_LOCAL_DATE.format(date);
    }

    public static String formatISO8601(long milliseconds) {
        return formatISO8601(fromMilli(milliseconds));
    }

    public static String formatISO8601(ZonedDateTime date) {
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(date);
    }

    public static String formatDate(long milliseconds) {
        return formatDate(fromMilli(milliseconds));
    }

    @Deprecated
    public static String formatDate(Date date) {
        if (date == null) return null;
        return date_format.format(date);
    }

    public static String formatDate(ZonedDateTime date) {
        return DateTimeFormatter.ISO_LOCAL_DATE.format(date);
    }

    public static ZonedDateTime fromFormatted(String str) {
        if (str == null) return null;
        try {
            return LocalDateTime.parse(str, input1_format).atZone(ZoneId.systemDefault());
        } catch (DateTimeParseException e1) {
            try {
                return LocalDateTime.parse(str, input2_format).atZone(ZoneId.systemDefault());
            } catch (DateTimeParseException e2) {
                try {
                    return LocalDateTime.parse(str, input3_format).atZone(ZoneId.systemDefault());
                } catch (DateTimeParseException e3) {
                    logger.warn("I met a time object in invalid format: \"{}\"", str);
                    return null;
                }
            }
        }
    }

    @Deprecated
    public static String format(String str) {
        if (str == null) return null;
        return format(fromFormatted(str));
    }

    @Deprecated
    public static int difference(Date lhs, Date rhs) {
        return (int) ((lhs.getTime() - rhs.getTime()) / 86400000);
    }

    public static int difference(ZonedDateTime lhs, ZonedDateTime rhs) {
        return (int) (lhs.until(rhs, ChronoUnit.DAYS));
    }
}
