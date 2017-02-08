package me.wbars.semantic;

import me.wbars.parser.models.Node;
import me.wbars.parser.models.Tokens;
import me.wbars.scanner.models.PartOfSpeech;
import me.wbars.semantic.models.*;
import me.wbars.semantic.models.types.TypeRegistry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static java.util.Collections.emptyList;

public class AST {
    private static ExprNode parseExpr(Node expr) {
        return expr.size() == 1
                ? new ExprNode(parseSimpleExpr(expr.head()))
                : new ExprNode(expr.child(1).getTerminal().getValue(), parseSimpleExpr(expr.head()), parseSimpleExpr(expr.child(2)));
    }

    private static ASTNode parseSimpleExpr(Node expr) {
        return expr.size() == 1
                ? parseTerm(expr.head())
                : new BinaryArithmeticOpNode(expr.child(1).getTerminal().getValue(), parseTerm(expr.head()), parseSimpleExpr(expr.child(1)));
    }

    private static ASTNode parseTerm(Node expr) {
        return expr.size() == 1
                ? parseFactor(expr.head())
                : new BinaryArithmeticOpNode(expr.child(1).getTerminal().getValue(), parseFactor(expr.head()), parseTerm(expr.child(1)));
    }

    private static boolean hasToken(Node node, String partOfSpeech) {
        return node.size() > 0 && isToken(node.head(), partOfSpeech);
    }

    private static boolean isToken(Node node, String partOfSpeech) {
        return node.getTerminal() != null && PartOfSpeech.getOrCreate(partOfSpeech).equals(node.getTerminal().getPos());
    }

    private static ASTNode parseFactor(Node expr) {
        if (expr.size() == 4) {
            return new ProcedureStmtNode(
                    parseLiteral(expr.head()),
                    collectRightRecursiveCall(expr.child(2), node -> parseArgument(node.head()))
            );
        }
        if (expr.size() == 1) {
            if (hasName(expr.head(), "varAccess")) return parseRightAssociativeOp(expr.head());
            return new LiteralNode(expr.head().getTerminal().getValue(), TypeRegistry.fromToken(expr.head().getTerminal()));
        }
        return parseSimpleExpr(expr.child(1));
    }

    private static boolean hasName(Node node, String... names) {
        return Arrays.stream(names).anyMatch(name -> node.getName().equals(name));
    }

