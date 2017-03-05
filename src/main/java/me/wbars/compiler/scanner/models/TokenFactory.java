package me.wbars.compiler.scanner.models;

import me.wbars.compiler.parser.models.Tokens;

public class TokenFactory {
    public static Token createSemicolon() {
        return Token.create(Tokens.SEMICOLON, ";");
    }

    public static Token createDot() {
        return Token.create(Tokens.DOT, ".");
    }

    public static Token createColon() {
        return Token.create(Tokens.COLON, ":");
    }

    public static Token createAssignment() {
        return Token.create(Tokens.ASSIGNMENT, ":=");
    }

    public static Token createDirection(boolean increment) {
        return Token.create(Tokens.DIRECTION, increment ? "to" : "downto");
    }

    public static Token openBracket() {
        return Token.create(Tokens.OPEN_BRACKET, "[");
    }

    public static Token closeBracket() {
        return Token.create(Tokens.CLOSE_BRACKET, "]");
    }

    public static Token openParen() {
        return Token.create(Tokens.OPEN_PAREN, "(");
    }

    public static Token closeParen() {
        return Token.create(Tokens.CLOSE_PAREN, ")");
    }

    public static Token createRelOp(String operation) {
        return Token.create(Tokens.RELOP, operation);
    }

    public static Token openCurly() {
        return Token.create(Tokens.OPEN_CURLY, "{");
    }

    public static Token closeCurly() {
        return Token.create(Tokens.CLOSE_CURLY, "}");
    }

    public static Token comma() {
        return Token.create(Tokens.COMMA, ",");
    }
}
