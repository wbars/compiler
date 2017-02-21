package me.wbars.semantic.models.types;

import static java.util.Objects.requireNonNull;

public class ArrayType implements Type {
    private Type type;
    private Integer lowerBound;
    private Integer upperBound;

    ArrayType(Type type, Integer lowerBound, Integer upperBound) {
        this.type = requireNonNull(type);
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    public Type getType() {
        return type;
    }

    public Integer getLowerBound() {
        return lowerBound;
    }

    public Integer getUpperBound() {
        return upperBound;
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

        if (!type.equals(arrayType.type)) return false;
        if (!lowerBound.equals(arrayType.lowerBound)) return false;
        return upperBound.equals(arrayType.upperBound);
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + lowerBound.hashCode();
        result = 31 * result + upperBound.hashCode();
        return result;
    }
}
