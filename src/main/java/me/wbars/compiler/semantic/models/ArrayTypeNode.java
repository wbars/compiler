package me.wbars.compiler.semantic.models;

import me.wbars.compiler.parser.models.Tokens;
import me.wbars.compiler.scanner.models.Token;
import me.wbars.compiler.scanner.models.TokenFactory;
import me.wbars.compiler.semantic.models.types.Type;
import me.wbars.compiler.semantic.models.types.TypeRegistry;

import java.util.Collection;
import java.util.List;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.of;
import static me.wbars.compiler.utils.CollectionsUtils.merge;

public class ArrayTypeNode extends CompoundTypeNode {
    private List<SubrangeTypeNode> indexes;
    private ASTNode componentType;

    public ArrayTypeNode(String name, List<SubrangeTypeNode> indexes, ASTNode componentType, boolean packed) {
        super(name, packed);
        this.indexes = indexes;
        this.componentType = componentType;
    }

    public ArrayTypeNode(String name, boolean packed) {
        this(name, null, null, packed);
    }

    public void setIndexes(List<SubrangeTypeNode> indexes) {
        this.indexes = indexes;
    }

    public void setComponentType(ASTNode componentType) {
        this.componentType = componentType;
    }

    public ASTNode getComponentType() {
        return componentType;
    }

    @Override
    protected Type getType(TypeRegistry typeRegistry) {
        return typeRegistry.processType(this);
    }

    @Override
    protected void replaceChild(int index, ASTNode node) {
        if (index == 0) componentType = node;
        else indexes.set(index - 1, (SubrangeTypeNode) node);
    }

    @Override
    public List<ASTNode> children() {
        return of(singletonList(componentType), indexes)
                .flatMap(Collection::stream)
                .collect(toList());
    }

    @Override
    public List<Token> tokens() {
        return merge(
                singletonList(Token.create(Tokens.IDENTIFIER, value)),
                singletonList(TokenFactory.createRelOp("=")),
                singletonList(Token.keyword(Tokens.ARRAY)),
                singletonList(Token.keyword(Tokens.OF)),
                componentType.tokens(),
                singletonList(TokenFactory.createSemicolon())
        );
    }

    public int size() {
        return indexes.size();
    }


}
