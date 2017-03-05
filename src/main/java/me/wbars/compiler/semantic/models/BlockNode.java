package me.wbars.compiler.semantic.models;

import me.wbars.compiler.generator.JvmBytecodeGenerator;
import me.wbars.compiler.parser.models.Tokens;
import me.wbars.compiler.scanner.models.Token;
import me.wbars.compiler.scanner.models.TokenFactory;
import me.wbars.compiler.semantic.models.types.Type;
import me.wbars.compiler.semantic.models.types.TypeRegistry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.of;
import static me.wbars.compiler.utils.CollectionsUtils.merge;

public class BlockNode extends ASTNode {
    private final List<LiteralNode> labels = new ArrayList<>();
    private final List<VarDeclarationNode> varDeclarations = new ArrayList<>();
    private final List<ASTNode> typeDefinitions = new ArrayList<>();
    private final List<ConstDefinitionNode> constDefinitions = new ArrayList<>();
    private final List<ProcOrFunctionDeclarationNode> procOrFunctionDeclarations = new ArrayList<>();
    private final List<ASTNode> statements = new ArrayList<>();

    public BlockNode() {
        super("block");
    }

    public List<ConstDefinitionNode> getConstDefinitions() {
        return constDefinitions;
    }

    public List<LiteralNode> getLabels() {
        return labels;
    }

    public List<ASTNode> getTypeDefinitions() {
        return typeDefinitions;
    }

    public List<VarDeclarationNode> getVarDeclarations() {
        return varDeclarations;
    }

    public List<ProcOrFunctionDeclarationNode> getProcOrFunctionDeclarations() {
        return procOrFunctionDeclarations;
    }

    public List<ASTNode> getStatements() {
        return statements;
    }

    @Override
    protected Type getType(TypeRegistry typeRegistry) {
        return typeRegistry.processType(this);
    }

    @Override
    public int generateCode(JvmBytecodeGenerator codeGenerator) {
        return codeGenerator.generate(this);
    }

    @Override
    protected void replaceChild(int index, ASTNode node) {
        int selectedIndex = index;
        if (index < labels.size()) {
            labels.set(selectedIndex, (LiteralNode) node);
            return;
        }

        selectedIndex -= labels.size();
        if (index < varDeclarations.size()) {
            varDeclarations.set(selectedIndex, (VarDeclarationNode) node);
            return;
        }

        selectedIndex -= varDeclarations.size();
        if (index < typeDefinitions.size()) {
            typeDefinitions.set(selectedIndex, node);
            return;
        }

        selectedIndex -= typeDefinitions.size();
        if (index < constDefinitions.size()) {
            constDefinitions.set(selectedIndex, (ConstDefinitionNode) node);
            return;
        }


        selectedIndex -= constDefinitions.size();
        if (index < procOrFunctionDeclarations.size()) {
            procOrFunctionDeclarations.set(selectedIndex, (ProcOrFunctionDeclarationNode) node);
            return;
        }

        selectedIndex -= procOrFunctionDeclarations.size();
        if (index < statements.size()) {
            statements.set(selectedIndex, node);
            return;
        }

        throw new IllegalArgumentException();
    }

    @Override
    public List<ASTNode> children() {
        return of(labels, varDeclarations, typeDefinitions, constDefinitions, procOrFunctionDeclarations, statements)
                .flatMap(Collection::stream).collect(toList());
    }

    @Override
    public List<Token> tokens() {
        List<Token> labelsPart = ifNotEmpty(labels, nodes -> merge(
                singletonList(Token.keyword(Tokens.LABEL)),
                nestedTokens(nodes, TokenFactory::comma),
                singletonList(Token.keyword(Tokens.SEMICOLON)
                ))
        );
        List<Token> constPart = ifNotEmpty(constDefinitions, nodes -> merge(
                singletonList(Token.keyword(Tokens.CONST)),
                nestedTokens(nodes, TokenFactory::comma)
        ));
        List<Token> typePart = ifNotEmpty(typeDefinitions, nodes -> merge(
                singletonList(Token.keyword(Tokens.TYPE)),
                nestedTokens(nodes, TokenFactory::comma)
        ));
        List<Token> varPart = ifNotEmpty(varDeclarations, nodes -> merge(
                singletonList(Token.keyword(Tokens.VAR)),
                nestedTokens(nodes)
        ));
        List<Token> funcProcPart = ifNotEmpty(procOrFunctionDeclarations, this::nestedTokens);
        List<Token> stmtsPart = merge(
                singletonList(Token.keyword(Tokens.BEGIN)),
                ifNotEmpty(statements, this::nestedStatements)
        );

        return merge(labelsPart, constPart, typePart, varPart, funcProcPart, stmtsPart);

    }
}
