package me.wbars.compiler.semantic.models;

import java.util.Collections;
import java.util.List;

public class FileTypeNode extends CompoundTypeNode {
    private ASTNode typeDenoter;
    public FileTypeNode(String name, ASTNode typeDenoter, boolean packed) {
        super(name, packed);
        this.typeDenoter = typeDenoter;
    }

    public FileTypeNode(String name, boolean packed) {
        this(name, null, packed);
    }

    public void setTypeDenoter(ASTNode typeDenoter) {
        this.typeDenoter = typeDenoter;
    }

    public ASTNode getTypeDenoter() {
        return typeDenoter;
    }

    @Override
    protected void replaceChild(int index, ASTNode node) {
        if (index > 0) throw new IllegalArgumentException();
        typeDenoter = node;
    }

    @Override
    public List<ASTNode> children() {
        return Collections.singletonList(typeDenoter);
    }
}