package me.wbars.generator.code;

import me.wbars.generator.CodeLine;
import me.wbars.generator.ConstantPool;

import java.util.List;

public class GeneratedCode {
    private final List<CodeLine> lines;
    private final ConstantPool constantPool;

    public GeneratedCode(List<CodeLine> lines, ConstantPool constantPool) {
        this.lines = lines;
        this.constantPool = constantPool;
    }

    public List<CodeLine> getLines() {
        return lines;
    }

    public ConstantPool getConstantPool() {
        return constantPool;
    }

    public int getMaxStack() {
        return 100;
    }

    public int getMaxLocals() {
        return 100;
    }
}
