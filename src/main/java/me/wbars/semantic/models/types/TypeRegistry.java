package me.wbars.semantic.models.types;

import me.wbars.parser.models.Tokens;
import me.wbars.scanner.models.Token;
import me.wbars.semantic.models.*;
import me.wbars.utils.Registry;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Optional.ofNullable;

public class TypeRegistry extends Registry<Type> {
    private final Map<String, ASTNode> constants = new HashMap<>();
    private final Map<String, ASTNode> functions = new HashMap<>();
    private final Map<Type, Set<Type>> typeCasts = new HashMap<>();

    private SymbolTable table = new SymbolTable(null);

    public static final IntegerType INTEGER = new IntegerType();

    public static final LongType LONG = new LongType();
    public static final StringType STRING = new StringType();
    public static final BooleanType BOOLEAN = new BooleanType();
    public static final CharType CHAR = new CharType();
    public static final DoubleType DOUBLE = new DoubleType();
    public static final VoidType VOID = new VoidType();
    public static final Utf8Type UTF8 = new Utf8Type();

    private static ArrayType createArrayType(Type type, Integer lowerBound, Integer upperBound) {
        return new ArrayType(type, lowerBound, upperBound);
    }

    private static SetType createSetType(Type type) {
        return new SetType(type);
    }

    private static SubrangeType createSubrangeType(String left, String right) {
        return new SubrangeType(left, right);
    }

    private static PointerType createPointerType(Type type) {
        return new PointerType(type);
    }

    private static EnumType createEnumType(List<String> variants) {
        return new EnumType(variants);
    }

    public TypeRegistry() {
        registerBaseTypes();
        registerTypeCasts();
    }

    private void registerTypeCasts() {
        typeCasts.put(STRING, singleton(CHAR));
        typeCasts.put(LONG, new HashSet<>(Collections.singletonList(INTEGER)));
        typeCasts.put(DOUBLE, new HashSet<>(asList(INTEGER, LONG)));
    }

    private void registerBaseTypes() {
        table.register("Real", DOUBLE);
        table.register("Integer", INTEGER);
        table.register("Long", LONG);
        table.register("Char", CHAR);
        table.register("String", STRING);
        table.register("Boolean", BOOLEAN);

        table.register("len", INTEGER);

    }

    public static Type fromToken(Token token) {
        switch (token.getPos().name) {
            case Tokens.SIGNED_INTEGER:
            case Tokens.UNSIGNED_INTEGER:
                long l = Math.abs(Long.parseLong(token.getValue()));
                if (l < Integer.MAX_VALUE) return INTEGER;
                return LONG;
            case Tokens.REALNUMBER:
                return DOUBLE;
            case Tokens.STRING_VAR:
                return STRING;
            case Tokens.BOOLEAN:
                return BOOLEAN;
            default:
                return null;
        }
    }

    public Type processType(ProgramNode program) {
        getProcessedType(program.getBlock());
        return VOID;
    }

    public Type processType(BlockNode block) {
        block.getConstDefinitions().forEach(constDefinitionNode -> {
            getProcessedType(constDefinitionNode);
            constants.put(constDefinitionNode.getValue(), constDefinitionNode.getExpr());
        });
        block.getTypeDefinitions().forEach(t -> {
            getProcessedType(t);
            register(t.getValue(), t.getType());
        });
        block.getVarDeclarations().forEach(this::getProcessedType);
        block.getProcOrFunctionDeclarations().forEach(this::getProcessedType);
        block.getStatements().forEach(this::getProcessedType);
        return VOID;
    }

    public Type processType(ConstDefinitionNode constDefinitionNode) {
        table.register(constDefinitionNode.getValue(), getProcessedType(constDefinitionNode.getExpr()));
        return VOID;
    }

    public Type processType(BinaryOpNode binaryOpNode) {
        Type leftType = getProcessedType(binaryOpNode.getLeft());
        if (binaryOpNode.getRight() == null) return leftType;

        Type rightType = getProcessedType(binaryOpNode.getRight());

        if (leftType == null)
            throw new RuntimeException("Cant find declaration of " + binaryOpNode.getLeft().getValue());
        if (rightType == null)
            throw new RuntimeException("Cant find declaration of " + binaryOpNode.getRight().getValue());

        return isRelationOperator(binaryOpNode.getValue()) ? BOOLEAN : typeCast(leftType, rightType);
    }

    private boolean isRelationOperator(String value) {
        return value.equals("=")
                || value.equals("!=")
                || value.equals(">")
                || value.equals("<")
                || value.equals(">=")
                || value.equals("<=")
                ;
    }

    private Type typeCast(Type first, Type second) {
        if (first.equals(second)) return first;
        if (typeCasts.getOrDefault(first, emptySet()).contains(second)) return first;
        if (typeCasts.getOrDefault(second, emptySet()).contains(first)) return second;
        throw new RuntimeException("Can't cast " + first.name() + " to " + second.name());
    }

    public Type processType(LiteralNode literal) {
        return lookup(literal.getValue());
    }

    public Type lookup(String identifier) {
        Type type = table.lookup(identifier);
        return type != null ? type : super.lookup(identifier);
    }

    public Type processType(ProcedureStmtNode procedureStmtNode) {
        procedureStmtNode.getArguments().forEach(this::getProcessedType);

        String procedureName = procedureStmtNode.getIdentifier().getValue();
        Type type = lookup(procedureName);
        if (type != null) return type;

        return ofNullable(functions.get(procedureName))
                .map(ASTNode::getType).orElse(null);
    }

