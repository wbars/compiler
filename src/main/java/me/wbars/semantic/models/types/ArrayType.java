package me.wbars.semantic.models.types;

public class ArrayType implements Type {
    private Type type;
    private String lowerBound;
    private String upperBound;

    ArrayType(Type type, String lowerBound, String upperBound) {
        this.type = type;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    public Type getType() {
        return type;
    }

    public String getLowerBound() {
        return lowerBound;
    }

    public String getUpperBound() {
        return upperBound;
    }

    @Override
    public String name() {
        return "Array";
    }
}
