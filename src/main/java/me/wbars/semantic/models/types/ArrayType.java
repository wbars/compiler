package me.wbars.semantic.models.types;

import static java.util.Objects.requireNonNull;

public class ArrayType implements Type {
    private Type type;

    ArrayType(Type type) {
        this.type = requireNonNull(type);
    }

    public Type getType() {
        return type;
    }

    @Override
    public String name() {
        return "Array";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ArrayType arrayType = (ArrayType) o;

        return type.equals(arrayType.type);
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }

    @Override
    public String alias() {
        return "[" + type.alias();
    }
}
