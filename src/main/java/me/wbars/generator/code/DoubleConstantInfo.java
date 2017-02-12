package me.wbars.generator.code;

import me.wbars.generator.IntegerToByteConverter;

import java.util.List;

public class DoubleConstantInfo implements ConstantInfo {
    private final double value;
    private final int index;

    public DoubleConstantInfo(double value, int index) {
        this.value = value;
        this.index = index;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public int getTag() {
        return 4;
    }

    @Override
    public String getRawValue() {
        return String.valueOf(value);
    }

    @Override
    public List<Byte> toBytes() {
        return IntegerToByteConverter.convert(value, 8);
    }

    public double getValue() {
        return value;
    }
}
