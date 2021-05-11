package org.forever613.anime_lyrics.utils;

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

    public static String findTitle(String content) {
        int h1start = content.indexOf("<h1>"), h1end = content.indexOf("</h1>");
        if (h1start == -1) {
            logger.warn("Fail to find <h1> title...");
            return null;
        }
        return content.substring(h1start + 4 /* <h1>=4 */, h1end);
    }

    public static String extractTitle(String fileName, File target) {
        String title;
        try {
            char[] buffer = new char[(int) target.length()];
            Reader reader = new InputStreamReader(new FileInputStream(target), StandardCharsets.UTF_8);
            //noinspection ResultOfMethodCallIgnored
            reader.read(buffer);
            title = findTitle(String.valueOf(buffer));
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
