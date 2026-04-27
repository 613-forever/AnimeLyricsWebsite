/*
 * This file is part of AnimeLyricsWebsite.
 * Copyright (C) 2021-2026 613_forever
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

package org.forever613.anime_lyrics;

import java.time.ZonedDateTime;
import java.util.List;

public class GeneratedFileInfo {
    private String author;
    private String title;
    private ZonedDateTime pubdate;
    private List<String> keywords;
    private String description;
    private String otherContent;

    public GeneratedFileInfo() {
        this.author = Config.getInstance().getNameFooter();
        this.title = "untitled";
        this.pubdate = ZonedDateTime.now();
        this.otherContent = "";
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ZonedDateTime getPubdate() {
        return pubdate;
    }

    public void setPubdate(ZonedDateTime pubdate) {
        this.pubdate = pubdate;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOtherContent() {
        return otherContent;
    }

    public void setOtherContent(String otherContent) {
        this.otherContent = otherContent;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("GeneratedFileInfo{");
        sb.append("author='").append(author).append('\'');
        sb.append(", title='").append(title).append('\'');
        sb.append(", pubdate=").append(pubdate);
        sb.append(", keywords='").append(String.join(",", keywords)).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", otherContent='").append(otherContent).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
