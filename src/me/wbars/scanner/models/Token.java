package me.wbars.scanner.models;

public class Token {
    private final PartOfSpeech pos;
    private final String value;

    public Token(PartOfSpeech pos, String value) {
        this.pos = pos;
        this.value = value;
    }

    public PartOfSpeech getPos() {
        return pos;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return pos.name.toUpperCase() + "(" + value + ")";
    }
}
