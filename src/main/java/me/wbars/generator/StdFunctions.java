package me.wbars.generator;

import me.wbars.semantic.models.types.Type;
import me.wbars.semantic.models.types.TypeRegistry;

import java.util.List;

public class StdFunctions {
    private StdFunctions() {}

    public static int printMethod(JvmBytecodeGenerator generator, List<Type> argumentTypes) {
        int outIndex = generator.getConstantPool().getFieldOrMethodIndex("java/lang/System.out", "Ljava/io/PrintStream;");
        int printIndex = generator.getConstantPool().getFieldOrMethodIndex("java/io/PrintStream.print", getTypeDescriptor(argumentTypes, TypeRegistry.VOID));
        generator.addCommand(JvmBytecodeCommandFactory::getStatic, outIndex);
        return printIndex;
    }

    public static int printLnMethod(JvmBytecodeGenerator generator, List<Type> argumentTypes) {
        int outIndex = generator.getConstantPool().getFieldOrMethodIndex("java/lang/System.out", "Ljava/io/PrintStream;");
        int printIndex = generator.getConstantPool().getFieldOrMethodIndex("java/io/PrintStream.println", getTypeDescriptor(argumentTypes, TypeRegistry.VOID));
        generator.addCommand(JvmBytecodeCommandFactory::getStatic, outIndex);
        return printIndex;
    }

    private static String getTypeDescriptor(List<Type> argumentTypes, Type resultType) {
        String argumentsDescriptor = argumentTypes.stream()
                .map(StdFunctions::getTypeAlias)
                .reduce((c, c2) -> c + c2).orElse("");
        return "(" + argumentsDescriptor + ")" + getTypeAlias(resultType);
    }

    private static String getTypeAlias(Type type) {
        if (type == TypeRegistry.BOOLEAN) return "Z";
        if (type == TypeRegistry.LONG) return "J";
        if (type == TypeRegistry.STRING) return "Ljava/lang/String;";
        return String.valueOf(type.name().toUpperCase().charAt(0));
    }
}
