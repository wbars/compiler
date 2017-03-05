package me.wbars.compiler.semantic.models;

import me.wbars.compiler.scanner.models.Token;

import java.util.List;

public abstract class CompoundTypeNode extends ASTNode {
    private final boolean packed;
    public CompoundTypeNode(String value, boolean packed) {
        super(value);
        this.packed = packed;
    }

    public boolean isPacked() {
        return packed;
    }

    public abstract List<Token> tokens();
}
