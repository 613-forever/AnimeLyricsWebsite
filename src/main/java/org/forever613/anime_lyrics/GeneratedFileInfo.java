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
