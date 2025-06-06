package org2;

public class Token {
    public final TokenType type;
    public final String text;
    public final String filename;
    public final int line;
    public final int column;

    public Token(TokenType type, String text, String filename, int line, int column) {
        this.type = type;
        this.text = text;
        this.filename = filename;
        this.line = line;
        this.column = column;
    }
}
