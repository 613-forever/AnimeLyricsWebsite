# The ALM (Anime Lyrics Markup) Language

ALM, short for "Anime Lyrics Mark-up", is a mark-up language invented to generate HTML pages for structured lyrics.
It is designed to allow taking in Japanese text augmented with *Furigana*s, and make computer generated *romaji* and so on.

Every source file in ALM language should be named with an ".alm.txt" extension, just to be selected by the program.

## The Headline and the Fence

Every ALM text file is supposed to start with a headline followed by a fence line.
A fence is a symbol of three equality signs `===` in a single line.
Headline will be rendered as an `H1`, added into the page title,
and also title in the list file (maybe later, an ID for cross-references).

Headline is handled as raw automatically.

## Inline Markup Elements

We will generate something special in lyrics, so they are not completely the same.

### Texts: Plain and Raw Text, Punctuations, and Spaces

To tell apart markup elements from texts, every character is not a plain text.
ALM ensures that, all alphabet-like characters are always handled as plain text,
along with underscore `_`, hyphen `-`, apostrophe `'`, which are used as a part of or join parts of words.
Note that all kinds of brackets `<>()[]{}` and AT signs `@` are reserved for tags.

To escape markup elements, two special rules are employed:
Any character (except a line feed `\n` or a combination of a cr-lf `\r\n`) escaped with a `\` (backslash),
will be handled as a plain character.
Any character strings without `\n` or `\r\n` characters within a pair of back quotes `\``
will be handled as a raw string, where only a backslash and a back quote `\\\`` will be replaced.

Punctuation marks like commas, periods, etc. are handled specially for they have impact.

Spaces between words, and between a word and an element are handled as configured,
either "always", "only between words" or "never".
The default configuration is "never" to follow the habits for Chinese and Japanese.

### Paragraphs: How to Break a Line and How not to

Markdown will always handle a single line break without two trailing spaces as a space,
which is troublesome for the languages without spaces.
Thus, we break a line every time a `\n` or a `\r\n` is inserted.
Every line will be processed into a single `P` tag.

To avoid line breaking, an escape character `\\` can be put at the end of a line.
An escaped line break will be handled as if there are neither characters, nor a line break here.

### Text-Like Elements

Prerequisite knowledge:

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

#### Furigana

