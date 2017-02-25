package me.wbars.generator;

import me.wbars.semantic.models.ActualParameterNode;
import me.wbars.semantic.models.ProcedureStmtNode;
import me.wbars.semantic.models.types.ArrayType;
import me.wbars.semantic.models.types.TypeRegistry;
import me.wbars.utils.Registry;

public class BuiltInFunctionsRegistry extends Registry<NativeFunction<ProcedureStmtNode>> {
    private final JvmBytecodeGenerator generator;

    BuiltInFunctionsRegistry(JvmBytecodeGenerator generator) {
        this.generator = generator;

        register("len", this::lenFunc);
        register("break", ignored -> breakFunc());
        register("array_push", this::arrayPush);
    }

    private Integer arrayPush(ProcedureStmtNode procedureStmtNode) {
        ActualParameterNode value = procedureStmtNode.getArguments().get(0);
        ActualParameterNode index = procedureStmtNode.getArguments().get(1);
        ActualParameterNode arrayRef = procedureStmtNode.getArguments().get(2);

        generator.addTypedCommand(JvmBytecodeCommandFactory::loadRegister, generator.getRegistersTable().lookupOrRegister(arrayRef.getValue()), arrayRef.getType());
        generator.addTypedGeneratedCommand(JvmBytecodeCommandFactory::loadRegister, index);
        generator.addTypedGeneratedCommand(JvmBytecodeCommandFactory::loadRegister, value);
        generator.addCodeLine(JvmBytecodeCommandFactory.arrayElementStore(value.getFirst().getType()));
        return -1;
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
