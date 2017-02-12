package me.wbars.generator.code;

import me.wbars.generator.IntegerToByteConverter;

import java.util.List;

public class LongConstantInfo implements ConstantInfo {
    private final long value;
    private final int index;

    public LongConstantInfo(long value, int index) {
        this.value = value;
        this.index = index;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public int getTag() {
        return 5;
    }

    @Override
    public String getRawValue() {
        return String.valueOf(value);
    }

    @Override
    public List<Byte> toBytes() {
        return IntegerToByteConverter.convert(value, 8);
    }

    public long getValue() {
        return value;
    }
}
