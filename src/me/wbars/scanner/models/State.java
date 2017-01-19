package me.wbars.scanner.models;

import java.util.HashSet;
import java.util.Set;

public class State {
    private final int id;
    private final boolean terminal;
    private final Set<Ridge> ridges = new HashSet<>();
    private static int counter = 0;

    private State(int id, boolean terminal) {
        this.id = id;
        this.terminal = terminal;
    }

    public static State nonTerminal() {
        return new State(counter++, false);
    }

    public static State terminal() {
        return new State(counter++, true);
    }

    public int getId() {
        return id;
    }

    public boolean isTerminal() {
        return terminal;
    }

    public Set<Ridge> getRidges() {
        return ridges;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        State state = (State) o;

        return id == state.id;
    }

    @Override
    public int hashCode() {
        return id;
    }


}
