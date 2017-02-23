package me.wbars;

import me.wbars.generator.CodeToHexClassFileConverter;
import me.wbars.generator.JvmBytecodeGenerator;
import me.wbars.generator.code.GeneratedCode;
import me.wbars.parser.Parser;
import me.wbars.parser.models.Node;
import me.wbars.scanner.Scanner;
import me.wbars.scanner.io.ScannerFilePersister;
import me.wbars.scanner.models.Token;
import me.wbars.scanner.models.TransitionTable;
import me.wbars.semantic.AST;
import me.wbars.semantic.models.ProgramNode;
import me.wbars.semantic.models.types.TypeRegistry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException {
//        GrammarReader stringGrammarReader = new FileGrammarReader("tokens.txt");
//        TransitionTable table = stringGrammarReader.readTable();
//        ScannerFilePersister.writeToFile(table, "table");
        TransitionTable table1 = ScannerFilePersister.fromFile("table");
        List<Token> scan = Scanner.scan(getFileContents("file2.txt"), table1);
        Node parse = Parser.parse(scan);
        ProgramNode ast = AST.parseProgram(parse);
        TypeRegistry typeRegistry = new TypeRegistry();
        ast.getProcessedType(typeRegistry);
        GeneratedCode generatedCode = JvmBytecodeGenerator.generateCode(ast);
        CodeToHexClassFileConverter.toFile(generatedCode, "Main.class");
    }

    private static String getFileContents(String path) throws IOException {
        return Files.readAllLines(Paths.get(path)).stream()
                .reduce((s, s2) -> s + "\n" + s2).orElse("");
    }
}
