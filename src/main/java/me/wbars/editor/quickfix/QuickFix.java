package me.wbars.editor.quickfix;

import me.wbars.compiler.semantic.models.ASTNode;

public interface QuickFix {
    ASTNode apply(ASTNode node);
    boolean isAcceptable(ASTNode node);
}
