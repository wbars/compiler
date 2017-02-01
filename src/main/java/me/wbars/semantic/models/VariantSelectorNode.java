package me.wbars.semantic.models;

public class VariantSelectorNode extends ASTNode {
    private final LiteralNode tagField;
    private final LiteralNode tagType;
    private final VariantNode variantNode;
    public VariantSelectorNode(String value, LiteralNode tagField, LiteralNode tagType, VariantNode variantNode) {
        super(value);
        this.tagField = tagField;
        this.tagType = tagType;
        this.variantNode = variantNode;
    }

    public LiteralNode getTagField() {
        return tagField;
    }

    public LiteralNode getTagType() {
        return tagType;
    }

    public VariantNode getVariantNode() {
        return variantNode;
    }
}
