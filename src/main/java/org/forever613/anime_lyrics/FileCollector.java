package org.forever613.anime_lyrics;

import org.forever613.anime_lyrics.renderers.*;
import org.forever613.anime_lyrics.utils.DateUtils;
import org.forever613.anime_lyrics.utils.HtmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FileCollector {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final List<SourceFileInfo> articles, helpArticles, sysArticles;
    private final File draftDir, cacheDir, targetDir;
    private final String listFileName, sitemapFileName, robotsFileName;
    private final String rootUrl;
    private boolean updated = false;

    public FileCollector(String draftDirName, String cacheDirName, String targetDirName,
                         String listFileName, String sitemapFileName, String robotsFileName, String rootUrlFileName) {
        draftDir = new File(draftDirName);
        if (!draftDir.exists() || !draftDir.isDirectory()) {
            logger.error("I have not found the directory for drafts.");
            throw new IllegalArgumentException();
        }
        cacheDir = new File(cacheDirName);
        if ((!cacheDir.exists() && !cacheDir.mkdir()) || !draftDir.isDirectory()) {
            logger.error("I have not found the directory for caches, and failed to create one.");
            throw new IllegalArgumentException();
        }
        targetDir = new File(targetDirName);
        if ((!targetDir.exists() && !targetDir.mkdir()) || !draftDir.isDirectory()) {
            logger.error("I have not found the directory to output files, and failed to create one.");
            throw new IllegalArgumentException();
        }
        File url = new File(draftDir, rootUrlFileName);
        if (!url.exists() || !url.isFile()) {
            logger.error("I have not found the configuration file to load root URL.");
            throw new IllegalArgumentException();
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(url), StandardCharsets.UTF_8))) {
            String buffer = reader.readLine();
            rootUrl = buffer.endsWith("/") ? buffer : buffer + "/";
        } catch (IOException e) {
            logger.error("I have failed to load root URL from the configuration file.");
            throw new IllegalArgumentException();
        }
        this.listFileName = listFileName;
        this.sitemapFileName = sitemapFileName;
        this.robotsFileName = robotsFileName;
        articles = new ArrayList<>();
        helpArticles = new ArrayList<>();
        sysArticles = new ArrayList<>();
    }

    public void run() {
        String[] mdFileNames = draftDir.list((file, name) -> name.endsWith(".md"));
        String[] almFileNames = draftDir.list((file, name) -> name.endsWith(".alm.txt"));

        if (mdFileNames == null || almFileNames == null) {
            logger.error("I have met some unknown error when scanning dirs, maybe draft directory is not a directory.");
            return;
        }

        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("/WEB-INF/templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setCharacterEncoding("UTF-8");
        templateResolver.setTemplateMode("HTML");
        templateResolver.setCacheable(false);

        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.addTemplateResolver(templateResolver);

        // handle the markdown files
        addFiles(mdFileNames, new MarkdownRenderer(templateEngine));
        // handle the anime-lyric markup files
        addFiles(almFileNames, new AnimeLyricsRenderer(templateEngine));

        File listFile = new File(targetDir, listFileName);
        File sitemapFile = new File(targetDir, sitemapFileName);
        if (updated || !listFile.exists() || !sitemapFile.exists()) {
            articles.sort(null);
            helpArticles.sort(null);
            sysArticles.sort(null);

            if (updated || !listFile.exists()) {
                // make the file list file
                new ListFileRenderer(templateEngine, this).render(null, listFile);
            }
            if (updated || !sitemapFile.exists()) {
                // make the sitemap
                new SitemapRenderer(this, rootUrl).render(null, sitemapFile);
            }
        }

        File robotsFile = new File(targetDir, robotsFileName);
        if (!robotsFile.exists()) {
            templateResolver.setTemplateMode("TEXT");
            new RobotsRenderer(templateEngine, rootUrl, sitemapFileName).render(null, robotsFile);
        }
    }

    private void addFiles(String[] fileNames, Renderer renderer) {
        for (String fileName : fileNames) {
            File draft = new File(draftDir, fileName);
            if (!draft.exists()) {
                logger.warn("I have listed a file but can not access it now. Skipping \"{}\"", fileName);
                throw new IllegalStateException();
            }
            String newFileName = fileName.substring(0, fileName.indexOf('.')) + ".html";
            File target = new File(targetDir, newFileName);
            GeneratedFileInfo info;
            Date modifiedDate = new Date(draft.lastModified());
            if (!target.exists() || target.lastModified() < draft.lastModified()) {
                info = renderer.render(draft, target);
                updated = true;
            } else {
                info = HtmlUtils.extractInfo(fileName, target);
            }
            if (info == null || info.getTitle() == null) {
                logger.warn("I think something must be wrong with \"{}\" to generate {}.", fileName, info);
                continue; // failure
            }
            SourceFileInfo sInfo = new SourceFileInfo(newFileName, info.getTitle(), info.getPubdate(), modifiedDate);
            addFile(sInfo);
        }
    }

    private void addFile(SourceFileInfo info) {
        String title = info.getTitle();
        if (title.startsWith("Help:")) {
            helpArticles.add(info);
        } else if (title.startsWith("System:")) {
            sysArticles.add(info);
        } else if (title.startsWith("Hidden:")) {
            logger.info("I am skipping the indexing for the file \"{}\" titled \"{}\" due to the prefix \"Hidden:\".",
                    info.getFilename(), title);
        } else {
            articles.add(info);
        }
    }

    public List<SourceFileInfo> getArticles() {
        return articles;
    }

    public List<SourceFileInfo> getHelpArticles() {
        return helpArticles;
    }

    public List<SourceFileInfo> getSysArticles() {
        return sysArticles;
    }
}
