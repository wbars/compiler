package me.wbars.generator.code;

import me.wbars.generator.NumberToByteConverter;

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
        return 6;
    }

    @Override
    public String getRawValue() {
        return String.valueOf(value);
    }

    @Override
    public List<Byte> toBytes() {
        return NumberToByteConverter.convert(value, getSize());
    }

    public double getValue() {
        return value;
    }

    @Override
    public int getSize() {
        return 8;
    }
}
