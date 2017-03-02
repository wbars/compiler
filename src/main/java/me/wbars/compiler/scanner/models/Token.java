package me.wbars.compiler.scanner.models;

import static java.util.Objects.requireNonNull;

public class Token {
    private final PartOfSpeech pos;
    private final String value;

    public Token(PartOfSpeech pos, String value) {
        this.pos = requireNonNull(pos);
        this.value = requireNonNull(value);
    }

    public PartOfSpeech getPos() {
        return pos;
    }

    public String getValue() {
        return value;
    }

    public static Token create(String posName, String value) {
        return new Token(PartOfSpeech.getOrCreate(posName), value);
    }

    @Override
    public String toString() {
        return pos.name.toUpperCase() + "(" + value + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Token token = (Token) o;

        if (!pos.equals(token.pos)) return false;
        return value.equals(token.value);
    }

    @Override
    public int hashCode() {
        int result = pos.hashCode();
        result = 31 * result + value.hashCode();
        return result;
    }
}
