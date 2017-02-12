package me.wbars.generator.code;

import me.wbars.generator.IntegerToByteConverter;

import java.util.List;

public class ClassConstantInfo implements ConstantInfo {
    private final String value;
    private final int classNameIndex;
    private final int index;

    public ClassConstantInfo(String value, int classNameIndex, int index) {
        this.value = value;
        this.classNameIndex = classNameIndex;
        this.index = index;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public int getTag() {
        return 7;
    }

    @Override
    public String getRawValue() {
        return value;
    }

    @Override
    public List<Byte> toBytes() {
        return IntegerToByteConverter.convert(classNameIndex, 2);
    }

    public int getClassNameIndex() {
        return classNameIndex;
    }
}
