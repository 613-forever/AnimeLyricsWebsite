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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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

        Context context = new Context();
        context.setVariable("nameTitle", Config.getInstance().getNameTitle());
        context.setVariable("nameFooter", Config.getInstance().getNameFooter());

        context.setVariable("articles", fileCollector.getArticles());
        context.setVariable("helpArticles", fileCollector.getHelpArticles());
        context.setVariable("sysArticles", fileCollector.getSysArticles());

        // Make it a plugin later.
        Map<String, String> otherConfigMap = Config.getInstance().getOtherConfigMap();
        if (!otherConfigMap.isEmpty()) {
            context.setVariable("googleAdSenseClient", otherConfigMap.get("googleAdSenseClient"));
            context.setVariable("googleAdSenseSlot", otherConfigMap.get("googleAdSenseSlot"));
        }

        try (Writer writer = new OutputStreamWriter(Files.newOutputStream(target.toPath()), StandardCharsets.UTF_8)) {
            writer.write(templateEngine.process("index", context));
        } catch (IOException e) {
            logger.warn("I have just failed to generate the index page. " + e.getMessage());
            logger.debug("ST: ", e);
            return null;
        }

        logger.info("I have generated the index page successfully.");
        GeneratedFileInfo info = new GeneratedFileInfo();
        info.setTitle("index");
        return info;
    }
}
