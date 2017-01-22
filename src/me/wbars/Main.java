package me.wbars;

import me.wbars.scanner.Scanner;
import me.wbars.scanner.io.ScannerFilePersister;
import me.wbars.scanner.models.Token;
import me.wbars.scanner.models.TransitionTable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException {
//        GrammarReader stringGrammarReader = new FileGrammarReader("grammar.txt");
//        TransitionTable table = stringGrammarReader.readTable();
//        ScannerFilePersister.writeToFile(table, "table");
        TransitionTable table1 = ScannerFilePersister.fromFile("table");
        List<Token> scan = Scanner.scan(getFileContents("file1.txt"), table1);
        scan.forEach(System.out::println);
    }

    private static String getFileContents(String path) throws IOException {
        return Files.readAllLines(Paths.get(path)).stream()
                .reduce((s, s2) -> s + "\n" + s2).orElse("");
    }
}
