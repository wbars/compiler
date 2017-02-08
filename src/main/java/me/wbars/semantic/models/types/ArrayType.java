package me.wbars.semantic.models.types;

public class ArrayType implements Type {
    private Type type;
    private Integer lowerBound;
    private Integer upperBound;

    ArrayType(Type type, Integer lowerBound, Integer upperBound) {
        this.type = type;
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
}
