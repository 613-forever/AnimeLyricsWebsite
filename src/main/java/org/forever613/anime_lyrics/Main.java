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

public class Main {
    static final String DRAFT_DIR = "./text/draft/";
    static final String CACHE_DIR = "./text/cache/";
    static final String TARGET_DIR = "./text/target/";

    static final String COPY_ONLY_DIR_NAME = "copy_only";
    static final String LIST_FILE_NAME = "List.html";
    static final String SITEMAP_FILE_NAME = "Sitemap.xml";
    static final String ROBOTS_FILE_NAME = "robots.txt";
    static final String ROOT_URL_FILE_NAME = "url.txt";

    public static void main(String[] args) {
        FileCollector fileCollector = new FileCollector(
                args.length > 1 ? args[1] : DRAFT_DIR,
                args.length > 2 ? args[2] : CACHE_DIR,
                args.length > 3 ? args[3] : TARGET_DIR,
                args.length > 4 ? args[4] : LIST_FILE_NAME,
                args.length > 5 ? args[5] : SITEMAP_FILE_NAME,
                args.length > 6 ? args[6] : ROBOTS_FILE_NAME,
                args.length > 7 ? args[7] : ROOT_URL_FILE_NAME,
                args.length > 8 ? args[8] : COPY_ONLY_DIR_NAME
        );
        fileCollector.run();
    }
}
