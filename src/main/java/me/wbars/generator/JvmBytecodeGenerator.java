package me.wbars.generator;

import me.wbars.generator.code.GeneratedCode;
import me.wbars.semantic.models.*;
import me.wbars.semantic.models.types.ArrayType;
import me.wbars.semantic.models.types.Type;
import me.wbars.semantic.models.types.TypeRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static me.wbars.generator.JvmBytecodeCommandFactory.*;

public class JvmBytecodeGenerator {
    private RegistersTable registersTable = new RegistersTable(null);
    private final List<CodeLine> lines = new ArrayList<>();
    private ConstantPool constantPool;
    private final Map<String, Function<List<Type>, Integer>> stdLibFunctions = new HashMap<>();

    private JvmBytecodeGenerator() {
        this(new ConstantPool());
    }

    public JvmBytecodeGenerator(ConstantPool constantPool) {
        this.constantPool = constantPool;
        registerStdLibFunction();
    }

    private void registerStdLibFunction() {
        stdLibFunctions.put("write", this::addPrintMethod);
        stdLibFunctions.put("writeln", this::addPrintLnMethod);
    }

    public static GeneratedCode generateCode(ProgramNode programNode) {
        JvmBytecodeGenerator generator = new JvmBytecodeGenerator();
        programNode.getBlock().generateCode(generator);
        return stateSnapshot(generator);
    }

    private static <T> List<T> merge(List<T> first, List<T> second) {
        ArrayList<T> result = new ArrayList<>();
        result.addAll(first);
        result.addAll(second);
        return result;
    }

    private static GeneratedCode stateSnapshot(JvmBytecodeGenerator generator) {
        return new GeneratedCode(merge(generator.lines, singletonList(JvmBytecodeCommandFactory.returnCommand(-1))), generator.constantPool);
    }

    private int generateCodeInNestedScope(ASTNode node) {
        RegistersTable saved = registersTable;
        registersTable = new RegistersTable(saved);
        int result = generateCode(node);
        registersTable = saved;
        return result;
    }

    private int generateCode(ASTNode node) {
        return node.generateCode(this);
    }

    public int generate(BlockNode blockNode) {
        blockNode.getVarDeclarations().stream()
                .flatMap(d -> d.getIdentifiers().stream())
                .forEach(d -> registersTable.register(d.getValue()));
        blockNode.getConstDefinitions().forEach(n -> registersTable.register(n.getValue()));
        blockNode.getStatements().forEach(this::generateCodeInNestedScope);
        return -1;
    }

    public int generate(AssignmentStatementNode assignment) {
        addTypedGeneratedCommand(JvmBytecodeCommandFactory::loadRegister, assignment.getRight());
        addTypedGeneratedCommand(JvmBytecodeCommandFactory::storeRegister, assignment.getLeft());
        return -1;
    }

    public int generate(LiteralNode literalNode) {
        if (literalNode.getType() == TypeRegistry.SHORT) {
            lines.add(pushValue(Integer.parseInt(literalNode.getValue()), literalNode.getType()));
            return addTypedCommand(JvmBytecodeCommandFactory::storeRegister, registersTable.nextRegister(), literalNode);
        }

        addTypedCommand(JvmBytecodeCommandFactory::loadConstant, constantPool.getConstantIndex(literalNode.getValue(), literalNode.getType()), literalNode);
        return addTypedCommand(JvmBytecodeCommandFactory::storeRegister, registersTable.nextRegister(), literalNode);
    }

    public int generate(IdentifierNode identifierNode) {
        return registersTable.lookupOrRegister(identifierNode.getValue());
    }

    public int generate(ExprNode exprNode) {
        if (exprNode.getRight() == null) return generateCode(exprNode.getLeft());
        return -1;//todo general case
    }

    public int generate(BinaryArithmeticOpNode binaryArithmeticOpNode) {
        Integer leftRegister = generateCode(binaryArithmeticOpNode.getLeft());
        Integer rightRegister = generateCode(binaryArithmeticOpNode.getRight());

        addTypedCommand(JvmBytecodeCommandFactory::loadRegister, leftRegister, binaryArithmeticOpNode.getLeft());
        addTypedCommand(JvmBytecodeCommandFactory::loadRegister, rightRegister, binaryArithmeticOpNode.getRight());

        addTypedCommand(binaryArithmeticOpNode, JvmBytecodeCommandFactory::arithmeticOperation);

        return addTypedCommand(JvmBytecodeCommandFactory::storeRegister, registersTable.nextRegister(), binaryArithmeticOpNode);
    }

    private void addTypedCommand(BinaryArithmeticOpNode binaryArithmeticOpNode, BiFunction<String, Type, CodeLine> factoryMethod) {
        lines.add(factoryMethod.apply(binaryArithmeticOpNode.getValue(), binaryArithmeticOpNode.getType()));
    }

