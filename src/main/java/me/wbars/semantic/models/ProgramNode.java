package me.wbars.semantic.models;

import me.wbars.semantic.models.types.Type;
import me.wbars.semantic.models.types.TypeRegistry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProgramNode extends ASTNode {
    private List<LiteralNode> identifiers = new ArrayList<>();
    private BlockNode block;

    public ProgramNode(String name, BlockNode block) {
        super(name);
        this.block = block;
    }

    public ProgramNode() {
        this(null, null);
    }

    public void setIdentifiers(List<LiteralNode> identifiers) {
        this.identifiers = identifiers;
    }

    public void setBlock(BlockNode block) {
        this.block = block;
    }

    public void addIdentifier(LiteralNode identifier) {
        identifiers.add(identifier);
    }

    public BlockNode getBlock() {
        return block;
    }

    public List<LiteralNode> getIdentifiers() {
        return identifiers;
    }

    @Override
    protected Type getType(TypeRegistry typeRegistry) {
        return typeRegistry.processType(this);
    }

    @Override
    protected void replaceChild(int index, ASTNode node) {
        if (index < identifiers.size()) identifiers.set(index, (LiteralNode) node);
        else block = (BlockNode) node;
    }

    @Override
    public List<ASTNode> children() {
        return Stream.of(identifiers, Collections.singletonList(block)).flatMap(Collection::stream).collect(Collectors.toList());
    }
}
