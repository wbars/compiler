package me.wbars.compiler.generator;

import me.wbars.compiler.semantic.models.ActualParameterNode;
import me.wbars.compiler.semantic.models.ProcedureStmtNode;
import me.wbars.compiler.semantic.models.types.ArrayType;
import me.wbars.compiler.utils.Registry;

public class BuiltInFunctionsRegistry extends Registry<NativeFunction<ProcedureStmtNode>> {
    private final JvmBytecodeGenerator generator;

    BuiltInFunctionsRegistry(JvmBytecodeGenerator generator) {
        this.generator = generator;

        register("len", this::lenFunc);
        register("break", ignored -> breakFunc());
        register("array_push", this::arrayPush);
        register("new_array", this::newArray);
    }

    private Integer newArray(ProcedureStmtNode procedureStmtNode) {
        ActualParameterNode size = procedureStmtNode.getArguments().get(0);
        ActualParameterNode type = procedureStmtNode.getArguments().get(1);
        generator.generateCode(size.getFirst());
        return generator.addCommand(JvmBytecodeCommandFactory::newPrimitiveArray, type.getType().aType());
    }

    private Integer arrayPush(ProcedureStmtNode procedureStmtNode) {
        ActualParameterNode value = procedureStmtNode.getArguments().get(0);
        ActualParameterNode index = procedureStmtNode.getArguments().get(1);
        ActualParameterNode arrayRef = procedureStmtNode.getArguments().get(2);

        generator.generateCode(arrayRef);
        generator.generateCode(index);
        generator.generateCode(value);

        generator.addCodeLine(JvmBytecodeCommandFactory.arrayElementStore(value.getFirst().getType()));
        return -1;
    }

    private int lenFunc(ProcedureStmtNode procedureStmtNode) {
        ActualParameterNode onlyParameter = procedureStmtNode.getArguments().get(0);
        if (onlyParameter.getType() instanceof ArrayType) return arrayLength(onlyParameter);
        throw new RuntimeException("Length of " + onlyParameter.getType() + " does not supported");
    }

    private int arrayLength(ActualParameterNode parameter) {
        generator.generateCode(parameter);
        generator.addCodeLine(JvmBytecodeCommandFactory.arrayLength());
        return -1;
    }

    private int breakFunc() {
        return generator.addCommand(JvmBytecodeCommandFactory::gotoCommand, generator.getOffsetToEndOfBlock());
    }
}
