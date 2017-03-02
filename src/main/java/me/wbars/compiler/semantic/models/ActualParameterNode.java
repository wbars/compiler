package me.wbars.compiler.semantic.models;

import me.wbars.compiler.generator.JvmBytecodeGenerator;
import me.wbars.compiler.semantic.models.types.Type;
import me.wbars.compiler.semantic.models.types.TypeRegistry;

import java.util.Arrays;
import java.util.List;

public class ActualParameterNode extends ASTNode {
    private BinaryOpNode first;
    private BinaryOpNode second;
    private BinaryOpNode third;
    public ActualParameterNode(BinaryOpNode first, BinaryOpNode second, BinaryOpNode third) {
        super(first != null ? first.getValue() : null);
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public ActualParameterNode() {
        this(null, null, null);
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

    public void setFirst(BinaryOpNode first) {
        this.first = first;
    }

    public void setSecond(BinaryOpNode second) {
        this.second = second;
    }

    public void setThird(BinaryOpNode third) {
        this.third = third;
    }

    @Override
    protected Type getType(TypeRegistry typeRegistry) {
        return typeRegistry.processType(this);
    }

    @Override
    public int generateCode(JvmBytecodeGenerator codeGenerator) {
        return codeGenerator.generate(this);
    }

    @Override
    protected void replaceChild(int index, ASTNode node) {
        if (index == 0) first = (BinaryArithmeticOpNode) node;
        if (index == 1) second = (BinaryArithmeticOpNode) node;
        if (index == 2) third = (BinaryArithmeticOpNode) node;
        throw new IllegalArgumentException();
    }

    @Override
    public List<ASTNode> children() {
        return Arrays.asList(first, second, third);
    }

}
