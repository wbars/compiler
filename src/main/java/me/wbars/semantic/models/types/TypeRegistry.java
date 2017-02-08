package me.wbars.semantic.models.types;

import java.util.HashMap;
import java.util.Map;

public class TypeRegistry {
    private final Map<String, Type> aliases = new HashMap<>();
    private final Map<String, Type> varTypes = new HashMap<>();

    public static final IntegerType INTEGER = new IntegerType();
    public static final StringType STRING = new StringType();
    public static final BooleanType BOOLEAN = new BooleanType();
    public static final CharType CHAR = new CharType();
    public static final DoubleType DOUBLE = new DoubleType();
    public static final VoidType VOID = new VoidType();

    public static ArrayType createArrayType(Type type, Integer lowerBound, Integer upperBound) {
        return new ArrayType(type, lowerBound, upperBound);
    }

    public static SetType createSetType(Type type) {
        return new SetType(type);
    }

    public static SubrangeType createSubrangeType(String left, String right) {
        return new SubrangeType(left, right);
    }

    public static PointerType createPointerType(Type type) {
        return new PointerType(type);
    }

    public AliasType createAlias(String name, Type type) {
        AliasType alias = new AliasType(name, type);
        aliases.put(name, alias);
        return alias;
    }

    public static EnumType createEnumType(String... variants) {
        return new EnumType(variants);
    }

    public void registerVarType(String identifier, Type type) {
        varTypes.put(identifier, type);
    }
}