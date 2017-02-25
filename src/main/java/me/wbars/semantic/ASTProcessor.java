package me.wbars.semantic;

import me.wbars.parser.models.Node;
import me.wbars.parser.models.Tokens;
import me.wbars.scanner.models.PartOfSpeech;
import me.wbars.semantic.models.*;
import me.wbars.semantic.models.types.Type;
import me.wbars.semantic.models.types.TypeRegistry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static me.wbars.utils.CollectionsUtils.last;

public class ASTProcessor {
    private Stack<ASTNode> parents = new Stack<>();

    private ExprNode parseExpr(Node expr) {
        return createInNestedScope(new ExprNode(), n -> {
            if (expr.size() == 1) {
                ASTNode simpleExpr = createWithParent(parseSimpleExpr(expr.head()));
                n.setLeft(simpleExpr);
                n.setValue(simpleExpr.getValue());
            } else {
                n.setValue(expr.child(1).getTerminal().getValue());
                n.setLeft(createWithParent(parseSimpleExpr(expr.head())));
                n.setRight(createWithParent(parseSimpleExpr(expr.child(2))));
            }
        });
    }

    private ASTNode parseSimpleExpr(Node expr) {
        return expr.size() == 1
                ? createWithParent(parseTerm(expr.head()))
                : createInNestedScope(new BinaryArithmeticOpNode(expr.child(1).getTerminal().getValue()), n -> {
            n.setLeft(createWithParent(parseTerm(expr.head())));
            n.setRight(createWithParent(parseSimpleExpr(expr.child(1))));
        });
    }

    private ASTNode parseTerm(Node expr) {
        return expr.size() == 1
                ? createWithParent(parseFactor(expr.head()))
                : createInNestedScope(new BinaryArithmeticOpNode(expr.child(1).getTerminal().getValue()), n -> {
            n.setLeft(createWithParent(parseFactor(expr.head())));
            n.setRight(createWithParent(parseTerm(expr.child(1))));
        });
    }

    private static boolean hasToken(Node node, String partOfSpeech) {
        return node.size() > 0 && isToken(node.head(), partOfSpeech);
    }

    private static boolean isToken(Node node, String partOfSpeech) {
        return node.getTerminal() != null && PartOfSpeech.getOrCreate(partOfSpeech).equals(node.getTerminal().getPos());
    }

    private ASTNode parseFactor(Node expr) {
        if (hasName(expr, "arrayLiteral")) return createArrayLiteralNode(expr);
        if (expr.size() == 4) {
            return createInNestedScope(new ProcedureStmtNode(), n -> {
                LiteralNode identifier = createWithParent(parseLiteral(expr.head()));
                n.setIdentifier(identifier);
                n.setValue(identifier.getValue());
                n.setArguments(createWithParent(collectRightRecursiveCall(expr.child(2), this::parseArgument)));
            });
        }
        if (expr.size() == 1) {
            if (hasName(expr.head(), "varAccess")) return parseRightAssociativeOp(expr.head());
            return createLiteralNode(expr.head());
        }
        return createWithParent(parseExpr(expr.child(1)));
    }

    private static boolean hasName(Node node, String... names) {
        return Arrays.stream(names).anyMatch(name -> node.getName().equals(name));
    }

    private ASTNode parseRightAssociativeOp(Node node) {
        return parseVarAccess(reverse(node));
    }

    private static Node reverse(Node node) {
        if (!hasName(node.last(), "byField", "byIndex", "byPointer")) return node;

        Node last = node.last();
        node.removeLastChild();
        Node reverse = reverse(last);
        last.addChildren(node);
        return reverse;
    }

    private LiteralNode parseLiteral(Node node) {
        return node.isEmpty() ? createLiteralNode(node) : parseLiteral(node.head());
    }

    private LiteralNode createLiteralNode(Node node) {
        String name = node.getTerminal().getValue();
        Type type = TypeRegistry.fromToken(node.getTerminal());
        return isToken(node, Tokens.IDENTIFIER) ? createWithParent(new IdentifierNode(name, type)) : createWithParent(new LiteralNode(name, type));
    }


