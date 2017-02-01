package me.wbars.parser;

import me.wbars.parser.exceptions.ParserException;
import me.wbars.parser.models.Node;
import me.wbars.parser.models.Tokens;
import me.wbars.scanner.models.PartOfSpeech;
import me.wbars.scanner.models.Token;

import java.util.List;
import java.util.function.Supplier;

public class Parser {
    private static class MyIterator<T> {
        private final List<T> value;
        private int counter = 0;

        private MyIterator(List<T> value) {
            this.value = value;
        }

        private boolean notFinished() {
            return counter < value.size();
        }

        private T current() {
            return notFinished() ? value.get(counter) : null;
        }

        private void advance() {
            counter++;
        }

        // can switch to LL(1)?
        private T lookahead() {
            return counter + 1 < value.size() ? value.get(counter + 1) : null;
        }

    }

    private final MyIterator<Token> tokens;

    private Parser(List<Token> tokens) {
        this.tokens = new MyIterator<>(tokens);
    }

    public static Node parse(List<Token> tokens) {
        return new Parser(tokens).program();
    }

    private Node derivate(Supplier<Node> derivation) {
        if (!tokens.notFinished()) return null;
        return derivation.get();
    }

    private Token requireTokenByType(String partOfSpeech) {
        Token current = tokens.current();
        if (!tokens.notFinished() || !isCurrentTokenHasPos(partOfSpeech)) throwParseException(partOfSpeech);
        tokens.advance();
        return current;
    }

    private Node tokenByTypeAndValue(String type, String value) {
        if (!isCurrentTokenHasValue(value)) return throwParseException(type + ": " + value);
        return tokenByType(type);
    }

    private Node throwParseException(String... expected) {
        Token current = tokens.current();
        throw new ParserException(current != null ? current.toString() : null, expected);
    }

    private Node tokenByType(String identifier) {
        Token nodeToken = requireTokenByType(identifier);
        return Node.terminal(nodeToken.getPos().name, nodeToken);
    }

    private boolean isCurrentTokenHasPos(String partOfSpeech) {
        return tokenHasPos(tokens.current(), partOfSpeech);
    }

    private boolean tokenHasPos(Token current, String partOfSpeech) {
        return current != null && current.getPos().equals(PartOfSpeech.getOrCreate(partOfSpeech));
    }

    private boolean isCurrentTokenHasValue(String value) {
        Token current = tokens.current();
        return current != null && current.getValue().equals(value);
    }

    private Supplier<Node> createRightRecursiveListSupplier(String name,
                                                            Supplier<Node> listItem,
                                                            String delimer) {
        return createRightRecursiveListSupplier(name, listItem, delimer, () -> false);
    }

    private Supplier<Node> createRightRecursiveListSupplier(String name,
                                                            Supplier<Node> listItem,
                                                            String delimer,
                                                            Supplier<Boolean> breakCondition) {
        Supplier<Node> recursiveCall = new Supplier<Node>() {
            @Override
            public Node get() {
                if (!isCurrentTokenHasPos(delimer) || breakCondition.get()) return null;

                Node node = tokenByType(delimer);
                node.addChildren(derivate(listItem));
                node.addChildren(derivate(this));
                return node;
            }
        };

        return () -> {
            Node node = Node.empty(name);
            node.addChildren(derivate(listItem));
            node.addChildren(derivate(recursiveCall));
            return node;
        };
    }

    private Supplier<Node> createListSupplier(String name, Supplier<Node> listItem, Supplier<Boolean> breakCondition) {
        return new Supplier<Node>() {
            @Override
            public Node get() {
                Node node = Node.empty(name);
                node.addChildren(derivate(listItem));
                if (!breakCondition.get()) {
                    node.addChildren(derivate(this));
                }
                return node;
            }
        };
    }

