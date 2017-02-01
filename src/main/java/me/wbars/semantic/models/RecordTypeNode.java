package me.wbars.semantic.models;

import java.util.ArrayList;
import java.util.List;

public class RecordTypeNode extends ASTNode {
    private final List<List<LiteralNode>> recordSections = new ArrayList<>();
    private final List<VariantSelectorNode> variants = new ArrayList<>();
    public RecordTypeNode(String name) {
        super(name);
    }

    public List<List<LiteralNode>> getRecordSections() {
        return recordSections;
    }

    public List<VariantSelectorNode> getVariants() {
        return variants;
    }
}