    private ArrayLiteralNode createArrayLiteralNode(Node node) {
        return createInNestedScope(new ArrayLiteralNode(), n -> {
            n.setItems(createWithParent(collectRightRecursiveCall(node.firstNonToken(Tokens.OPEN_CURLY), this::parseExpr)));
        });
    }

    private ASTNode parseVarAccess(Node varAccess) {
        if (varAccess.size() < 2) return createWithParent(parseLiteral(varAccess));
        if (hasToken(varAccess, Tokens.OPEN_BRACKET)) {
            return createInNestedScope(new GetIndexNode(), n -> {
                createWithParent(collectRightRecursiveCall(varAccess.firstNonToken(Tokens.OPEN_BRACKET), this::parseExpr))
                        .forEach(n::addIndex);
                n.setTarget(createWithParent(parseVarAccess(varAccess.last())));
            });
        }
        if (hasToken(varAccess, Tokens.DOT)) {
            return createInNestedScope(new GetFieldNode(parseVarAccess(varAccess.firstNonToken(Tokens.DOT)).getValue()), n -> {
                n.setTarget(createWithParent(parseVarAccess(varAccess.last())));
            });
        }
        if (hasToken(varAccess, Tokens.UPARROW)) {
            return createInNestedScope(new GetPointerNode(), n -> {
                n.setTarget(createWithParent(parseVarAccess(varAccess.last())));
            });
        }
        throw new RuntimeException();
    }

    public ProgramNode parseProgram(Node parse) {
        Node heading = parse.head();
        return createInNestedScope(new ProgramNode(), n -> {
            n.setValue(parseVarAccess(heading.child(1)).getValue());
            n.setBlock(createWithParent(parseBlock(parse.child(2))));
            if (heading.size() > 2) {
                createWithParent(collectRightRecursiveCall(heading.child(3), this::parseLiteral))
                        .forEach(n::addIdentifier);
            }
        });
    }

    private static <T> List<T> collectRightRecursiveCall(Node node, Function<Node, T> parseItem) {
        List<T> result = new ArrayList<>();
        while (true) {
            if (node.size() < 1) break;
            result.add(parseItem.apply(node.head()));
            if (node.size() == 1) break;
            node = node.child(1);
        }
        return result;
    }

    private <T extends ASTNode> T createWithParent(T node) {
        if (node != null) node.setParent(parents.peek());
        return node;
    }

    private <T extends ASTNode> List<T> createWithParent(List<T> nodes) {
        return nodes.stream()
                .map(this::createWithParent)
                .collect(Collectors.toList());
    }

    private BlockNode parseBlock(Node block) {
        BlockNode blockNode = new BlockNode();
        parents.push(blockNode);

        addPart(block, "labelDeclaration", blockNode.getLabels(), node -> createWithParent(new LiteralNode(parseVarAccess(node).getValue(), TypeRegistry.STRING)));
        addPart(block, "constDeclaration", blockNode.getConstDefinitions(), this::parseConstDefinition);
        addPart(block, "typeDeclaration", blockNode.getTypeDefinitions(), node -> parseTypeDefinition(parseVarAccess(node.head()).getValue(), node.child(2)));
        addPart(block, "varDeclarationPart", blockNode.getVarDeclarations(), this::parseVarDeclaration);
        addPart(block, "funcOrProcDeclaration", blockNode.getProcOrFunctionDeclarations(), this::parseProcOrFunc);
        if (block.size() > 0) blockNode.getStatements().addAll(parseStmtSeq(block.last()));

        if (!(last(blockNode.getStatements()) instanceof ReturnStmtNode)) {
            blockNode.getStatements().add(new ReturnStmtNode(null));
        }

        parents.pop();
        return blockNode;
    }

    private List<ASTNode> parseStmtSeq(Node block) {
        return collectStatements(block.firstNonToken(Tokens.BEGIN));
    }

    private <T extends ASTNode> T createInNestedScope(T node, Consumer<T> postProcessing) {
        parents.push(node);
        postProcessing.accept(node);
        parents.pop();
        return node;
    }

