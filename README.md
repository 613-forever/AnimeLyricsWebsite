# Anime-Lyrics Website

A generator for the static website ['s Anime Lyrics Collection](https://anime-lyrics.forever613.top/).

## Introduction

### Structure

### Usage

```shell
java -jar AnimeLyrics-X.X.X-jar-with-dependencies.jar
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
- External templates. It might be a little risky to insert external templates written by strangers.
- Cross-references between pages. (To mark related pages.)

### Features 

## The ALM (Anime Lyrics Markup) Language

ALM, short for "Anime Lyrics Mark-up", is a mark-up language invented to generate HTML pages for structured lyrics.

Every source file in ALM language should be named with an ".alm.txt" extension, to be selected by the program.

### The Headline and the Fence

Every ALM text file is supposed to start with a headline followed by a fence line.
A fence is a symbol of three equality signs `===` in a single line.
Headline will be rendered as an `H1`, added into the page title,
and also title in the list file (maybe later, an ID for cross-references).

Headline is handled as raw automatically.

### Plain Markup Elements

We will generate something special in lyrics, so they are not completely the same.

#### Texts: Plain and Raw Text, Punctuations, and Spaces

To tell apart markup elements from texts, every character is not a plain text.
ALM ensures that, all alphabet-like characters are always handled as plain text,
along with `_` (underscore), `-` (hyphen), `'` (apostrophe), which are used as a part of or join parts of words.
Note that all kinds of brackets and `@` (at sign) are reserved for tags.

To escape markup elements, two special rules are employed:
Any character (except a line feed `\n` or a combination of a cr-lf `\r\n`) escaped with a `\` (backslash),
will be handled as a plain character.
Any character strings without `\n` or `\r\n` characters within a pair of `\`` (back quote)
will be handled as a raw string, where only `\\\`` (a backslash and a back quote) are replaced.

Punctuation marks like commas, periods, etc. are handled specially for they have impact.

Spaces between words, and between a word and an element are handled as configured, 
either "always", "only between words" or "never".
The default configuration is "never" to follow the habits for Chinese and Japanese.

#### Paragraphs: How to Break a Line and How not to

Markdown will always handle a single line break without two trailing spaces as a space, 
which is troublesome for the languages without spaces.
Thus, we break a line every time a `\n` or a `\r\n` is inserted.
Every line will be processed into a single `P` tag. 

To avoid line breaking, an escape character can be put at the end of a line.
An escaped line break will be handled as if there are neither characters, nor a line break here.

#### Text-Like Elements:

`RUBY` is an HTML tag to annotate pronunciations.

For example:
```HTML
<span lang="ja">
  <ruby>振<rt>ふ</rt></ruby>り<ruby>仮<rt>が</rt>名<rt>な</rt></ruby>
</span>
```
should be rendered as:
<span lang="ja">
  <ruby>振<rt>ふ</rt></ruby>り<ruby>仮<rt>が</rt>名<rt>な</rt></ruby>
</span>.

##### Furigana

One type is *[furigana](https://en.wikipedia.org/wiki/Furigana)* in Japanese,
where *kana*s are annotated as pronunciation for *Kanji*s.

In ALM, the tag above should be marked as:
```text
{振}[ふ]り{仮}[が]{名}[な]
```
Wrap the attached *Kanji* in braces, and the pronunciation in brackets.
I have considered removing braces for single characters,
only to end up in colliding with brackets in markdown-styled links.

##### Writing and Singing Different Words

The other type of situations is, printing a more formal, poetic, or rhetorical word, and annotating it to be sung as a more oral one,
or a word more fitting the rhythm.
An example is in [this question on StackExchange](https://japanese.stackexchange.com/questions/198/why-are-some-lyrics-words-written-in-kanji-whose-usual-reading-is-not-how-it-is).

Some call them *[ateji](https://en.wikipedia.org/wiki/Ateji)*,
but in fact *ateji* is a much more general name for writing a word in *Kanji* not related in etymology.
Other may call them *furigana* or ruby, as there are is something annotated to mark the pronunciation,
but they are totally different from the situation above.

For convenience, although *ateji* includes traditional borrow words also, 
we use this terminology to ONLY describe the ad-hoc *furigana* usages.

I will take the following as an instance.

A word <span lang="ja">“未来”（みらい）</span>(lit. future) annotated with a word <span lang="ja">“明日”（あす）</span>(lit. tomorrow):
```text
{未来}{あす}
{未来}{{明日}[あす]}
{{未}[み]{来}[らい]}{{明日}[あす]}
```
Right. With enough information to render literal lyrics and pronunciation *kana*,
three of the above are all permitted.
Note that when using *ateji* syntax, it often nests *furigana* as the pronunciation.

Two braces are selected, to show they are two different words.

#### Default Links and Predefined Links

##### External Links (Default)

To imitate markdown, a following grammar is selected.
```text
[forever613/ALW](https://github.com/forever613/AnimeLyricsWebsite)
```
and we will process it into:
```html
<a class="external-link"
   rel="nofollow noopener noreferrer"
   target="_blank"
   title="forever613/ALW"
   href="https://github.com/forever613/AnimeLyricsWebsite">
  forever613/ALW
</a>
```

A class `external-link` is added automatically to specify styles (e.g., `:after` icon) for them.

##### Predefined Links

For those that are not external, we use:
```text
[README.md]{internal}(./README.md)
[README.md]{i}(./README.md)
```
`i` is an abbr for `internal`.
We will process it into:
```html
<a class="internal-link"
   title="README.md"
   href="./README.md">
  README.md
</a>
```
Similarly, `a` or `anchor` is for an anchor within the same page; 
external links can be marked with `e` or `external`.

Other predefined external links also exist to make quick links.
They are prototyped in `WEB-INF/templates/templates.html` and inflated in `TemplateParser`.

##### Link: Notes

- Only texts are allowed in the literal part.
- Everything before the `)` (closing parenthesis) will be considered part of the link.
- A URL including `)` itself, should be encoded as `%29`.

#### General Angled Tags

Following tags follows an HTML-like tag syntax, but we don't care what is after the slash character, 
so omission is also okay.
A parameter is allowed, every character between the `=` (equal sign) and `>` (closing angle bracket) 
are considered parameter.
All the markup elements can be nested into general angled tags.

##### Colored Text

`color` or `c` for short.
```text
<c=red>RED</>
<color=#0f0>GREEN</>
<c=#00f>{青}[あお]</c>
```
A parameter will be used in `style="color:red"` as is.

##### Covered Text

`covered` or `co` for short.
```text
<covered>spoiler!!!</>
```
No parameters are required.

##### Time Text

`time` or `ti` for short.
```text
<time>2021/5/6</>
```
It is processed into a `TIME` tag to provide semantic information for search engines.
An optional parameter is used to generate the `value` attribute for the `TIME` tag.

##### Language-Specified Text

`lang` or `l` for short.
```text
<l>日本語</>
<l=ja>日本語</>
```
An optional parameter specifying language will be directly used in the `lang` attribute.
When it is missing, `lang="ja"` will be used.

##### New Line

```text
There will be a br tag in<n>this line.
```
will insert a `BR` tag.
Note that a text-like line will be processed into a `P` tag, 
this should be used when you need `BR` tags to avoid extra `text-indent` and `margin`.

```text
A line ends with a br tag.<n>\
And another line in the same p tag.
```
will be a good idea to end a line with `BR` tag instead of create a new `P` tag.

#### Headings, Lists and Templates

All the elements talked above are inline elements. 
Besides, there are 3 (or 4?) block elements, which should only be used in a separated line.

##### Headings

```text
# An H2 Heading
## An H3 Heading
### An H4 Heading
#### An H5 Heading
##### An H6 Heading
```
We reserved H1 for the page title, so normal headings start from `H2` tags.

The `#` (number signs) and at least one space is required.
Only texts are allowed to follow them.

Every heading also has the content as their ID automatically.

##### Lists

```text
* Item 1
** Item 1-1
** Item 1-2
* Item 2
* Item 3
```
Use `*` (asterisk) to create lists. Multiple asterisk can be used for nested lists.
The space is also required.

Inline markup elements are allowed in the contents.

Only unordered list will be created for the time, to avoid difficulty in parsing mixed ordered and unordered lists.

##### Templates

```text
@{template_name}
@{template_name}(param1)
@{template_name}(param1)(param2)
```
This will call the `TemplateParser` to render the template with a list of given parameters.
All the params are handled in raw strings, except `\\)` (backslash and closing parenthesis).

#### Footnote

The footnote definition is part of list elements, and the footnote link is an inline element.

```text
Some contents with footnote [^1].
```
will create something like:
```html
<p>Some content with footnote<sup><a class="footnote-link" href="#footnote-1">[1]</a></sup>.</p>
```

Footnotes should be defined in a list.
```text
* @[^1] Footnote 1.
```
For this, we will create something like:
```html
<ul>
  <li><span class="footnote-definition" id="footnote-1">[1]:&nbsp;Footnote 1.</span></li>
</ul>
```

### Lyrics Markup Structure
% TODO: To modify.
