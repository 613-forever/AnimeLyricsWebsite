package org.forever613.anime_lyrics.renderers;

import org.forever613.anime_lyrics.FileCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class RobotsRenderer implements Renderer {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final TemplateEngine templateEngine;
    private final String rootUrl;
    private final String sitemap;

    public RobotsRenderer(TemplateEngine templateEngine, String rootUrl, String sitemap) {
        this.templateEngine = templateEngine;
        this.rootUrl = rootUrl;
        this.sitemap = sitemap;
    }

    @Override
    public String render(File draft, File target) {
        assert draft == null;

        Context context = new Context();
        context.setVariable("url", rootUrl);
        context.setVariable("sitemap", sitemap);

        try (Writer writer = new OutputStreamWriter(new FileOutputStream(target), StandardCharsets.UTF_8)) {
            writer.write(templateEngine.process("robots.txt", context));
        } catch (IOException e) {
            logger.warn("I have just failed to generate the \"robots.txt\" file. " + e.getMessage());
            logger.debug("ST: ", e);
            return null;
        }

        logger.info("I have generated the robots.txt successfully.");
        return "index";
    }
}
