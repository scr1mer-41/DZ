package org2;
import java.io.*;
import java.util.*;

public class Lexer {
    private final String filename;
    private final List<String> lines;
    private final List<Token> tokens = new ArrayList<>();
    private int lineNum = 1;
    private int colNum = 1;
    private int pos = 0;
    private String currentLine = "";

    // Множество ключевых слов
    private static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList(
            "abort","abs","accept","access","all","and","array","at","begin","body","case","constant",
            "declare","delay","delta","digits","do","else","elsif","end","entry","exception","exit",
            "for","function","generic","goto","if","in","is","limited","loop","mod","new","not","null",
            "of","or","others","out","package","pragma","private","procedure","raise","range","record",
            "rem","renames","return","reverse","select","separate","subtype","task","terminate","then",
            "type","use","when","while","with","xor"
    ));

    // Составные ограничители
    private static final Set<String> COMPOSITE_DELIMITERS = Set.of(
            "=>", "..", "**", ":=", "/=", ">=", "<=", "<<", ">>", "<>"
    );
    // Простые ограничители
    private static final Set<Character> SIMPLE_DELIMITERS = Set.of(
            '&', '\'', '(', ')', '*', '+', ',', '-', '.', '/', ':', ';', '<', '=', '>', '_', '\"', '#'
    );

    private static final Set<Character> additional_special_characters = Set.of(
        '!', '%', '?', '@', '[', ']', '\\', '`', '{', '}', '¤', '^', '‾'
    );

    public Lexer(String filename) throws IOException {
        this.filename = filename;
        this.lines = java.nio.file.Files.readAllLines(java.nio.file.Paths.get(filename));
    }

    public List<Token> tokenize() {
        for (String line : lines) {
            currentLine = line;
            pos = 0;
            colNum = 1;
            while (pos < currentLine.length()) {
                char ch = currentLine.charAt(pos);

                // Пропуск пробелов и табуляции
                if (Character.isWhitespace(ch)) {
                    pos++;
                    colNum++;
                    continue;
                }

                int startCol = colNum;
                // Комментарий --
                if (ch == '-' && peek(1) == '-') {
                    tokens.add(new Token(TokenType.COMMENT, currentLine.substring(pos), filename, lineNum, startCol));
                    break; // До конца строки
                }

                // Строковый литерал "..."
                if (ch == '"' || ch == '%') {
                    char quote = ch;
                    int start = pos;
                    StringBuilder sb = new StringBuilder();
                    pos++; colNum++;
                    boolean closed = false;
                    while (pos < currentLine.length()) {
                        char next = currentLine.charAt(pos);
                        if (next == quote) {
                            // Проверяем удвоение для экранирования
                            if (peek(1) == quote) {
                                sb.append(quote);
                                pos += 2;
                                colNum += 2;
                                continue;
                            } else {
                                closed = true;
                                pos++; colNum++;
                                break;
                            }
                        } else {
                            sb.append(next);
                            pos++; colNum++;
                        }
                    }
                    if (!closed) {
                        tokens.add(new Token(TokenType.ERROR, "Unterminated string literal", filename, lineNum, startCol));
                    } else {
                        tokens.add(new Token(TokenType.STRING, sb.toString(), filename, lineNum, startCol));
                    }
                    continue;
                }

                // Символьный литерал 'A'
                if (ch == '\'') {
                    int start = pos;
                    int localCol = colNum;
                    pos++; colNum++;
                    if (pos < currentLine.length()) {
                        char next = currentLine.charAt(pos);
                        // Случай 1: сразу вторая '
                        if (next == '\'') {
                            // Может быть ''' (символ-апостроф)
                            if (pos + 1 < currentLine.length() && currentLine.charAt(pos + 1) == '\'') {
                                // Символьный литерал: апостроф
                                tokens.add(new Token(TokenType.CHAR, "'''", filename, lineNum, localCol));
                                pos += 2; // шагнули на второй и третий '
                                colNum += 2;
                            } else {
                                // Символьный литерал: пустой апостроф
                                tokens.add(new Token(TokenType.CHAR, "''", filename, lineNum, localCol));
                                pos++; colNum++;
                            }
                            continue;
                        }
                        // Случай 2: 'c'
                        else if (pos + 1 < currentLine.length() && currentLine.charAt(pos + 1) == '\'') {
                            String literal = currentLine.substring(start, pos + 2);
                            tokens.add(new Token(TokenType.CHAR, literal, filename, lineNum, localCol));
                            pos += 2; // на символ и закрывающую '
                            colNum += 2;
                            continue;
                        }
                        // Ошибка: литерал не закрыт или длина не соответствует правилам
                        else {
                            tokens.add(new Token(TokenType.ERROR, "Invalid char literal", filename, lineNum, localCol));
                            pos++; colNum++;
                            continue;
                        }
                    } else {
                        tokens.add(new Token(TokenType.ERROR, "Unterminated char literal", filename, lineNum, localCol));
                        continue;
                    }
                }

                // Числовой литерал: сначала цифра
                if (Character.isDigit(ch)) {





                    int start = pos;
                    int localCol = colNum;

                    // 1. Читаем основание (для возможного литерала с основанием)
                    while (pos < currentLine.length() && Character.isDigit(currentLine.charAt(pos))) {
                        pos++; colNum++;
                    }
                    boolean isBased = false;
                    int afterBase = pos;

                    if (pos < currentLine.length() && currentLine.charAt(pos) == '#') {
                        isBased = true;
                        pos++; colNum++;
                        // 2. Читаем число в основании (расширенные цифры, _ и .)
                        while (pos < currentLine.length() &&
                                (Character.isLetterOrDigit(currentLine.charAt(pos)) ||
                                        currentLine.charAt(pos) == '_' ||
                                        currentLine.charAt(pos) == '.')) {
                            pos++; colNum++;
                        }
                        // 3. Второй #
                        if (pos < currentLine.length() && currentLine.charAt(pos) == '#') {
                            pos++; colNum++;
                            // 4. После второго # сразу (!) смотрим на экспоненту (E/e)
                            if (pos < currentLine.length() &&
                                    (currentLine.charAt(pos) == 'E' || currentLine.charAt(pos) == 'e')) {
                                pos++; colNum++;
                                // + или -
                                if (pos < currentLine.length() &&
                                        (currentLine.charAt(pos) == '+' || currentLine.charAt(pos) == '-')) {
                                    pos++; colNum++;
                                }
                                // цифры порядка
                                while (pos < currentLine.length() &&
                                        Character.isDigit(currentLine.charAt(pos))) {
                                    pos++; colNum++;
                                }
                            }
                            // Весь фрагмент - это один токен NUMBER!
                            tokens.add(new Token(TokenType.NUMBER, currentLine.substring(start, pos), filename, lineNum, localCol));
                            continue;
                        } else {
                            // Нет второго # — ошибка синтаксиса числа с основанием
                            tokens.add(new Token(TokenType.ERROR, "Invalid based literal", filename, lineNum, localCol));
                            continue;
                        }
                    }
                    // Проверяем точку и порядок для вещественного
                    if (pos < currentLine.length() && currentLine.charAt(pos) == '.') {
                        pos++; colNum++;
                        while (pos < currentLine.length() && Character.isDigit(currentLine.charAt(pos))) {
                            pos++; colNum++;
                        }
                        // экспонента
                        if (pos < currentLine.length() && (currentLine.charAt(pos) == 'E' || currentLine.charAt(pos) == 'e')) {
                            pos++; colNum++;
                            if (pos < currentLine.length() && (currentLine.charAt(pos) == '+' || currentLine.charAt(pos) == '-')) {
                                pos++; colNum++;
                            }
                            while (pos < currentLine.length() && Character.isDigit(currentLine.charAt(pos))) {
                                pos++; colNum++;
                            }
                        }
                        tokens.add(new Token(TokenType.NUMBER, currentLine.substring(start, pos), filename, lineNum, startCol));
                        continue;
                    }




                    // Вариант 1Е+10 (экспонента для целого)
                    if (pos < currentLine.length() && (currentLine.charAt(pos) == 'E' || currentLine.charAt(pos) == 'e')) {
                        pos++; colNum++;
                        if (pos < currentLine.length() && (currentLine.charAt(pos) == '+' || currentLine.charAt(pos) == '-')) {
                            pos++; colNum++;
                        }
                        while (pos < currentLine.length() && Character.isDigit(currentLine.charAt(pos))) {
                            pos++; colNum++;
                        }
                    }
                    tokens.add(new Token(TokenType.NUMBER, currentLine.substring(start, pos), filename, lineNum, startCol));
                    continue;
                }

                // Составные ограничители
                boolean foundDelimiter = false;
                for (String delim : COMPOSITE_DELIMITERS) {
                    if (currentLine.startsWith(delim, pos)) {
                        tokens.add(new Token(TokenType.DELIMITER, delim, filename, lineNum, startCol));
                        pos += delim.length();
                        colNum += delim.length();
                        foundDelimiter = true;
                        break;
                    }
                }
                if (foundDelimiter) continue;

                // Простые ограничители
                if (SIMPLE_DELIMITERS.contains(ch)) {
                    tokens.add(new Token(TokenType.DELIMITER, Character.toString(ch), filename, lineNum, startCol));
                    pos++;
                    colNum++;
                    continue;
                }

                // Идентификатор или ключевое слово
                if (Character.isLetter(ch)) {
                    int start = pos;
                    pos++; colNum++;
                    while (pos < currentLine.length()) {
                        char c = currentLine.charAt(pos);
                        if (Character.isLetterOrDigit(c) || c == '_') {
                            pos++; colNum++;
                        } else {
                            break;
                        }
                    }
                    String word = currentLine.substring(start, pos);
                    TokenType ttype = KEYWORDS.contains(word.toLowerCase()) ? TokenType.KEYWORD : TokenType.IDENTIFIER;
                    tokens.add(new Token(ttype, word, filename, lineNum, startCol));
                    continue;
                }

                // pragma
                if (currentLine.startsWith("pragma", pos)) {
                    tokens.add(new Token(TokenType.PRAGMA, "pragma", filename, lineNum, startCol));
                    pos += 6;
                    colNum += 6;
                    continue;
                }

                // Неожиданный символ
                tokens.add(new Token(TokenType.ERROR, "Unexpected character: " + ch, filename, lineNum, startCol));
                pos++; colNum++;
            }
            lineNum++;
        }
        return tokens;
    }

    private char peek(int offset) {
        if (pos + offset < currentLine.length())
            return currentLine.charAt(pos + offset);
        return 0;
    }
}