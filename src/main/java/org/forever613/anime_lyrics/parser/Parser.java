package org.forever613.anime_lyrics.parser;

import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.HTMLWriter;
import org.dom4j.io.OutputFormat;
import org.forever613.anime_lyrics.GeneratedFileInfo;
import org.forever613.anime_lyrics.parser.grammar.AnimeLyricsL;
import org.forever613.anime_lyrics.parser.grammar.AnimeLyricsP;
import org.forever613.anime_lyrics.parser.grammar.AnimeLyricsPBaseVisitor;
import org.forever613.anime_lyrics.utils.DateUtils;
import org.forever613.anime_lyrics.utils.HtmlUtils;
import org.forever613.anime_lyrics.utils.KanaUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.*;

public class Parser extends AnimeLyricsPBaseVisitor<Element> {
    Logger logger = LoggerFactory.getLogger(this.getClass());
    String name, creationTime;
    TemplateParser templateParser;
    int lyricsStartLine = -1;
    static final String DEFAULT_LANG = "zh", JAPANESE_LANG = "ja", JAPANESE_ROMAJI = "ja-latn";
    Set<String> trans = new LinkedHashSet<>();

    public Parser(TemplateEngine templateEngine) {
        templateParser = new TemplateParser(templateEngine, this);
    }

    public GeneratedFileInfo parse(Reader reader, Writer writer) {
        try {
            AnimeLyricsL lexer = new AnimeLyricsL(CharStreams.fromReader(reader));
            AnimeLyricsP parser = new AnimeLyricsP(new BufferedTokenStream(lexer));
            OutputFormat format = OutputFormat.createCompactFormat();
            HTMLWriter htmlWriter = new HTMLWriter(format);
            htmlWriter.setEscapeText(false);
            htmlWriter.setWriter(writer);
            htmlWriter.write(visitFile(parser.file()));
            return new GeneratedFileInfo(name, DateUtils.fromFormatted(creationTime), null);
        } catch (IOException e) {
            logger.error("An exception is thrown when loading or writing: {}.", e.getMessage());
            logger.debug("The exception ST: ", e);
            throw new ParsingException();
        }
    }

    public TemplateParser getTemplateParser() {
        return templateParser;
    }

    @Override
    public Element visitFile(AnimeLyricsP.FileContext ctx) {
        Element root = DocumentHelper.createElement("div");
        root.addAttribute("class", "anime_lyrics");

        name = HtmlUtils.escapeHtml(ctx.name().getText().trim());

        Element article = visitArticle(ctx.article());
        article.addAttribute("class", "article");
        root.add(article);

        return root;
    }

    @Override
    public Element visitArticle(AnimeLyricsP.ArticleContext ctx) {
        Element article = DocumentHelper.createElement("article");
        List<AnimeLyricsP.Article_lineContext> article_line = ctx.article_line();
        for (int i = 0; i < article_line.size(); i++) {
            AnimeLyricsP.Article_lineContext articleLineContext = article_line.get(i);
            if (articleLineContext.markup_text() != null) {
                article.add(visitMarkup_text(articleLineContext.markup_text()));
            } else if (articleLineContext.header() != null) {
                article.add(visitHeader(articleLineContext.header()));
            } else if (articleLineContext.list() != null) {
                List<AnimeLyricsP.ListContext> sequentLists = new ArrayList<>();
                sequentLists.add(articleLineContext.list());
                while (i + 1 < article_line.size() && article_line.get(i + 1).list() != null) {
                    sequentLists.add(article_line.get(++i).list());
                }
                article.add(makeLists(sequentLists));
            } else if (articleLineContext.hr() != null) {
                article.addElement("hr");
            } else if (articleLineContext.template() != null) {
                article.add(visitTemplate(articleLineContext.template()));
            } else if (articleLineContext.lyrics() != null) {
                Element lyrics_root = visitLyrics(articleLineContext.lyrics());
                article.add(lyrics_root);
            } else if (articleLineContext.parse_only() != null) {
                visitMarkup_text(articleLineContext.parse_only().markup_text()); // ignore results
            }
        }
        return article;
    }

