package org.forever613.anime_lyrics.renderers;

import org.forever613.anime_lyrics.GeneratedFileInfo;

import java.io.File;

@FunctionalInterface
public interface Renderer {
    GeneratedFileInfo render(File draft, File target);
}