    private final Supplier<Node> indexList = createRightRecursiveListSupplier("indexList", this::ordinalType, Tokens.COMMA);
    private final Supplier<Node> term = createRightRecursiveListSupplier("term", this::factor, Tokens.SIGN,
            () -> !isCurrentTokenHasValue("*") && !isCurrentTokenHasValue("/"));
    private final Supplier<Node> simpleExpr = createRightRecursiveListSupplier("simpleExpr", term, Tokens.SIGN,
            () -> !isCurrentTokenHasValue("+") && !isCurrentTokenHasValue("-"));
    private final Supplier<Node> labelList = createRightRecursiveListSupplier("labelList", () -> tokenByType(Tokens.UNSIGNED_INTEGER), Tokens.COMMA);
    private final Supplier<Node> actualParamList = createRightRecursiveListSupplier("actualParamList", this::actualParam, Tokens.COMMA);
    private final Supplier<Node> formalParameterSectionList = createRightRecursiveListSupplier("formalParameterSectionList", this::formalParameterSection, Tokens.SEMICOLON);
    private final Supplier<Node> identifierList = createRightRecursiveListSupplier("identifierList", () -> tokenByType(Tokens.IDENTIFIER), Tokens.COMMA);
    private final Supplier<Node> variantList = createRightRecursiveListSupplier("variantList", this::variant, Tokens.SEMICOLON);
    private final Supplier<Node> caseConstantList = createRightRecursiveListSupplier("caseConstantList", this::caseConstant, Tokens.COMMA);
    private final Supplier<Node> recordSectionList = createListSupplier("recordSectionList", this::recordSection, () -> isCurrentTokenHasPos(Tokens.END));
    private final Supplier<Node> indexExprList = createRightRecursiveListSupplier("indexExprList", this::expr, Tokens.COMMA);

    private Node expr() {
        Node expr = Node.empty("expr");
        expr.addChildren(derivate(simpleExpr));
        if (!isCurrentTokenHasPos(Tokens.RELOP)) return expr;

        expr.addChildren(tokenByType(Tokens.RELOP));
        expr.addChildren(derivate(simpleExpr));
        return expr;
    }

    private Node factor() {
        Node factor = Node.empty("Factor");
        if (isCurrentTokenHasPos(Tokens.OPEN_PAREN)) {
            addParensDerivate(factor, this.simpleExpr);
            return factor;
        }
        if (isCurrentTokenHasPos(Tokens.UNSIGNED_INTEGER)) {
            factor.addChildren(tokenByType(Tokens.UNSIGNED_INTEGER));
            return factor;
        }

        if (isCurrentTokenHasPos(Tokens.REALNUMBER)) {
            factor.addChildren(tokenByType(Tokens.REALNUMBER));
            return factor;
        }
        if (isCurrentTokenHasPos(Tokens.IDENTIFIER)) {
            factor.addChildren(derivate(this::varAccess));
            if (isCurrentTokenHasPos(Tokens.OPEN_PAREN)) {
                factor.addChildren(tokenByType(Tokens.OPEN_PAREN));
                factor.addChildren(derivate(actualParamList));
                factor.addChildren(tokenByType(Tokens.CLOSE_PAREN));
            }
            return factor;
        }
        if (isCurrentTokenHasPos(Tokens.STRING_VAR)) {
            factor.addChildren(tokenByType(Tokens.STRING_VAR));
            return factor;
        }
        throwParseException(Tokens.OPEN_PAREN, Tokens.UNSIGNED_INTEGER, Tokens.IDENTIFIER, Tokens.STRING_VAR);
        return null;
    }

    private void addParensDerivate(Node node, Supplier<Node> supplier) {
        node.addChildren(tokenByType(Tokens.OPEN_PAREN));
        node.addChildren(derivate(supplier));
        node.addChildren(tokenByType(Tokens.CLOSE_PAREN));
    }

    private Node program() {
        Node program = Node.empty("program");
        program.addChildren(derivate(this::programHeading));
        program.addChildren(tokenByType(Tokens.SEMICOLON));
        program.addChildren(derivate(this::block));
        program.addChildren(tokenByType(Tokens.DOT));
        return program;
    }

    private Node block() {
        Node block = Node.empty("block");
        block.addChildren(derivate(this::labelDeclaration));
        block.addChildren(derivate(this::constDeclaration));
        block.addChildren(derivate(this::typeDeclaration));
        block.addChildren(derivate(this::varDeclarationPart));
        block.addChildren(derivate(this::funcOrProcDeclarationPart));
        block.addChildren(derivate(this::stmtPart));
        return block;
    }

    private Node stmtPart() {
        return derivate(this::compoundStatement);
    }

    private Node compoundStatement() {
        Node compoundStmt = Node.empty("compoundStmt");

        if (isCurrentTokenHasPos(Tokens.BEGIN)) {
            compoundStmt.addChildren(tokenByType(Tokens.BEGIN));
            compoundStmt.addChildren(derivate(this::stmtSeq));
            compoundStmt.addChildren(tokenByType(Tokens.END));
            return compoundStmt;
        }

        // wrap plays role of hack for AST processor
        // this node will be recognized as one-element right recursive seq
        compoundStmt.addChildren(wrap(derivate(this::stmt)));
        return compoundStmt;
    }

