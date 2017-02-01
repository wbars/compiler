package me.wbars.scanner.io;

public class StringGrammarReader extends GrammarReader {
    private final String content;

    public StringGrammarReader(String content) {
        this.content = content;
    }

    @Override
    public String getGrammarContent() {
        return content;
    }
}