    private ASTNode parseStatement(Node node) {
        Node head = node.head();
        if (hasName(head, "assignmentStmt")) {
            return createInNestedScope(new AssignmentStatementNode(), n -> {
                n.setLeft(createWithParent(parseRightAssociativeOp(head.head())));
                n.setRight(createWithParent(parseExpr(head.child(2))));
            });
        }
        if (hasName(head, "procedureStmt")) {
            return createInNestedScope(new ProcedureStmtNode(), n -> {
                LiteralNode identifier = createWithParent(parseLiteral(head.head()));
                n.setIdentifier(identifier);
                n.setValue(identifier.getValue());
                if (head.size() <= 1 || head.child(2).isEmpty()) {
                    n.setArguments(emptyList());
                } else {
                    n.setArguments(createWithParent(collectRightRecursiveCall(head.child(2), this::parseArgument)));
                }
            });
        }

        if (hasName(head, "whileStmt")) {
            return createInNestedScope(new WhileStmtNode(), n -> {
                n.setCondition(createWithParent(parseExpr(head.child(1))));
                n.setStatements(createWithParent(collectStatements(head.child(3).firstNonToken(Tokens.BEGIN))));
            });
        }

        if (hasName(head, "returnStmt")) {
            return createInNestedScope(new ReturnStmtNode(), n -> {
                if (head.size() > 1) n.setExpr(createWithParent(parseExpr(head.child(1))));
            });
        }

        if (hasName(head, "forStmt")) {
            return createInNestedScope(new ForStmtNode(parseLiteral(head.child(4)).getValue().equals("to")), n -> {
                n.setControlVar(createWithParent(parseLiteral(head.child(1))));
                n.setInitialValue(createWithParent(parseExpr(head.child(3))));
                n.setFinalValue(createWithParent(parseExpr(head.child(5))));
                n.setStatements(createWithParent(collectStatements(head.last().firstNonToken(Tokens.BEGIN))));
            });
        }
        if (hasName(head, "repeatStmt")) {
            return createInNestedScope(new RepeatStmtNode(), n -> {
                n.setStatements(createWithParent(collectStatements(head.child(1))));
                n.setUntilExpression(createWithParent(parseExpr(head.last())));
            });
        }
        if (hasName(head, "ifStmt")) {
            return createInNestedScope(new IfStmtNode(), n -> {
                n.setCondition(createWithParent(parseExpr(head.child(1))));
                n.setTrueBranch(createWithParent(parseStmtSeq(head.child(3))));
                n.setFalseBranch(createWithParent(head.size() > 4 ? parseStmtSeq(head.last()) : emptyList()));
            });
        }
        throw new RuntimeException();
    }

    private List<ASTNode> collectStatements(Node node) {
        return collectRightRecursiveCall(node, this::parseStatement);
    }

    private ActualParameterNode parseArgument(Node head) {
        return createInNestedScope(new ActualParameterNode(), n -> {
            ExprNode first = createWithParent(parseExpr(head.head()));
            n.setFirst(first);
            n.setValue(first.getValue());
            if (head.size() > 2) n.setSecond(createWithParent(parseExpr(head.child(2))));
            if (head.size() > 3) n.setThird(createWithParent(parseExpr(head.child(4))));
        });
    }

    private ProcOrFunctionDeclarationNode parseProcOrFunc(Node node) {
        String blockName = hasName(node, "procedureDeclaration") ? "procedureHeading" : "functionHeading";
        return createInNestedScope(new ProcOrFunctionDeclarationNode(), n -> {
            n.setHeading(createWithParent(parseHeading(node.firstChildWithName(blockName).orElseThrow(RuntimeException::new))));
            n.setBody(createWithParent(node.firstChildWithName("block").map(this::parseBlock).orElse(null)));
        });
    }

