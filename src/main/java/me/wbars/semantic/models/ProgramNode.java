package me.wbars.semantic.models;

import me.wbars.semantic.models.types.Type;
import me.wbars.semantic.models.types.TypeRegistry;

import java.util.ArrayList;
import java.util.List;

public class ProgramNode extends ASTNode {
    private final List<LiteralNode> identifiers = new ArrayList<>();
    private final BlockNode block;

    public ProgramNode(String name, BlockNode block) {
        super(name);
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
}
