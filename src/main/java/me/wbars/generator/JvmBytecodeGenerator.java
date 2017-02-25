package me.wbars.generator;

import me.wbars.generator.code.GeneratedCode;
import me.wbars.semantic.ReturnStmtNode;
import me.wbars.semantic.models.*;
import me.wbars.semantic.models.types.ArrayType;
import me.wbars.semantic.models.types.Type;
import me.wbars.semantic.models.types.TypeRegistry;
import me.wbars.utils.Registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static me.wbars.generator.JvmBytecodeCommandFactory.*;
import static me.wbars.utils.CollectionsUtils.merge;

public class JvmBytecodeGenerator {
    public RegistersTable getRegistersTable() {
        return registersTable;
    }

    private RegistersTable registersTable;
    private final List<CodeLine> lines = new ArrayList<>();
    private ConstantPool constantPool;
    private final Map<String, Function<Integer, CodeLine>> reverseRelationMappers = new HashMap<>();
    private final Registry<NativeFunction<ProcedureStmtNode>> builtInFunctionsRegistry = new BuiltInFunctionsRegistry(this);
    private final Registry<NativeFunction<List<Type>>> stdFunctionRegistry = new Registry<>();


    private JvmBytecodeGenerator() {
        this(new ConstantPool(), new RegistersTable(null, -1));
    }

    private JvmBytecodeGenerator(ConstantPool constantPool, RegistersTable registersTable) {
        this.constantPool = constantPool;
        this.registersTable = registersTable;

        registerRelationMappers();
        registerStdLibFunctions();
    }

    public JvmBytecodeGenerator(ConstantPool constantPool) {
        this(constantPool, new RegistersTable(null, -1));
    }

    private void registerStdLibFunctions() {
        stdFunctionRegistry.register("write", types -> BytecodeFunctionsUtils.printMethod(this, types));
        stdFunctionRegistry.register("writeln", types -> BytecodeFunctionsUtils.printLnMethod(this, types));
    }

    private void registerRelationMappers() {
        reverseRelationMappers.put("=", JvmBytecodeCommandFactory::ifCmpNe);
        reverseRelationMappers.put("!=", JvmBytecodeCommandFactory::ifCmp);
        reverseRelationMappers.put(">", JvmBytecodeCommandFactory::ifLessOrEqualThan);
        reverseRelationMappers.put("<", JvmBytecodeCommandFactory::ifGreaterOrEqualThan);

        reverseRelationMappers.put(">=", JvmBytecodeCommandFactory::ifLess);
        reverseRelationMappers.put("<=", JvmBytecodeCommandFactory::ifGreater);

        reverseRelationMappers.put("", JvmBytecodeCommandFactory::ifEq);
        reverseRelationMappers.put("!", JvmBytecodeCommandFactory::ifNe);
    }

    public static GeneratedCode generateCode(BlockNode blockNode) {
        JvmBytecodeGenerator generator = new JvmBytecodeGenerator();
        blockNode.generateCode(generator);
        return stateSnapshot(generator);
    }

    public static GeneratedCode generateCode(BlockNode blockNode, ConstantPool constantPool, RegistersTable registerTable) {
        JvmBytecodeGenerator generator = new JvmBytecodeGenerator(constantPool, registerTable);
        blockNode.generateCode(generator);
        return stateSnapshot(generator);
    }

    private static GeneratedCode stateSnapshot(JvmBytecodeGenerator generator) {
        return new GeneratedCode(generator.lines, generator.constantPool);
    }

    private int generateCodeInNestedScope(ASTNode node, int forBlockEnd) {
        RegistersTable saved = registersTable;
        registersTable = new RegistersTable(saved, forBlockEnd);
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
        blockNode.getProcOrFunctionDeclarations().forEach(declaration -> BytecodeFunctionsUtils.registerMethod(this, declaration));
        blockNode.getStatements().forEach(this::generateCode);
        return -1;
    }

    public int generate(AssignmentStatementNode assignment) {
        addTypedGeneratedCommand(JvmBytecodeCommandFactory::loadRegister, assignment.getRight());
        addTypedGeneratedCommand(JvmBytecodeCommandFactory::storeRegister, assignment.getLeft());
        return -1;
    }

    public int generate(LiteralNode literalNode) {
        addTypedCommand(JvmBytecodeCommandFactory::loadConstant, constantPool.getConstantIndex(literalNode.getValue(), literalNode.getType()), literalNode);
        return storeInNextRegister(literalNode.getType());
    }