    @Override
    public Element visitHeader(AnimeLyricsP.HeaderContext ctx) {
        int level = ctx.mark.size() + 1;
        level = Math.max(2, Math.min(level, 6));
        Element header = DocumentHelper.createElement("h" + level);
        String text = makeWords(ctx.words());
        header.addText(text);
        header.addAttribute("id", text);
        return header;
    }

    @Override
    public Element visitMarkup_text(AnimeLyricsP.Markup_textContext ctx) {
        Element para = DocumentHelper.createElement("p");
        para.setContent(makeMarkup(ctx));
        return para;
    }

    @Override
    public Element visitTemplate(AnimeLyricsP.TemplateContext ctx) {
        String name = ctx.templ.getText();
        List<String> params = new ArrayList<>();
        for (Token param : ctx.params) {
            params.add(makeWord(param.getText()));
        }
        String html;
        try {
            html = templateParser.makeTemplate(name, params);
        } catch (ParsingException e) {
            logger.error("At {}:{}, an exception is thrown handling named template \"{}\" with params {}.",
                    ctx.start.getLine(), ctx.start.getCharPositionInLine(), name, params);
            throw e;
        }
        try {
            return DocumentHelper.parseText(html).getRootElement();
        } catch (DocumentException e) {
            logger.error("At {}:{}, an exception is thrown handling named template {}.",
                    ctx.start.getLine(), ctx.start.getCharPositionInLine(), name);
            logger.debug("HTML: {}", html);
            logger.debug("ST: ", e);
            throw (ParsingException) new ParsingException().initCause(e);
        }
    }

    @Override
    public Element visitLyrics(AnimeLyricsP.LyricsContext ctx) {
        boolean mode = (ctx.start_lyrics_fense().VALUE() == null ||
                Boolean.parseBoolean(ctx.start_lyrics_fense().VALUE().getText().substring(1)));
        List<Node> lyricsLines = new ArrayList<>();
        String background = null;
        if (mode) {
            lyricsLines.add(null); // reserve for head control.
        }
        lyricsStartLine = ctx.start.getLine();
        for (AnimeLyricsP.Meta_lineContext meta_lineContext : ctx.meta_line()) {
            if (meta_lineContext.markup_text() != null) {
                if (meta_lineContext.markup_text().getText().startsWith("background:")) {
                    background = meta_lineContext.markup_text().getText().substring(11);
                } else {
                    Element line = DocumentHelper.createElement("p");
                    line.addAttribute("class", "lyrics-meta");
                    line.addAttribute("lang", DEFAULT_LANG);
                    line.setContent(makeMarkup(meta_lineContext.markup_text()));
                    lyricsLines.add(line);
                }
            }
        }
        for (AnimeLyricsP.Lyrics_lineContext lyrics_lineContext : ctx.lyrics_line()) {
            // work around for return 4 p-tags.
            lyricsLines.addAll(makeLyrics_line(lyrics_lineContext));
        }

        Element lyrics_root, lyrics;

        if (mode) {
            lyrics_root = DocumentHelper.createElement("div");
            lyrics_root.addAttribute("class", "lyrics_outer");
            if (background != null) {
                lyrics_root.addAttribute("style", "background-image: url(\"" + background + "\")");
            }
            lyrics = lyrics_root.addElement("section");
            lyrics.addAttribute("class", "lyrics");

            String controlName = "lyrics-control";
            String html = templateParser.makeButtonTemplate(controlName, trans.toArray(new String[0]));

            try {
                Element node = DocumentHelper.parseText(html).getRootElement();
                lyricsLines.set(0, node);
                lyricsLines.add(node.createCopy());
            } catch (DocumentException e) {
                logger.error("At {}:{}, an exception is thrown handling control template {}",
                        ctx.start.getLine(), ctx.start.getCharPositionInLine(), controlName);
                logger.debug("HTML: {}", html);
                logger.debug("ST: ", e);
                throw (ParsingException) new ParsingException().initCause(e);
            }
        } else {
            lyrics = DocumentHelper.createElement("section");
            lyrics_root = lyrics;
            lyrics.addAttribute("class", "lyrics");
        }
        lyrics.setContent(lyricsLines);
        return lyrics_root;
    }

