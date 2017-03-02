package me.wbars.compiler.semantic.models;

public class GetFieldNode extends UnaryOpNode {
    public GetFieldNode(String fieldName) {
        super(fieldName);
    }
}
