package org.forever613.anime_lyrics.utils;

import java.util.List;
import java.util.NoSuchElementException;

public class KanaUtils {
    private enum Consonant {
        ZERO, K, G, S, SH, Z, J, T, D, CH, TS, N, H, F, B, P, M, Y, R, W;

        @Override
        public String toString() {
            if (this == ZERO) {
                return "";
            } else {
                return name().toLowerCase();
            }
        }

        public String toYoonString() {
            if (this == SH || this == CH || this == J) {
                return this.toString();
            } else {
                return this + "y";
            }
        }
    }

    private enum Vowel {
        A, I, U, E, O;

        @Override
        public String toString() {
            return name().toLowerCase();
        }

        public String toLongVowelString() {
            switch (this) {
            case A:
                return "ā";
            case I:
                return "ii";
            case U:
                return "ū";
            case E:
                return "ē";
            case O:
                return "ō";
            default:
                assert false;
                return null;
            }
        }
    }

    private static class Syllable {
        Consonant c;
        boolean yoon;
        Vowel v;
        boolean long_vowel;

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (!yoon) {
                sb.append(c);
            } else {
                sb.append(c.toYoonString());
            }
            if (!long_vowel) {
                sb.append(v);
            } else {
                sb.append(v.toLongVowelString());
            }
            return sb.toString();
        }
    }

    private static boolean isKana(char c) {
        return c >= 0x3041 && c <= 0x30FF;
    }

    private static boolean isHiragana(char c) {
        return c >= 0x3041 && c <= 0x3093;
    }

    private static Syllable splitKana(char c) {
        if (c > 0x309F) { // convert katakana to hiragana
            c -= 0x60;
        }
        // handle "chisai monji" as normal ones, to handle them directly in long vowels.
        switch (c) {
        case 'ぁ':
            c = 'あ';
            break;
        case 'ぃ':
            c = 'い';
            break;
        case 'ぅ':
            c = 'う';
            break;
        case 'ぇ':
            c = 'え';
            break;
        case 'ぉ':
            c = 'お';
            break;
        }
        assert (isHiragana(c));
        Syllable s = new Syllable();
        switch (c) {
        case 'あ':
        case 'か':
        case 'が':
        case 'さ':
        case 'ざ':
        case 'た':
        case 'だ':
        case 'な':
        case 'は':
        case 'ば':
        case 'ぱ':
        case 'ま':
        case 'や':
        case 'ら':
        case 'わ':
            s.v = Vowel.A;
            break;
        case 'い':
        case 'き':
        case 'ぎ':
        case 'し':
        case 'じ':
        case 'ち':
        case 'ぢ':
        case 'に':
        case 'ひ':
        case 'び':
        case 'ぴ':
        case 'み':
        case 'り':
            s.v = Vowel.I;
            break;
        case 'う':
        case 'く':
        case 'ぐ':
        case 'す':
        case 'ず':
        case 'つ':
        case 'づ':
        case 'ぬ':
        case 'ふ':
        case 'ぶ':
        case 'ぷ':
        case 'む':
        case 'ゆ':
        case 'る':
            s.v = Vowel.U;
            break;
        case 'え':
        case 'け':
        case 'げ':
        case 'せ':
        case 'ぜ':
        case 'て':
        case 'で':
        case 'ね':
        case 'へ':
        case 'べ':
        case 'ぺ':
        case 'め':
        case 'れ':
            s.v = Vowel.E;
            break;
        case 'お':
        case 'こ':
        case 'ご':
        case 'そ':
        case 'ぞ':
        case 'と':
        case 'ど':
        case 'の':
        case 'ほ':
        case 'ぼ':
        case 'ぽ':
        case 'も':
        case 'よ':
        case 'ろ':
        case 'を':
            s.v = Vowel.O;
            break;
        }
        switch (c) {
        case 'あ':
        case 'い':
        case 'う':
        case 'え':
        case 'お':
        case 'を': // !
            s.c = Consonant.ZERO;
            break;
        case 'か':
        case 'き':
        case 'く':
        case 'け':
        case 'こ':
            s.c = Consonant.K;
            break;
        case 'が':
        case 'ぎ':
        case 'ぐ':
        case 'げ':
        case 'ご':
            s.c = Consonant.G;
            break;
        case 'さ':
        case 'す':
        case 'せ':
        case 'そ':
            s.c = Consonant.S;
            break;
        case 'し':
            s.c = Consonant.SH;
            break;
        case 'ざ':
        case 'ず':
        case 'づ': // !
        case 'ぜ':
        case 'ぞ':
            s.c = Consonant.Z;
            break;
        case 'じ': // !
        case 'ぢ': // !
            s.c = Consonant.J;
            break;
        case 'た':
        case 'て':
        case 'と':
            s.c = Consonant.T;
            break;
        case 'ち': // !
            s.c = Consonant.CH;
            break;
        case 'つ': // !
            s.c = Consonant.TS;
            break;
        case 'だ':
        case 'で':
        case 'ど':
            s.c = Consonant.D;
            break;
        case 'な':
        case 'に':
        case 'ぬ':
        case 'ね':
        case 'の':
            s.c = Consonant.N;
            break;
        case 'は':
        case 'ひ': // special constant variant
        case 'へ':
        case 'ほ':
            s.c = Consonant.H;
            break;
        case 'ふ': // !
            s.c = Consonant.F;
            break;
        case 'ば':
        case 'び':
        case 'ぶ':
        case 'べ':
        case 'ぼ':
            s.c = Consonant.B;
            break;
        case 'ぱ':
        case 'ぴ':
        case 'ぷ':
        case 'ぺ':
        case 'ぽ':
            s.c = Consonant.P;
            break;
        case 'ま':
        case 'み':
        case 'む':
        case 'め':
        case 'も':
            s.c = Consonant.M;
            break;
        case 'や':
        case 'ゆ':
        case 'よ':
            s.c = Consonant.Y;
            break;
        case 'ら':
        case 'り':
        case 'る':
        case 'れ':
        case 'ろ':
            s.c = Consonant.R;
            break;
        case 'わ':
            s.c = Consonant.W;
            break;
        }
        return s;
    }

    private static class CharPointer {
        int indexInList, indexInString;
        List<String> list;

        CharPointer(List<String> strList) {
            list = strList;
            indexInList = 0;
            indexInString = 0;
        }

        void next() throws NoSuchElementException {
            if (indexInList == list.size()) {
                throw new NoSuchElementException();
            }
            ++indexInString;
            if (indexInString == list.get(indexInList).length()) {
                ++indexInList;
                indexInString = 0;
            }
        }

        char current() {
            if (indexInList == list.size()) return '@';
            return list.get(indexInList).charAt(indexInString);
        }

        void remove() {
            if (indexInList == list.size()) return;
            String str = list.get(indexInList);
            String newStr = str.substring(0, indexInString) + str.substring(indexInString + 1);
            list.set(indexInList, newStr);
            --indexInString;
        }
    }

    public static String romaji(List<String> kanaList) {
        StringBuilder sb = new StringBuilder();
        CharPointer pointer = new CharPointer(kanaList);
        try {
            //noinspection InfiniteLoopStatement
            while (true) {
                char c = pointer.current();
                if (c == '\\') {
                    pointer.remove();
                    pointer.next();
                    sb.append(pointer.current()); // all escapes, e.g. "\@" is dealt with here
                    pointer.next();
                    continue;
                }
                if (c == '@') {
//                    pointer.remove();
                    pointer.next();
                    c = pointer.current();
                    switch (c) {
                    case 'は':
                    case 'ハ':
                        sb.append("wa");
                        pointer.next();
                        continue;
                    case 'へ':
                    case 'ヘ':
                        sb.append("e");
                        pointer.next();
                        continue;
                    case 'を':
                    case 'ヲ':
                        sb.append("wo");
                        pointer.next();
                        continue;
                    default:
                        continue;
                    }
                    // default: handle it next iteration, by advancing the pointer only once
                }
                if (c < '\u3041' || (c > '\u3093' && c < '\u30A1') || c > '\u30F3') {
                    // not a normal kana
                    // elongated sign is \u30FC, but it should have been handled and skipped last iteration
                    // punctuation marks will not trigger this function
                    sb.append(c);
                    pointer.next();
                    continue;
                }
                if (c == 'ん' || c == 'ン') {
                    pointer.next();
                    c = pointer.current();
                    if (/*c != '/' && c != '@'*/ isKana(c)) {
                        Syllable next = splitKana(c);
                        switch (next.c) {
                        // case B:
                        // case P:
                        // case M:
                        //     return "m";
                        case ZERO:
                        case Y:
                            sb.append("n'");
                            continue;
                        default:
                            sb.append("n");
                            continue;
                        }
                    } else {
                        sb.append("n");
                        continue;
                    }
                }
                if (c == 'っ' || c == 'ッ') {
                    pointer.next();
                    c = pointer.current();
                    if (/*c != '/' && c != '@'*/ isKana(c)) {
                        Syllable next = splitKana(c);
                        switch (next.c) {
                        case SH:
                            sb.append("s");
                            continue;
                        case TS:
                        case CH:
                            sb.append("t");
                            continue;
                        default: // ignore illegal spelling
                            sb.append(next.c);
                            continue;
                        }
                    } else {
                        sb.append("'");
                        continue;
                    }
                }
                // normal
                Syllable syllable = splitKana(c);
                pointer.next();
                // check yoon
                c = pointer.current();
                if (c != '/' && c != '@') {
                    Vowel yoon = null;
                    if (syllable.v == Vowel.I) {
                        switch (c) {
                        case 'ゃ':
                        case 'ャ':
                            yoon = Vowel.A;
                            break;
                        case 'ょ':
                        case 'ョ':
                            yoon = Vowel.O;
                            break;
                        case 'ゅ':
                        case 'ュ':
                            yoon = Vowel.U;
                            break;
                        }
                        // note, only these three are handled, @,/ and others are default
                        if (yoon != null) {
                            syllable.yoon = true;
                            syllable.v = yoon;
                            pointer.next();
                        }
                    }
                }
                // check long vowel
                c = pointer.current();
                if (c != '/' && c != '@') {
                    if (c == 'ー') {
                        syllable.long_vowel = true;
                        pointer.next();
                    } else {
                        Syllable next = splitKana(c);
                        if (next.c == Consonant.ZERO) {
                            switch (syllable.v) {
                            case A:
                                if (next.v == Vowel.A) {
                                    syllable.long_vowel = true;
                                    pointer.next();
                                }
                                break;
                            case I:
                                break;
                            case U:
                                if (next.v == Vowel.U) {
                                    syllable.long_vowel = true;
                                    pointer.next();
                                }
                                break;
                            case E:
                                if (next.v == Vowel.E) {
                                    syllable.long_vowel = true;
                                    pointer.next();
                                }
                                break;
                            case O:
                                if (next.v == Vowel.O || next.v == Vowel.U) {
                                    syllable.long_vowel = true;
                                    pointer.next();
                                }
                                break;
                            }
                        }
                    }
                }

                sb.append(syllable);
            }
        } catch (NoSuchElementException ignored) {
        }
        return sb.toString();
    }
}
