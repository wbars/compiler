package me.wbars.scanner.models;

import java.util.HashSet;
import java.util.Set;

public class DfaNode {
    public Set<State> getStates() {
        return states;
    }

    public Set<Edge> getEdges() {
        return edges;
    }

    public boolean isTerminal() {
        return states.stream().anyMatch(State::isTerminal);
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

    public DfaNode(Set<State> states) {
        this.states = states;
    }

    public static int computeHashCode(Set<State> states) {
        return new DfaNode(states).hashCode();
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
