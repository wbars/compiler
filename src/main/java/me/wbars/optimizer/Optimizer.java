package me.wbars.optimizer;

import me.wbars.semantic.models.ASTNode;

public interface Optimizer {
    boolean traverse(ASTNode node);
}
