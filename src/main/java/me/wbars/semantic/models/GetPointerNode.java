package me.wbars.semantic.models;

import me.wbars.parser.models.Tokens;

public class GetPointerNode extends UnaryOpNode {

    public GetPointerNode() {
        super(Tokens.UPARROW);
    }
}