    private Node wrap(Node child) {
        Node wrapper = Node.empty("wrapper");
        wrapper.addChildren(child);
        return wrapper;
    }

    private Node stmtSeq() {
        Node stmtSeq = Node.empty("stmtSeq");
        stmtSeq.addChildren(derivate(this::stmt));
        stmtSeq.addChildren(derivate(this::stmtSeq1));
        return stmtSeq;
    }

    private Node stmt() {
        Node stmt = Node.empty("stmt");
        if (isCurrentTokenHasPos(Tokens.UNSIGNED_INTEGER)) {
            stmt.addChildren(tokenByType(Tokens.UNSIGNED_INTEGER));
            stmt.addChildren(tokenByType(Tokens.COLON));
        }
        if (isCurrentTokenHasPos(Tokens.REPEAT)) {
            stmt.addChildren(derivate(this::repeatStmt));
            return stmt;
        }
        if (isCurrentTokenHasPos(Tokens.WHILE)) {
            stmt.addChildren(derivate(this::whileStmt));
            return stmt;
        }
        if (isCurrentTokenHasPos(Tokens.FOR)) {
            stmt.addChildren(derivate(this::fortStmt));
            return stmt;
        }
        if (isCurrentTokenHasPos(Tokens.IF)) {
            stmt.addChildren(derivate(this::ifStmt));
            return stmt;
        }

        if (isCurrentTokenHasPos(Tokens.IDENTIFIER)) {
            if (tokenHasPos(tokens.lookahead(), Tokens.SEMICOLON) || tokenHasPos(tokens.lookahead(), Tokens.OPEN_PAREN)) {
                stmt.addChildren(derivate(this::procedureStmt));
            } else {
                stmt.addChildren(derivate(this::assignmentStmt));
            }
            return stmt;
        }

        throwParseException(Tokens.REPEAT, Tokens.WHILE, Tokens.FOR, Tokens.IF, Tokens.IDENTIFIER);
        return null;
    }

    private Node assignmentStmt() {
        Node stmt = Node.empty("assignmentStmt");
        stmt.addChildren(derivate(this::varAccess));
        stmt.addChildren(tokenByType(Tokens.ASSIGNMENT));
        stmt.addChildren(derivate(this::expr));
        return stmt;
    }

    private Node varAccess() {
        Node varAccess = Node.empty("varAccess");
        varAccess.addChildren(tokenByType(Tokens.IDENTIFIER));
        varAccess.addChildren(derivate(this::varAccess1));
        return varAccess;
    }

    private Node varAccess1() {
        if (isCurrentTokenHasPos(Tokens.OPEN_BRACKET)) {
            return varByIndex();
        } else if (isCurrentTokenHasPos(Tokens.DOT)) {
            return varByField();
        } else if (isCurrentTokenHasPos(Tokens.UPARROW)) {
            return varByPointer();
        }
        return null;
    }

    private Node varByPointer() {
        Node byPointer = Node.empty("byPointer");
        byPointer.addChildren(tokenByType(Tokens.UPARROW));
        byPointer.addChildren(derivate(this::varAccess1));
        return byPointer;
    }

    private Node varByField() {
        Node byField = Node.empty("byField");
        byField.addChildren(tokenByType(Tokens.DOT));
        byField.addChildren(tokenByType(Tokens.IDENTIFIER));
        byField.addChildren(derivate(this::varAccess1));
        return byField;
    }

    private Node varByIndex() {
        Node byIndex = Node.empty("byIndex");
        byIndex.addChildren(tokenByType(Tokens.OPEN_BRACKET));
        byIndex.addChildren(derivate(indexExprList));
        byIndex.addChildren(tokenByType(Tokens.CLOSE_BRACKET));
        byIndex.addChildren(derivate(this::varAccess1));
        return byIndex;
    }

    private Node procedureStmt() {
        Node stmt = Node.empty("procedureStmt");
        stmt.addChildren(tokenByType(Tokens.IDENTIFIER));
        if (!isCurrentTokenHasPos(Tokens.OPEN_PAREN)) return stmt;
        addParensDerivate(stmt, actualParamList);
        return stmt;
    }