    public int storeInNextRegister(Type type) {
        return addTypedCommand(JvmBytecodeCommandFactory::storeRegister, registersTable.nextRegister(), type);
    }

    public int generate(IdentifierNode identifierNode) {
        return registersTable.lookupOrRegister(identifierNode.getValue());
    }

    private boolean isRelationOperator(String operator) {
        return reverseRelationMappers.containsKey(operator);
    }

    public int generate(ExprNode exprNode) {
        if (exprNode.getRight() == null) return generateCode(exprNode.getLeft());
        if (isRelationOperator(exprNode.getValue())) {
            addTypedGeneratedCommand(JvmBytecodeCommandFactory::loadRegister, exprNode.getLeft());
            addTypedGeneratedCommand(JvmBytecodeCommandFactory::loadRegister, exprNode.getRight());

            String operator = exprNode.getValue();
            List<CodeLine> trueBranch = singletonList(loadInteger(1));
            List<CodeLine> falseBranch = singletonList(loadInteger(0));

            OpCommand dummyOperatorCommand = reverseRelationMappers.get(operator).apply(-1).getCommand();
            OpCommand dummyGotoCommand = JvmBytecodeCommandFactory.gotoCommand(-1).getCommand();

            addCommand(reverseRelationMappers.get(operator), getSize(merge(
                    getCommands(trueBranch),
                    asList(dummyGotoCommand, dummyOperatorCommand)))
            );
            trueBranch.forEach(this::addCodeLine);

            addCommand(JvmBytecodeCommandFactory::gotoCommand, getSize(merge(
                    getCommands(falseBranch),
                    singletonList(dummyGotoCommand))));
            falseBranch.forEach(this::addCodeLine);
            return addTypedCommand(JvmBytecodeCommandFactory::storeRegister, registersTable.nextRegister(), TypeRegistry.INTEGER);
        }
        return -1;
    }

    private List<OpCommand> getCommands(List<CodeLine> trueBranch) {
        return trueBranch.stream().map(CodeLine::getCommand).collect(Collectors.toList());
    }

    private int getSize(List<OpCommand> commands) {
        return commands.stream()
                .mapToInt(OpCommand::bytecodeSize)
                .sum();
    }

    public int generate(BinaryArithmeticOpNode binaryArithmeticOpNode) {
        Integer leftRegister = generateCode(binaryArithmeticOpNode.getLeft());
        Integer rightRegister = generateCode(binaryArithmeticOpNode.getRight());

        addTypedCommand(JvmBytecodeCommandFactory::loadRegister, leftRegister, binaryArithmeticOpNode.getLeft());
        addTypedCommand(JvmBytecodeCommandFactory::loadRegister, rightRegister, binaryArithmeticOpNode.getRight());

        addTypedCommand(binaryArithmeticOpNode, JvmBytecodeCommandFactory::arithmeticOperation);

        return storeInNextRegister(binaryArithmeticOpNode.getType());
    }

    private void addTypedCommand(BinaryArithmeticOpNode binaryArithmeticOpNode, BiFunction<String, Type, CodeLine> factoryMethod) {
        addCodeLine(factoryMethod.apply(binaryArithmeticOpNode.getValue(), binaryArithmeticOpNode.getType()));
    }

    Integer addTypedGeneratedCommand(BiFunction<Integer, Type, CodeLine> factoryMethod, ASTNode node) {
        return addTypedCommand(factoryMethod, generateCode(node), node);
    }

    private int addTypedCommand(BiFunction<Integer, Type, CodeLine> factoryMethod, Integer register, ASTNode node) {
        addTypedCommand(factoryMethod, register, node.getType());
        return register;
    }

    public int addTypedCommand(BiFunction<Integer, Type, CodeLine> factoryMethod, Integer register, Type type) {
        addCodeLine(factoryMethod.apply(register, type));
        return register;
    }

    int addCommand(Function<Integer, CodeLine> factoryMethod, Integer register) {
        addCodeLine(factoryMethod.apply(register));
        return register;
    }

    void addCodeLine(CodeLine apply) {
        lines.add(apply);
    }

    public int generate(ProcedureStmtNode procedure) {
        NativeFunction<ProcedureStmtNode> builtInFunction = builtInFunctionsRegistry.lookup(procedure.getIdentifier().getValue());
        return builtInFunction != null ? builtInFunction.apply(procedure) : addMethodCall(procedure);
    }

