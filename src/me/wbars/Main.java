package me.wbars;

import me.wbars.parser.Parser;
import me.wbars.parser.models.Node;
import me.wbars.scanner.Scanner;
import me.wbars.scanner.io.FileGrammarReader;
import me.wbars.scanner.io.GrammarReader;
import me.wbars.scanner.io.ScannerFilePersister;
import me.wbars.scanner.models.Token;
import me.wbars.scanner.models.TransitionTable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException {
        GrammarReader stringGrammarReader = new FileGrammarReader("tokens.txt");
        TransitionTable table = stringGrammarReader.readTable();
        ScannerFilePersister.writeToFile(table, "table");
        TransitionTable table1 = ScannerFilePersister.fromFile("table");
        List<Token> scan = Scanner.scan(getFileContents("file1.txt"), table1);
        Node parse = Parser.parse(scan);
        System.out.println("Done");
    }

    private static String getFileContents(String path) throws IOException {
        return Files.readAllLines(Paths.get(path)).stream()
                .reduce((s, s2) -> s + "\n" + s2).orElse("");
    }
}
