package me.wbars.compiler.semantic.models;

import java.util.Collections;
import java.util.List;

public abstract class UnaryOpNode extends ASTNode {
    public UnaryOpNode(String value) {
        super(value);
    }

    @Override
    protected void replaceChild(int index, ASTNode node) {
        if (index != 0) throw new IllegalArgumentException();
        target = node;
    }

    @Override
    public List<ASTNode> children() {
        return Collections.singletonList(target);
    }

    private ASTNode target;

    public ASTNode getTarget() {
        return target;
    }

    public void setTarget(ASTNode target) {
        this.target = target;
    }
}
