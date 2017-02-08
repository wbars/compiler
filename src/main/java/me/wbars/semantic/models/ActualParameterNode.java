package me.wbars.semantic.models;

import me.wbars.semantic.models.types.Type;
import me.wbars.semantic.models.types.TypeRegistry;

public class ActualParameterNode extends ASTNode {
    private final BinaryOpNode first;
    private final BinaryOpNode second;
    private final BinaryOpNode third;
    public ActualParameterNode(BinaryOpNode first, BinaryOpNode second, BinaryOpNode third) {
        super(first.getValue());
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public BinaryOpNode getFirst() {
        return first;
    }

    public BinaryOpNode getSecond() {
        return second;
    }

    public BinaryOpNode getThird() {
        return third;
    }

    @Override
    protected Type getType(TypeRegistry typeRegistry) {
        return typeRegistry.processType(this);
    }
}
