package me.wbars.compiler.optimizer;

import me.wbars.compiler.semantic.models.ASTNode;

public interface Optimizer {
    boolean traverse(ASTNode node);
}
