package me.wbars.generator.code;

import me.wbars.generator.NumberToByteConverter;

import java.util.List;

public class IntegerConstantInfo implements ConstantInfo {
    private final int value;
    private final int index;

    public IntegerConstantInfo(int value, int index) {
        this.value = value;
        this.index = index;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public int getTag() {
        return 3;
    }

    @Override
    public String getRawValue() {
        return Integer.toString(value);
    }

    @Override
    public List<Byte> toBytes() {
        return NumberToByteConverter.convert(value, getSize());
    }

    @Override
    public int getSize() {
        return 4;
    }

    public int getValue() {
        return value;
    }
}
