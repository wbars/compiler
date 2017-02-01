package me.wbars.semantic.models;

import java.util.ArrayList;
import java.util.List;

public class ProgramNode extends ASTNode {
    private final List<LiteralNode> identifiers = new ArrayList<>();
    private ASTNode block;

    public ProgramNode(String name) {
        super(name);
    }

    public void addIdentifier(LiteralNode identifier) {
        identifiers.add(identifier);
    }

    public ASTNode getBlock() {
        return block;
    }

    public void setBlock(ASTNode block) {
        this.block = block;
    }

    public List<LiteralNode> getIdentifiers() {
        return identifiers;
    }
}
