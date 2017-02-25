package me.wbars.generator;

import me.wbars.generator.code.GeneratedCode;
import me.wbars.semantic.ReturnStmtNode;
import me.wbars.semantic.models.*;
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
    private JvmBytecodeGenerator(String className) {
        this(new ConstantPool(), new RegistersTable(null, -1), className);
    }

    public RegistersTable getRegistersTable() {
        return registersTable;
    }

    private RegistersTable registersTable;
    private final List<CodeLine> lines = new ArrayList<>();
    private ConstantPool constantPool;
    private final Map<String, Function<Integer, CodeLine>> reverseRelationMappers = new HashMap<>();
    private final Registry<NativeFunction<ProcedureStmtNode>> builtInFunctionsRegistry = new BuiltInFunctionsRegistry(this);
    private final Registry<NativeFunction<List<Type>>> stdFunctionRegistry = new Registry<>();
    private final String className;


    private JvmBytecodeGenerator(ConstantPool constantPool, RegistersTable registersTable, String className) {
        this.className = className;
        this.constantPool = constantPool;
        this.registersTable = registersTable;

        registerRelationMappers();
        registerStdLibFunctions();
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

    private static String capitalize(String str) {
        if (str.isEmpty()) return str;
        if (str.length() == 1) return str.toUpperCase();
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static GeneratedCode generateCode(ProgramNode programNode) {
        JvmBytecodeGenerator generator = new JvmBytecodeGenerator(capitalize(programNode.getValue()));
        generator.generateCode(programNode.getBlock());
        return new GeneratedCode(generator.lines, generator.constantPool, generator.className);
    }

    public static GeneratedCode generateCode(BlockNode blockNode, ConstantPool constantPool, RegistersTable registerTable, String className) {
        JvmBytecodeGenerator generator = new JvmBytecodeGenerator(constantPool, registerTable, className);
        blockNode.generateCode(generator);
        return new GeneratedCode(generator.lines, generator.constantPool, className);
    }

    private int generateCodeInNestedScope(ASTNode node, int forBlockEnd) {
        RegistersTable saved = registersTable;
        registersTable = new RegistersTable(saved, forBlockEnd);
        int result = generateCode(node);
        registersTable = saved;
        return result;
    }

    public int generateCode(ASTNode node) {
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
        generateCode(assignment.getRight());
        addTypedCommand(JvmBytecodeCommandFactory::storeRegister, registersTable.lookupOrRegister(assignment.getLeft().getValue()), assignment.getLeft().getType());
        return -1;
    }

    public int generate(LiteralNode literalNode) {
        return addTypedCommand(JvmBytecodeCommandFactory::loadConstant, constantPool.getConstantIndex(literalNode.getValue(), literalNode.getType()), literalNode);
    }

    public int storeInNextRegister(Type type) {
        return addTypedCommand(JvmBytecodeCommandFactory::storeRegister, registersTable.nextRegister(), type);
    }

    public int generate(IdentifierNode identifierNode) {
        int index = registersTable.lookupOrRegister(identifierNode.getValue());
        return addTypedCommand(JvmBytecodeCommandFactory::loadRegister, index, identifierNode.getType());
    }

    private boolean isRelationOperator(String operator) {
        return reverseRelationMappers.containsKey(operator);
    }

    public int generate(ExprNode exprNode) {
        if (exprNode.getRight() == null) return generateCode(exprNode.getLeft());
        if (isRelationOperator(exprNode.getValue())) {
            generateCode(exprNode.getLeft());
            generateCode(exprNode.getRight());

            List<CodeLine> trueBranch = singletonList(loadInteger(1));
            List<CodeLine> falseBranch = singletonList(loadInteger(0));

            Function<Integer, CodeLine> operator = reverseRelationMappers.get(exprNode.getValue());
            OpCommand dummyOperatorCommand = operator.apply(-1).getCommand();
            OpCommand dummyGotoCommand = JvmBytecodeCommandFactory.gotoCommand(-1).getCommand();

            addCommand(operator, getSize(merge(
                    getCommands(trueBranch),
                    asList(dummyGotoCommand, dummyOperatorCommand)))
            );
            trueBranch.forEach(this::addCodeLine);

            addCommand(JvmBytecodeCommandFactory::gotoCommand, getSize(merge(
                    getCommands(falseBranch),
                    singletonList(dummyGotoCommand))));
            falseBranch.forEach(this::addCodeLine);
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
        generateCode(binaryArithmeticOpNode.getLeft());
        generateCode(binaryArithmeticOpNode.getRight());
        addTypedCommand(binaryArithmeticOpNode, JvmBytecodeCommandFactory::arithmeticOperation);
        return -1;
    }

    private void addTypedCommand(BinaryArithmeticOpNode binaryArithmeticOpNode, BiFunction<String, Type, CodeLine> factoryMethod) {
        addCodeLine(factoryMethod.apply(binaryArithmeticOpNode.getValue(), binaryArithmeticOpNode.getType()));
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
        procedureStmtNode.getArguments().forEach(p -> generateCode(p.getFirst()));

        if (isCustom(methodName)) {
            return addCommand(JvmBytecodeCommandFactory::invokeStatic, register);
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
        addCommand(JvmBytecodeCommandFactory::newPrimitiveArray, arrayLiteralNode.getType().getType().aType());
        for (int i = 0; i < arrayLiteralNode.getItems().size(); i++) {
            addCodeLine(dup());
            pushInteger(i);
            ExprNode item = arrayLiteralNode.getItems().get(i);
            generateCode(item);
            addCodeLine(arrayElementStore(item.getType()));
        }
        return -1;

    }

    private void pushInteger(int value) {
        addCodeLine(loadInteger(value));
    }

    private CodeLine loadInteger(int value) {
        return loadConstant(constantPool.getConstantIndex(String.valueOf(value), TypeRegistry.INTEGER), TypeRegistry.INTEGER);
    }

    public int generate(GetIndexNode getIndexNode) {
        generateCode(getIndexNode.getTarget());
        getIndexNode.getIndexes().forEach(this::generateCode);
        addCodeLine(JvmBytecodeCommandFactory.arrayElementLoad(getIndexNode.getType()));
        return -1;
    }

    public int generate(IfStmtNode ifStmtNode) {
        generateCode(ifStmtNode.getCondition());
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
        JvmBytecodeGenerator generator = new JvmBytecodeGenerator(constantPool, registersTable, className);
        nodes.forEach(generator::generateCode);
        return generator.lines;
    }

    public int generate(RepeatStmtNode repeatStmtNode) {
        int currentIndex = getCurrentIndex();
        int blockSize = getCommandsSize(merge(
                getLines(repeatStmtNode.getStatements()),
                getLines(singletonList(repeatStmtNode.getUntilExpression()))
        ));
        repeatStmtNode.getStatements().forEach(node -> generateCodeInNestedScope(node, blockSize + currentIndex + getSize(singletonList(OpCommand.IFEQ))));
        generateCode(repeatStmtNode.getUntilExpression());
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
        generateCode(forStmtNode.getInitialValue());
        int controlVariable = addTypedCommand(JvmBytecodeCommandFactory::storeRegister, registersTable.lookupOrRegister(forStmtNode.getControlVar().getValue()), forStmtNode.getControlVar().getType());

        int blockSize = getCommandsSize(merge(
                singletonList(JvmBytecodeCommandFactory.ifGreater(-1)),
                getLines(forStmtNode.getStatements()),
                asList(
                        dummyLoadRegister(), //load initial value
                        dummyLoadRegister(), //load inc value
                        JvmBytecodeCommandFactory.inc(controlVariable, 1) //increment
                ),
                singletonList(gotoCommand(-1)),
                singletonList(dummyLoadRegister()))
        );

        generateCode(forStmtNode.getFinalValue());
        int finalValueIndex = storeInNextRegister(forStmtNode.getFinalValue().getType());

        generateCode(forStmtNode.getControlVar());
        addTypedCommand(JvmBytecodeCommandFactory::loadRegister, finalValueIndex, TypeRegistry.INTEGER);

        addCommand(JvmBytecodeCommandFactory::ifGreater, blockSize - getCommandsSize(singletonList(dummyLoadRegister())));

        int tailSize = getCommandsSize(asList(
                dummyLoadRegister(),
                dummyLoadRegister(),
                dummyStoreRegister(),
                JvmBytecodeCommandFactory.gotoCommand(-1))
        );
        forStmtNode.getStatements().forEach(node -> generateCodeInNestedScope(node, blockSize + currentIndex + tailSize));

        generateCode(forStmtNode.getControlVar());
        pushInteger(1);
        addCodeLine(JvmBytecodeCommandFactory.inc(controlVariable, 1));
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
            generateCode(returnStmtNode.getExpr());
            addCodeLine(JvmBytecodeCommandFactory.returnCommand(returnStmtNode.getExpr().getType()));
        }
        return -1;
    }

    public int generate(WhileStmtNode whileStmtNode) {
        int currentIndex = getCurrentIndex();
        int currentSize = lines.size();
        generateCode(whileStmtNode.getCondition());
        List<CodeLine> conditionLoadCommands = lines.subList(currentSize + 1, lines.size());

        int blockSize = getCommandsSize(merge(
                singletonList(JvmBytecodeCommandFactory.ifEq(-1)),
                getLines(whileStmtNode.getStatements()),
                singletonList(gotoCommand(-1)),
                conditionLoadCommands)
        );
        addCommand(JvmBytecodeCommandFactory::ifEq, blockSize - getCommandsSize(conditionLoadCommands));
        whileStmtNode.getStatements().forEach(node -> generateCodeInNestedScope(node, blockSize + currentIndex));
        return addCommand(JvmBytecodeCommandFactory::gotoCommand, -1 * blockSize);
    }

    public String getClassName() {
        return className;
    }
}