    private Node actualParam() {
        Node actualParam = Node.empty("actualParam");
        actualParam.addChildren(derivate(this::expr));
        if (!isCurrentTokenHasPos(Tokens.COLON)) return actualParam;

        actualParam.addChildren(tokenByType(Tokens.COLON));
        actualParam.addChildren(derivate(this::expr));
        if (!isCurrentTokenHasPos(Tokens.COLON)) return actualParam;

        actualParam.addChildren(tokenByType(Tokens.COLON));
        actualParam.addChildren(derivate(this::expr));
        return actualParam;
    }

    private Node ifStmt() {
        Node stmt = Node.empty("ifStmt");
        stmt.addChildren(tokenByType(Tokens.IF));
        stmt.addChildren(derivate(this::booleanExpr));
        stmt.addChildren(tokenByType(Tokens.THEN));
        stmt.addChildren(derivate(this::stmt));
        if (!isCurrentTokenHasPos(Tokens.ELSE)) return stmt;

        stmt.addChildren(tokenByType(Tokens.ELSE));
        stmt.addChildren(derivate(this::stmt));
        return stmt;
    }

    private Node fortStmt() {
        Node stmt = Node.empty("forStmt");
        stmt.addChildren(tokenByType(Tokens.FOR));
        stmt.addChildren(tokenByType(Tokens.IDENTIFIER));
        stmt.addChildren(tokenByType(Tokens.ASSIGNMENT));
        stmt.addChildren(derivate(this::expr));
        stmt.addChildren(tokenByType(Tokens.DIRECTION));
        stmt.addChildren(derivate(this::expr));
        stmt.addChildren(tokenByType(Tokens.DO));
        stmt.addChildren(derivate(this::compoundStatement));
        return stmt;
    }

    private Node whileStmt() {
        Node stmt = Node.empty("whileStmt");
        stmt.addChildren(tokenByType(Tokens.WHILE));
        stmt.addChildren(derivate(this::booleanExpr));
        stmt.addChildren(tokenByType(Tokens.DO));
        stmt.addChildren(derivate(this::stmt));
        return stmt;
    }

    private Node repeatStmt() {
        Node stmt = Node.empty("repeatStmt");
        stmt.addChildren(tokenByType(Tokens.REPEAT));
        stmt.addChildren(derivate(this::stmtSeq));
        stmt.addChildren(tokenByType(Tokens.UNTIL));
        stmt.addChildren(derivate(this::booleanExpr));
        return stmt;
    }

    private Node booleanExpr() {
        return derivate(this::expr);
    }

    private Node stmtSeq1() {
        if (!isCurrentTokenHasPos(Tokens.SEMICOLON)) return null;
        Node stmtSeq1 = Node.terminal("stmtSeq1", requireTokenByType(Tokens.SEMICOLON));
        if (isCurrentTokenHasPos(Tokens.END)) return null;
        stmtSeq1.addChildren(derivate(this::stmt));
        stmtSeq1.addChildren(derivate(this::stmtSeq1));
        return stmtSeq1;
    }

    private Node funcOrProcDeclarationPart() {
        if (!isCurrentTokenHasPos(Tokens.PROCEDURE) && !isCurrentTokenHasPos(Tokens.FUNCTION)) return null;

        Node funcOrProcDeclaration = Node.empty("funcOrProcDeclaration");
        funcOrProcDeclaration.addChildren(derivate(this::funcOrProcDeclarationList));
        return funcOrProcDeclaration;
    }

    private Node funcOrProcDeclarationList() {
        Node funcDeclarationList = Node.empty("funcOrProcDeclarationList");
        funcDeclarationList.addChildren(derivate(this::funcOrProcDeclaration));
        funcDeclarationList.addChildren(derivate(this::funcOrProcDeclarationList1));
        return funcDeclarationList;
    }

    private Node funcOrProcDeclarationList1() {
        if (!isCurrentTokenHasPos(Tokens.PROCEDURE) && !isCurrentTokenHasPos(Tokens.FUNCTION)) return null;
        Node funcDeclarationList = Node.empty("funcOrProcDeclarationList");
        funcDeclarationList.addChildren(derivate(this::funcOrProcDeclaration));
        funcDeclarationList.addChildren(derivate(this::funcOrProcDeclarationList1));
        return funcDeclarationList;
    }

    private Node funcOrProcDeclaration() {
        if (isCurrentTokenHasPos(Tokens.PROCEDURE)) return derivate(this::procedureDeclaration);
        if (isCurrentTokenHasPos(Tokens.FUNCTION)) return derivate(this::functionDeclaration);
        return null;
    }