One type is *[furigana](https://en.wikipedia.org/wiki/Furigana)* in Japanese,
where *kana*s are annotated as pronunciation for *Kanji*s.

In ALM, the tag above should be marked as:
```text
{振}[ふ]り{仮}[が]{名}[な]
```
Wrap the attached *Kanji* in braces, and the pronunciation in brackets.
I have considered removing braces for single characters,
only to end up in colliding with brackets in markdown-styled links.

Since v1.1, braces for *furigana*s above a single character can be omitted.
```text
振[ふ]り仮[が]名[な]
```
But when brackets are followed by another parenthesis-like characters, braces should not be omitted, to avoid ambiguity.

#### Writing and Singing Different Words

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

### Links

#### External Links (Default)

To imitate markdown, a following grammar is selected.
```text
[613-forever/ALW](https://github.com/613-forever/AnimeLyricsWebsite)
```
and we will process it into:
```html
<a class="external-link"
   rel="nofollow noopener noreferrer"
   target="_blank"
   title="forever613/ALW"
   href="https://github.com/613-forever/AnimeLyricsWebsite">
  613-forever/ALW
</a>
```

A class `external-link` is added automatically to specify styles (e.g., `:after` icon) for them.

#### Predefined Links

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

#### Link: Notes

- Only texts are allowed in the literal part.
- Everything before the `)` (closing parenthesis) will be considered part of the link WITHOUT any further process.
- A URL including `(` or `)` should encode them as `%28` and `%29`. They are also preserved characters in RFC3986.

#### Footnote Links

The footnote definition is part of list elements, and the footnote link is an inline element.

```text
Some contents with footnote [^1].
```
will create something like:
```html
<p>Some content with footnote<sup><a class="footnote-link" href="#footnote-1">[1]</a></sup>.</p>
```

See also `Block Elements > List > Footnote Definition` section.

### General Angled Tags

Following tags follows an HTML-like tag syntax, but we don't care what is after the slash character,
so omission is also okay.
A parameter is allowed, every character between the `=` (equal sign) and `>` (closing angle bracket)
are considered parameter.
All the markup elements can be nested into general angled tags.

#### Colored Text

`color` or `c` for short.
```text
<c=red>RED</>
<color=#0f0>GREEN</>
<c=#00f>{青}[あお]</c>
```
A parameter will be used in `style="color:red"` as is.

#### Covered Text

`covered` or `co` for short.
```text
<covered>spoiler!!!</>
```
No parameters are required.

#### Time Text

`time` or `ti` for short.
```text
<time>2021/5/6</>
```
It is processed into a `TIME` tag to provide semantic information for search engines.
An optional parameter is used to generate the `value` attribute for the `TIME` tag.

Though `Time Text` accepts any kinds of markup contents, a `value` should be provided if content is not plain.

#### Language-Specified Text

`lang` or `l` for short.
```text
<l>日本語</>
<l=ja>日本語</>
```
An optional parameter specifying language will be directly used in the `lang` attribute.
When it is missing, `lang="ja"` will be used.

#### New Line

Note that any text-like line (delimited by line breaks) will be processed into a `P` tag, meaning a new paragraph.

When a new line is required without a new paragraph semantically,
to avoid extra `text-indent` and `margin`, you need `BR` tags.

```text
There will be a br tag in<n>this line.
```
will insert a `BR` tag.

```text
A line ends with a br tag.<n>\
And another line in the same p tag.
```
will be a good idea to end a line with `BR` tag instead of create a new `P` tag.

## Block Elements

All the elements talked above are inline elements.
Besides, there are 3 (or 4?) block elements, which should only be used in a separated line.

### Headings

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

### Lists

```text
* Item 1
** Item 1-1
** Item 1-2
* Item 2
* Item 3
```
Use `*` (asterisk) to create lists. Multiple asterisk can be used for nested lists.
The space is also required.

Any blank line between asterisk-preceded lines will break lists.

Inline markup elements are allowed in the contents.

Only unordered list will be created for the time, to avoid difficulty in parsing mixed ordered and unordered lists.

#### Footnote Definition

Footnotes should be defined in a list, start with a AT-bracket `@[` combination.
```text
* @[^1] Footnote 1.
```
For this, we will create something like:
```html
<ul>
  <li><span class="footnote-definition" id="footnote-1">[1]:&nbsp;Footnote 1.</span></li>
</ul>
```

See also `Inline Markup Elements > Links > Footnote Links` section.

### Templates

```text
@{template_name}
@{template_name}(param1)
@{template_name}(param1)(param2)
```
This will call the `TemplateParser` to render the template with a list of given parameters.

All the params are handled in RAW strings, except `\\)` (backslash and closing parenthesis).

> For the trickiness, `)` and `\\)` might trigger in-conformal behavior among versions.

Templates are placed in a template sheet name and identified by a template name.
CSS and JS with the same name can be allowed, see `org.forever613.anime_lyrics.parser.TemplateParser` for details.

## Lyrics Markup Structure

Lyrics blocks are delimited by two fences (`===` in a single line), and starts with at least zero lines for meta-info.
At least one line of lyrics is required to make a lyrics block.

The first fence can be followed with a `main` to suggest that it be considered as the main lyrics part,
if more than one lie in a single document.

### Meta Lines

The same as angled tags, except that "meta line"s are lines.
So nothing else can be put in the same line, and the closing tag `</>` is allowed to be omitted.

`meta` or `m` for short.

```text
<m>动漫歌词收藏册网站</>
<meta>ALM</>
<m>Anime Lyrics Website
<m><l>アニメ歌詞ウェブサイト</></>
```

Note that omitting closing tag is risky to confuse the engine,
when the line will end with another closing tag without this one.

Inline markup elements are allowed in meta lines.
Notice that colon `:` may introduce unexpected results for some parsable lines.
`\\:` can be used instead.

#### Parsed Meta Lines

> This feature may be changed to make a more graceful grammar.

When the content of a meta line starts with a `xxx:` prefix, we may parse it into some other commands.
`background:` is the only parsed meta line since v0.1.0.

```text
<m>background:/img/abc.jpg
```
will create a
```
style="background:url(/img/abc.jpg)"
```
attribute for the `DIV`.

### Non-Text Lyrics Lines

After some lines for meta-info, at least one line of lyrics is required.
Any of the three kinds can be interpreted as a lyrics line.

Text lines means lyrics itself.
They are those normal lines without following two kind of tags.

#### Blank Lines

Used between paragraphs in lyrics.
It is the same as the inline markup element, except that it is required to be a single line.
```text
<n>
<n/>
```

Two consequent blank lines can be used to denote stanzas.

#### Translation Lines

A tag with `trans`, or `t` for short.
A parameter of group name is preserved to allow multiple translations in the future.

Following are two different groups of translations follows one text line.
```text
アニメ歌詞ウェブサイト
<t=en>Anime Lyrics Website</>
<t=zh>动漫歌词网站</>
```

Any inline markup elements are allowed.

### Lyrics Text

*Romaji* text, raw Japanese text, *kanji*-with-*furigana* text, *kanji*-with-parentheses text,
*kana*-only text are generated from lyrics texts,
so some special techniques are leveraged.

We only support [modified Hepburn romanization](https://en.wikipedia.org/wiki/Hepburn_romanization).

Unlike plain text, spaces are not always ignored anymore, but they are used in generated *romaji* to split words.

#### Single Word or Punctuation Markups

##### Furigana

Same format as in `Inline Markup Elements > Text-Like Elements > Furigana`.

```text
{振}[ふ]り{仮}[が]{名}[な]
```
will generate
```text
<p class="normal-text-line" lang="ja"><span data-alm-position="line-3-0" class="">振り仮名</span></p>
<p class="furigana-text-line" lang="ja"><span data-alm-position="line-3-0" class=""><ruby>振<rt>ふ</rt></ruby>り<ruby>仮<rt>が</rt></ruby><ruby>名<rt>な</rt></ruby></span></p>
<p class="kana-text-line" lang="ja"><span data-alm-position="line-3-0" class="">ふりがな</span></p>
<p class="bracket-text-line" lang="ja"><span data-alm-position="line-3-0" class="">振(ふ)り仮(が)名(な)</span></p>
<p class="romaji-text-line" lang="ja-Latn"><span data-alm-position="line-3-0" class="">furigana</span></p>
```

Every `P` tag is a line of lyrics, and every `SPAN` is a word.
Attribute `data-alm-position` is used in human interaction.

Though it is surely allowed to annotate the whole word at once,
we highly suggest annotating by every *kanji* if possible.
(Consider annotating at once just a fallback for *jukujikun*s.)

##### Punctuation Marks

Input them in full-width characters along with Japanese text.
Corresponding half-width ones will be generated in *romaji* text.
(Except quotes, opening and closing one used in English text has the same code points as in Japanese text.)

##### Word-Split Space & Printed Space

Use spaces ` ` to split words.
Space will not be generated in result Japanese texts.

Precede a space with an AT sign `@` to make it shown even in Japaneses texts.
Full width space (U+3000) should also be inserted in this way.

> It is a known glitch to generate erroneous word border for a line start with a printed space `@ `.

Note that `\\` is a mark to escape the next character as a number or a letter, will not behave as expected.

#### Romaji Tricks

##### Hyphened Romaji

For Japanese words with a hyphen denoting sub-word borders, use `@-`.
It will generate a hyphen `-` in *romaji*, and nothing in Japanese texts.

##### Particles with Pronunciations 

Particles read differently from written *kana*s, can be preceded with `@`.

|name|ALM|Japanese|*romaji*|
|:-:|:-:|:-:|:-:|
|normal ha|`は`|は|ha|
|particle ha|`@は`|は|wa|
|normal he|`へ`|へ|he|
|particle he|`@へ`|は|wa|
|particle wo|`を`|を|o|

We preserve the following escaped to be used in special contexts.
|name|ALM|Japanese|*romaji*|
|:-:|:-:|:-:|:-:|
|escaped wo|`@を`|を|wo|

##### Break Auto-Elongate

When generating *romaji*, the long vowels, "aa", "ee", "oo", "ou", "uu" are generated as macron-indicated letters automatically.
("ei", "ii" is not written this way in modified Hepburn romanization.)
But on morpheme border, they should be written separately.

Just use a `@` to break.

Luckily, no rule collision with particles appears.

##### Romaji Capitalization

*Romaji* requires capitalizing for the beginning of sentences and proper nouns, which is not provided by Japanese texts.
Special token `@C` can be prepended to tell that.

Another case-related token is `@U`, which uppers the whole word.

`@S` and `@L` are planned to preserve for small letters / lower cases, but are not defined yet.

#### Loanwords & Non-Japanese Text

##### Normal Loanwords

In transcription, loadwords are expected to be written in source language with italic font.

Just as *furigana*-notation, but use round parentheses instead.

```text
{ルビー}(ruby)
```

Japanese text will preserve it as ルビー but in *romaji* it is *ruby*.

##### Modified Loanwords

If the loanword is further modified in Japanese, the etymology should be explained in parentheses.

For example, テレビ should be transcripted as terebi(*television*).

We can generate it as:
```text
{テレビ}(+television)
```

Note that only *kana* are allowed in this form.

##### Foreign Text

If the lyrics itself is not Japanese, use an equal sign `=` in the parentheses as a shortcut to repeat the text.

```text
{Yes}(=)
```

Japanese text will render it as Yes but in *romaji* it is an italic *Yes*.

#### Multiple Words Markups

##### Colored Lyrics

Same as in `Inline Markup Elements > General Angled Tags > Colored Text`,
except that only lyrics text is allowed within the tag.

##### Writing and Singing Different Words

Same format as in `Inline Markup Elements > Text-Like Elements > Writing and Singing Different Words`.

It will generate:
- In Japanese text: Pronunciation marked by full width parentheses `（）`.
- In *romaji*: Pronunciation only.

More than one words and punctuation marks are allowed.
