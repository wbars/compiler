package me.wbars.compiler.scanner.regexp.models;

import java.util.HashSet;
import java.util.Set;

public class State {
    private final int id;
    private boolean terminal;
    private final Set<Ridge> ridges = new HashSet<>();
    private static int counter = 0;

    private State(int id, boolean terminal) {
        this.id = id;
        this.terminal = terminal;
    }

    public static State create() {
        return new State(counter++, false);
    }

    public int getId() {
        return id;
    }

    public boolean isTerminal() {
        return terminal;
    }

    public void setTerminal(boolean terminal) {
        this.terminal = terminal;
    }

    public Set<Ridge> getRidges() {
        return ridges;
    }

    public void addEmptyRidge(State to) {
        ridges.add(Ridge.empty(this, to));
    }

    public void addRidge(State to, char ch) {
        ridges.add(Ridge.ridge(this, to, ch));
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
