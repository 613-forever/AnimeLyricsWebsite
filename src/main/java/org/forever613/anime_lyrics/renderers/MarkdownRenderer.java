package org.forever613.anime_lyrics.renderers;

import org.forever613.anime_lyrics.GeneratedFileInfo;
import org.forever613.anime_lyrics.utils.DateUtils;
import org.forever613.anime_lyrics.utils.HtmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.*;
import java.nio.charset.StandardCharsets;

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
        try (Reader reader = new InputStreamReader(new FileInputStream(tempFile), StandardCharsets.UTF_8)) {
            char[] buffer = new char[(int) tempFile.length()];
            //noinspection ResultOfMethodCallIgnored
            reader.read(buffer);
            String content = String.valueOf(buffer);
            info = HtmlUtils.splitInfo(content);

            Context context = new Context();
            context.setVariable("title", info.getTitle());
            context.setVariable("author", "forever613");
            context.setVariable("createdTime", DateUtils.format(info.getPubdate()));
            context.setVariable("modifiedTime", DateUtils.format(draft.lastModified()));
            context.setVariable("content", info.getOtherContent());
            context.setVariable("lyricsJS", false);

            String html = templateEngine.process("embed", context);
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(target), StandardCharsets.UTF_8)) {
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