    private Node functionDeclaration() {
        Node functionDeclaration = Node.empty("functionDeclaration");
        if (isCurrentTokenHasPos(Tokens.SEMICOLON)) {
            functionDeclaration.addChildren(derivate(this::functionIdenntification));
            functionDeclaration.addChildren(tokenByType(Tokens.SEMICOLON));
            functionDeclaration.addChildren(derivate(this::block));
            return functionDeclaration;
        }
        functionDeclaration.addChildren(derivate(this::functionHeading));
        functionDeclaration.addChildren(tokenByType(Tokens.SEMICOLON));
        if (isCurrentTokenHasPos(Tokens.DIRECTIVE)) {
            functionDeclaration.addChildren(tokenByType(Tokens.DIRECTIVE));
        } else {
            functionDeclaration.addChildren(derivate(this::block));
            functionDeclaration.addChildren(tokenByType(Tokens.SEMICOLON));
        }
        return functionDeclaration;
    }

    private Node functionIdenntification() {
        Node functionIdentification = Node.empty("functionIdentification");
        functionIdentification.addChildren(tokenByType(Tokens.FUNCTION));
        functionIdentification.addChildren(tokenByType(Tokens.IDENTIFIER));
        return functionIdentification;
    }

    private Node procedureDeclaration() {
        Node procedureDeclaration = Node.empty("procedureDeclaration");
        procedureDeclaration.addChildren(derivate(this::procedureHeading));
        procedureDeclaration.addChildren(tokenByType(Tokens.SEMICOLON));

        if (isCurrentTokenHasPos(Tokens.DIRECTIVE)) {
            procedureDeclaration.addChildren(tokenByType(Tokens.DIRECTIVE));
        } else {
            procedureDeclaration.addChildren(derivate(this::block));
            procedureDeclaration.addChildren(tokenByType(Tokens.SEMICOLON));
        }
        return procedureDeclaration;
    }

    private Node procedureHeading() {
        Node procedureHeading = Node.empty("procedureHeading");
        procedureHeading.addChildren(tokenByType(Tokens.PROCEDURE));
        procedureHeading.addChildren(tokenByType(Tokens.IDENTIFIER));
        procedureHeading.addChildren(derivate(this::formalParameterList));
        return procedureHeading;

    }

    private Node formalParameterList() {
        if (!isCurrentTokenHasPos(Tokens.OPEN_PAREN)) return null;
        Node formalParameterList = Node.empty("formalParameterList");
        addParensDerivate(formalParameterList, formalParameterSectionList);
        return formalParameterList;
    }

    private Node formalParameterSection() {
        Node formalParameterSection = Node.empty("formalParameterSection");
        if (isCurrentTokenHasPos(Tokens.IDENTIFIER)) {
            formalParameterSection.addChildren(derivate(this::valueParameterSpec));
        } else if (isCurrentTokenHasPos(Tokens.VAR)) {
            formalParameterSection.addChildren(derivate(this::variableParameterSpec));
        } else if (isCurrentTokenHasPos(Tokens.PROCEDURE)) {
            formalParameterSection.addChildren(derivate(this::proceduralParameterSpec));
        } else if (isCurrentTokenHasPos(Tokens.FUNCTION)) {
            formalParameterSection.addChildren(derivate(this::functionalParameterSpec));
        } else {
            throwParseException(Tokens.IDENTIFIER, Tokens.VAR, Tokens.PROCEDURE, Tokens.FUNCTION);
        }
        return formalParameterSection;
    }

    private Node functionalParameterSpec() {
        return derivate(this::functionHeading);
    }

    private Node functionHeading() {
        Node functionHeading = Node.empty("functionHeading");
        functionHeading.addChildren(tokenByType(Tokens.FUNCTION));
        functionHeading.addChildren(tokenByType(Tokens.IDENTIFIER));
        functionHeading.addChildren(derivate(this::formalParameterList));
        functionHeading.addChildren(tokenByType(Tokens.COLON));
        functionHeading.addChildren(tokenByType(Tokens.IDENTIFIER));
        return functionHeading;
    }

    private Node proceduralParameterSpec() {
        return derivate(this::procedureHeading);
    }

