package me.wbars.semantic.models;

import me.wbars.semantic.models.types.Type;
import me.wbars.semantic.models.types.TypeRegistry;

public class ProcOrFunctionDeclarationNode extends ASTNode {
    private final FuncOrProcHeadingNode heading;
    private final BlockNode body;

    public ProcOrFunctionDeclarationNode(FuncOrProcHeadingNode heading, BlockNode body) {
        super(heading.getValue());
        this.heading = heading;
        this.body = body;
    }

    public FuncOrProcHeadingNode getHeading() {
        return heading;
    }

    public BlockNode getBody() {
        return body;
    }

    @Override
    protected Type getType(TypeRegistry typeRegistry) {
        return typeRegistry.processType(this);
    }
}
