package me.wbars.semantic.models;

import java.util.ArrayList;
import java.util.List;

public class VariantNode extends ASTNode {
    private final List<SubrangeTypeNode> caseConstants = new ArrayList<>();
    private final VariantSelectorNode variantSelector;
    private final List<List<LiteralNode>> recordSections = new ArrayList<>();

    public VariantNode(String value, VariantSelectorNode variantSelector) {
        super(value);
        this.variantSelector = variantSelector;
    }

    public List<SubrangeTypeNode> getCaseConstants() {
        return caseConstants;
    }

    public VariantSelectorNode getVariantSelector() {
        return variantSelector;
    }

    public List<List<LiteralNode>> getRecordSections() {
        return recordSections;
    }
}