    private Node variableParameterSpec() {
        Node varParameterSpec = Node.empty("varParameterSpec");
        varParameterSpec.addChildren(tokenByType(Tokens.VAR));
        varParameterSpec.addChildren(derivate(identifierList));
        varParameterSpec.addChildren(tokenByType(Tokens.COLON));
        varParameterSpec.addChildren(tokenByType(Tokens.IDENTIFIER));
        return varParameterSpec;
    }

    private Node valueParameterSpec() {
        Node valueParameterSpec = Node.empty("valueParameterSpec");
        valueParameterSpec.addChildren(derivate(identifierList));
        valueParameterSpec.addChildren(tokenByType(Tokens.COLON));
        valueParameterSpec.addChildren(tokenByType(Tokens.IDENTIFIER));
        return valueParameterSpec;
    }

    private Node varDeclarationPart() {
        if (!isCurrentTokenHasPos(Tokens.VAR)) return null;

        Node varDeclaration = Node.empty("varDeclarationPart");
        varDeclaration.addChildren(tokenByType(Tokens.VAR));
        varDeclaration.addChildren(derivate(this::varDeclarationList));
        return varDeclaration;
    }

    private Node varDeclarationList() {
        Node typeList = Node.empty("varDeclarationList");
        typeList.addChildren(derivate(this::varDeclaration));
        typeList.addChildren(derivate(this::varDeclarationList1));
        return typeList;
    }

    private Node varDeclarationList1() {
        if (!isCurrentTokenHasPos(Tokens.IDENTIFIER)) return null;
        Node varDeclarationsList1 = Node.empty("varDeclarationsList1");
        varDeclarationsList1.addChildren(derivate(this::varDeclaration));
        varDeclarationsList1.addChildren(derivate(this::varDeclarationList1));
        return varDeclarationsList1;
    }

    private Node varDeclaration() {
        Node varDeclaration = Node.empty("varDeclaration");
        varDeclaration.addChildren(derivate(identifierList));
        varDeclaration.addChildren(tokenByType(Tokens.COLON));
        varDeclaration.addChildren(derivate(this::typeDenoter));
        varDeclaration.addChildren(tokenByType(Tokens.SEMICOLON));
        return varDeclaration;
    }

    private Node typeDeclaration() {
        if (!isCurrentTokenHasPos(Tokens.TYPE)) return null;

        Node typeDeclaration = Node.empty("typeDeclaration");
        typeDeclaration.addChildren(tokenByType(Tokens.TYPE));
        typeDeclaration.addChildren(derivate(this::typeDefinitionList));
        return typeDeclaration;
    }

    private Node typeDefinitionList() {
        Node typeList = Node.empty("typeDefinitionList");
        typeList.addChildren(derivate(this::typeDefinition));
        typeList.addChildren(derivate(this::typeDefinitionList1));
        return typeList;
    }

    private Node typeDefinitionList1() {
        if (!isCurrentTokenHasPos(Tokens.IDENTIFIER)) return null;

        Node typeDefinitionsList1 = Node.empty("typeDefinitionsList1");
        typeDefinitionsList1.addChildren(derivate(this::typeDefinition));
        typeDefinitionsList1.addChildren(derivate(this::typeDefinitionList1));
        return typeDefinitionsList1;
    }

    private Node typeDefinition() {
        Node typeDefinition = Node.empty("typeDefinition");
        typeDefinition.addChildren(tokenByType(Tokens.IDENTIFIER));
        typeDefinition.addChildren(tokenByTypeAndValue(Tokens.RELOP, "="));
        typeDefinition.addChildren(derivate(this::typeDenoter));
        typeDefinition.addChildren(tokenByType(Tokens.SEMICOLON));
        return typeDefinition;
    }

    private Node arrayType() {
        Node arrayType = Node.empty("arrayType");
        arrayType.addChildren(tokenByType(Tokens.ARRAY));
        arrayType.addChildren(tokenByType(Tokens.OPEN_BRACKET));
        arrayType.addChildren(derivate(indexList));
        arrayType.addChildren(tokenByType(Tokens.CLOSE_BRACKET));
        arrayType.addChildren(tokenByType(Tokens.OF));
        arrayType.addChildren(derivate(this::typeDenoter));
        return arrayType;
    }

