package me.wbars.compiler.optimizer;

import me.wbars.compiler.semantic.models.ASTNode;
import me.wbars.compiler.semantic.models.BinaryArithmeticOpNode;
import me.wbars.compiler.semantic.models.IdentifierNode;
import me.wbars.compiler.semantic.models.LiteralNode;
import me.wbars.compiler.semantic.models.types.Type;
import me.wbars.compiler.semantic.models.types.TypeRegistry;

public class ConstantFoldingOptimizer implements Optimizer {
    private ASTNode executeTrivialOperation(String operation, ASTNode left, ASTNode right) {
        Type resultingType = left.getTypeRegistry().typeCast(left.getType(), right.getType());
        return new LiteralNode(ArithmeticProcessor.execute(operation, left.getValue(), right.getValue(), resultingType), resultingType);
    }

    public boolean traverse(ASTNode node) {
        if (node instanceof IdentifierNode) return false;
        if (node instanceof LiteralNode) return TypeRegistry.isFoldable(node.getType());
        if (node instanceof BinaryArithmeticOpNode) {
            BinaryArithmeticOpNode binaryOpNode = ((BinaryArithmeticOpNode) node);
            if (traverse(binaryOpNode.getLeft()) && traverse(binaryOpNode.getRight())) {
                node.replace(executeTrivialOperation(node.getValue(), binaryOpNode.getLeft(), binaryOpNode.getRight()));
                return true;
            }
        }
        return false;
    }
}
