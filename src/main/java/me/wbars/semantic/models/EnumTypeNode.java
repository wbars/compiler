package me.wbars.semantic.models;

import me.wbars.semantic.models.types.Type;
import me.wbars.semantic.models.types.TypeRegistry;

import java.util.List;

public class EnumTypeNode extends ASTNode {
    private final List<LiteralNode> identifiers;

    public EnumTypeNode(String name, List<LiteralNode> identifiers) {
        super(name);
        this.identifiers = identifiers;
    }

    public List<LiteralNode> getIdentifiers() {
        return identifiers;
    }

    @Override
    protected Type getType(TypeRegistry typeRegistry) {
        return typeRegistry.processType(this);
    }
}
