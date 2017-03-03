package me.wbars.compiler;

import me.wbars.compiler.generator.CodeToHexClassFileConverter;
import me.wbars.compiler.generator.JvmBytecodeGenerator;
import me.wbars.compiler.generator.code.GeneratedCode;
import me.wbars.compiler.optimizer.ConstantFoldingOptimizer;
import me.wbars.compiler.optimizer.OptimizeProcessor;
import me.wbars.compiler.parser.Parser;
import me.wbars.compiler.parser.models.Node;
import me.wbars.compiler.scanner.Scanner;
import me.wbars.compiler.scanner.io.ScannerFilePersister;
import me.wbars.compiler.scanner.models.Token;
import me.wbars.compiler.semantic.ASTProcessor;
import me.wbars.compiler.semantic.models.ProgramNode;
import me.wbars.compiler.semantic.models.types.TypeRegistry;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class Compiler {

    private final Scanner scanner;

    public Compiler() {
        scanner = new Scanner(Objects.requireNonNull(ScannerFilePersister.fromFile("table")));
    }

    public String compile(String sourceCode) {
        List<Token> scan = scanner.scan(sourceCode);
        Node parse = Parser.parse(scan);
        ASTProcessor astProcessor = new ASTProcessor();
        ProgramNode ast = astProcessor.parseProgram(parse);
        TypeRegistry typeRegistry = new TypeRegistry();
        ast.getProcessedType(typeRegistry);

        OptimizeProcessor optimizeProcessor = new OptimizeProcessor();
        optimizeProcessor.register("constant folding", new ConstantFoldingOptimizer());
        optimizeProcessor.process(ast);

        GeneratedCode generatedCode = JvmBytecodeGenerator.generateCode(ast);
        try {
            return CodeToHexClassFileConverter.toFile(generatedCode);
        } catch (IOException e) {
            return null;
        }
    }

    public Scanner getScanner() {
        return scanner;
    }
}
