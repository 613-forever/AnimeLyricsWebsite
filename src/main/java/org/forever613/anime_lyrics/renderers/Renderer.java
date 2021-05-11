package org.forever613.anime_lyrics.renderers;

import java.io.File;

@FunctionalInterface
public interface Renderer {
    String render(File draft, File target);
}
