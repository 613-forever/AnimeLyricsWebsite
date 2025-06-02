/*
 * This file is part of AnimeLyricsWebsite.
 * Copyright (C) 2021 613_forever
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

package org.forever613.anime_lyrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;

public class Config {
    private static final Logger LOGGER = LoggerFactory.getLogger(Config.class);

    private String nameTitle;
    private String nameFooter;
    private String rootUrl;
    private Map<String, String> configs;

    private static Config instance;

    static void loadConfig(File file) {
        if (instance != null) {
            throw new IllegalStateException();
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(file.toPath()), StandardCharsets.UTF_8))) {
            instance = new Config();
            instance.configs = new java.util.HashMap<>();
            String buffer = reader.readLine();
            while (buffer != null) {
                if (buffer.contains("=")) {
                    String[] kv = buffer.split("=", 2);
                    switch (kv[0]) {
                    case "nameTitle":
                        instance.nameTitle = kv[1];
                        break;
                    case "nameFooter":
                        instance.nameFooter = kv[1];
                        break;
                    case "url":
                        instance.rootUrl = kv[1].endsWith("/") ? kv[1] : kv[1] + "/";
                        break;
                    default:
                        instance.configs.put(kv[0], kv[1]);
                    }
                }
                buffer = reader.readLine();
            }
        } catch (IOException e) {
            LOGGER.error("I have failed to load root URL from the configuration file.");
            throw new IllegalArgumentException();
        }
    }

    public static Config getInstance() {
        if (instance == null) {
            throw new IllegalStateException();
        }
        return instance;
    }

    public String getNameTitle() {
        return nameTitle;
    }

    public String getNameFooter() {
        return nameFooter;
    }

    public String getRootUrl() {
        return rootUrl;
    }

    public Map<String, String> getOtherConfigMap() {
        return configs;
    }
}
