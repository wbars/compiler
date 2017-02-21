package me.wbars.generator;

import me.wbars.semantic.models.types.Type;
import me.wbars.semantic.models.types.TypeRegistry;
import me.wbars.utils.Registry;

import java.util.List;
import java.util.function.Function;

public class FunctionsRegistry extends Registry<Function<List<Type>, Integer>> {
    private final JvmBytecodeGenerator generator;

    public FunctionsRegistry(JvmBytecodeGenerator generator) {
        this.generator = generator;
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

    private static String getTypeDescriptor(List<Type> argumentTypes, Type resultType) {
        String argumentsDescriptor = argumentTypes.stream()
                .map(FunctionsRegistry::getTypeAlias)
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