    public List<Element> makeLyrics_line(AnimeLyricsP.Lyrics_lineContext ctx) {
        List<Element> elements = new ArrayList<>();
        if (ctx.lyrics_text() != null) {
            JapaneseTextNode textNode = makeLyricsMarkup(ctx.lyrics_text());
            textNode.addTo(elements);
        } else if (ctx.translation_text() != null) {
            Element line = DocumentHelper.createElement("p");
            line.setContent(makeMarkup(ctx.translation_text().markup_text()));
            Token token = ctx.translation_text().start_trans().lang;
            String lang = token != null ? token.getText().substring(1) : DEFAULT_LANG;
            trans.add(lang);
            line.addAttribute("lang", lang);
            line.addAttribute("class", "trans-" + lang + "-text-line");
            elements.add(line);
        } else {
            elements.add(DocumentHelper.createElement("br"));
        }
        return elements;
    }

    /**
     * Make "words" contexts into strings.
     * "Words" contains space-separated "word", which is plain and escaped characters only.
     */
    private String makeWords(AnimeLyricsP.WordsContext wordsContext) {
        StringBuilder sb = new StringBuilder();
        for (AnimeLyricsP.Word_or_puncContext word : wordsContext.word_or_punc()) {
            sb.append(makeWordOrPunctuation(word));
        }
        return sb.toString();
    }

    /**
     * Make "word" contexts into strings.
     * Contains either plain and escaped characters only, or escaped raw string surrounded by backward quotation marks.
     */
    private String makeWordOrPunctuation(AnimeLyricsP.Word_or_puncContext wordContext) {
        if (wordContext.PUNCTUATION() != null) {
            return HtmlUtils.escapeHtml(wordContext.PUNCTUATION().getText());
        } else if (wordContext.WORD() != null) {
            return makeWord(wordContext.WORD().getText());
        } else if (wordContext.RAW() != null) {
            String wrapped = wordContext.RAW().getText();
            String content = wrapped.substring(1, wrapped.length() - 1).replace("\\`", "`");
            return HtmlUtils.escapeHtml(content);
        }
        throw new IllegalStateException();
    }

