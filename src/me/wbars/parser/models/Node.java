package me.wbars.parser.models;

import me.wbars.scanner.models.Token;

import java.util.ArrayList;
import java.util.List;

public class Node {
    private final String name;
    private final Token terminal;
    private final Node parent;
    private final List<Node> children;

    public Node(String name, Token terminal, Node parent, List<Node> children) {
        this.name = name;
        this.terminal = terminal;
        this.parent = parent;
        this.children = children;
    }

    public static Node empty(String name) {
        return new Node(name, null, null, new ArrayList<>());
    }


    public static Node terminal(String name, Token terminal) {
        return new Node(name, terminal, null, new ArrayList<>());
    }

    public String getName() {
        return name;
    }

    public Token getTerminal() {
        return terminal;
    }

    public Node getParent() {
        return parent;
    }

    public List<Node> getChildren() {
        return children;
    }

    public boolean isTerminal() {
        return terminal != null;
    }

    public void addChildren(Node node) {
        if (node != null) children.add(node);
    }
}
