package me.wbars.semantic.models;

public class UnaryOpNode extends ASTNode {
    public UnaryOpNode(String value) {
        super(value);
    }

    private ASTNode target;

    public ASTNode getTarget() {
        return target;
    }

    public void setTarget(ASTNode target) {
        this.target = target;
    }
}
