package me.wbars.generator;

import me.wbars.generator.code.*;
import me.wbars.semantic.models.types.Type;
import me.wbars.semantic.models.types.TypeRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.lang.Boolean.parseBoolean;
import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;
import static java.util.Objects.requireNonNull;

public class ConstantPool {
    private List<ConstantInfo> constants = new ArrayList<>();
    private Map<Type, Function<String, ConstantInfo>> typeConstantMapping = new HashMap<>();

    public ConstantPool() {
        registerTypeConstantMapping();
        constants.add(null); //reserved
    }

    private void registerTypeConstantMapping() {
        typeConstantMapping.put(TypeRegistry.INTEGER, (value) -> new IntegerConstantInfo(parseInt(value), constants.size()));
        typeConstantMapping.put(TypeRegistry.BOOLEAN, (value) -> new IntegerConstantInfo(parseBoolean(value) ? 1 : 0, constants.size()));
        typeConstantMapping.put(TypeRegistry.DOUBLE, (value) -> new DoubleConstantInfo(parseDouble(value), constants.size()));
        typeConstantMapping.put(TypeRegistry.LONG, (value) -> new DoubleConstantInfo(parseLong(value), constants.size()));
        typeConstantMapping.put(TypeRegistry.STRING, (value) -> new StringConstantInfo(trimQuotes(value), getConstantIndex(trimQuotes(value), TypeRegistry.UTF8), constants.size()));
        typeConstantMapping.put(TypeRegistry.UTF8, s -> new Utf8ConstantInfo(s, constants.size()));
    }

    private static String trimQuotes(String value) {
        return value.substring(1, value.length() - 1);
    }

    public int getFieldOrMethodIndex(String name, String typeDescriptor) {
        return getOrRegister(() -> registerStaticFieldOrMethod(name, typeDescriptor));
    }

    public int getConstantIndex(String value, Type type) {
        return getOrRegister(() -> registerConstant(value, type));
    }

    private ConstantInfo registerConstant(String value, Type type) {
        if (!typeConstantMapping.containsKey(type)) throw new RuntimeException("Invalid constant type");
        return typeConstantMapping.get(type).apply(value);
    }

    private int getOrRegister(Supplier<ConstantInfo> factoryMethod) {
        //todo figure out tag without eager creating object
        ConstantInfo constantInfo = factoryMethod.get();
        return constants.stream()
                .filter(c -> c != null && c.getRawValue().equals(constantInfo.getRawValue()) && c.getTag() == constantInfo.getTag())
                .findAny().orElseGet(() -> addToPool(constantInfo)).getIndex();
    }

    private <T extends ConstantInfo> T addToPool(T constantInfo) {
        constants.add(requireNonNull(constantInfo));
        return constantInfo;
    }

    private ConstantInfo registerStaticFieldOrMethod(String name, String descriptor) {
        String[] nameParts = name.split("\\.");
        int classIndex = getClass(nameParts[0]);
        int nameAndTypeIndex = getOrRegister(() -> registerNameAndType(nameParts[1], descriptor));
        return descriptor.startsWith("(")
                ? new MethodConstantInfo(getRepresentation(name, descriptor), classIndex, nameAndTypeIndex, constants.size())
                : new FieldConstantInfo(getRepresentation(name, descriptor), classIndex, nameAndTypeIndex, constants.size());
    }

    public int getClass(String name) {
        return getOrRegister(() -> registerClass(name));
    }

    private static String getRepresentation(String name, String descriptor) {
        return name + ":" + descriptor;
    }

    private NameAndTypeConstantInfo registerNameAndType(String name, String typeDescriptor) {
        return new NameAndTypeConstantInfo(
                getRepresentation(name, typeDescriptor),
                getConstantIndex(name, TypeRegistry.UTF8),
                getConstantIndex(typeDescriptor, TypeRegistry.UTF8),
                constants.size()
        );
    }

    private ClassConstantInfo registerClass(String name) {
        return new ClassConstantInfo(
                name,
                getConstantIndex(name, TypeRegistry.UTF8),
                constants.size()
        );
    }

    public int size() {
        return constants.size();
    }

    public List<ConstantInfo> getConstants() {
        return constants;
    }
}
