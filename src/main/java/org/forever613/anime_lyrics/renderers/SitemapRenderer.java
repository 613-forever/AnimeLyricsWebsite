package org.forever613.anime_lyrics.renderers;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.io.XMLWriter;
import org.forever613.anime_lyrics.FileCollector;
import org.forever613.anime_lyrics.GeneratedFileInfo;
import org.forever613.anime_lyrics.SourceFileInfo;
import org.forever613.anime_lyrics.utils.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

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
        urlElement.addElement("lastmod", xmlns).addText(DateUtils.format(lastMod));
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
            addUrl(root, info, "weekly",  0.8);
        }
        for (SourceFileInfo info: fileCollector.getHelpArticles()) {
            addUrl(root, info,  "monthly", 0.4);
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
        return new GeneratedFileInfo("sitemap", null, null);
    }
}
