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

import org.forever613.anime_lyrics.GeneratedFileInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class HtmlUtils {
    private static Logger logger = LoggerFactory.getLogger("HtmlUtils");

    public static String escapeHtml(char c) {
        switch (c) {
        case '"':
            return "&quot;";
        case '&':
            return "&amp;";
        case '\'':
            return "&apos;";
        case '<':
            return "&lt;";
        case '>':
            return "&gt;";
        }
        return String.valueOf(c);
    }

    public static String escapeHtml(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            sb.append(escapeHtml(s.charAt(i)));
        }
        return sb.toString();
    }

    public static GeneratedFileInfo splitInfo(String content) {
        GeneratedFileInfo info = new GeneratedFileInfo();
        {
            int h1start = content.indexOf("<h1");
            if (h1start == -1) {
                logger.warn("I failed to find <h1> title...");
            } else {
                int h1before_content = content.indexOf('>', h1start);
                int h1end = content.indexOf("</h1>", h1start);
                info.setTitle(content.substring(h1before_content + 1, h1end));
                content = content.substring(0, h1start).trim() + content.substring(h1end + 5).trim();
            }
        }
        {
            int author_start = content.indexOf("<span id=\"author\"");
            if (author_start == -1) {
                logger.warn("I failed to find <span id=author>... ");
            } else {
                int author_before_content = content.indexOf('>', author_start);
                int author_end = content.indexOf("</span>", author_start);
                info.setAuthor(content.substring(author_before_content + 1, author_end));
                content = content.substring(0, author_start).trim() + content.substring(author_end + 7).trim();
            }
        }
        {
            int pubdate_start = content.indexOf("<time pubdate");
            if (pubdate_start == -1) {
                logger.warn("I failed to find <time pubdate>... ");
            } else {
                int pubdate_before_content = content.indexOf('>', pubdate_start);
                int pubdate_end = content.indexOf("</time>", pubdate_start);
                info.setPubdate(DateUtils.fromFormatted(content.substring(pubdate_before_content + 1, pubdate_end)));
                content = content.substring(0, pubdate_start).trim() + content.substring(pubdate_end + 7).trim();
            }
        }
        info.setOtherContent(content);
        return info;
    }

    public static GeneratedFileInfo extractInfo(String fileName, File target) {
        GeneratedFileInfo info;
        try {
            char[] buffer = new char[(int) target.length()];
            Reader reader = new InputStreamReader(new FileInputStream(target), StandardCharsets.UTF_8);
            //noinspection ResultOfMethodCallIgnored
            reader.read(buffer);
            info = splitInfo(String.valueOf(buffer));
            reader.close();
        } catch (FileNotFoundException ignored) {
            return null;
        } catch (IOException e) {
            logger.warn("Fail to find <h1> title in file " + fileName);
            return null;
        }
        return info;
    }
}
