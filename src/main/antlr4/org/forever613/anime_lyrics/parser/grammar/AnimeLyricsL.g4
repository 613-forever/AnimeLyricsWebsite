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

lexer grammar AnimeLyricsL;

NEWLINE: '\r'? '\n';
COMMENT: '%' (~'!' ~[\r\n]*)? NEWLINE -> skip;
PARSE_ONLY_SIGN: '%!';
fragment ESC: '\\' . | '\\' '\r'? '\n' ;
fragment AUX: '@' ~[{[ s\u3000-] ;
SPACE: ' ';
PUNCTUATION: [\u00B7\u201C\u201D\u2026\u3000-\u3002\u3008-\u3011\uFF01-\uFF0F\uFF1A-\uFF20\uFF3B-\uFF40\uFF5B-\uFF5E] | '@ ' | '@-' | '@\u3000' | '@s';
fragment HCHAR: [\u00A0-\u00B6\u00B8-\u201B\u201E-\u2025\u2027-\u2FFF\u3003-\u3007\u3012-\u9FAF\uFF10-\uFF19\uFF21-\uFF3A\uFF41-\uFF5A] | AUX;
FENCE: '===';
ASTER: '*';
ANGLE_L: '<' -> pushMode(PARAM) ;
AT_BRACKET_L: '@[';
BRACKET_L: '[';
BRACKET_R: ']';
PAREN_L: '(' -> pushMode(WILD) ;
AT_BRACE_L: '@{';
BRACE_L: '{';
BRACE_R: '}';
HASH: '#';
HAT: '^';
PLUS: '+';
HORIZONTAL_RULE: '---';
CHAR: ([0-9a-zA-Z,.!?_/':-] | HCHAR | ESC);
RAW: '`' (~[`\\] | ESC)+ '`' ;

mode PARAM;
ANGLE_R: '>' -> popMode;
SLASH_AND_THEN_ANY: '/' (~[>\\] | ESC)*;
COLOR: 'c' | 'color';
COVERED: 'co' | 'covered';
BKCOLOR: 'b' | 'backcolor';
NEWLINE_TAG: 'n' | 'new-line';
LANG: 'l' | 'lang';
TRANS: 't' | 'trans';
META: 'm' | 'meta';
MAIN: 'main';
VALUE: '=' ~'>'+;
AUTHOR: 'a' | 'author';
TIME: 'ti' | 'time';
KEYWORD: 'key' | 'keyword' | 'keywords';
DESCRIPTION: 'desc' | 'description';

mode WILD; // do not recognize anything until we pop it
PAREN_R: ')' -> popMode;
CONTENT: (~[)\\] | ESC)+;
