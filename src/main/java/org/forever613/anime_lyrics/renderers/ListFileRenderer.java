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

import org.forever613.anime_lyrics.Config;
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
import java.nio.file.Files;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Map;

public class ListFileRenderer implements Renderer {
    private final TemplateEngine templateEngine;
    private final FileCollector fileCollector;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public ListFileRenderer(TemplateEngine templateEngine, FileCollector fileCollector) {
        this.templateEngine = templateEngine;
        this.fileCollector = fileCollector;
    }

    @Override
    public GeneratedFileInfo render(File draft, File target) {
        assert draft == null;

        ZonedDateTime createdTime = ZonedDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        for (SourceFileInfo fileInfo : fileCollector.getArticles()) {
            if (fileInfo.getCreatedDate().isBefore(createdTime)) {
                createdTime = fileInfo.getCreatedDate();
            }
        }
        for (SourceFileInfo fileInfo : fileCollector.getHelpArticles()) {
            if (fileInfo.getCreatedDate().isBefore(createdTime)) {
                createdTime = fileInfo.getCreatedDate();
            }
        }
        for (SourceFileInfo fileInfo : fileCollector.getSysArticles()) {
            if (fileInfo.getCreatedDate().isBefore(createdTime)) {
                createdTime = fileInfo.getCreatedDate();
            }
        }
        ZonedDateTime modifiedTime = ZonedDateTime.now().truncatedTo(ChronoUnit.SECONDS);

        Context context = new Context();
        context.setVariable("nameTitle", Config.getInstance().getNameTitle());
        context.setVariable("nameFooter", Config.getInstance().getNameFooter());

        context.setVariable("articles", fileCollector.getArticles());
        context.setVariable("helpArticles", fileCollector.getHelpArticles());
        context.setVariable("sysArticles", fileCollector.getSysArticles());

        GeneratedFileInfo info = new GeneratedFileInfo();
        info.setTitle("index");
        info.setKeywords(Arrays.asList("动画歌词", "动漫歌词", "歌曲列表"));
        info.setDescription(String.format("本页面是%s的歌词收藏册的主页及列表页。", info.getAuthor()));

        context.setVariable("author", info.getAuthor());
        context.setVariable("title", info.getTitle());
        context.setVariable("keywords", String.join(", ", info.getKeywords()));
        context.setVariable("description", info.getDescription());
        context.setVariable("createdTimeISO8601", DateUtils.formatISO8601(createdTime));
        context.setVariable("modifiedTimeISO8601", DateUtils.formatISO8601(modifiedTime));
        context.setVariable("url", Config.getInstance().getRootUrl());

        // Make it a plugin later.
        Map<String, String> otherConfigMap = Config.getInstance().getOtherConfigMap();
        if (!otherConfigMap.isEmpty()) {
            context.setVariable("googleAdSenseClient", otherConfigMap.get("googleAdSenseClient"));
            context.setVariable("googleAdSenseSlot", otherConfigMap.get("googleAdSenseSlot"));
        }

        try (Writer writer = new OutputStreamWriter(Files.newOutputStream(target.toPath()), StandardCharsets.UTF_8)) {
            writer.write(templateEngine.process("index", context));
        } catch (IOException e) {
            logger.warn("I have just failed to generate the index page. {}", e.getMessage());
            logger.debug("ST: ", e);
            return null;
        }

        logger.info("I have generated the index page successfully.");

        return info;
    }
}
