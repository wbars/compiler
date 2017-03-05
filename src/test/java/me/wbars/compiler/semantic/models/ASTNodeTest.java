package me.wbars.compiler.semantic.models;

import me.wbars.compiler.Main;
import me.wbars.compiler.parser.Parser;
import me.wbars.compiler.parser.models.Node;
import me.wbars.compiler.scanner.Scanner;
import me.wbars.compiler.scanner.io.ScannerFilePersister;
import me.wbars.compiler.scanner.models.Token;
import me.wbars.compiler.scanner.models.TransitionTable;
import me.wbars.compiler.semantic.ASTProcessor;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ASTNodeTest {

    public void testTokenEquals(String filename) throws Exception {
        TransitionTable table1 = ScannerFilePersister.fromFile("table");
        List<Token> scan = Scanner.scan(getFileContents(filename), table1);
        Node parse = Parser.parse(scan);
        ASTProcessor astProcessor = new ASTProcessor();
        ProgramNode ast = astProcessor.parseProgram(parse);

        assertEquals(scan, ast.getNodesTokens().get(ast));
        //todo handle parens
    }

    @Test
    public void testCalculator() throws Exception {
        testTokenEquals("calculator.pas");
    }

    @Test
    public void testVariables() throws Exception {
        testTokenEquals("variables.pas");
    }

    @Test
    public void testFactorial() throws Exception {
        testTokenEquals("factorial.pas");
    }

    @Test
    public void testBinarySearch() throws Exception {
        testTokenEquals("binarySearch.pas");
    }

    @Test
    public void testMergeSort() throws Exception {
        testTokenEquals("mergeSort.pas");
    }

    private static String getFileContents(String filename) throws IOException {
        URL resource = Main.class.getClassLoader().getResource("compiler/examples/" + filename);
        if (resource == null) throw new IllegalArgumentException();
        return Files.readAllLines(Paths.get(resource.getPath())).stream()
                .reduce((s, s2) -> s + "\n" + s2).orElse("");
    }
}