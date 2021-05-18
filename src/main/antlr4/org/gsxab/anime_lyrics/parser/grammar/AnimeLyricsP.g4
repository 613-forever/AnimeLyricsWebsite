parser grammar AnimeLyricsP;

options { tokenVocab=AnimeLyricsL; }

file: name NEWLINE FENCE NEWLINE+ article EOF;

words: SPACE* word_or_punc (SPACE* word_or_punc)* SPACE*;
word_or_punc: WORD | PUNCTUATION | RAW;
name: words;

article: (article_line)+;

article_line: (header | list | parse_only | markup_text | hr | template | lyrics) NEWLINE+;

header: mark+=HASH+ SPACE+ words;
list: mark+=ASTER+ SPACE+ (footnote_def SPACE+)? markup_text;
parse_only: PARSE_ONLY_SIGN SPACE+ markup_text;
markup_text: (nodes+=markup_element SPACE*)+;
markup_element: (words | time_text | link | color_text | covered_text | lang_text | br | ateji | ruby | footnote_ref /*| inline_lyrics*/);
hr: HORIZONTAL_RULE;

template: AT_BRACE_L templ=WORD BRACE_R (PAREN_L params+=CONTENT PAREN_R)*;
link: BRACKET_L literal=words BRACKET_R (BRACE_L templ=WORD BRACE_R)? PAREN_L href=CONTENT PAREN_R;
br: ANGLE_L NEWLINE_TAG SLASH_AND_THEN_ANY? ANGLE_R;
time_text: start_time markup_text end_tag;
lang_text: start_lang markup_text end_tag;
color_text: start_color markup_text end_tag;
covered_text: start_cover markup_text end_tag;
start_lang: ANGLE_L LANG (lang=VALUE)? ANGLE_R;
start_color: ANGLE_L COLOR color=VALUE ANGLE_R;
start_cover: ANGLE_L COVERED ANGLE_R;
start_time: ANGLE_L TIME (time=VALUE)? ANGLE_R;
end_tag: ANGLE_L SLASH_AND_THEN_ANY ANGLE_R;

footnote_ref: BRACKET_L HAT ft_id=WORD BRACKET_R;

footnote_def: AT_BRACKET_L HAT ft_id=WORD BRACKET_R;

ateji: BRACE_L (literal+=lyrics_word_or_punc | SPACE)+ BRACE_R BRACE_L (real+=lyrics_word_or_punc | SPACE)+ BRACE_R;

ruby: BRACE_L literal=WORD BRACE_R BRACKET_L pron=WORD BRACKET_R;
foreign_ruby: BRACE_L literal=WORD BRACE_R PLUS? PAREN_L verbatim=CONTENT PAREN_R;

lyrics: start_lyrics_fense meta_line* lyrics_line+ end_lyrics_fense;
start_lyrics_fense: FENCE (ANGLE_L MAIN VALUE)? NEWLINE+;
end_lyrics_fense: FENCE;

meta_line: start_meta markup_text end_tag? NEWLINE+;
start_meta: ANGLE_L META ANGLE_R;

lyrics_line: (lyrics_text | translation_text | br) NEWLINE+;
lyrics_text: nodes+=lyrics_element+;
lyrics_element: (lyrics_word_or_punc | SPACE | lyrics_color_text | ateji);
lyrics_color_text: start_color lyrics_text end_tag;

lyrics_word_or_punc: lyrics_word | PUNCTUATION;
lyrics_word: nodes+=lyrics_slice+ | foreign_ruby; // A word (space seperated)
lyrics_slice: WORD | ruby; // A kana or a rubied kanji

translation_text: start_trans markup_text end_tag?;
start_trans: ANGLE_L TRANS (lang=VALUE)? ANGLE_R;
