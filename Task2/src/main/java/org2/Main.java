package org2;

import java.util.*;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("Использование: java Main файл1.ada файл2.ada ...");
            return;
        }
        LexicalAnalyzer la = new LexicalAnalyzer();
        la.processFiles(Arrays.asList(args));
        la.report();
    }
}
