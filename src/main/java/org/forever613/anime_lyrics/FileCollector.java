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

import org.forever613.anime_lyrics.renderers.*;
import org.forever613.anime_lyrics.utils.HtmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FileCollector {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileCollector.class);

    private final List<SourceFileInfo> articles, helpArticles, sysArticles;
    private final File draftDir, cacheDir, targetDir;
    private final String copyOnlyDirName;
    private final String listFileName, sitemapFileName, robotsFileName;
    private boolean updated = false;

    public FileCollector(String draftDirName, String cacheDirName, String targetDirName,
                         String listFileName, String sitemapFileName, String robotsFileName, String rootConfigFileName,
                         String copyOnlyDirName) {
        draftDir = new File(draftDirName);
        if (!draftDir.exists() || !draftDir.isDirectory()) {
            LOGGER.error("I have not found the directory for drafts.");
            throw new IllegalArgumentException();
        }
        cacheDir = new File(cacheDirName);
        if ((!cacheDir.exists() && !cacheDir.mkdir()) || !draftDir.isDirectory()) {
            LOGGER.error("I have not found the directory for caches, and failed to create one.");
            throw new IllegalArgumentException();
        }
        targetDir = new File(targetDirName);
        if ((!targetDir.exists() && !targetDir.mkdir()) || !draftDir.isDirectory()) {
            LOGGER.error("I have not found the directory to output files, and failed to create one.");
            throw new IllegalArgumentException();
        }
        File config = new File(draftDir, rootConfigFileName);
        if (!config.exists() || !config.isFile()) {
            LOGGER.error("I have not found the configuration file to load configs.");
            throw new IllegalArgumentException();
        }
        Config.loadConfig(config);
        this.listFileName = listFileName;
        this.sitemapFileName = sitemapFileName;
        this.robotsFileName = robotsFileName;
        this.copyOnlyDirName = copyOnlyDirName;
        articles = new ArrayList<>();
        helpArticles = new ArrayList<>();
        sysArticles = new ArrayList<>();
    }

    public void run() {
        String[] mdFileNames = draftDir.list((file, name) -> name.endsWith(".md"));
        String[] almFileNames = draftDir.list((file, name) -> name.endsWith(".alm.txt"));

        if (mdFileNames == null || almFileNames == null) {
            LOGGER.error("I have met some unknown error when scanning dirs, maybe draft directory is not a directory.");
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
                new SitemapRenderer(this, Config.getInstance().getRootUrl()).render(null, sitemapFile);
            }
        }

        File robotsFile = new File(targetDir, robotsFileName);
        if (!robotsFile.exists()) {
            templateResolver.setTemplateMode("TEXT");
            new RobotsRenderer(templateEngine, sitemapFileName).render(null, robotsFile);
        }

        File copyOnlyDir = new File(draftDir, copyOnlyDirName);
        if (copyOnlyDir.exists()) {
            recursiveCopy(copyOnlyDir, targetDir);
        }
    }

    private void addFiles(String[] fileNames, Renderer renderer) {
        for (String fileName : fileNames) {
            File draft = new File(draftDir, fileName);
            if (!draft.exists()) {
                LOGGER.warn("I have listed a file but can not access it now. Skipping \"{}\"", fileName);
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
                LOGGER.warn("I think something must be wrong with \"{}\" to generate {}.", fileName, info);
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
        } else if (title.startsWith("Hidden:") || title.startsWith("Draft:")) {
            LOGGER.info("I am skipping the file \"{}\" titled \"{}\" during indexing due to the prefix.",
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

    public void recursiveCopy(File src, File target) {
        if (src.isDirectory()) {
            File[] files = src.listFiles();
            if (files == null) return;
            for (File file : files) {
                if (file.getName().equals(".hidden")) {
                    return;
                }
                File targetFile = new File(target, file.getName());
                recursiveCopy(file, targetFile);
            }
        } else if (src.isFile()) {
            if (src.lastModified() > target.lastModified()) {
                try {
                    Files.copy(src.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    LOGGER.info("I am copying \"{}\"", src.getPath());
                } catch (IOException e) {
                    LOGGER.warn("I failed to copy \"{}\" to \"{}\".", src.getPath(), target.getPath());
                    LOGGER.debug("ST: ", e);
                }
            }
        }
    }
}
