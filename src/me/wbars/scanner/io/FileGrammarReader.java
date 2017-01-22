package me.wbars.scanner.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileGrammarReader extends GrammarReader {
    private final String filePath;

    public FileGrammarReader(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public String getGrammarContent() {
        try {
            return Files.readAllLines(Paths.get(filePath)).stream()
                    .reduce((s, s2) -> s + "\n" + s2).orElse("");
        } catch (IOException e) {
            return "";
        }
    }
}
