package me.wbars.semantic.models;

import java.util.Arrays;
import java.util.List;

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

    @Override
    protected void replaceChild(int index, ASTNode node) {
        throw new UnsupportedOperationException(); //todo
    }

    @Override
    public List<ASTNode> children() {
        return Arrays.asList(tagField, tagType, variantNode);
    }
}
