package me.wbars.generator;

import me.wbars.semantic.models.ActualParameterNode;
import me.wbars.semantic.models.ProcedureStmtNode;
import me.wbars.semantic.models.types.ArrayType;
import me.wbars.semantic.models.types.TypeRegistry;
import me.wbars.utils.Registry;

import java.util.function.Function;

public class BuiltInFunctionsRegistry extends Registry<Function<ProcedureStmtNode, Integer>> {
    private final JvmBytecodeGenerator generator;

    BuiltInFunctionsRegistry(JvmBytecodeGenerator generator) {
        this.generator = generator;

        register("len", this::lenFunc);
        register("break", ignored -> breakFunc());
    }

    private int lenFunc(ProcedureStmtNode procedureStmtNode) {
        ActualParameterNode onlyParameter = procedureStmtNode.getArguments().get(0);
        if (onlyParameter.getType() instanceof ArrayType) return arrayLength(onlyParameter);
        throw new RuntimeException("Length of " + onlyParameter.getType() + " does not supported");
    }

    private int arrayLength(ActualParameterNode parameter) {
        generator.addTypedGeneratedCommand(JvmBytecodeCommandFactory::loadRegister, parameter);
        generator.addCodeLine(JvmBytecodeCommandFactory.arrayLength());
        return generator.storeInNextRegister(TypeRegistry.INTEGER);
    }

    private int breakFunc() {
        return generator.addCommand(JvmBytecodeCommandFactory::gotoCommand, generator.getOffsetToEndOfBlock());
    }
}