    private Node typeDenoter() {
        if (tokenHasPos(tokens.lookahead(), Tokens.SEMICOLON)) return tokenByType(Tokens.IDENTIFIER);

        Node typeDenoter = Node.empty("newType");
        if (isCurrentTokenHasPos(Tokens.UPARROW)) {
            typeDenoter.addChildren(derivate(this::newPointerType));
            return typeDenoter;
        }
        if (isCurrentTokenHasPos(Tokens.OPEN_PAREN)
                || isCurrentTokenHasPos(Tokens.SIGN)
                || isCurrentTokenHasPos(Tokens.UNSIGNED_INTEGER)
                || isCurrentTokenHasPos(Tokens.IDENTIFIER)) {
            typeDenoter.addChildren(derivate(this::ordinalType));
            return typeDenoter;
        }

        if (isCurrentTokenHasPos(Tokens.PACKED)) {
            typeDenoter.addChildren(tokenByType(Tokens.PACKED));
        }
        if (isCurrentTokenHasPos(Tokens.ARRAY)) {
            typeDenoter.addChildren(derivate(this::arrayType));
            return typeDenoter;
        }

        if (isCurrentTokenHasPos(Tokens.RECORD)) {
            typeDenoter.addChildren(derivate(this::recordType));
            return typeDenoter;
        }

        if (isCurrentTokenHasPos(Tokens.SET)) {
            typeDenoter.addChildren(tokenByType(Tokens.SET));
            typeDenoter.addChildren(tokenByType(Tokens.OF));
            typeDenoter.addChildren(derivate(this::typeDenoter));
            return typeDenoter;
        }

        if (isCurrentTokenHasPos(Tokens.PFILE)) {
            typeDenoter.addChildren(tokenByType(Tokens.PFILE));
            typeDenoter.addChildren(tokenByType(Tokens.OF));
            typeDenoter.addChildren(derivate(this::typeDenoter));
            return typeDenoter;
        }
        throwParseException(Tokens.UPARROW, Tokens.OPEN_PAREN, Tokens.SIGN, Tokens.UNSIGNED_INTEGER, Tokens.IDENTIFIER,
                Tokens.PACKED, Tokens.ARRAY, Tokens.RECORD, Tokens.SET, Tokens.PFILE);
        return null;
    }

    private Node recordType() {
        Node recordType = Node.empty("recordType");
        recordType.addChildren(tokenByType(Tokens.RECORD));
        if (isCurrentTokenHasPos(Tokens.CASE)) {
            recordType.addChildren(derivate(this::variantPart));
            recordType.addChildren(tokenByType(Tokens.END));
            return recordType;
        }
        recordType.addChildren(derivate(recordSectionList));
        if (isCurrentTokenHasPos(Tokens.SEMICOLON)) {
            recordType.addChildren(tokenByType(Tokens.SEMICOLON));
            recordType.addChildren(derivate(this::variantPart));
        }
        recordType.addChildren(tokenByType(Tokens.END));
        return recordType;
    }

    private Node newPointerType() {
        Node newPointerType = Node.empty("newPointerType");
        newPointerType.addChildren(tokenByType(Tokens.UPARROW));
        newPointerType.addChildren(tokenByType(Tokens.IDENTIFIER));
        return newPointerType;
    }

    private Node variantPart() {
        Node variantPart = Node.empty("variantPart");
        variantPart.addChildren(tokenByType(Tokens.CASE));
        variantPart.addChildren(derivate(this::variantSelector));
        variantPart.addChildren(tokenByType(Tokens.OF));
        variantPart.addChildren(derivate(variantList));
        if (isCurrentTokenHasPos(Tokens.SEMICOLON)) {
            variantPart.addChildren(tokenByType(Tokens.SEMICOLON));
        }
        return variantPart;

    }

    private Node variant() {
        Node variant = Node.empty("variant");
        variant.addChildren(derivate(caseConstantList));
        variant.addChildren(tokenByType(Tokens.COLON));
        variant.addChildren(tokenByType(Tokens.OPEN_PAREN));
        if (isCurrentTokenHasPos(Tokens.CASE)) {
            variant.addChildren(derivate(this::variantPart));
        } else {
            variant.addChildren(derivate(recordSectionList));
            if (isCurrentTokenHasPos(Tokens.SEMICOLON)) {
                variant.addChildren(tokenByType(Tokens.SEMICOLON));
                variant.addChildren(derivate(this::variantPart));
            }
        }
        variant.addChildren(tokenByType(Tokens.CLOSE_PAREN));
        return variant;
    }

    private Node recordSection() {
        Node recordSection = Node.empty("recordSection");
        recordSection.addChildren(derivate(identifierList));
        recordSection.addChildren(tokenByType(Tokens.COLON));
        recordSection.addChildren(derivate(this::typeDenoter));
        recordSection.addChildren(tokenByType(Tokens.SEMICOLON));
        return recordSection;
    }

