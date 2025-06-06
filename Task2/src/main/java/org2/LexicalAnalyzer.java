package org2;
import java.util.*;
import java.io.*;

public class LexicalAnalyzer {
    private final List<Token> allTokens = new ArrayList<>();
    private final Map<TokenType, Integer> freq = new EnumMap<>(TokenType.class);
    private final Map<String, List<Token>> identifiers = new TreeMap<>();

    public void processFiles(List<String> files) throws IOException {
        for (String file : files) {
            Lexer lexer = new Lexer(file);
            List<Token> tokens = lexer.tokenize();
            for (Token t : tokens) {
                allTokens.add(t);
                freq.put(t.type, freq.getOrDefault(t.type, 0) + 1);
                if (t.type == TokenType.IDENTIFIER) {
                    identifiers.computeIfAbsent(t.text, k -> new ArrayList<>()).add(t);
                }
            }
        }
    }

    public void report() {
        int total = allTokens.size();
        System.out.println("Общее число лексем: " + total);
        for (TokenType t : TokenType.values()) {
            int abs = freq.getOrDefault(t, 0);
            double rel = total == 0 ? 0 : (100.0 * abs / total);
            System.out.printf("%-12s: %4d (%.2f%%)\n", t, abs, rel);
        }

        System.out.println("\nИдентификаторы (в лексикографическом порядке):");
        for (String id : identifiers.keySet()) {
            System.out.print(id + " : ");
            for (Token t : identifiers.get(id)) {
                System.out.printf("(%s:%d) ", t.filename, t.line);
            }
            System.out.println();
        }

        // Ошибки
        for (Token t : allTokens) {
            if (t.type == TokenType.ERROR) {
                System.err.printf("Ошибка: %s в файле %s, строка %d, колонка %d\n",
                        t.text, t.filename, t.line, t.column);
            }
        }
    }
}

