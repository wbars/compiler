package me.wbars.compiler;

import me.wbars.compiler.generator.CodeToHexClassFileConverter;
import me.wbars.compiler.generator.JvmBytecodeGenerator;
import me.wbars.compiler.generator.code.GeneratedCode;
import me.wbars.compiler.optimizer.ConstantFoldingOptimizer;
import me.wbars.compiler.optimizer.OptimizeProcessor;
import me.wbars.compiler.parser.Parser;
import me.wbars.compiler.parser.models.Node;
import me.wbars.compiler.scanner.Scanner;
import me.wbars.compiler.scanner.io.FileGrammarReader;
import me.wbars.compiler.scanner.io.GrammarReader;
import me.wbars.compiler.scanner.io.ScannerFilePersister;
import me.wbars.compiler.scanner.models.Token;
import me.wbars.compiler.scanner.models.TransitionTable;
import me.wbars.compiler.semantic.ASTProcessor;
import me.wbars.compiler.semantic.models.ProgramNode;
import me.wbars.compiler.semantic.models.types.TypeRegistry;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException {
        GrammarReader stringGrammarReader = new FileGrammarReader("tokens.txt");
        TransitionTable table = stringGrammarReader.readTable();
        ScannerFilePersister.writeToFile(table, "table");
        TransitionTable table1 = ScannerFilePersister.fromFile("table");

        List<Token> scan = Scanner.scan(getFileContents(args[0]), table1);
        Node parse = Parser.parse(scan);
        ASTProcessor astProcessor = new ASTProcessor();
        ProgramNode ast = astProcessor.parseProgram(parse);
        TypeRegistry typeRegistry = new TypeRegistry();
        ast.getProcessedType(typeRegistry);

        OptimizeProcessor optimizeProcessor = new OptimizeProcessor();
        optimizeProcessor.register("constant folding", new ConstantFoldingOptimizer());
        optimizeProcessor.process(ast);

        GeneratedCode generatedCode = JvmBytecodeGenerator.generateCode(ast);
        CodeToHexClassFileConverter.toFile(generatedCode);
    }

    private static String getFileContents(String filename) throws IOException {
        URL resource = Main.class.getClassLoader().getResource("compiler/examples/" + filename);
        if (resource == null) throw new IllegalArgumentException();
        return Files.readAllLines(Paths.get(resource.getPath())).stream()
                .reduce((s, s2) -> s + "\n" + s2).orElse("");
    }
}
