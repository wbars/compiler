package me.wbars.generator;

import me.wbars.semantic.models.types.Type;
import me.wbars.semantic.models.types.TypeRegistry;

import java.util.List;

public class StdLibFunctionsRegistry extends FunctionsRegistry {
    public StdLibFunctionsRegistry(JvmBytecodeGenerator generator) {
        super(generator);
        register("write", this::printMethod);
        register("writeln", this::printLnMethod);
    }

    private int printMethod(List<Type> argumentTypes) {
        int outIndex = generator.getConstantPool().getFieldOrMethodIndex("java/lang/System.out", "Ljava/io/PrintStream;");
        int printIndex = generator.getConstantPool().getFieldOrMethodIndex("java/io/PrintStream.print", getTypeDescriptor(argumentTypes, TypeRegistry.VOID));
        generator.addCommand(JvmBytecodeCommandFactory::getStatic, outIndex);
        return printIndex;
    }

    private int printLnMethod(List<Type> argumentTypes) {
        int outIndex = generator.getConstantPool().getFieldOrMethodIndex("java/lang/System.out", "Ljava/io/PrintStream;");
        int printIndex = generator.getConstantPool().getFieldOrMethodIndex("java/io/PrintStream.println", getTypeDescriptor(argumentTypes, TypeRegistry.VOID));
        generator.addCommand(JvmBytecodeCommandFactory::getStatic, outIndex);
        return printIndex;
    }
}
