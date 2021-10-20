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

package org.forever613.anime_lyrics.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.IContext;

import java.util.*;

public class TemplateParser {
    Logger logger = LoggerFactory.getLogger(this.getClass());
    private final TemplateEngine templateEngine;
    private final Parser parser;
    private final Set<String> requiredCSS = new HashSet<>(), requiredJS = new HashSet<>();

    public TemplateParser(TemplateEngine templateEngine, Parser parser) {
        this.templateEngine = templateEngine;
        this.parser = parser;
    }

    private String process(String templateSheet, String templateName, IContext context, boolean css, boolean js) {
        if (css) {
            requiredCSS.add(templateSheet + "::" + templateName + "-css");
        }
        if (js) {
            requiredJS.add(templateSheet + "::" + templateName + "-js");
        }
        return templateEngine.process(templateSheet, Collections.singleton(templateName), context);
    }

    String makeButtonTemplate(String controllingTemplateName, String[] langs) {
        Context context = new Context();
        context.setVariable("langs", langs);
        return process("controls", controllingTemplateName, context, true, true);
    }

    String makeLinkTemplate(String templateName, String literal, String href) {
        switch (templateName) {
        case "m":
        case "moegirl": {
            return process("templates", "moegirl", wrapLinkParams(literal, href), false, false);
        }
        case "b":
        case "bilibili": {
            return process("templates", "bilibili", wrapLinkParams(literal, href), false, false);
        }
        case "bv":
        case "bilibili_video": {
            return process("templates", "bilibili-video", wrapBiliLinkParamsWithPage(literal, href), false, false);
        }
        case "i":
        case "internal": {
            if (href.startsWith("//") || (!href.startsWith(".") && !href.startsWith("/"))) {
                href = "./" + href;
            }
            Context context = new Context();
            context.setVariable("literal", literal);
            context.setVariable("href", href);
            return process("templates", "internal", context, false, false);
        }
        case "a":
        case "anchor": {
            if (!href.startsWith("#")) {
                href = "#" + href;
            }
            Context context = new Context();
            context.setVariable("literal", literal);
            context.setVariable("href", href);
            return process("templates", "anchor", context, false, false);
        }
        case "e":
        case "external":
        case "": {
            Context context = new Context();
            context.setVariable("literal", literal);
            context.setVariable("href", href);
            return process("templates", "external", context, false, false);
        }
        default: {
            throw new TemplateParsingException(templateName);
        }
        }
    }

    public String makeTemplate(String name, List<String> params) {
        switch (name) {
        // with css
        case "music163-card":
        case "spoiler": {
            return process("templates", name, wrapParams(params), true, false);
        }
        case "bvc":
        case "bilibili-video-card": {
            return process("templates", "bilibili-video-card", wrapBiliLinkParamsWithPage(params), true, true);
        }
        // with no dependent css and js
        default: {
            return process("templates", name, wrapParams(params), false, false);
        }
        }
    }

    private Context wrapParams(List<String> params) {
        Context context = new Context();
        context.setVariable("params", params);
        return context;
    }

    private Context wrapLinkParams(List<String> params) {
        Context context = new Context();
        context.setVariable("literal", params.get(0));
        context.setVariable("href", params.get(1));
        return context;
    }

    private Context wrapLinkParams(String literal, String href) {
        Context context = new Context();
        context.setVariable("literal", literal);
        context.setVariable("href", href);
        return context;
    }

    private Context wrapBiliLinkParamsWithPage(List<String> params) {
        if (params.size() != 2) {
            logger.error("Bilibili link is called with an incorrect number of params.");
            throw new ParsingException();
        }
        Context context = new Context();
        context.setVariable("literal", params.get(0));
        if (params.get(1).contains(",")) {
            String[] names = params.get(1).split(",", 2);
            context.setVariable("bvId", names[0]);
            context.setVariable("page", names[1]);
        } else {
            context.setVariable("bvId", params.get(1));
            context.setVariable("page", "1");
        }
        return context;
    }

    private Context wrapBiliLinkParamsWithPage(String literal, String href) {
        Context context = new Context();
        context.setVariable("literal", literal);
        if (href.contains(",")) {
            String[] names = href.split(",", 2);
            context.setVariable("bvId", names[0]);
            context.setVariable("page", names[1]);
        } else {
            context.setVariable("bvId", href);
            context.setVariable("page", "1");
        }
        return context;
    }

    private List<String> getRequiredSource(Set<String> requiredCSS) {
        List<String> css = new ArrayList<>();
        Context context = new Context();
        for (String cssFrag : requiredCSS) {
            String[] templateId = cssFrag.split("::", 2);
            css.add(templateEngine.process(templateId[0], Collections.singleton(templateId[1]), context));
        }
        return css;
    }

    public List<String> getRequiredCSS() {
        return getRequiredSource(requiredCSS);
    }

    public List<String> getRequiredJS() {
        return getRequiredSource(requiredJS);
    }
}
