package org;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AdaLexer {

    public enum TokenType {
        KEYWORD, IDENTIFIER, INTEGER_LITERAL, REAL_LITERAL, BASED_LITERAL,
        CHARACTER_LITERAL, STRING_LITERAL, OPERATOR, PRAGMA, ERROR
    }

    public static class Token {
        private final TokenType type;
        private final String value;
        private final String fileName;
        private final int line;

        public Token(TokenType type, String value, String fileName, int line) {
            this.type = type;
            this.value = value;
            this.fileName = fileName;
            this.line = line;
        }

        public TokenType getType() {
            return type;
        }

        public String getValue() {
            return value;
        }

        public String getFileName() {
            return fileName;
        }

        public int getLine() {
            return line;
        }

        @Override
        public String toString() {
            return String.format("Token{type=%s, value='%s', file='%s', line=%d}", type, value, fileName, line);
        }
    }

    private static final Pattern STRING_DOUBLE = Pattern.compile("^\"(\"\"|[^\"\n])*\"");
    private static final Pattern STRING_PERCENT = Pattern.compile("^%(%|[^%\n])*%");
    private static final Pattern CHARACTER_LITERAL = Pattern.compile("^'([^']|'')'");
    private static final Pattern BASED_LITERAL = Pattern.compile("^\\d+[#:][0-9a-fA-F_]+(\\.[0-9a-fA-F_]+)?[#:]([eE][+-]?[0-9_]+)?");
    private static final Pattern REAL_LITERAL = Pattern.compile("^(\\d[\\d_]*\\.[\\d_]+([eE][+-]?[\\d_]+)?|\\.[\\d_]+([eE][+-]?[\\d_]+)?|\\d[\\d_]*[eE][-][\\d_]+)");
    private static final Pattern INTEGER_LITERAL = Pattern.compile("^\\d[\\d_]*([eE]\\+?[\\d_]+)?");
    private static final Pattern KEYWORD = Pattern.compile("^(?i)(abort|abs|accept|access|all|and|array|at|begin|body|case|constant|declare|delay|delta|digits|do|else|elsif|end|entry|exception|exit|for|function|generic|goto|if|in|is|limited|loop|mod|new|not|null|of|or|others|out|package|pragma|private|procedure|raise|range|record|rem|renames|return|reverse|select|separate|subtype|task|terminate|then|type|use|when|while|with|xor)\\b");
    private static final Pattern IDENTIFIER = Pattern.compile("^[a-zA-Zа-яА-ЯёЁ][a-zA-Zа-яА-ЯёЁ0-9_]*");
    private static final Pattern OPERATOR = Pattern.compile("^(=>|\\*\\*|:=|/=|>=|<=|<>|<<|>>|\\.\\.|[(),.;+\\-*/&:<>=_])");
    private static final Pattern PRAGMA = Pattern.compile("^pragma\\s+\\w+", Pattern.CASE_INSENSITIVE);
    private static final Pattern NEWLINE = Pattern.compile("\n");

    public List<Token> tokenize(String input, String fileName) throws LexerError {
        List<Token> tokens = new ArrayList<>();
        int index = 0;
        int line = 1;

        while (index < input.length()) {
            char currentChar = input.charAt(index);
            if (Character.isWhitespace(currentChar)) {
                if (currentChar == '\n') {
                    line++;
                }
                index++;
                continue;
            }

            if ((index + 1) < (input.length()) && (currentChar == '-') && (input.charAt(index + 1) == '-')) {
                while (index < input.length() && input.charAt(index) != '\n') {
                    index++;
                }
                continue;
            }

            String substring = input.substring(index);
            Matcher matcher = matchPattern(STRING_DOUBLE, substring);
            if (matcher == null) matcher = matchPattern(STRING_PERCENT, substring);
            if (matcher == null) matcher = matchPattern(CHARACTER_LITERAL, substring);
            if (matcher == null) matcher = matchPattern(BASED_LITERAL, substring);
            if (matcher == null) matcher = matchPattern(REAL_LITERAL, substring);
            if (matcher == null) matcher = matchPattern(INTEGER_LITERAL, substring);
            if (matcher == null) matcher = matchPattern(PRAGMA, substring);
            if (matcher == null) matcher = matchPattern(KEYWORD, substring);
            if (matcher == null) matcher = matchPattern(IDENTIFIER, substring);
            if (matcher == null) matcher = matchPattern(OPERATOR, substring);

            if (matcher != null) {
                String tokenValue = matcher.group();
                TokenType type = determineTokenType(matcher);
                tokens.add(new Token(type, tokenValue, fileName, line));

                // Обновляем счетчик строк
                for (int i = 0; i < tokenValue.length(); i++) {
                    if (tokenValue.charAt(i) == '\n') {
                        line++;
                    }
                }

                index += tokenValue.length();
            } else {
                int k = 0;
                for (int i = 0; i <= index; i++) {
                    System.out.print(input.charAt(i));
                    if (input.charAt(i) == '\n') {
                        k = i;
                    }
                }

                System.out.println();
                String str = "";
                for (int i = 0; i < (index-k); i++) {
                    str += " ";
                }
                str = str + "^";

                System.out.println(str);
                throw new LexerError("unknown symbol");
//                tokens.add(new Token(TokenType.ERROR, String.valueOf(currentChar), fileName, line));
//                if (currentChar == '\n') {
//                    line++;
//                }
//                index++;
            }
        }
        return tokens;
    }

    private Matcher matchPattern(Pattern pattern, String input) {
        Matcher matcher = pattern.matcher(input);
        return matcher.find() ? matcher : null;
    }

    private TokenType determineTokenType(Matcher matcher) {
        String tokenValue = matcher.group();
        if (matcher.pattern() == STRING_DOUBLE || matcher.pattern() == STRING_PERCENT) {
            return TokenType.STRING_LITERAL;
        } else if (matcher.pattern() == CHARACTER_LITERAL) {
            return TokenType.CHARACTER_LITERAL;
        } else if (matcher.pattern() == BASED_LITERAL) {
            return TokenType.BASED_LITERAL;
        } else if (matcher.pattern() == REAL_LITERAL) {
            return TokenType.REAL_LITERAL;
        } else if (matcher.pattern() == INTEGER_LITERAL) {
            return TokenType.INTEGER_LITERAL;
        } else if (matcher.pattern() == PRAGMA) {
            return TokenType.PRAGMA;
        } else if (matcher.pattern() == KEYWORD) {
            return TokenType.KEYWORD;
        } else if (matcher.pattern() == IDENTIFIER) {
            return TokenType.IDENTIFIER;
        } else {
            return TokenType.OPERATOR;
        }
    }

    private void updateLineCount(String tokenValue, int line, int lineStart) {
        long newlineCount = tokenValue.chars().filter(c -> c == '\n').count();
        if (newlineCount > 0) {
            line += (int) newlineCount;
            lineStart = tokenValue.lastIndexOf('\n') + 1;
        }
    }
}