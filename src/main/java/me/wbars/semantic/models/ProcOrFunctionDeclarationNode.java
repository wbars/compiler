package me.wbars.semantic.models;

public class ProcOrFunctionDeclarationNode extends ASTNode {
    private final FuncOrProcHeadingNode heading;
    private final ASTNode body;

    public ProcOrFunctionDeclarationNode(FuncOrProcHeadingNode heading, ASTNode body) {
        super(heading.getValue());
        this.heading = heading;
        this.body = body;
    }

    public FuncOrProcHeadingNode getHeading() {
        return heading;
    }

    public ASTNode getBody() {
        return body;
    }
}
