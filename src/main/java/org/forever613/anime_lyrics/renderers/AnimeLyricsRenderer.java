package org.forever613.anime_lyrics.renderers;

import org.forever613.anime_lyrics.GeneratedFileInfo;
import org.forever613.anime_lyrics.parser.Parser;
import org.forever613.anime_lyrics.parser.ParsingException;
import org.forever613.anime_lyrics.utils.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class AnimeLyricsRenderer implements Renderer {
    private final TemplateEngine templateEngine;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public AnimeLyricsRenderer(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    @Override
    public GeneratedFileInfo render(File draft, File target) {
        GeneratedFileInfo info;
        try (Reader reader = new InputStreamReader(new FileInputStream(draft), StandardCharsets.UTF_8)) {
            StringWriter stringWriter = new StringWriter();
            logger.debug("I am planning to generate \"{}\" from \"{}\"", target.getName(), draft.getName());

            Parser parser = new Parser(templateEngine);
            try {
                info = parser.parse(reader, stringWriter);
            } catch (ParsingException e) {
                logger.warn("I have just failed to generate file \"{}\" from \"{}\" due to syntax errors.",
                        target.getName(), draft.getName());
                logger.debug("");
                return null;
            }
            String cachedString = stringWriter.getBuffer().toString();

            Context context = new Context();
            context.setVariable("title", info.getTitle());
            context.setVariable("author", "forever613");
            context.setVariable("createdTime", DateUtils.format(info.getPubdate()));
            context.setVariable("modifiedTime", DateUtils.format(draft.lastModified()));
            context.setVariable("content", cachedString);
            context.setVariable("styles", parser.getTemplateParser().getRequiredCSS());
            context.setVariable("scripts", parser.getTemplateParser().getRequiredJS());

            String html = templateEngine.process("embed", context);
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(target), StandardCharsets.UTF_8)) {
                writer.write(html);
            } catch (IOException e) {
                logger.warn("I have just failed to generate file \"{}\" when writing : {}", target.getName(), e.getMessage());
                logger.debug("ST: ", e);
                return null;
            }
        } catch (IOException e) {
            logger.warn("I have just failed to generate file \"{}\" when reading and parsing : {}", target.getName(), e.getMessage());
            logger.debug("ST: ", e);
            return null;
        }
        logger.info("I have generated the file \"{}\" successfully.", target.getName());
        return info;
    }
}
