package me.wbars.generator.code;

import me.wbars.generator.NumberToByteConverter;

import java.util.ArrayList;
import java.util.List;

public abstract class FieldOrMethodConstantInfo implements ConstantInfo {
    private final String value;
    private final int classIndex;
    private final int nameAndTypeIndex;
    private final int index;

    public FieldOrMethodConstantInfo(String value, int classIndex, int nameAndTypeIndex, int index) {
        this.value = value;
        this.classIndex = classIndex;
        this.nameAndTypeIndex = nameAndTypeIndex;
        this.index = index;
    }

    @Override
    public int getIndex() {
        return index;
    }

    public abstract int getTag();

    @Override
    public String getRawValue() {
        return value;
    }

    public int getClassIndex() {
        return classIndex;
    }

    public int getNameAndTypeIndex() {
        return nameAndTypeIndex;
    }

    @Override
    public List<Byte> toBytes() {
        List<Byte> result = new ArrayList<>();
        result.addAll(NumberToByteConverter.convert(classIndex, 2));
        result.addAll(NumberToByteConverter.convert(nameAndTypeIndex, 2));
        return result;
    }

    @Override
    public int getSize() {
        return 4;
    }
}
