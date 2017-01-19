package me.wbars.scanner.models;

public class Ridge {
    private final boolean empty;
    private final char ch;
    private final State state;

    private Ridge(State state, char ch, boolean empty) {
        this.state = state;
        this.ch = ch;
        this.empty = empty;
    }

    public static Ridge empty(State state) {
        return new Ridge(state, ' ', true);
    }

    public static Ridge ridge(State state, char ch) {
        return new Ridge(state, ch, false);
    }

    public boolean isEmpty() {
        return empty;
    }

    public char getCh() {
        return ch;
    }

    public State getState() {
        return state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Ridge ridge = (Ridge) o;

        if (empty != ridge.empty) return false;
        if (ch != ridge.ch) return false;
        return state.equals(ridge.state);
    }

    @Override
    public int hashCode() {
        int result = (empty ? 1 : 0);
        result = 31 * result + (int) ch;
        result = 31 * result + state.hashCode();
        return result;
    }
}
