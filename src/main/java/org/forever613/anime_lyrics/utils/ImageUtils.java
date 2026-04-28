/*
 * This file is part of AnimeLyricsWebsite.
 * Copyright (C) 2026 613_forever
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

package org.forever613.anime_lyrics.utils;

import org.forever613.anime_lyrics.Config;
import org.forever613.anime_lyrics.ImageInfo;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

public class ImageUtils {
    public static ImageInfo loadImageInfo(File draftDir, String image) {
        ImageInfo info = new ImageInfo();
        String relativeToRootPath = image.charAt(0) == '/' ? image.substring(1) : image;
        info.setAbsolutePath(Config.getInstance().getRootUrl() + relativeToRootPath);

        Path imageFilePath = draftDir.toPath().resolve("copy_only").resolve(relativeToRootPath);
        try (ImageInputStream iis = ImageIO.createImageInputStream(Files.newInputStream(imageFilePath))) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            if (readers.hasNext()) {
                ImageReader reader = readers.next();
                reader.setInput(iis, false, false);
                String mimeType = reader.getOriginatingProvider().getMIMETypes()[0];
                info.setMimeType(mimeType);
                info.setWidth(reader.getWidth(0));
                info.setHeight(reader.getHeight(0));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return info;
    }
}
