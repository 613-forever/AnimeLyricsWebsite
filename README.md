# Anime-Lyrics Website

A generator for static websites.

Here, the word anime follows the second definition in wikipedia:

> Anime (Japanese: アニメ, IPA: [aɲime]) is hand-drawn and computer animation originating from Japan.
> In Japan and in Japanese, anime (a term derived from the English word animation) describes all animated works, regardless of style or origin. 
> However, outside of Japan and in English, *anime is colloquial for Japanese animation and refers specifically to animation produced in Japan*.
> Animation produced outside of Japan with similar style to Japanese animation is referred to as anime-influenced animation.
> 
> -- [Anime - Wikipedia](https://en.wikipedia.org/wiki/Anime)

## License

This project AnimeLyricsWebsite, is free software under AGPL-3.0-or-later.
You can redistribute it and/or modify it under the terms of AGPL-3.0-or-later.

This project is distributed without any warranty or liability.

Every derivation should be under AGPL-3.0-or-later, too.
The license applies only to the source code.
Any execution-generated source can be used arbitrarily.

## Introduction

### Structure

### Usage

```shell
java -jar AnimeLyrics-X.X.X-jar-with-dependencies.jar
```
will simply start the engine, rendering from `text/draft` to `text/target`, also using `text/cache`,
and copy `text/draft/copy_only` to `text/target`, relative to current dir.

Parameters and their default values are as follows:
```plain
args.length > 1 ? args[1] : DRAFT_DIR,
args.length > 2 ? args[2] : CACHE_DIR,
args.length > 3 ? args[3] : TARGET_DIR,
args.length > 4 ? args[4] : LIST_FILE_NAME,
args.length > 5 ? args[5] : SITEMAP_FILE_NAME,
args.length > 6 ? args[6] : ROBOTS_FILE_NAME,
args.length > 7 ? args[7] : CONFIG_FILE_NAME,
args.length > 8 ? args[8] : COPY_ONLY_DIR_NAME

static final String DRAFT_DIR = "./text/draft/";
static final String CACHE_DIR = "./text/cache/";
static final String TARGET_DIR = "./text/target/";

static final String COPY_ONLY_DIR_NAME = "copy_only"; // relative to DRAFT_DIR, "./text/target/copy_only"
static final String LIST_FILE_NAME = "List.html";
static final String SITEMAP_FILE_NAME = "Sitemap.xml";
static final String ROBOTS_FILE_NAME = "robots.txt";
static final String ROOT_URL_FILE_NAME = "url.txt"; // relative to DRAFT_DIR, "./text/target/config.txt"
```

## Features

- Pages:
    - Multilingual pages:
      Inserts language attributes `lang=` to add styles for different language.
      It is very useful when we need two language with overlapping Unicode code points, and suppose to employ different fonts.
    - Color:
      Changes colors to identify singers if necessary.
    - Multiple representation.
      Generates normal text, kana-only text and romaji text in separated tags to control with JS.
    - Covered Text:
      Covers spoilers with black.
      That means, it might display spoilers as black text with black background, unless mouse hovers over it.
    - Simple support for headings, footnote, lists, and links.
      Generates them in almost the same syntax as in Markdown,
      though there is a bit of difference to avoid difficulties in parsing indents.
      However, in the current version we will not check sanity of them.
    - Templates.
      To include templates for common uses, e.g., fancy links, music or video player cards, spoiler warning messages.
      (It is bundled with embedded players provided by Bilibili and Music163 , but they are not internationalized.)
- Website:
    - A list file.
      Generates a list of pages to serve as an entrance.
    - Sitemap.
      Generates a sitemap to make the site more optimized for search engines.

### Features to Be Supported Later

- A template for scoring.
- A template for inline lyrics. (It should be there, but somehow not necessary.)
- Forced romaji.
- External templates. Though It might be a little risky to insert external templates written by strangers.
- Cross-references between pages. (To mark related pages.)

## The ALM (Anime Lyrics Markup) Language

See [ALM.md](ALM.md).

## Language and Font

Generated pages are naturally multilingual.
Among CJKV, there are many subtle differences in characters within the same code point.
As a result, we should change fonts for different languages.

We use a large number of `lang` tags to tell them apart, and render them in different fonts.

`lang="ja"` is normal Japanese text.
`lang="ja-Latn"` is for Japanese *romaji*.

By default, text are tagged `lang="zh"` because they are Chinese by default.
Default language setting will be available in the future.
`org.forever613.anime_lyrics.parser.Parser.DEFAULT_LANG` and templates should be modified for this.