    public Type processType(VarDeclarationNode varDeclarationNode) {
        ASTNode typeDenoter = varDeclarationNode.getTypeDenoter();
        varDeclarationNode.getIdentifiers()
                .forEach(literal -> table.register(literal.getValue(), getProcessedType(typeDenoter)));
        return VOID;
    }

    private Type getProcessedType(ASTNode node) {
        return node.getProcessedType(this);
    }

    public Type processType(PointerTypeNode pointerTypeNode) {
        return createPointerType(getProcessedType(pointerTypeNode.getDomainType()));
    }

    public Type processType(EnumTypeNode enumTypeNode) {
        return createEnumType(enumTypeNode.getIdentifiers().stream()
                .map(LiteralNode::getValue)
                .collect(Collectors.toList())
        );
    }

    public Type processType(SubrangeTypeNode subrangeTypeNode) {
        return createSubrangeType(
                subrangeTypeNode.getLeftBound().getValue(),
                subrangeTypeNode.getRightBound().getValue()
        );
    }

    public Type processType(ArrayTypeNode arrayTypeNode) {
        List<SubrangeTypeNode> indexes = arrayTypeNode.getIndexes();
        if (indexes.size() > 1) {
            //todo rid-off from side effect
            SubrangeTypeNode lastIndexes = indexes.remove(indexes.size() - 1);
            return createArrayType(
                    processType(arrayTypeNode),
                    Integer.parseInt(lastIndexes.getLeftBound().getValue()),
                    Integer.parseInt(lastIndexes.getRightBound().getValue())
            );
        }

        return createArrayType(
                getProcessedType(arrayTypeNode.getComponentType()),
                Integer.parseInt(indexes.get(0).getLeftBound().getValue()),
                Integer.parseInt(indexes.get(0).getRightBound().getValue())
        );
    }

    public Type processType(TypeAliasNode typeAliasNode) {
        return getProcessedType(typeAliasNode.getBaseType());
    }

    public Type processType(SetTypeNode setTypeNode) {
        return createSetType(getProcessedType(setTypeNode.getBaseType()));
    }

    public Type processType(ProcOrFunctionDeclarationNode declaration) {
        LiteralNode resultType = declaration.getHeading().getResultType();
        if (resultType != null) getProcessedType(resultType);

        processProcOrFuncBodyTypes(declaration, resultType);

        functions.put(declaration.getHeading().getValue(), declaration);
        return resultType != null ? getProcessedType(resultType) : VOID;
    }

    private void processProcOrFuncBodyTypes(ProcOrFunctionDeclarationNode declaration, LiteralNode resultType) {
        dispatchInNestedScope(() -> {
            if (resultType != null) table.register(declaration.getHeading().getValue(), getProcessedType(resultType));
            declaration.getHeading().getParameters().forEach(this::getProcessedType);
            getProcessedType(declaration.getBody());
            return null;
        });
    }

    private <T> T dispatchInNestedScope(Supplier<T> s) {
        SymbolTable saved = captureTableAndCreateNested();
        T result = s.get();
        table = saved;
        return result;
    }

    public Type processType(ForStmtNode forStmtNode) {
        return dispatchInNestedScope(() -> {
            final LiteralNode controlVar = forStmtNode.getControlVar();
            final ExprNode initialValue = forStmtNode.getInitialValue();
            table.register(controlVar.getValue(), getProcessedType(initialValue));
            getProcessedType(controlVar);

            getProcessedType(forStmtNode.getBody());

            return VOID;
        });
    }

    private SymbolTable captureTableAndCreateNested() {
        SymbolTable saved = table;
        table = new SymbolTable(table);
        return saved;
    }

    public Type processType(GetIndexNode getIndexNode) {
        Type type = getProcessedType(getIndexNode.getTarget());
        getIndexNode.getIndexes().forEach(this::getProcessedType);
        for (int i = 0; i < getIndexNode.getIndexes().size(); i++) {
            // surely multidimensional array
            type = ((ArrayType) type).getType();
        }
        return type;
    }

    public Type processType(LiteralParameterNode parameterNode) {
        Type type = getProcessedType(parameterNode.getNameIdentifier());
        parameterNode.getIdentifiers().forEach(literalNode -> {
            table.register(literalNode.getValue(), type);
        });
        return type;
    }

    public Type processType(ActualParameterNode actualParameterNode) {
        return getProcessedType(actualParameterNode.getFirst());
    }

    public Type processType(ArrayLiteralNode arrayLiteralNode) {
        Set<Type> itemsTypes = arrayLiteralNode.getItems().stream()
                .map(exprNode -> exprNode.getProcessedType(this))
                .collect(Collectors.toSet());
        if (itemsTypes.size() > 1)
            throw new RuntimeException("Items of array " + arrayLiteralNode.getValue() + " has different types");
        return createArrayType(itemsTypes.iterator().next(), 0, arrayLiteralNode.getItems().size() - 1);
    }

    public Map<String, ASTNode> getConstants() {
        return constants;
    }

    public Map<String, ASTNode> getFunctions() {
        return functions;
    }

    public SymbolTable getTable() {
        return table;
    }

    public Type processType(IfStmtNode ifStmtNode) {
        getProcessedType(ifStmtNode.getCondition());
        ifStmtNode.getTrueBranch().forEach(this::getProcessedType);
        ifStmtNode.getFalseBranch().forEach(this::getProcessedType);
        return VOID;
    }

    public Type processType(RepeatStmtNode repeatStmtNode) {
        getProcessedType(repeatStmtNode.getUntilExpression());
        repeatStmtNode.getStatements().forEach(this::getProcessedType);
        return VOID;
    }
}
