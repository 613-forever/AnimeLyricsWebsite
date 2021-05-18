package org.forever613.anime_lyrics;

import java.util.Date;

public class GeneratedFileInfo {
    private final String title;
    private final Date pubdate;
    private final String otherContent;

    public GeneratedFileInfo(String title, Date pubdate, String otherContent) {
        this.title = title;
        this.pubdate = pubdate;
        this.otherContent = otherContent;
    }

    public String getTitle() {
        return title;
    }

    public Date getPubdate() {
        return pubdate;
    }

    public String getOtherContent() {
        return otherContent;
    }

    @Override
    public String toString() {
        return "GeneratedFileInfo{title='" + title +
                "', pubdate=" + (pubdate != null ? pubdate : "auto") + ", otherContent=" +
                (otherContent != null ? "string of length(" + otherContent.length() + ")" : "auto") + "}";
    }
}
