package me.wbars.compiler.generator;

import me.wbars.compiler.utils.ObjectsUtils;

public class CodeLine {
    private final OpCommand command;
    private final Integer argument;

    private CodeLine(OpCommand command, Integer argument) {
        this.command = command;
        this.argument = argument;
    }

    public static CodeLine line(OpCommand command, Integer argument) {
        return new CodeLine(command, argument);
    }

    public static CodeLine line(OpCommand command) {
        return new CodeLine(command, null);
    }

    public OpCommand getCommand() {
        return command;
    }

    public Integer getArgument() {
        return argument;
    }

    @Override
    public String toString() {
        return argument == null ? String.valueOf(command) : ObjectsUtils.spaceConcat(command, argument);
    }
}
