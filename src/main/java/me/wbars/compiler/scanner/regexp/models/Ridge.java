package me.wbars.compiler.scanner.regexp.models;

public class Ridge {
    private final boolean empty;
    private final boolean any;
    private final char ch;
    private final State to;
    private final State from;

    private Ridge(State from, State to, char ch, boolean empty, boolean any) {
        this.from = from;
        this.to = to;
        this.ch = ch;
        this.empty = empty;
        this.any = any;
    }

    public static Ridge empty(State from, State to) {
        return new Ridge(from, to, ' ', true, false);
    }

    public static Ridge ridge(State from, State to, char ch, boolean any) {
        return new Ridge(from, to, ch, false, any);
    }

    public boolean isEmpty() {
        return empty;
    }

    public char getCh() {
        return ch;
    }

    public State getTo() {
        return to;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Ridge ridge = (Ridge) o;

        if (!to.equals(ridge.to)) return false;
        return from.equals(ridge.from);
    }

    @Override
    public int hashCode() {
        int result = to.hashCode();
        result = 31 * result + from.hashCode();
        return result;
    }

    public State getFrom() {
        return from;
    }

    public void remove() {
        from.getRidges().remove(this);
    }

    public boolean isAny() {
        return any;
    }
}
