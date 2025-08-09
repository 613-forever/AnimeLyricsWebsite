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
import org.forever613.anime_lyrics.GeneratedFileInfo;
import org.forever613.anime_lyrics.utils.DateUtils;
import org.forever613.anime_lyrics.utils.HtmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;

public class MarkdownRenderer implements Renderer {
    private final TemplateEngine templateEngine;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public MarkdownRenderer(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    @Override
    public GeneratedFileInfo render(File draft, File target) {
        GeneratedFileInfo info;
        File tempFile;
        try {
            tempFile = File.createTempFile("cache", ".html");
            tempFile.deleteOnExit();
            logger.debug("I have created a temporary file \"{}\" and marked it to be deleted on exit.", tempFile.getAbsolutePath());
            String cmd = "pandoc -f markdown_phpextra -t html5 -o \"" +
                    tempFile.getAbsolutePath() + "\" \"" + draft.getAbsolutePath() + "\"";
            logger.debug("I am planning to run \"{}\"", cmd);
            int exitValue = Runtime.getRuntime().exec(cmd).waitFor();
            logger.debug("I have called pandoc for \"{}\". It exited with the exit value {}", draft.getName(), exitValue);
            if (exitValue != 0) {
                logger.warn("Pandoc for \"{}\" exits with an exit value {}.", draft.getName(), exitValue);
                return null;
            }
        } catch (IOException | InterruptedException e) {
            logger.warn("An exception is thrown when running pandoc for \"{}\" externally: {}", draft.getName(), e.getMessage());
            logger.debug("ST: ", e);
            return null;
        }
        try (Reader reader = new InputStreamReader(Files.newInputStream(tempFile.toPath()), StandardCharsets.UTF_8)) {
            char[] buffer = new char[(int) tempFile.length()];
            //noinspection ResultOfMethodCallIgnored
            reader.read(buffer);
            String content = String.valueOf(buffer);
            info = HtmlUtils.splitInfo(content);

            Context context = new Context();
            context.setVariable("nameTitle", Config.getInstance().getNameTitle());
            context.setVariable("nameFooter", Config.getInstance().getNameFooter());

            context.setVariable("title", info.getTitle());
            context.setVariable("author", info.getAuthor());
            if (info.getKeywords() != null) context.setVariable("keywords", String.join(", ", info.getKeywords()));
            if (info.getDescription() != null) context.setVariable("description", info.getDescription());
            context.setVariable("createdTime", DateUtils.format(info.getPubdate()));
            context.setVariable("modifiedTime", DateUtils.format(draft.lastModified()));
            context.setVariable("content", info.getOtherContent());
            context.setVariable("lyricsJS", false);

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
                logger.warn("I have just failed to generate an HTML \"{}\": {}", target.getName(), e.getMessage());
                logger.debug("ST: ", e);
                return null;
            }
        } catch (IOException e) {
            logger.warn("I have just failed to open the generated HTML for \"{}\": {}", draft.getName(), e.getMessage());
            logger.debug("ST: ", e);
            return null;
        }
        logger.info("I have generated the file \"{}\" successfully.", target.getName());
        return info;
    }
}
