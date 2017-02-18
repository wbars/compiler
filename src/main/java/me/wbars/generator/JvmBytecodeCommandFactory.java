package me.wbars.generator;

import me.wbars.semantic.models.types.ArrayType;
import me.wbars.semantic.models.types.Type;
import me.wbars.semantic.models.types.TypeRegistry;

import java.util.HashMap;
import java.util.Map;

public class JvmBytecodeCommandFactory {
    private JvmBytecodeCommandFactory() {
    }

    private static final Map<Type, String> simpleTypePrefixes;

    static {
        simpleTypePrefixes = new HashMap<>();
        simpleTypePrefixes.put(TypeRegistry.INTEGER, "i");
        simpleTypePrefixes.put(TypeRegistry.SHORT, "s");
        simpleTypePrefixes.put(TypeRegistry.LONG, "l");
        simpleTypePrefixes.put(TypeRegistry.STRING, "a");
        simpleTypePrefixes.put(TypeRegistry.DOUBLE, "d");
    }

    public static CodeLine loadRegister(Integer register, Type type) {
        return CodeLine.line(typedCommand(type, "load"), register);
    }

    private static OpCommand typedCommand(Type type, String mnemonic) {
        if (mnemonic.equals("ldc_w")) {
            return type == TypeRegistry.DOUBLE || type == TypeRegistry.LONG ? OpCommand.LDC2_W : OpCommand.LDC_W;
        }
        if (mnemonic.equals("ipush")) return OpCommand.SIPUSH; //todo handle byte
        if (isArithmetic(mnemonic) && type == TypeRegistry.SHORT) return typedCommand(TypeRegistry.INTEGER, mnemonic);

        if (mnemonic.equals("store") && type == TypeRegistry.SHORT) return OpCommand.ISTORE;
        if (mnemonic.equals("load") && type == TypeRegistry.SHORT) return OpCommand.ILOAD;

        return OpCommand.fromMnemonic(getTypePrefix(type) + mnemonic);
    }

    private static String getTypePrefix(Type type) {
        if (type instanceof ArrayType) return "a";
        return simpleTypePrefixes.get(type);
    }

    private static boolean isArithmetic(String command) {
        return command.equals("add") || command.equals("sub") || command.equals("div") || command.equals("mul");
    }

    public static CodeLine storeRegister(Integer register, Type type) {
        return CodeLine.line(typedCommand(type, "store"), register);
    }

    public static CodeLine arrayElementStore(Type type) {
        return CodeLine.line(typedCommand(type, "astore"));
    }

    public static CodeLine loadConstant(Integer constantIndex, Type type) {
        return CodeLine.line(typedCommand(type, "ldc_w"), constantIndex);
    }

    public static CodeLine pushValue(Integer value, Type type) {
        return CodeLine.line(typedCommand(type, "ipush"), value);
    }

    public static CodeLine arithmeticOperation(String value, Type type) {
        switch (value) {
            case "+":
                return CodeLine.line(typedCommand(type, "add"));
            case "-":
                return CodeLine.line(typedCommand(type, "sub"));
            case "/":
                return CodeLine.line(typedCommand(type, "div"));
            case "*":
                return CodeLine.line(typedCommand(type, "mul"));
            default:
                throw new RuntimeException();
        }
    }

    public static CodeLine getStatic(Integer index) {
        return CodeLine.line(OpCommand.GETSTATIC, index);
    }

    public static CodeLine invokeVirtual(Integer index) {
        return CodeLine.line(OpCommand.INVOKEVIRTUAL, index);
    }

    public static CodeLine returnCommand(Integer ignored) {
        return CodeLine.line(OpCommand.RETURN);
    }

    public static CodeLine invokeSpecial(Integer index) {
        return CodeLine.line(OpCommand.INVOKESPECIAL, index);
    }

    public static CodeLine newPrimitiveArray(Integer aType) {
        return CodeLine.line(OpCommand.NEWARRAY, aType);
    }

    public static CodeLine dup() {
        return CodeLine.line(OpCommand.DUP);
    }
}