    private String makeWord(String text) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); ) {
            char c = text.charAt(i);
            ++i;
            if (c == '\\') {
                if (text.charAt(i) == '\r') {
                    ++i;
                    ++i;
                } else if (text.charAt(i) == '\n') {
                    ++i;
                } else {
                    sb.append(HtmlUtils.escapeHtml(text.charAt(i)));
                    ++i;
                }
            } else if (c != '@') {
                sb.append(HtmlUtils.escapeHtml(c));
            }
        }
        return sb.toString();
    }

    /**
     * Make unordered lists from sequent "list" contexts.
     */
    private Element makeLists(List<AnimeLyricsP.ListContext> sequentLists) {
        Element rootList = DocumentHelper.createElement("ul");
        Element[] parents = new Element[6];
        int currentLevel = 1;
        parents[0] = rootList;
        Element currentItem = parents[currentLevel - 1].addElement("li");
        for (int i = 0; ; ) {
            // extract content from this line
            AnimeLyricsP.ListContext listContext = sequentLists.get(i);
            List<Node> content = makeMarkup(listContext.markup_text());
            if (listContext.footnote_def() == null) {
                currentItem.setContent(content);
            } else {
                Element span = currentItem.addElement("span");
                span.addAttribute("class", "footnote-definition");
                String id = makeWord(listContext.footnote_def().ft_id.getText());
                span.addAttribute("id", "footnote-" + id);
                span.setContent(content);
                span.content().add(0, DocumentHelper.createText("[" + id + "]:&nbsp;"));
            }
            if (i + 1 == sequentLists.size()) {
                break;
            }
            // preview next line to change list level
            AnimeLyricsP.ListContext nextListContext = sequentLists.get(++i);
            int nextLevel = nextListContext.mark.size();
            if (nextLevel == currentLevel) {
                currentItem = parents[currentLevel - 1].addElement("li");
            } else if (nextLevel == currentLevel + 1) {
                parents[nextLevel - 1] = currentItem.addElement("ul");
                currentLevel = nextLevel;
                currentItem = parents[currentLevel - 1].addElement("li");
            } else if (nextLevel < currentLevel) {
                for (int j = nextLevel; j < currentLevel; ++j) {
                    parents[j] = null;
                }
                currentLevel = nextLevel;
                while (parents[currentLevel - 1] == null) {
                    --currentLevel;
                }
                currentItem = parents[currentLevel - 1].addElement("li");
            }
        }
        return rootList;
    }

    private List<Node> makeMarkup(AnimeLyricsP.Markup_textContext plainContext) {
        List<Node> nodes = new ArrayList<>();
        for (AnimeLyricsP.Markup_elementContext node : plainContext.nodes) {
            if (node.br() != null) {
                nodes.add(DocumentHelper.createElement("br"));
            } else if (node.time_text() != null) {
                Element span = DocumentHelper.createElement("time");
                Token timeToken = node.time_text().start_time().time;
                boolean pubDate = false;
                if (timeToken != null) {
                    String timeStr = timeToken.getText().substring(1);
                    if (timeStr.charAt(0) == '!') {
                        pubDate = true;
                        timeStr = timeStr.substring(1);
                    }
                    if (!timeStr.isEmpty()) {
                        String unescapedTime = makeWord(timeStr);
                        span.addAttribute("datetime", unescapedTime);
                        if (pubDate) {
                            creationTime = unescapedTime;
                            pubDate = false;
                        }
                    }
                }
                List<Node> timeContentNodes = makeMarkup(node.time_text().markup_text());
                span.setContent(timeContentNodes);
                if (pubDate) {
                    StringBuilder sb = new StringBuilder();
                    for (Node node1 : timeContentNodes) {
                        sb.append(node1.asXML());
                    }
                    creationTime = sb.toString();
                }
                nodes.add(span);
            } else if (node.link() != null) {
                nodes.add(makeLink(node.link()));
            } else if (node.words() != null) {
                nodes.add(DocumentHelper.createText(makeWords(node.words())));
            } else if (node.color_text() != null) {
                Element span = DocumentHelper.createElement("span");
                span.addAttribute("style", "color:" + node.color_text().start_color().color.getText().substring(1));
                span.setContent(makeMarkup(node.color_text().markup_text()));
                nodes.add(span);
            } else if (node.covered_text() != null) {
                Element span = DocumentHelper.createElement("span");
                span.addAttribute("class", "covered");
                span.addAttribute("title", "涉及剧透");
                span.setContent(makeMarkup(node.covered_text().markup_text()));
                nodes.add(span);
            } else if (node.lang_text() != null) {
                Element span = DocumentHelper.createElement("span");
                Token token = node.lang_text().start_lang().lang;
                String lang = token != null ? token.getText().substring(1) : JAPANESE_LANG;
                span.addAttribute("lang", lang);
                span.setContent(makeMarkup(node.lang_text().markup_text()));
                nodes.add(span);
            } else if (node.ateji() != null) {
                nodes.add(makeMarkupAteji(node.ateji()));
            } else if (node.ruby() != null) {
                Element ruby = DocumentHelper.createElement("ruby");
                ruby.addText(makeWord(node.ruby().literal.getText()));
                Element rt = ruby.addElement("rt");
                rt.addText(makeWord(node.ruby().pron.getText()));
                nodes.add(ruby);
            } else if (node.footnote_ref() != null) {
                Element sup = DocumentHelper.createElement("sup");
                String id = makeWord(node.footnote_ref().ft_id.getText());
                Element link = sup.addElement("a");
                link.addAttribute("class", "footnote-link");
                link.addAttribute("href", "#footnote-" + id);
                link.addText("[" + id + "]");
                nodes.add(sup);
            }
        }
        return nodes;
    }

    private Element makeLink(AnimeLyricsP.LinkContext linkContext) {
        String templateName = (linkContext.templ == null) ? "" : linkContext.templ.getText();
        String html, literal = makeWords(linkContext.literal), href = linkContext.href.getText();
        try {
            html = templateParser.makeLinkTemplate(templateName, literal, href);
        } catch (TemplateParsingException e) {
            logger.warn(e.getMessage() + " (at " + linkContext.start.getLine() + ":" + linkContext.start.getCharPositionInLine() + "). Falling back to an external one.");
            html = templateParser.makeLinkTemplate("", literal, href);
        }
        try {
            return DocumentHelper.parseText(html).getRootElement();
        } catch (DocumentException e) {
            logger.error("An exception is thrown when parsing a template named \"{}\" at {}:{}!", templateName,
                    linkContext.start.getLine(), linkContext.start.getCharPositionInLine());
            logger.debug("HTML: {}", html);
            logger.debug("ST: ", e);
            throw (ParsingException) new ParsingException().initCause(e);
        }
    }

    static class JapaneseTextNode {
        Element realText, rubyText, kanaText, bracketText, romajiText;
        // a space is inserted only when two "needsPadding" meets.
        boolean needsPaddingSpaceAfter, needsPaddingSpaceBefore;

        JapaneseTextNode() {
            needsPaddingSpaceAfter = false; // no space when something is appended to the line beginning
            needsPaddingSpaceBefore = true; // space when this is appended to another
            realText = DocumentHelper.createElement("span");
            rubyText = DocumentHelper.createElement("span");
            kanaText = DocumentHelper.createElement("span");
            bracketText = DocumentHelper.createElement("span");
            romajiText = DocumentHelper.createElement("span");
        }

        void setAttribute(String name, String value) {
            realText.addAttribute(name, value);
            rubyText.addAttribute(name, value);
            kanaText.addAttribute(name, value);
            bracketText.addAttribute(name, value);
            romajiText.addAttribute(name, value);
        }

        private void mergeNode(JapaneseTextNode other, boolean blockRomaji) {
            realText.add(other.realText);
            rubyText.add(other.rubyText);
//            rubyText.content().addAll(other.rubyText.content()); // don't create hierarchy for ruby text
            kanaText.add(other.kanaText);
            bracketText.add(other.bracketText);
            if (!blockRomaji) {
                if (needsPaddingSpaceAfter && other.needsPaddingSpaceBefore) {
                    romajiText.addText("&nbsp;");
                }
                romajiText.add(other.romajiText);
                needsPaddingSpaceAfter = other.needsPaddingSpaceAfter;
            }
        }

        void merge(JapaneseTextNode other) {
            mergeNode(other, false);
        }

        void mergeWithoutRomaji(JapaneseTextNode other) {
            mergeNode(other, true);
        }

        JapaneseTextNode addText(String ja, String latin) {
            realText.addText(ja);
            rubyText.addText(ja);
            kanaText.addText(ja);
            bracketText.addText(ja);
            romajiText.addText(latin);
            return this;
        }

        JapaneseTextNode addText(String jaAndLatin) {
            return addText(jaAndLatin, jaAndLatin);
        }

        JapaneseTextNode padOnlyBeforeThis() {
            needsPaddingSpaceBefore = true;
            needsPaddingSpaceAfter = false;
            return this;
        }

        JapaneseTextNode padOnlyAfterThis() {
            needsPaddingSpaceBefore = false;
            needsPaddingSpaceAfter = true;
            return this;
        }

        JapaneseTextNode padBoth() {
            needsPaddingSpaceBefore = needsPaddingSpaceAfter = true;
            return this;
        }

        JapaneseTextNode padNeither() {
            needsPaddingSpaceBefore = needsPaddingSpaceAfter = false;
            return this;
        }

        void addTo(List<Element> elements) { // line should be a `p` tag
            realText.setName("p");
            realText.addAttribute("class", "normal-text-line");
            realText.addAttribute("lang", JAPANESE_LANG);
            elements.add(realText);
            rubyText.setName("p");
            rubyText.addAttribute("class", "furigana-text-line");
            rubyText.addAttribute("lang", JAPANESE_LANG);
            elements.add(rubyText);
            kanaText.setName("p");
            kanaText.addAttribute("class", "kana-text-line");
            kanaText.addAttribute("lang", JAPANESE_LANG);
            elements.add(kanaText);
            bracketText.setName("p");
            bracketText.addAttribute("class", "bracket-text-line");
            bracketText.addAttribute("lang", JAPANESE_LANG);
            elements.add(bracketText);
            romajiText.setName("p");
            romajiText.addAttribute("class", "romaji-text-line");
            romajiText.addAttribute("lang", JAPANESE_ROMAJI);
            elements.add(romajiText);
        }
    }

    private JapaneseTextNode makeLyricsMarkup(AnimeLyricsP.Lyrics_textContext plainContext) {
        JapaneseTextNode textNode = new JapaneseTextNode();
        for (AnimeLyricsP.Lyrics_elementContext node : plainContext.nodes) {
            if (node.lyrics_word_or_punc() != null) {
                AnimeLyricsP.Lyrics_word_or_puncContext ctx = node.lyrics_word_or_punc();
                textNode.merge(makeLyricsWordOrPunc(ctx));
            } else if (node.ateji() != null) {
                textNode.merge(makeLyricsAteji(node.ateji()));
            } else if (node.lyrics_color_text() != null) {
                JapaneseTextNode elements = makeLyricsMarkup(node.lyrics_color_text().lyrics_text());
                elements.setAttribute("style", "color:" + node.lyrics_color_text().start_color().color.getText().substring(1));
                textNode.merge(elements);
            }
            // else: SPACE
        }
        return textNode;
    }

    private JapaneseTextNode makeLyricsWordOrPunc(AnimeLyricsP.Lyrics_word_or_puncContext ctx) {
        JapaneseTextNode elements;
        if (ctx.PUNCTUATION() != null) {
            elements = makeLyricsPunctuation(ctx.PUNCTUATION().getSymbol());
        } else {
            elements = makeLyricsWord(ctx.lyrics_word());
        }
        return elements;
    }

    private Element makeMarkupAteji(AnimeLyricsP.AtejiContext context) {
        Element ruby = DocumentHelper.createElement("ruby");
        for (AnimeLyricsP.Lyrics_word_or_puncContext lyrics_word_or_puncContext : context.literal) {
            ruby.add(makeTextOnlyLyricsWordOrPunc(lyrics_word_or_puncContext));
        }
        Element rt = ruby.addElement("rt");
        for (AnimeLyricsP.Lyrics_word_or_puncContext lyrics_word_or_puncContext : context.real) {
            rt.add(makeTextOnlyLyricsWordOrPunc(lyrics_word_or_puncContext));
        }
        return ruby;
    }

    private JapaneseTextNode makeLyricsAteji(AnimeLyricsP.AtejiContext context) {
        JapaneseTextNode elements = new JapaneseTextNode();
        for (AnimeLyricsP.Lyrics_word_or_puncContext lyrics_word_or_puncContext : context.literal) {
            elements.mergeWithoutRomaji(makeLyricsWordOrPunc(lyrics_word_or_puncContext));
        }
        elements.addText("（", "");
        for (AnimeLyricsP.Lyrics_word_or_puncContext lyrics_word_or_puncContext : context.real) {
            elements.merge(makeLyricsWordOrPunc(lyrics_word_or_puncContext));
        }
        elements.addText("）", "");
        return elements;
    }

    private Node makeTextOnlyLyricsWordOrPunc(AnimeLyricsP.Lyrics_word_or_puncContext context) {
        Node node;
        if (context.PUNCTUATION() != null) {
            node = makeTextOnlyLyricsPunctuation(context.PUNCTUATION().getSymbol());
        } else {
            node = makeTextOnlyLyricsWord(context.lyrics_word());
        }
        return node;
    }

    /**
     * 处理一个单词，其中可能有ruby标注，但不会有ateji。其中不会出现分词空格。
     * 生成非注音形式的，用于ateji之类的没有地方注音的情况。
     */
    private Node makeTextOnlyLyricsWord(AnimeLyricsP.Lyrics_wordContext context) {
        if (context.foreign_ruby() != null) {
            String literal = makeWord(context.foreign_ruby().literal.getText());
            return DocumentHelper.createText(literal);
        } else {
            StringBuilder sb = new StringBuilder();
            for (AnimeLyricsP.Lyrics_sliceContext sliceContext : context.nodes) {
                if (sliceContext.ruby() != null) {
                    sb.append(sliceContext.ruby().literal.getText());
                } else if (sliceContext.WORD() != null) {
                    String text = sliceContext.WORD().getText();
                    sb.append(text);
                }
            }
            return DocumentHelper.createText(makeWord(sb.toString()));
        }
    }

    /**
     * 处理一个单词，其中可能有ruby标注，可能有分音@，但不会有ateji。其中不会出现分词空格。
     */
    private JapaneseTextNode makeLyricsWord(AnimeLyricsP.Lyrics_wordContext context) {
        JapaneseTextNode elements = new JapaneseTextNode();
        if (lyricsStartLine != -1) {
            elements.setAttribute("data-alm-position",
                    "line-" + (context.start.getLine() - lyricsStartLine) + "-" + context.start.getCharPositionInLine());
        }
        if (context.foreign_ruby() != null) {
            String text = context.foreign_ruby().verbatim.getText(),
                    literalText = context.foreign_ruby().literal.getText();
            String capitalClass = "";
            if (text.startsWith("@C")) {
                text = text.substring(2);
                capitalClass = "capitalize";
            } else if (literalText.startsWith("@C")) {
                literalText = literalText.substring(2);
                capitalClass = "capitalize";
            } else if (text.startsWith("@U")) {
                text = text.substring(2);
                capitalClass = "uppercase";
            } else if (literalText.startsWith("@U")) {
                literalText = literalText.substring(2);
                capitalClass = "uppercase";
            }
            String foreignWord = makeWord(text), literal = makeWord(literalText);

            elements.realText.addText(literal);
            elements.bracketText.addText(literal);
            if (foreignWord.length() > 0) {
                if (context.foreign_ruby().PLUS() == null) {
                    elements.romajiText.addAttribute("class", "gairaigo " + capitalClass).addText(foreignWord);
                } else {
                    elements.romajiText.addText(KanaUtils.romaji(Collections.singletonList(literal)) + "(");
                    elements.romajiText.addElement("span").addAttribute("class", "gairaigo " + capitalClass)
                            .addText(foreignWord);
                    elements.romajiText.addText(")");
                }
                elements.padBoth();
            }
            elements.kanaText.addText(literal);
            elements.rubyText.addText(literal);
        } else {
            List<String> realText = new ArrayList<>(), kanaText = new ArrayList<>();
            for (AnimeLyricsP.Lyrics_sliceContext sliceContext : context.nodes) {
                if (sliceContext.ruby() != null) {
                    realText.add(sliceContext.ruby().literal.getText());
                    kanaText.add(sliceContext.ruby().pron.getText());
                } else if (sliceContext.WORD() != null) {
                    String text = sliceContext.WORD().getText();
                    realText.add(text);
                    kanaText.add(text);
                }
            }
            String capitalClass = "";
            if (kanaText.get(0).startsWith("@C")) {
                capitalClass = "capitalize";
            } else if (kanaText.get(0).startsWith("@U")) {
                capitalClass = "uppercase";
            }
            if (!capitalClass.isEmpty()) {
                if (kanaText.get(0).length() == 2) {
                    kanaText.remove(0);
                    realText.remove(0);
                } else {
                    kanaText.set(0, kanaText.get(0).substring(2));
                    realText.set(0, realText.get(0).substring(2));
                }
            }
            String romaji = KanaUtils.romaji(kanaText);

            if (romaji.length() > 0) {
                elements.romajiText.addText(makeWord(romaji)).addAttribute("class", capitalClass);
                elements.padBoth();
            }
            elements.kanaText.addText(makeWord(String.join("", kanaText)));
            elements.realText.addText(makeWord(String.join("", realText)));

            for (int i = 0; i < realText.size(); i++) {
                String text = realText.get(i), kana = kanaText.get(i);
                if (text.equals(kana)) {
                    String escaped = makeWord(text);
                    elements.rubyText.addText(escaped);
                    elements.bracketText.addText(escaped);
                } else {
                    Element ruby = elements.rubyText.addElement("ruby");
                    String escaped = makeWord(text), escapedKana = makeWord(kana);
                    ruby.addText(escaped);
                    Element rt = ruby.addElement("rt");
                    rt.addText(escapedKana);
                    elements.bracketText.addText(escaped + "(" + escapedKana + ")");
                }
            }
        }
        return elements;
    }

    private JapaneseTextNode makeLyricsPunctuation(Token token) {
        String punctuation = token.getText();
        char c = punctuation.charAt(0);
        if (c == '@') {
            switch (punctuation.charAt(1)) {
            case '-': {
                return new JapaneseTextNode().addText("", "-").padNeither();
            }
            case ' ': { // space 0x20
                return new JapaneseTextNode().addText("&nbsp;").padNeither(); // replace with simple space
            }
            case 's':
            case '\u3000': {
                return new JapaneseTextNode().addText("&emsp;", "&nbsp;").padNeither();
            }
            default:
                logger.warn("Unrecognizable @-prepended character, @\"{}\"", c);
                throw new IllegalArgumentException();
            }
        } else if (c >= '\uFF01' && c <= '\uFF5E') {
            c = (char) ((c & 0xFF) + 0x20);
            JapaneseTextNode elements = new JapaneseTextNode().addText(punctuation, String.valueOf(c));
            switch (c) {
            case ',':
            case '.':
            case '!':
            case '?':
            case ')':
            case ']':
            case '}':
            case '%':
                elements.padOnlyAfterThis();
                break;
            case '(':
            case '[':
            case '{':
                elements.padOnlyBeforeThis();
                break;
            default:
                elements.padBoth();
                break;
            }
            return elements;
        } else {
            switch (c) {
            case '“': { // 201C
                return new JapaneseTextNode().addText("“").padOnlyBeforeThis();
            }
            case '”': { // 201D
                return new JapaneseTextNode().addText("”").padOnlyAfterThis();
            }
            case '·':
            case '・': { // 00B7, 30FB
                return new JapaneseTextNode().addText("・", "").padBoth();
            }
            case '…': { // 2026
                return new JapaneseTextNode().addText("…", "...").padOnlyAfterThis();
            }
            case '　': { // 3000
                logger.warn("I have met a full-width space without an @ sign prepended at {}:{} and considered it a normal space.",
                        token.getLine(), token.getCharPositionInLine());
                return new JapaneseTextNode().padOnlyAfterThis();
            }
            case '、': { // 3001
                return new JapaneseTextNode().addText("、", ",").padOnlyAfterThis();
            }
            case '。': { // 3002
                return new JapaneseTextNode().addText("。", ".").padOnlyAfterThis();
            }
            case '〈': { // 3008
                return new JapaneseTextNode().addText("〈", "“").padOnlyBeforeThis();
            }
            case '〉': { // 3009
                return new JapaneseTextNode().addText("〉", "”").padOnlyAfterThis();
            }
            case '《': { // 300A
                return new JapaneseTextNode().addText("《", "“").padOnlyBeforeThis();
            }
            case '》': { // 300B
                return new JapaneseTextNode().addText("》", "”").padOnlyAfterThis();
            }
            case '「': { // 300C
                return new JapaneseTextNode().addText("「", "“").padOnlyBeforeThis();
            }
            case '」': { // 300D
                return new JapaneseTextNode().addText("」", "”").padOnlyAfterThis();
            }
            case '『': { // 300E
                return new JapaneseTextNode().addText("『", "‘").padOnlyBeforeThis();
            }
            case '』': { // 300F
                return new JapaneseTextNode().addText("』", "’").padOnlyAfterThis();
            } // inner quote or book name
            case '【': { // 3010
                return new JapaneseTextNode().addText("【", "[").padOnlyBeforeThis();
            }
            case '】': { // 3011
                return new JapaneseTextNode().addText("】", "]").padOnlyAfterThis();
            }
            default:
                logger.warn("I have met an unidentified punctuation at {}:{} and skipped it.",
                        token.getLine(), token.getCharPositionInLine());
                return new JapaneseTextNode();
            }
        }
    }

    private Node makeTextOnlyLyricsPunctuation(Token token) {
        return makeLyricsPunctuation(token).realText;
    }
}
