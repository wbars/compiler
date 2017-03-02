package me.wbars.compiler.generator;

import me.wbars.compiler.semantic.models.LiteralNode;
import me.wbars.compiler.semantic.models.ProcOrFunctionDeclarationNode;
import me.wbars.compiler.semantic.models.types.Type;
import me.wbars.compiler.semantic.models.types.TypeRegistry;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class BytecodeFunctionsUtils {
    private BytecodeFunctionsUtils() {
    }

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

    public static int customMethod(JvmBytecodeGenerator generator, String name, String typeDescriptor) {
        return generator.getConstantPool().getFieldOrMethodIndex(generator.getClassName() + "." + name, typeDescriptor);
    }

    private static String getTypeDescriptor(List<Type> argumentTypes, Type resultType) {
        String argumentsDescriptor = argumentTypes.stream()
                .map(Type::alias)
                .reduce((c, c2) -> c + c2).orElse("");
        return "(" + argumentsDescriptor + ")" + resultType.alias();
    }

    public static int registerMethod(JvmBytecodeGenerator generator, ProcOrFunctionDeclarationNode declaration) {
        List<Type> paramTypes = declaration.getHeading().getParameters().stream()
                .flatMap(parameter -> replicate(parameter.getNameIdentifier().getType(), parameter.getIdentifiers().size()))
                .collect(Collectors.toList());

        String name = declaration.getHeading().getValue();
        LiteralNode resultType = declaration.getHeading().getResultType();
        String typeDescriptor = getTypeDescriptor(paramTypes, resultType != null ? resultType.getType() : TypeRegistry.VOID);
        generator.getConstantPool().getCustomFunctionDescriptorRegistry().register(name, typeDescriptor);
        generator.getConstantPool().getCustomFunctionIndexesRegistry().register(name, types -> BytecodeFunctionsUtils.customMethod(generator, name, typeDescriptor));

        RegistersTable registerTable = new RegistersTable(null, 0);
        declaration.getHeading().getParameters().stream()
                .flatMap(literalParameterNode -> literalParameterNode.getIdentifiers().stream())
                .forEach(l -> registerTable.register(l.getValue()));
        generator.getConstantPool().getCustomFunctionCodeRegistry().register(name, JvmBytecodeGenerator.generateCode(declaration.getBody(), generator.getConstantPool(), registerTable, generator.getClassName()));

        return generator.getConstantPool().getFieldOrMethodIndex(generator.getClassName() + "." + name, typeDescriptor);
    }

    private static <T> Stream<T> replicate(T item, int count) {
        return IntStream.range(0, count).boxed().map(i -> item);
    }
}