    private int addMethodCall(ProcedureStmtNode procedureStmtNode) {
        List<Type> argumentTypes = procedureStmtNode.getArguments().stream()
                .map(ActualParameterNode::getType)
                .collect(Collectors.toList());

        String methodName = procedureStmtNode.getIdentifier().getValue();
        int register = getMethodCall(methodName, argumentTypes);
        procedureStmtNode.getArguments().forEach(p -> addTypedGeneratedCommand(
                JvmBytecodeCommandFactory::loadRegister,
                p.getFirst())
        );

        if (isCustom(methodName)) {
            addCommand(JvmBytecodeCommandFactory::invokeStatic, register);
            if (procedureStmtNode.getType() == TypeRegistry.VOID) return -1;
            return addTypedCommand(JvmBytecodeCommandFactory::storeRegister, registersTable.nextRegister(), TypeRegistry.INTEGER);
        }

        return addCommand(JvmBytecodeCommandFactory::invokeVirtual, register);
    }

    private boolean isCustom(String methodName) {
        return constantPool.getCustomFunctionIndexesRegistry().lookup(methodName) != null;
    }

    private int getMethodCall(String procedureName, List<Type> argumentTypes) {
        NativeFunction<List<Type>> function = stdFunctionRegistry.lookup(procedureName);
        if (function == null) function = constantPool.getCustomFunctionIndexesRegistry().lookup(procedureName);
        if (function == null)
            throw new RuntimeException("Undefined function: " + procedureName);
        return function.apply(argumentTypes);
    }

    public int generate(ArrayLiteralNode arrayLiteralNode) {
        pushInteger(arrayLiteralNode.getItems().size());
        addCommand(JvmBytecodeCommandFactory::newPrimitiveArray, getAType(((ArrayType) arrayLiteralNode.getType()).getType()));
        for (int i = 0; i < arrayLiteralNode.getItems().size(); i++) {
            addCodeLine(dup());
            pushInteger(i);
            ExprNode item = arrayLiteralNode.getItems().get(i);
            addTypedGeneratedCommand(JvmBytecodeCommandFactory::loadRegister, item);
            addCodeLine(arrayElementStore(item.getType()));
        }
        return addTypedCommand(JvmBytecodeCommandFactory::storeRegister, registersTable.lookupOrRegister(arrayLiteralNode.getValue()), arrayLiteralNode.getType());

    }

    private void pushInteger(int value) {
        addCodeLine(loadInteger(value));
    }

    private CodeLine loadInteger(int value) {
        return loadConstant(constantPool.getConstantIndex(String.valueOf(value), TypeRegistry.INTEGER), TypeRegistry.INTEGER);
    }

    private int getAType(Type type) {
        if (type == TypeRegistry.INTEGER) return 10;
        if (type == TypeRegistry.LONG) return 11;
        if (type == TypeRegistry.DOUBLE) return 7;
        if (type == TypeRegistry.CHAR) return 5;
        if (type == TypeRegistry.BOOLEAN) return 4;
        throw new RuntimeException();
    }

    public int generate(GetIndexNode getIndexNode) {
        addTypedCommand(JvmBytecodeCommandFactory::loadRegister, registersTable.lookupOrRegister(getIndexNode.getTarget().getValue()), getIndexNode.getTarget());
        getIndexNode.getIndexes().forEach(n -> addTypedGeneratedCommand(JvmBytecodeCommandFactory::loadRegister, n));
        addCodeLine(JvmBytecodeCommandFactory.arrayElementLoad(getIndexNode.getType()));
        return addTypedCommand(JvmBytecodeCommandFactory::storeRegister, registersTable.nextRegister(), getIndexNode.getType());
    }

    public int generate(IfStmtNode ifStmtNode) {
        addTypedGeneratedCommand(JvmBytecodeCommandFactory::loadRegister, ifStmtNode.getCondition());
        OpCommand dummyOperatorCommand = reverseRelationMappers.get("").apply(-1).getCommand();
        OpCommand dummyGotoCommand = JvmBytecodeCommandFactory.gotoCommand(-1).getCommand();

        addCommand(reverseRelationMappers.get(""), getSize(merge(
                getCommands(getLines(ifStmtNode.getTrueBranch())),
                asList(dummyGotoCommand, dummyOperatorCommand)))
        );
        ifStmtNode.getTrueBranch().forEach(this::generateCode);

        addCommand(JvmBytecodeCommandFactory::gotoCommand, getSize(merge(
                getCommands(getLines(ifStmtNode.getFalseBranch())),
                singletonList(dummyGotoCommand))));
        ifStmtNode.getFalseBranch().forEach(this::generateCode);
        return -1;
    }

