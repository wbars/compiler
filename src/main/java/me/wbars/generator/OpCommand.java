package me.wbars.generator;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public enum OpCommand {
    ALOAD(0x19, 1),
    INVOKESPECIAL(0xb7, 2),
    RETURN(0xb1),
    GETSTATIC(0xb2, 2),
    LDC_W(0x13, 2),
    LDC2_W(0x14, 2),
    ASTORE(0x3a, 1),
    INVOKEVIRTUAL(0xb6, 2),
    SIPUSH(0x11, 2),
    ISTORE(0x36, 1),
    ILOAD(0x15, 1),
    IADD(0x60),
    IMUL(0x68),
    IDIV(0x6c),
    ISUB(0x64),
    SALOAD(0x35),
    SASTORE(0x56),
    NEWARRAY(0xbc, 1),
    DUP(0x59)
    ;

    private final String mnemonic;
    private final int code;
    private final int argumentsSize;

    private static final Map<String, OpCommand> mnemonicsCommands = Arrays.stream(values())
            .collect(Collectors.toMap(OpCommand::getMnemonic, Function.identity()));

    OpCommand(int code, int argumentsSize) {
        this.mnemonic = name().toLowerCase();
        this.code = code;
        this.argumentsSize = argumentsSize;
    }

    OpCommand(int code) {
        this(code, 0);
    }

    public String getMnemonic() {
        return mnemonic;
    }

    public int getCode() {
        return code;
    }

    public int getArgumentsSize() {
        return argumentsSize;
    }

    public static OpCommand fromMnemonic(String mnemonic) {
        return requireNonNull(mnemonicsCommands.get(mnemonic));
    }

    @Override
    public String toString() {
        return mnemonic;
    }
}
