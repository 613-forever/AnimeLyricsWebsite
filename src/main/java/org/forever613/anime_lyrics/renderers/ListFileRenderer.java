package org.forever613.anime_lyrics.renderers;

import org.forever613.anime_lyrics.FileCollector;
import org.forever613.anime_lyrics.GeneratedFileInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.*;
import java.nio.charset.StandardCharsets;

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
        context.setVariable("articles", fileCollector.getArticles());
        context.setVariable("helpArticles", fileCollector.getHelpArticles());
        context.setVariable("sysArticles", fileCollector.getSysArticles());

        try (Writer writer = new OutputStreamWriter(new FileOutputStream(target), StandardCharsets.UTF_8)) {
            writer.write(templateEngine.process("index", context));
        } catch (IOException e) {
            logger.warn("I have just failed to generate the index page. " + e.getMessage());
            logger.debug("ST: ", e);
            return null;
        }

        logger.info("I have generated the index page successfully.");
        return new GeneratedFileInfo("index", null, null);
    }
}
