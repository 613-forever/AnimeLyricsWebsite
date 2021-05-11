package org.forever613.anime_lyrics.parser;

public class ParsingException extends RuntimeException {
    @Override
    public String getMessage() {
        return "Exception raised in parsing source files.";
    }
}