    private FuncOrProcHeadingNode parseHeading(Node heading) {
        return createInNestedScope(new FuncOrProcHeadingNode(parseLiteral(heading.child(1)).getValue()), n -> {
            n.setResultType(createWithParent(heading.size() > 2 && isToken(heading.last(), Tokens.IDENTIFIER) ? parseLiteral(heading.last()) : null));
            n.setParameters(createWithParent(heading.size() > 2 ? collectRightRecursiveCall(heading.child(2).child(1), this::parseParameter) : emptyList()));
        });

    }

    private LiteralParameterNode parseParameter(Node parameter) {
        return createInNestedScope(new LiteralParameterNode(), n -> {
            n.setIdentifiers(createWithParent(collectRightRecursiveCall(parameter.firstNonToken(Tokens.VAR).head(), this::parseLiteral)));
            n.setNameIdentifier(createWithParent(parseLiteral(parameter.head().last())));
        });
    }

    private static <T> void addPart(Node block, String blockName, List<T> items, Function<Node, T> itemSupplier) {
        block.firstChildWithName(blockName).ifPresent(node -> items.addAll(collectRightRecursiveCall(node.firstNullToken(), itemSupplier)));
    }

    private VarDeclarationNode parseVarDeclaration(Node varDeclaration) {
        return createInNestedScope(new VarDeclarationNode(), n -> {
            n.setIdentifiers(createWithParent(collectRightRecursiveCall(varDeclaration.head(), this::parseLiteral)));
            n.setTypeDenoter(createWithParent(parseTypeDefinition(varDeclaration.child(2))));
        });
    }

    private ASTNode parseTypeDefinition(Node type) {
        return parseTypeDefinition("", type);
    }

    private ASTNode parseTypeDefinition(String name, Node type) {
        if (type.size() == 0) return createInNestedScope(new TypeAliasNode(name), n -> {
            n.setBaseType(createWithParent(parseVarAccess(type)));
        });

        boolean packed = hasToken(type, Tokens.PACKED);
        final Node head = packed ? type.child(1) : type.head();

        if (hasName(head, "newPointerType")) {
            return createInNestedScope(new PointerTypeNode(name), n -> {
                n.setDomainType(createWithParent(parseLiteral(head.child(1))));
            });
        }
        if (hasName(head, "ordinalType")) {
            if (hasToken(head, Tokens.OPEN_PAREN)) {
                return createInNestedScope(new EnumTypeNode(name), n -> {
                    n.setIdentifiers(createWithParent(collectRightRecursiveCall(head.child(1), this::parseLiteral)));
                });
            }
            return createWithParent(parseSubrangeType(name, head));
        }
        if (hasName(head, "arrayType")) {
            return createInNestedScope(new ArrayTypeNode(name, packed), n -> {
                n.setIndexes(createWithParent(head.size() > 3 ? collectRightRecursiveCall(head.child(2), this::parseSubrangeType) : emptyList()));
                n.setComponentType(createWithParent(parseTypeDefinition(head.last())));
            });
        }
        //todo add record type
        if (hasToken(type, Tokens.SET)) {
            return createInNestedScope(new SetTypeNode(name, packed), n -> {
                n.setBaseType(createWithParent(new SetTypeNode(name, parseTypeDefinition(type.child(2)), packed)));
            });
        }
        if (hasToken(type, Tokens.PFILE)) {
            return createInNestedScope(new FileTypeNode(name, packed), n -> {
                n.setTypeDenoter(createWithParent(parseTypeDefinition(type.child(2))));
            });
        }
        throw new RuntimeException();
    }

    private SubrangeTypeNode parseSubrangeType(String name, Node node) {
        return createInNestedScope(new SubrangeTypeNode(name), n -> {
            n.setLeftBound(createWithParent(parseLiteral(node.head())));
            n.setRightBound(createWithParent(parseLiteral(node.child(3))));
        });
    }

    private SubrangeTypeNode parseSubrangeType(Node node) {
        return parseSubrangeType("", node);
    }

    private ConstDefinitionNode parseConstDefinition(Node node) {
        return createInNestedScope(new ConstDefinitionNode(parseVarAccess(node.head()).getValue()), n -> {
            n.setExpr(createWithParent(parseSimpleExpr(node.child(2))));
        });
    }
}
