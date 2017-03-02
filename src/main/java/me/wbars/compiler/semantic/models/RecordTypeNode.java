package me.wbars.compiler.semantic.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RecordTypeNode extends ASTNode {
    private final List<List<LiteralNode>> recordSections = new ArrayList<>();
    private final List<VariantSelectorNode> variants = new ArrayList<>();

    public RecordTypeNode(String name) {
        super(name);
    }

    @Override
    protected void replaceChild(int index, ASTNode node) {
        throw new UnsupportedOperationException(); //todo
    }

    @Override
    public List<ASTNode> children() {
        return Stream.of(
                recordSections.stream().flatMap(Collection::stream).collect(Collectors.toList()),
                variants
        ).flatMap(Collection::stream).collect(Collectors.toList());
    }

    public List<List<LiteralNode>> getRecordSections() {
        return recordSections;
    }

    public List<VariantSelectorNode> getVariants() {
        return variants;
    }
}
