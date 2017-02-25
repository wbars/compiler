package me.wbars.generator.code;

import me.wbars.generator.NumberToByteConverter;

import java.util.ArrayList;
import java.util.List;

public class NameAndTypeConstantInfo implements ConstantInfo {
    private final String value;
    private final int nameIndex;
    private final int typeIndex;
    private final int index;

    public NameAndTypeConstantInfo(String value, int nameIndex, int typeIndex, int index) {
        this.value = value;
        this.nameIndex = nameIndex;
        this.typeIndex = typeIndex;
        this.index = index;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public int getTag() {
        return 12;
    }

    @Override
    public String getRawValue() {
        return value;
    }

    @Override
    public List<Byte> toBytes() {
        List<Byte> result = new ArrayList<>();
        result.addAll(NumberToByteConverter.convert(nameIndex, 2));
        result.addAll(NumberToByteConverter.convert(typeIndex, 2));
        return result;
    }

    @Override
    public int getSize() {
        return 4;
    }
}
