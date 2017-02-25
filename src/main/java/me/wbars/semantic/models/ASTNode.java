package me.wbars.semantic.models;

import me.wbars.generator.JvmBytecodeGenerator;
import me.wbars.semantic.models.types.Type;
import me.wbars.semantic.models.types.TypeRegistry;

import java.util.List;
import java.util.stream.IntStream;

public abstract class ASTNode {
    protected String value;
    protected Type type;
    protected ASTNode parent;
    private TypeRegistry typeRegistry;

    public void setValue(String value) {
        this.value = value;
    }

    public ASTNode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public Type getType() {
        return type;
    }

    protected Type getType(TypeRegistry typeRegistry) {
        return null;
    }

    public final Type getProcessedType(TypeRegistry typeRegistry) {
        this.typeRegistry = typeRegistry;
        if (type == null) {
            type = getType(typeRegistry);
        }
        return type;
    }

    public int generateCode(JvmBytecodeGenerator codeGenerator) {
        throw new UnsupportedOperationException();
    }

    public ASTNode getParent() {
        return parent;
    }

    public void setParent(ASTNode parent) {
        this.parent = parent;
    }

    public ASTNode child(int i) {
        return children().get(i);
    }

    public void replace(ASTNode node) {
        int thisIndex = IntStream.range(0, parent.children().size()).boxed()
                .filter(i -> parent.child(i) == this).findAny()
                .orElseThrow(IllegalStateException::new);
        parent.replaceChild(thisIndex, node);
    }

    protected abstract void replaceChild(int index, ASTNode node);

    public abstract List<ASTNode> children();

    public TypeRegistry getTypeRegistry() {
        return typeRegistry;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName().replaceAll("Node", "") + "(" + value + ")";

    }

    public String pretty() {
        return pretty(0);
    }

    public String pretty(int ident) {
        String result = "|";
        for (int i = 0; i < ident; i++) {
            result += "\t";
        }
        result += "|" + toString();
        if (children() != null) {
            for (ASTNode child : children()) {
                if (child != null) {
                    result += "\n" + child.pretty(ident + 1);
                }

            }

        }
        return result;
    }
}
