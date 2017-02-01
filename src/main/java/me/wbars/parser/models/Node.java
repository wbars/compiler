package me.wbars.parser.models;

import me.wbars.scanner.models.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    public Node child(int index) {
        return children.get(index);
    }

    public Node head() {
        return child(0);
    }

    public int size() {
        return children.size();
    }

    public Node last() {
        return children.get(children.size() - 1);
    }

    public Node fromEnd(int i) {
        return children.get(children.size() - 1 - i);
    }

    public Node firstNonToken(String partOfSpeech) {
        return children.stream()
                .filter(r -> r.getTerminal() == null || !r.getTerminal().getPos().name.equals(partOfSpeech))
                .findFirst().orElse(null);
    }

    public void removeLastChild() {
        children.remove(children.size() - 1);
    }

    public Optional<Node> firstChildWithName(String name) {
        return children.stream().filter(node -> node.getName().equals(name)).findFirst();
    }

    public boolean isEmpty() {
        return children.isEmpty();
    }

    public Node firstNullToken() {
        return children.stream()
                .filter(r -> r.getTerminal() == null)
                .findFirst().orElse(null);
    }
}
