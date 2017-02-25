package me.wbars.semantic.models;

import me.wbars.semantic.models.types.Type;
import me.wbars.semantic.models.types.TypeRegistry;

import java.util.Arrays;
import java.util.List;

public class ProcOrFunctionDeclarationNode extends ASTNode {
    private FuncOrProcHeadingNode heading;
    private BlockNode body;

    public ProcOrFunctionDeclarationNode(FuncOrProcHeadingNode heading, BlockNode body) {
        super(heading != null ? heading.getValue() : "");
        this.heading = heading;
        this.body = body;
    }

    public ProcOrFunctionDeclarationNode() {
        this(null, null);
    }

    public FuncOrProcHeadingNode getHeading() {
        return heading;
    }

    public BlockNode getBody() {
        return body;
    }

    public void setHeading(FuncOrProcHeadingNode heading) {
        this.heading = heading;
    }

    public void setBody(BlockNode body) {
        this.body = body;
    }

    @Override
    protected Type getType(TypeRegistry typeRegistry) {
        return typeRegistry.processType(this);
    }

    @Override
    protected void replaceChild(int index, ASTNode node) {
        if (index == 0) heading = (FuncOrProcHeadingNode) node;
        if (index == 1) body = (BlockNode) node;
        throw new IllegalArgumentException();
    }

    @Override
    public List<ASTNode> children() {
        return Arrays.asList(heading, body);
    }
}