    private List<CodeLine> getLines(List<ASTNode> nodes) {
        JvmBytecodeGenerator generator = new JvmBytecodeGenerator(constantPool, registersTable);
        nodes.forEach(generator::generateCode);
        return generator.lines;
    }

    public int generate(RepeatStmtNode repeatStmtNode) {
        int currentIndex = getCurrentIndex();
        int blockSize = getCommandsSize(merge(
                getLines(repeatStmtNode.getStatements()),
                getLines(singletonList(repeatStmtNode.getUntilExpression())),
                singletonList(dummyLoadRegister())
        ));
        repeatStmtNode.getStatements().forEach(node -> generateCodeInNestedScope(node, blockSize + currentIndex));
        addTypedGeneratedCommand(JvmBytecodeCommandFactory::loadRegister, repeatStmtNode.getUntilExpression());
        return addCommand(JvmBytecodeCommandFactory::ifEq, -1 * blockSize);
    }

    public int getCommandsSize(List<CodeLine> merge) {
        return getSize(getCommands(merge));
    }

    private int getCurrentIndex() {
        return getCommandsSize(lines);
    }

    public int generate(ActualParameterNode actualParameterNode) {
        return generateCode(actualParameterNode.getFirst());
    }

    int getOffsetToEndOfBlock() {
        return registersTable.blockEndIndex() - (getCurrentIndex() + 1);
    }

    ConstantPool getConstantPool() {
        return constantPool;
    }

    public int generate(ForStmtNode forStmtNode) {
        int currentIndex = getCurrentIndex();
        addTypedGeneratedCommand(JvmBytecodeCommandFactory::loadRegister, forStmtNode.getInitialValue());
        int controlVariable = addTypedGeneratedCommand(JvmBytecodeCommandFactory::storeRegister, forStmtNode.getControlVar());

        int blockSize = getCommandsSize(merge(
                singletonList(JvmBytecodeCommandFactory.ifGreater(-1)),
                getLines(forStmtNode.getStatements()),
                asList(
                        dummyLoadRegister(), //load initial value
                        dummyLoadRegister(), //load inc value
                        JvmBytecodeCommandFactory.inc(controlVariable << 4 | (1 << 4)) //increment
                ),
                singletonList(gotoCommand(-1)),
                singletonList(dummyLoadRegister()))
        );

        int finalValueIndex = addTypedGeneratedCommand(JvmBytecodeCommandFactory::loadRegister, forStmtNode.getFinalValue());

        addTypedCommand(JvmBytecodeCommandFactory::loadRegister, controlVariable, TypeRegistry.INTEGER);
        addTypedCommand(JvmBytecodeCommandFactory::loadRegister, finalValueIndex, TypeRegistry.INTEGER);

        addCommand(JvmBytecodeCommandFactory::ifGreater, blockSize - getCommandsSize(singletonList(dummyLoadRegister())));

        int tailSize = getCommandsSize(asList(
                dummyLoadRegister(),
                dummyLoadRegister(),
                dummyStoreRegister(),
                dummyStoreRegister(),
                JvmBytecodeCommandFactory.gotoCommand(-1))
        );
        forStmtNode.getStatements().forEach(node -> generateCodeInNestedScope(node, blockSize + currentIndex + tailSize));

        addTypedCommand(JvmBytecodeCommandFactory::loadRegister, controlVariable, TypeRegistry.INTEGER);
        pushInteger(1);
        addCommand(JvmBytecodeCommandFactory::inc, ((controlVariable & 0xFF) << 8) | (1 & 0xFF));
        return addCommand(JvmBytecodeCommandFactory::gotoCommand, -1 * blockSize);
    }

    private CodeLine dummyLoadRegister() {
        return JvmBytecodeCommandFactory.loadRegister(-1, TypeRegistry.INTEGER);
    }

    private CodeLine dummyStoreRegister() {
        return JvmBytecodeCommandFactory.storeRegister(-1, TypeRegistry.INTEGER);
    }

    public int generate(ReturnStmtNode returnStmtNode) {
        if (returnStmtNode.getExpr() == null) {
            addCodeLine(JvmBytecodeCommandFactory.returnCommand());
        } else {
            addTypedGeneratedCommand(JvmBytecodeCommandFactory::loadRegister, returnStmtNode.getExpr());
            addCodeLine(JvmBytecodeCommandFactory.returnCommand(returnStmtNode.getExpr().getType()));
        }
        return -1;
    }
}

