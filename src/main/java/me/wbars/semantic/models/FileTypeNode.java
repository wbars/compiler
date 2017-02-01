package me.wbars.semantic.models;

public class FileTypeNode extends CompoundTypeNode {
    private final ASTNode typeDenoter;
    public FileTypeNode(String name, ASTNode typeDenoter, boolean packed) {
        super(name, packed);
        this.typeDenoter = typeDenoter;
    }

    public ASTNode getTypeDenoter() {
        return typeDenoter;
    }
}
