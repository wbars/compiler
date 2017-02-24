package me.wbars.generator;

import me.wbars.semantic.models.types.Type;
import me.wbars.semantic.models.types.TypeRegistry;
import me.wbars.utils.Registry;

import java.util.List;

public class FunctionsRegistry extends Registry<NativeFunction<List<Type>>> {
    protected JvmBytecodeGenerator generator;

    public FunctionsRegistry(JvmBytecodeGenerator generator) {
        this.generator = generator;
    }

    static String getTypeDescriptor(List<Type> argumentTypes, Type resultType) {
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
