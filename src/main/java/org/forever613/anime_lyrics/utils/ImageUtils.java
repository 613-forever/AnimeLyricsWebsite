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

import java.io.*;
import java.nio.file.Files;

public class ImageUtils {
    public static ImageInfo loadImageInfo(File draftDir, String image) {
        ImageInfo info = new ImageInfo();
        info.setAbsolutePath(Config.getInstance().getRootUrl() + (image.charAt(0) == '/' ? image.substring(1) : image));

        try (InputStream is = Files.newInputStream(draftDir.toPath().resolve("copy_only").resolve(image))) {
            extractDimension(info, is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return info;
    }

    public static void extractDimension(ImageInfo info, InputStream is) throws IOException {
        int readSize = 30;
        byte[] data = new byte[readSize];
        int result = is.read(data);
        if (result == readSize && new String(data, 0, 4).equals("RIFF") && data[15] == 'X') {
            info.setWidth(1 + get24bit(data, 24));
            info.setHeight(1 + get24bit(data, 27));
        }
    }

    // 读取24位小端序(Little-Endian)整数
    private static int get24bit(byte[] data, int index) {
        return (data[index] & 0xFF) |
                ((data[index + 1] & 0xFF) << 8) |
                ((data[index + 2] & 0xFF) << 16);
    }
}