    private Node caseConstant() {
        Node caseConstant = Node.empty("caseConstant");
        caseConstant.addChildren(derivate(this::constant));
        if (isCurrentTokenHasPos(Tokens.DOT)) {
            caseConstant.addChildren(tokenByType(Tokens.DOT));
            caseConstant.addChildren(tokenByType(Tokens.DOT));
            caseConstant.addChildren(derivate(this::constant));
        }
        return caseConstant;
    }

    private Node variantSelector() {
        Node variantSelector = Node.empty("variantSelector");
        variantSelector.addChildren(tokenByType(Tokens.IDENTIFIER));
        if (isCurrentTokenHasPos(Tokens.COLON)) {
            variantSelector.addChildren(tokenByType(Tokens.COLON));
            variantSelector.addChildren(tokenByType(Tokens.IDENTIFIER));
        }
        return variantSelector;
    }

    private Node ordinalType() {
        Node ordinalType = Node.empty("ordinalType");
        if (isCurrentTokenHasPos(Tokens.OPEN_PAREN)) {
            addParensDerivate(ordinalType, identifierList);
            return ordinalType;
        }
        ordinalType.addChildren(derivate(this::constant));
        ordinalType.addChildren(tokenByType(Tokens.DOT));
        ordinalType.addChildren(tokenByType(Tokens.DOT));
        ordinalType.addChildren(derivate(this::constant));
        return ordinalType;
    }

    private Node constant() {
        Node constant = Node.empty("const");
        if (isCurrentTokenHasPos(Tokens.SIGN)) {
            constant.addChildren(tokenByType(Tokens.SIGN));
        }
        if (isCurrentTokenHasPos(Tokens.UNSIGNED_INTEGER)) {
            constant.addChildren(tokenByType(Tokens.UNSIGNED_INTEGER));
            return constant;
        }

        if (isCurrentTokenHasPos(Tokens.IDENTIFIER)) {
            constant.addChildren(tokenByType(Tokens.IDENTIFIER));
            return constant;
        }

        throwParseException(Tokens.SIGN, Tokens.UNSIGNED_INTEGER, Tokens.IDENTIFIER);
        return null;
    }

    private Node constDeclaration() {
        if (!isCurrentTokenHasPos(Tokens.CONST)) return null;

        Node constDefinition = Node.empty("constDeclaration");
        constDefinition.addChildren(tokenByType(Tokens.CONST));
        constDefinition.addChildren(derivate(this::constList));
        return constDefinition;
    }

    private Node constList() {
        Node constantList = Node.empty("constList");
        constantList.addChildren(derivate(this::constDefinition));
        constantList.addChildren(derivate(this::constList1));
        return constantList;
    }

    private Node constList1() {
        if (!isCurrentTokenHasPos(Tokens.IDENTIFIER)) return null;
        Node constList1 = Node.empty("constList1");
        constList1.addChildren(derivate(this::constDefinition));
        constList1.addChildren(derivate(this::constList1));
        return constList1;
    }

    private Node constDefinition() {
        Node constDefinition = Node.empty("constDefinition");
        constDefinition.addChildren(tokenByType(Tokens.IDENTIFIER));
        constDefinition.addChildren(tokenByTypeAndValue(Tokens.RELOP, "="));
        constDefinition.addChildren(derivate(simpleExpr));
        constDefinition.addChildren(tokenByType(Tokens.SEMICOLON));
        return constDefinition;
    }

    private Node labelDeclaration() {
        if (!isCurrentTokenHasPos(Tokens.LABEL)) return null;

        Node labelDeclaration = Node.empty("labelDeclaration");
        labelDeclaration.addChildren(tokenByTypeAndValue(Tokens.LABEL, "label"));
        labelDeclaration.addChildren(derivate(labelList));
        labelDeclaration.addChildren(tokenByType(Tokens.SEMICOLON));
        return labelDeclaration;
    }

    private Node programHeading() {
        Node heading = Node.empty("programHeading");
        heading.addChildren(tokenByTypeAndValue(Tokens.PROGRAM, "program"));
        heading.addChildren(tokenByType(Tokens.IDENTIFIER));
        if (!isCurrentTokenHasPos(Tokens.OPEN_PAREN)) return heading;

        addParensDerivate(heading, identifierList);
        return heading;
    }
}
