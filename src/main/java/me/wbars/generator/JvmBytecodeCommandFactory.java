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
        simpleTypePrefixes.put(TypeRegistry.LONG, "l");
        simpleTypePrefixes.put(TypeRegistry.STRING, "a");
        simpleTypePrefixes.put(TypeRegistry.DOUBLE, "d");
        simpleTypePrefixes.put(TypeRegistry.BOOLEAN, "i");
    }

    public static CodeLine loadRegister(Integer register, Type type) {
          return CodeLine.line(typedCommand(type, "load"), register);
    }

    private static OpCommand typedCommand(Type type, String mnemonic) {
        if (mnemonic.equals("ldc_w")) {
            return type == TypeRegistry.DOUBLE || type == TypeRegistry.LONG ? OpCommand.LDC2_W : OpCommand.LDC_W;
        }

        return OpCommand.fromMnemonic(getTypePrefix(type) + mnemonic);
    }

    private static String getTypePrefix(Type type) {
        if (type instanceof ArrayType) return "a";
        return simpleTypePrefixes.get(type);
    }

    private static boolean isArithmetic(String command) {
        return command.equals("add")
                || command.equals("sub")
                || command.equals("div")
                || command.equals("mul")
                || command.equals("and")
                || command.equals("or")
                ;
    }

    public static CodeLine storeRegister(Integer register, Type type) {
        return CodeLine.line(typedCommand(type, "store"), register);
    }

    public static CodeLine arrayElementStore(Type type) {
        return CodeLine.line(typedCommand(type, "astore"));
    }

    public static CodeLine arrayElementLoad(Type type) {
        return CodeLine.line(typedCommand(type, "aload"));
    }

    public static CodeLine loadConstant(Integer constantIndex, Type type) {
        return CodeLine.line(typedCommand(type, "ldc_w"), constantIndex);
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
            case "&&":
                return CodeLine.line(typedCommand(type, "and"));
            case "||":
                return CodeLine.line(typedCommand(type, "or"));
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

    public static CodeLine returnCommand() {
        return CodeLine.line(OpCommand.RETURN);
    }

    public static CodeLine returnCommand(Type type) {
        return CodeLine.line(typedCommand(type, "return"));
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

    public static CodeLine ifCmpNe(int branchIndex) {
        return CodeLine.line(OpCommand.fromMnemonic("if_icmpne"), branchIndex);
    }

    public static CodeLine gotoCommand(Integer label) {
        return CodeLine.line(OpCommand.GOTO, label);
    }

    public static CodeLine ifCmp(Integer branchIndex) {
        return CodeLine.line(OpCommand.fromMnemonic("if_icmpeq"), branchIndex);
    }

    public static CodeLine ifLessOrEqualThan(Integer branchIndex) {
        return CodeLine.line(OpCommand.fromMnemonic("if_icmple"), branchIndex);
    }

    public static CodeLine ifGreaterOrEqualThan(Integer branchIndex) {
        return CodeLine.line(OpCommand.fromMnemonic("if_icmpge"), branchIndex);
    }

    public static CodeLine ifLess(Integer branchIndex) {
        return CodeLine.line(OpCommand.fromMnemonic("if_icmplt"), branchIndex);
    }

    public static CodeLine ifGreater(Integer branchIndex) {
        return CodeLine.line(OpCommand.fromMnemonic("if_icmpgt"), branchIndex);
    }

    public static CodeLine ifNe(Integer branchIndex) {
        return CodeLine.line(OpCommand.fromMnemonic("ifne"), branchIndex);
    }

    public static CodeLine ifEq(Integer branchIndex) {
        return CodeLine.line(OpCommand.fromMnemonic("ifeq"), branchIndex);
    }

    public static CodeLine arrayLength() {
        return CodeLine.line(OpCommand.ARRAYLENGTH);
    }

    public static CodeLine inc(Integer argument /** todo rename **/) {
        return CodeLine.line(OpCommand.INC, argument);
    }
}
