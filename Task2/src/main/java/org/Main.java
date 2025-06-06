package org;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.*;

class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: java AdaLexer <file1> [file2 ...]");
            System.exit(1);
        }
        AdaLexer lexer = new AdaLexer();
        List<AdaLexer.Token> allTokens = new ArrayList<>();
        List<AdaLexer.Token> identifiers = new ArrayList<>();
        Map<AdaLexer.TokenType, Integer> tokenCounts = new EnumMap<>(AdaLexer.TokenType.class);

        for (String file : args) {
            try {
                String content = new String(Files.readAllBytes(Paths.get(file)));
                List<AdaLexer.Token> tokens = lexer.tokenize(content, file);
                allTokens.addAll(tokens);

                for (AdaLexer.Token token : tokens) {
                    tokenCounts.put(token.getType(), tokenCounts.getOrDefault(token.getType(), 0) + 1);
                    if (token.getType() == AdaLexer.TokenType.IDENTIFIER) {
                        identifiers.add(token);
                    }
                }
            } catch (IOException e) {
                System.err.println("Error reading file " + file + ": " + e.getMessage());
            } catch (LexerError e) {
                System.err.println("Error reading file " + file + ": " + e.getMessage());
                System.exit(0);
            }
        }

        int totalTokens = allTokens.size();
        System.out.println("Total tokens: " + totalTokens);
        System.out.println("\nToken frequencies (absolute and relative):");
        for (AdaLexer.TokenType type : tokenCounts.keySet()) {
            int count = tokenCounts.get(type);
            double frequency = (double) count / totalTokens;
            System.out.printf("%-18s: %-6d (%.4f)%n", type, count, frequency);
        }

        System.out.println("\nIdentifiers in lexicographical order:");
        identifiers.stream()
                .sorted(Comparator.comparing(AdaLexer.Token::getValue, String.CASE_INSENSITIVE_ORDER))
                .forEach(token -> System.out.printf("%-20s in %-20s at line %d%n",
                        token.getValue(), token.getFileName(), token.getLine()));
    }
}