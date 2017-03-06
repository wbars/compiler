package me.wbars.compiler.semantic.models;

import me.wbars.compiler.parser.models.Tokens;
import me.wbars.compiler.scanner.models.Token;
import me.wbars.compiler.scanner.models.TokenFactory;
import me.wbars.compiler.semantic.models.types.Type;
import me.wbars.compiler.semantic.models.types.TypeRegistry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static me.wbars.compiler.utils.CollectionsUtils.merge;

public class ProgramNode extends ASTNode {
    private List<IdentifierNode> identifiers = new ArrayList<>();
    private BlockNode block;

    public ProgramNode(String name, BlockNode block) {
        super(name);
        this.block = block;
    }

    public ProgramNode() {
        this(null, null);
    }

    public void setIdentifiers(List<IdentifierNode> identifiers) {
        this.identifiers = identifiers;
    }

    public void setBlock(BlockNode block) {
        this.block = block;
    }

    public void addIdentifier(IdentifierNode identifier) {
        identifiers.add(identifier);
    }

    public BlockNode getBlock() {
        return block;
    }

    public List<IdentifierNode> getIdentifiers() {
        return identifiers;
    }

    @Override
    protected Type getType(TypeRegistry typeRegistry) {
        return typeRegistry.processType(this);
    }

    @Override
    protected void replaceChild(int index, ASTNode node) {
        if (index < identifiers.size()) identifiers.set(index, (IdentifierNode) node);
        else block = (BlockNode) node;
    }

    @Override
    public List<ASTNode> children() {
        return Stream.of(identifiers, singletonList(block))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Override
    public List<Token> tokens() {
        return merge(
                singletonList(Token.keyword(Tokens.PROGRAM)),
                singletonList(Token.create(Tokens.IDENTIFIER, value)),
                nestedTokens(identifiers, TokenFactory::comma),
                singletonList(TokenFactory.createSemicolon()),
                block.tokens(),
                singletonList(Token.keyword(Tokens.END)),
                singletonList(TokenFactory.createDot())
        );
    }

}
