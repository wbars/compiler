package me.wbars.compiler.generator.code;

import me.wbars.compiler.generator.NumberToByteConverter;

import java.util.List;

public class StringConstantInfo implements ConstantInfo {
    private final String value;
    private final Integer stringIndex;
    private final int index;

    public StringConstantInfo(String value, Integer stringIndex, int index) {
        this.value = value;
        this.stringIndex = stringIndex;
        this.index = index;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public int getTag() {
        return 8;
    }

    @Override
    public String getRawValue() {
        return value;
    }

    @Override
    public List<Byte> toBytes() {
        return NumberToByteConverter.convert(stringIndex, getSize());
    }

    @Override
    public int getSize() {
        return 2;
    }

    public Integer getStringIndex() {
        return stringIndex;
    }
}
