package me.wbars.editor.quickfix;

import me.wbars.compiler.semantic.models.ASTNode;
import me.wbars.compiler.semantic.models.BinaryOpNode;
import me.wbars.compiler.semantic.models.IdentifierNode;
import me.wbars.compiler.semantic.models.LiteralNode;
import me.wbars.compiler.semantic.models.types.Type;
import me.wbars.compiler.semantic.models.types.TypeRegistry;
import me.wbars.compiler.utils.ObjectsUtils;

public class ConstantFoldingQuickFix implements QuickFix {
    private ASTNode executeTrivialOperation(String operation, ASTNode left, ASTNode right) {
        TypeRegistry typeRegistry = ObjectsUtils.firstNonNull(left.getTypeRegistry(), right.getTypeRegistry());
        Type resultingType = typeRegistry.typeCast(left.getType(), right.getType());
        return new LiteralNode(ArithmeticProcessor.execute(operation, left.getValue(), right.getValue(), resultingType), resultingType);
    }

    public ASTNode apply(ASTNode node) {
        LiteralNode literalNode = ObjectsUtils.tryCast(node, LiteralNode.class);
        if (literalNode != null) return literalNode;

        BinaryOpNode binaryOpNode = ObjectsUtils.tryCast(node, BinaryOpNode.class);
        if (binaryOpNode == null) return null;

        ASTNode left = fold(binaryOpNode.getLeft());
        ASTNode right = fold(binaryOpNode.getRight());
        return right != null ? executeTrivialOperation(node.getValue(), left, right) : left;
    }

    public ASTNode fold(ASTNode node) {
        return isAcceptable(node, true) ? apply(node) : node;
    }

    @Override
    public boolean isAcceptable(ASTNode node) {
        return isAcceptable(node, false);
    }

    private boolean isAcceptable(ASTNode node, boolean considerLiterals) {
        if (node instanceof IdentifierNode) return false;
        if (node instanceof LiteralNode) return considerLiterals && TypeRegistry.isFoldable(node.getType());

        BinaryOpNode binaryOpNode = ObjectsUtils.tryCast(node, BinaryOpNode.class);
        return binaryOpNode != null
                && isAcceptable(binaryOpNode.getLeft(), true)
                && ((binaryOpNode.getRight() == null && considerLiterals) || isAcceptable(binaryOpNode.getRight(), true));
    }
}
