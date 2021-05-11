package org.forever613.anime_lyrics;

import java.util.Date;

public class SourceFileInfo implements Comparable<SourceFileInfo> {
    private final String filename;
    private final String title;
    private final Date modifiedDate;

    public SourceFileInfo(String filename, String title, Date modifiedDate) {
        this.filename = filename;
        this.title = title;
        this.modifiedDate = modifiedDate;
    }

    public String getFilename() {
        return filename;
    }

    public String getTitle() {
        return title;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    @Override
    public int compareTo(SourceFileInfo info) {
        return this.title.compareTo(info.title);
    }
}
