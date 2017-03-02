package me.wbars.compiler.generator.code;

import java.util.List;

public interface ConstantInfo {
    int getIndex();
    int getTag();
    String getRawValue();

    List<Byte> toBytes();

    int getSize();
}
