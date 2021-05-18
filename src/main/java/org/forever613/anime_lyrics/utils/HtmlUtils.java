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
        String title = null;
        {
            int h1start = content.indexOf("<h1");
            if (h1start == -1) {
                logger.warn("I failed to find <h1> title...");
            } else {
                int h1before_content = content.indexOf('>', h1start);
                int h1end = content.indexOf("</h1>", h1start);
                title = content.substring(h1before_content + 1, h1end);
                content = content.substring(0, h1start).trim() + content.substring(h1end + 5).trim();
            }
        }
        String pubdate = null;
        {
            int pubdate_start = content.indexOf("<time pubdate");
            if (pubdate_start == -1) {
                logger.warn("I failed to find <time pubdate>... ");
            } else {
                int pubdate_before_content = content.indexOf('>', pubdate_start);
                int pubdate_end = content.indexOf("</time>", pubdate_start);
                pubdate = content.substring(pubdate_before_content + 1, pubdate_end);
                content = content.substring(0, pubdate_start).trim() + content.substring(pubdate_end + 7).trim();
            }
        }
        return new GeneratedFileInfo(title, DateUtils.fromFormatted(pubdate), content);
    }

    public static GeneratedFileInfo extractInfo(String fileName, File target) {
        GeneratedFileInfo title;
        try {
            char[] buffer = new char[(int) target.length()];
            Reader reader = new InputStreamReader(new FileInputStream(target), StandardCharsets.UTF_8);
            //noinspection ResultOfMethodCallIgnored
            reader.read(buffer);
            title = splitInfo(String.valueOf(buffer));
            reader.close();
        } catch (FileNotFoundException ignored) {
            return null;
        } catch (IOException e) {
            logger.warn("Fail to find <h1> title in file " + fileName);
            return null;
        }
        return title;
    }
}
