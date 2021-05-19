package org.forever613.anime_lyrics.parser;

public class TemplateParsingException extends ParsingException {
    public String templateName;

    public TemplateParsingException(String templateName) {
        this.templateName = templateName;
    }

    @Override
    public String getMessage() {
        return "An erroneous link template named \"" + templateName + "\" is called.";
    }
}
