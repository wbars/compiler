package me.wbars.compiler.generator.code;

public class FieldConstantInfo extends FieldOrMethodConstantInfo {
    public FieldConstantInfo(String value, int classIndex, int nameAndTypeIndex, int index) {
        super(value, classIndex, nameAndTypeIndex, index);
    }

    @Override
    public int getTag() {
        return 9;
    }
}
