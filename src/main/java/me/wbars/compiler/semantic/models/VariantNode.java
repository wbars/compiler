package me.wbars.compiler.semantic.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    @Override
    protected void replaceChild(int index, ASTNode node) {
        throw new UnsupportedOperationException(); //todo
    }

    @Override
    public List<ASTNode> children() {
        return Stream.of(caseConstants, Collections.singletonList(variantSelector), recordSections.stream().flatMap(Collection::stream).collect(Collectors.toList()))
                .flatMap(Collection::stream).collect(Collectors.toList());
    }
}