    private static ASTNode parseRightAssociativeOp(Node node) {
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

    private static LiteralNode parseLiteral(Node node) {
        return node.isEmpty() ? new LiteralNode(node.getTerminal().getValue(), TypeRegistry.fromToken(node.getTerminal())) : parseLiteral(node.head());
    }

    private static ASTNode parseVarAccess(Node varAccess) {
        if (varAccess.size() < 2) return parseLiteral(varAccess);
        if (hasToken(varAccess, Tokens.OPEN_BRACKET)) {
            GetIndexNode byIndex = new GetIndexNode();
            collectRightRecursiveCall(varAccess.firstNonToken(Tokens.OPEN_BRACKET), node -> parseExpr(node.head()))
                    .forEach(byIndex::addIndex);
            byIndex.setTarget(parseVarAccess(varAccess.last()));
            return byIndex;
        }
        if (hasToken(varAccess, Tokens.DOT)) {
            GetFieldNode byField = new GetFieldNode(parseVarAccess(varAccess.firstNonToken(Tokens.DOT)).getValue());
            byField.setTarget(parseVarAccess(varAccess.last()));
            return byField;
        }
        if (hasToken(varAccess, Tokens.UPARROW)) {
            GetPointerNode byPointer = new GetPointerNode();
            byPointer.setTarget(parseVarAccess(varAccess.last()));
            return byPointer;
        }
        throw new RuntimeException();
    }

    public static ProgramNode parseProgram(Node parse) {
        Node heading = parse.head();
        String name = parseVarAccess(heading.child(1)).getValue();
        ProgramNode programNode = new ProgramNode(name, parseBlock(parse.child(2)));
        if (heading.size() > 2) {
            collectRightRecursiveCall(heading.child(3), node -> parseLiteral(node.head()))
                    .forEach(programNode::addIdentifier);
        }
        return programNode;
    }

    private static <T> List<T> collectRightRecursiveCall(Node node, Function<Node, T> parseItem) {
        List<T> result = new ArrayList<>();
        while (true) {
            result.add(parseItem.apply(node));
            if (node.size() <= 1) break;
            node = node.child(1);
        }
        return result;
    }

    private static BlockNode parseBlock(Node block) {
        BlockNode blockNode = new BlockNode();

        addPart(block, "labelDeclaration", blockNode.getLabels(), node -> new LiteralNode(parseVarAccess(node.head()).getValue(), TypeRegistry.STRING));
        addPart(block, "constDeclaration", blockNode.getConstDefinitions(), node -> parseConstDefinition(node.head()));
        addPart(block, "typeDeclaration", blockNode.getTypeDefinitions(), node -> parseTypeDefinition(parseVarAccess(node.head().head()).getValue(), node.head().child(2)));
        addPart(block, "varDeclarationPart", blockNode.getVarDeclarations(), node -> parseVarDeclaration(node.head()));
        addPart(block, "funcOrProcDeclaration", blockNode.getProcOrFunctionDeclarations(), node -> parseProcOrFunc(node.head()));
        if (block.size() > 0) blockNode.getStatements().addAll(parseStmtSeq(block.last()));

        return blockNode;
    }

    private static List<ASTNode> parseStmtSeq(Node block) {
        return collectRightRecursiveCall(block.firstNonToken(Tokens.BEGIN), node -> parseStatement(node.head()));
    }

    private static ASTNode parseStatement(Node node) {
        Node head = node.head();
        if (hasName(head, "assignmentStmt")) {
            return new AssignmentStatementNode(
                    parseRightAssociativeOp(head.head()),
                    parseExpr(head.child(2))
            );
        }
        if (hasName(head, "procedureStmt")) {
            return new ProcedureStmtNode(
                    parseLiteral(head.head()),
                    head.size() > 1 ? collectRightRecursiveCall(
                            head.child(2), n1 -> parseArgument(n1.head())) : emptyList()
            );
        }

        if (hasName(head, "whileStmt")) {
            return new WhileStmtNode(
                    parseExpr(head.child(1)),
                    parseStatement(head.child(3))
            );
        }

        if (hasName(head, "forStmt")) {
            return new ForStmtNode(
                    parseLiteral(head.child(1)),
                    parseExpr(head.child(3)),
                    parseExpr(head.child(5)),
                    parseLiteral(head.child(4)).getValue().equals("to"),
                    parseBlock(head)
            );
        }
        throw new RuntimeException();
    }

    private static ActualParameterNode parseArgument(Node head) {
        ExprNode first = parseExpr(head.head());
        ExprNode second = null;
        ExprNode third = null;
        if (head.size() > 2) second = parseExpr(head.child(2));
        if (head.size() > 3) third = parseExpr(head.child(4));
        return new ActualParameterNode(first, second, third);
    }

    private static ProcOrFunctionDeclarationNode parseProcOrFunc(Node node) {
        String blockName = hasName(node, "procedureDeclaration") ? "procedureHeading" : "functionHeading";
        return new ProcOrFunctionDeclarationNode(
                parseHeading(node.firstChildWithName(blockName).orElseThrow(RuntimeException::new)),
                node.firstChildWithName("block").map(AST::parseBlock).orElse(null)
        );
    }

    private static FuncOrProcHeadingNode parseHeading(Node heading) {
        String name = parseLiteral(heading.child(1)).getValue();
        List<ASTNode> parameters = heading.size() > 2 ? collectRightRecursiveCall(heading.child(2).child(1), node -> parseParameter(node.head())) : emptyList();
        LiteralNode type = heading.size() > 2 && isToken(heading.last(), Tokens.IDENTIFIER) ? parseLiteral(heading.last()) : null;
        return new FuncOrProcHeadingNode(name, type, parameters);
    }

    private static ASTNode parseParameter(Node parameter) {
        return hasName(parameter, "procedureHeading") || hasName(parameter, "functionHeading")
                ? parseHeading(parameter)
                : new LiteralParameterNode(
                collectRightRecursiveCall(parameter.firstNonToken(Tokens.VAR).head(), node -> parseLiteral(node.head())),
                parseLiteral(parameter.head().last())
        );
    }

    private static <T> void addPart(Node block, String blockName, List<T> items, Function<Node, T> itemSupplier) {
        block.firstChildWithName(blockName).ifPresent(node -> {
            items.addAll(collectRightRecursiveCall(node.firstNullToken(), itemSupplier));
        });
    }

    private static VarDeclarationNode parseVarDeclaration(Node varDeclaration) {
        return new VarDeclarationNode(
                collectRightRecursiveCall(varDeclaration.head(), node -> parseLiteral(node.head())),
                parseTypeDefinition(varDeclaration.child(2))
        );
    }

    private static ASTNode parseTypeDefinition(Node type) {
        return parseTypeDefinition("", type);
    }

    private static ASTNode parseTypeDefinition(String name, Node type) {
        if (type.size() == 0) return new TypeAliasNode(name, parseVarAccess(type));
        boolean packed = hasToken(type, Tokens.PACKED);
        final Node head = packed ? type.child(1) : type.head();
        if (hasName(head, "newPointerType")) {
            return new PointerTypeNode(name, parseLiteral(head.child(1)));
        }
        if (hasName(head, "ordinalType")) {
            if (hasToken(head, Tokens.OPEN_PAREN)) {
                return new EnumTypeNode(name, collectRightRecursiveCall(head.child(1), node -> parseLiteral(node.head())));
            }
            return parseSubrangeType(name, head);
        }
        if (hasName(head, "arrayType")) {
            return new ArrayTypeNode(name, collectRightRecursiveCall(head.child(2), node -> parseSubrangeType(node.head())), parseTypeDefinition(head.last()), packed);
        }
        //todo add record type
        if (hasToken(type, Tokens.SET)) {
            return new SetTypeNode(name, parseTypeDefinition(type.child(2)), packed);
        }
        if (hasToken(type, Tokens.PFILE)) {
            return new FileTypeNode(name, parseTypeDefinition(type.child(2)), packed);
        }
        throw new RuntimeException();
    }

    private static SubrangeTypeNode parseSubrangeType(String name, Node node) {
        return new SubrangeTypeNode(name, parseLiteral(node.head()), parseLiteral(node.child(3)));
    }

    private static SubrangeTypeNode parseSubrangeType(Node node) {
        return parseSubrangeType("", node);
    }

    private static ConstDefinitionNode parseConstDefinition(Node node) {
        return new ConstDefinitionNode(
                parseVarAccess(node.head()).getValue(),
                parseSimpleExpr(node.child(2))
        );
    }
}
