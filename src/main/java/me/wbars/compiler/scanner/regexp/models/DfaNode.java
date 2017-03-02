package me.wbars.compiler.scanner.regexp.models;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class DfaNode {
    private Set<DfaNode> nodes;
    private static final Set<DfaNode> allNodes = new HashSet<>();
    private static int counter = 0;
    private final int id;

    public static DfaNode create(Set<State> states) {

        Optional<DfaNode> existingNode = allNodes.stream()
                .filter(n -> n.getStates().equals(states))
                .findAny();
        if (existingNode.isPresent()) return existingNode.get();

        DfaNode node = new DfaNode(states);
        allNodes.add(node);
        return node;
    }

    public Set<State> getStates() {
        return states;
    }

    public Set<Edge> getEdges() {
        return edges;
    }

    public boolean isTerminal() {
        return states.stream().anyMatch(State::isTerminal);
    }

    public int getId() {
        return id;
    }

    public static class Edge {
        private final char ch;
        private final DfaNode node;

        public Edge(char ch, DfaNode node) {
            this.ch = ch;
            this.node = node;
        }

        public char getCh() {
            return ch;
        }

        public DfaNode getNode() {
            return node;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Edge edge = (Edge) o;

            if (ch != edge.ch) return false;
            return node.equals(edge.node);
        }

        @Override
        public int hashCode() {
            int result = (int) ch;
            result = 31 * result + node.hashCode();
            return result;
        }
    }

    private final Set<State> states;
    private final Set<Edge> edges = new HashSet<>();

    private DfaNode(Set<State> states) {
        this.states = states;
        this.id = counter++;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DfaNode dfaNode = (DfaNode) o;

        return states.equals(dfaNode.states);
    }

    @Override
    public int hashCode() {
        return states.hashCode();
    }
}
