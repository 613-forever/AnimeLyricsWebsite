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

    public static int difference(Date lhs, Date rhs) {
        return (int) ((lhs.getTime() - rhs.getTime()) / 86400000);
    }
}
