package me.wbars.compiler.semantic.models;

import me.wbars.compiler.parser.models.Tokens;

public class GetPointerNode extends UnaryOpNode {

    public GetPointerNode() {
        super(Tokens.UPARROW);
    }
}
