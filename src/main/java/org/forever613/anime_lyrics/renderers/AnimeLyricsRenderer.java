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

package org.forever613.anime_lyrics.renderers;

import org.forever613.anime_lyrics.Config;
import org.forever613.anime_lyrics.GeneratedFileInfo;
import org.forever613.anime_lyrics.ImageInfo;
import org.forever613.anime_lyrics.parser.Parser;
import org.forever613.anime_lyrics.parser.ParsingException;
import org.forever613.anime_lyrics.utils.DateUtils;
import org.forever613.anime_lyrics.utils.ImageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;

public class AnimeLyricsRenderer implements Renderer {
    private final TemplateEngine templateEngine;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public AnimeLyricsRenderer(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    @Override
    public GeneratedFileInfo render(File draft, File target) {
        StringWriter stringWriter = new StringWriter();
        logger.debug("I am planning to generate \"{}\" from \"{}\"", target.getName(), draft.getName());

        Parser parser = new Parser(templateEngine);
        GeneratedFileInfo info;
        try {
            info = parser.parse(draft.toPath(), stringWriter);
        } catch (ParsingException e) {
            logger.warn("I have just failed to generate file \"{}\" from \"{}\" due to loading or syntactic errors.",
                    target.getName(), draft.getName());
            logger.debug("");
            return null;
        }
        String cachedString = stringWriter.getBuffer().toString();

        ImageInfo image = null;
        if (info.getImage() != null) {
            image = ImageUtils.loadImageInfo(draft.getParentFile(), info.getImage());
        }

        Context context = new Context();
        context.setVariable("nameTitle", Config.getInstance().getNameTitle());
        context.setVariable("nameFooter", Config.getInstance().getNameFooter());

        context.setVariable("title", info.getTitle());
        context.setVariable("author", info.getAuthor());
        context.setVariable("keywords", String.join(", ", info.getKeywords()));
        context.setVariable("keywordsList", info.getKeywords());
        context.setVariable("description", info.getDescription());
        context.setVariable("createdTime", DateUtils.format(info.getPubdate()));
        context.setVariable("modifiedTime", DateUtils.format(draft.lastModified()));
        context.setVariable("createdTimeISO8601", DateUtils.formatISO8601(info.getPubdate()));
        context.setVariable("modifiedTimeISO8601", DateUtils.formatISO8601(draft.lastModified()));
        context.setVariable("content", cachedString);
        context.setVariable("styles", parser.getTemplateParser().getRequiredCSS());
        context.setVariable("scripts", parser.getTemplateParser().getRequiredJS());
        context.setVariable("url", Config.getInstance().getRootUrl() + target.getName());
        context.setVariable("image", image);

        // Make it a plugin later.
        Map<String, String> otherConfigMap = Config.getInstance().getOtherConfigMap();
        if (!otherConfigMap.isEmpty()) {
            context.setVariable("googleAdSenseClient", otherConfigMap.get("googleAdSenseClient"));
            context.setVariable("googleAdSenseSlot", otherConfigMap.get("googleAdSenseSlot"));
        }

        String html = templateEngine.process("embed", context);
        try (Writer writer = new OutputStreamWriter(Files.newOutputStream(target.toPath()), StandardCharsets.UTF_8)) {
            writer.write(html);
        } catch (IOException e) {
            logger.warn("I have just failed to generate file \"{}\" when writing : {}", target.getName(), e.getMessage());
            logger.debug("ST: ", e);
            return null;
        }

        logger.info("I have generated the file \"{}\" successfully.", target.getName());
        return info;
    }
}
