package me.wbars.compiler.generator.code;

public class MethodConstantInfo extends FieldOrMethodConstantInfo {
    public MethodConstantInfo(String value, int classIndex, int nameAndTypeIndex, int index) {
        super(value, classIndex, nameAndTypeIndex, index);
    }

    @Override
    public int getTag() {
        return 10;
    }
}
