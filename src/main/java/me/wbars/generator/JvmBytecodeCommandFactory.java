package me.wbars.generator;

import me.wbars.semantic.models.types.Type;
import me.wbars.semantic.models.types.TypeRegistry;

public class JvmBytecodeCommandFactory {
    private JvmBytecodeCommandFactory() {
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
        if (type == TypeRegistry.BOOLEAN) return "i";
        if (type == TypeRegistry.STRING) return "a";
        return String.valueOf(type.name().toLowerCase().charAt(0));
    }

    static CodeLine storeRegister(Integer register, Type type) {
        return CodeLine.line(typedCommand(type, "store"), register);
    }

    static CodeLine arrayElementStore(Type type) {
        return CodeLine.line(typedCommand(type, "astore"));
    }

    static CodeLine arrayElementLoad(Type type) {
        return CodeLine.line(typedCommand(type, "aload"));
    }

    static CodeLine loadConstant(Integer constantIndex, Type type) {
        return CodeLine.line(typedCommand(type, "ldc_w"), constantIndex);
    }

    static CodeLine arithmeticOperation(String value, Type type) {
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

    static CodeLine getStatic(Integer index) {
        return CodeLine.line(OpCommand.GETSTATIC, index);
    }

    static CodeLine invokeVirtual(Integer index) {
        return CodeLine.line(OpCommand.INVOKEVIRTUAL, index);
    }

    static CodeLine returnCommand() {
        return CodeLine.line(OpCommand.RETURN);
    }

    static CodeLine returnCommand(Type type) {
        return CodeLine.line(typedCommand(type, "return"));
    }

    static CodeLine invokeSpecial(Integer index) {
        return CodeLine.line(OpCommand.INVOKESPECIAL, index);
    }

    static CodeLine newPrimitiveArray(Integer aType) {
        return CodeLine.line(OpCommand.NEWARRAY, aType);
    }

    static CodeLine dup() {
        return CodeLine.line(OpCommand.DUP);
    }

    static CodeLine ifCmpNe(int branchIndex) {
        return CodeLine.line(OpCommand.fromMnemonic("if_icmpne"), branchIndex);
    }

    static CodeLine gotoCommand(Integer label) {
        return CodeLine.line(OpCommand.GOTO, label);
    }

    static CodeLine ifCmp(Integer branchIndex) {
        return CodeLine.line(OpCommand.fromMnemonic("if_icmpeq"), branchIndex);
    }

    static CodeLine ifLessOrEqualThan(Integer branchIndex) {
        return CodeLine.line(OpCommand.fromMnemonic("if_icmple"), branchIndex);
    }

    static CodeLine ifGreaterOrEqualThan(Integer branchIndex) {
        return CodeLine.line(OpCommand.fromMnemonic("if_icmpge"), branchIndex);
    }

    static CodeLine ifLess(Integer branchIndex) {
        return CodeLine.line(OpCommand.fromMnemonic("if_icmplt"), branchIndex);
    }

    static CodeLine ifGreater(Integer branchIndex) {
        return CodeLine.line(OpCommand.fromMnemonic("if_icmpgt"), branchIndex);
    }

    static CodeLine ifNe(Integer branchIndex) {
        return CodeLine.line(OpCommand.fromMnemonic("ifne"), branchIndex);
    }

    static CodeLine ifEq(Integer branchIndex) {
        return CodeLine.line(OpCommand.fromMnemonic("ifeq"), branchIndex);
    }

    static CodeLine arrayLength() {
        return CodeLine.line(OpCommand.ARRAYLENGTH);
    }

    static CodeLine inc(int variable, int incrementValue) {
        return CodeLine.line(OpCommand.INC, ((variable & 0xFF) << 8) | (incrementValue & 0xFF));
    }

    static CodeLine invokeStatic(Integer methodIndex) {
        return CodeLine.line(OpCommand.INVOKESTATIC, methodIndex);
    }
}