    public Integer addTypedGeneratedCommand(BiFunction<Integer, Type, CodeLine> factoryMethod, ASTNode node) {
        return addTypedCommand(factoryMethod, generateCode(node), node);
    }

    public int addTypedCommand(BiFunction<Integer, Type, CodeLine> factoryMethod, Integer register, ASTNode node) {
        addTypedCommand(factoryMethod, register, node.getType());
        return register;
    }

    public int addTypedCommand(BiFunction<Integer, Type, CodeLine> factoryMethod, Integer register, Type type) {
        lines.add(factoryMethod.apply(register, type));
        return register;
    }

    public int addCommand(Function<Integer, CodeLine> factoryMethod, Integer register) {
        lines.add(factoryMethod.apply(register));
        return register;
    }

    public int generate(ProcedureStmtNode procedureStmtNode) {
        List<Type> argumentTypes = procedureStmtNode.getArguments().stream()
                .map(ActualParameterNode::getType)
                .collect(Collectors.toList());

        int register = addMethod(procedureStmtNode.getIdentifier().getValue(), argumentTypes);
        procedureStmtNode.getArguments().forEach(p -> addTypedGeneratedCommand(
                JvmBytecodeCommandFactory::loadRegister,
                p.getFirst())
        );

        return addCommand(
                JvmBytecodeCommandFactory::invokeVirtual,
                register
        );
    }

    private int addMethod(String procedureName, List<Type> argumentTypes) {
        if (!stdLibFunctions.containsKey(procedureName)) throw new RuntimeException("Only write available now");
        return stdLibFunctions.get(procedureName).apply(argumentTypes);
    }

    private String getTypeAlias(Type type) {
        if (type == TypeRegistry.BOOLEAN) return "Z";
        if (type == TypeRegistry.LONG) return "J";
        if (type == TypeRegistry.STRING) return "Ljava/lang/String;";
        return String.valueOf(type.name().toUpperCase().charAt(0));
    }

    private String getTypeDescriptor(List<Type> argumentTypes, Type resultType) {
        String argumentsDescriptor = argumentTypes.stream()
                .map(this::getTypeAlias)
                .reduce((c, c2) -> c + c2).orElse("");
        return "(" + argumentsDescriptor + ")" + getTypeAlias(resultType);
    }

    private int addPrintMethod(List<Type> argumentTypes) {
        int outIndex = constantPool.getFieldOrMethodIndex("java/lang/System.out", "Ljava/io/PrintStream;");
        int printIndex = constantPool.getFieldOrMethodIndex("java/io/PrintStream.print", getTypeDescriptor(argumentTypes, TypeRegistry.VOID));
        addCommand(JvmBytecodeCommandFactory::getStatic, outIndex);
        return printIndex;
    }

    private int addPrintLnMethod(List<Type> argumentTypes) {
        int outIndex = constantPool.getFieldOrMethodIndex("java/lang/System.out", "Ljava/io/PrintStream;");
        int printIndex = constantPool.getFieldOrMethodIndex("java/io/PrintStream.println", getTypeDescriptor(argumentTypes, TypeRegistry.VOID));
        addCommand(JvmBytecodeCommandFactory::getStatic, outIndex);
        return printIndex;
    }

    public int generate(ArrayLiteralNode arrayLiteralNode) {
        addTypedCommand(JvmBytecodeCommandFactory::pushValue, arrayLiteralNode.getItems().size(), TypeRegistry.SHORT);
        addCommand(JvmBytecodeCommandFactory::newPrimitiveArray, getAType(((ArrayType)arrayLiteralNode.getType()).getType()));
        for (int i = 0; i < arrayLiteralNode.getItems().size(); i++) {
            lines.add(dup());
            lines.add(pushValue(i, TypeRegistry.SHORT));
            ExprNode item = arrayLiteralNode.getItems().get(i);
            addTypedGeneratedCommand(JvmBytecodeCommandFactory::loadRegister, item);
            lines.add(arrayElementStore(item.getType()));
        }
        return addTypedCommand(JvmBytecodeCommandFactory::storeRegister, registersTable.nextRegister(), arrayLiteralNode);
    }

    private int getAType(Type type) {
        if (type == TypeRegistry.INTEGER) return 10;
        if (type == TypeRegistry.SHORT) return 9;
        if (type == TypeRegistry.LONG) return 11;
        if (type == TypeRegistry.DOUBLE) return 7;
        if (type == TypeRegistry.CHAR) return 5;
        if (type == TypeRegistry.BOOLEAN) return 4;
        throw new RuntimeException();
    }
}
