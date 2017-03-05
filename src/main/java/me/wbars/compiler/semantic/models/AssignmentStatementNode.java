package me.wbars.compiler.semantic.models;

import me.wbars.compiler.generator.JvmBytecodeGenerator;
import me.wbars.compiler.parser.models.Tokens;
import me.wbars.compiler.scanner.models.Token;

import java.util.Collections;
import java.util.List;

import static me.wbars.compiler.utils.CollectionsUtils.merge;

public class AssignmentStatementNode extends BinaryOpNode {
    public AssignmentStatementNode(ASTNode left, ASTNode right) {
        super("", left, right);
    }

    public AssignmentStatementNode() {
        this(null, null);
    }

    @Override
    public int generateCode(JvmBytecodeGenerator codeGenerator) {
        return codeGenerator.generate(this);
    }

    @Override
    public List<Token> tokens() {
        return merge(
                left.tokens(),
                Collections.singletonList(Token.create(Tokens.ASSIGNMENT, ":=")),
                right.tokens()
        );
    }
}
