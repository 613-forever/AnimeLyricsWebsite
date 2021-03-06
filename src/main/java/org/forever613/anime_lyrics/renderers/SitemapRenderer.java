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

package org.forever613.anime_lyrics.renderers;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.XMLWriter;
import org.forever613.anime_lyrics.FileCollector;
import org.forever613.anime_lyrics.GeneratedFileInfo;
import org.forever613.anime_lyrics.SourceFileInfo;
import org.forever613.anime_lyrics.utils.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Date;

public class SitemapRenderer implements Renderer {
    private final FileCollector fileCollector;
    private final String rootUrl;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @SuppressWarnings("HttpUrlsUsage")
    private final String xmlns = "http://www.sitemaps.org/schemas/sitemap/0.9";

    public SitemapRenderer(FileCollector fileCollector, String rootUrl) {
        this.fileCollector = fileCollector;
        this.rootUrl = rootUrl;
    }

    public void addUrl(Element root, String url, Date lastMod, String changeFreq, double priority) {
        Element urlElement = root.addElement("url", xmlns);
        urlElement.addElement("loc", xmlns).addText(rootUrl + url);
        urlElement.addElement("lastmod", xmlns).addText(DateUtils.formatDate(lastMod));
        urlElement.addElement("changefreq", xmlns).addText(changeFreq);
        urlElement.addElement("priority", xmlns).addText(String.valueOf(priority));
    }

    public void addUrl(Element root, SourceFileInfo info, String changeFreq, double priority) {
        addUrl(root, info.getFilename(), info.getModifiedDate(), changeFreq, priority);
    }

    @Override
    public GeneratedFileInfo render(File draft, File target) {
        assert draft == null;

        Document doc = DocumentHelper.createDocument();
        Element root = doc.addElement("urlset", xmlns);

        addUrl(root, "", new Date(), "daily", 0.2);
        for (SourceFileInfo info: fileCollector.getArticles()) {
            int past = DateUtils.difference(info.getModifiedDate(), new Date());
            addUrl(root, info, past < 10 ? "weekly" : past < 50 ? "monthly" : "yearly",  0.8);
        }
        for (SourceFileInfo info: fileCollector.getHelpArticles()) {
            int past = DateUtils.difference(info.getModifiedDate(), new Date());
            addUrl(root, info, past < 30 ? "monthly" : "yearly", 0.4);
        }
        for (SourceFileInfo info: fileCollector.getSysArticles()) {
            addUrl(root, info,  "yearly", 0.0);
        }

        try (Writer writer = new OutputStreamWriter(new FileOutputStream(target), StandardCharsets.UTF_8)) {
            XMLWriter xmlWriter = new XMLWriter();
            xmlWriter.setWriter(writer);
            xmlWriter.write(doc);
        } catch (IOException e) {
            logger.warn("I failed to generate the site map file. " + e.getMessage());
            logger.debug("ST: ", e);
            return null;
        }

        logger.info("I have generated the site map file successfully.");
        GeneratedFileInfo info = new GeneratedFileInfo();
        info.setTitle("sitemap.xml");
        return info;
    }
}
