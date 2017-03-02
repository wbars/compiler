package me.wbars.compiler.scanner.regexp.models;

public class StateComponent {
    private final State head;
    private final State tail;

    private StateComponent(State head, State tail) {
        this.head = head;
        this.tail = tail;
    }

    public static StateComponent create(State head, State tail) {
        return new StateComponent(head, tail);
    }

    public static StateComponent single(State state) {
        return new StateComponent(state, state);
    }

    public State getHead() {
        return head;
    }

    public State getTail() {
        return tail;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StateComponent that = (StateComponent) o;

        if (!head.equals(that.head)) return false;
        return tail.equals(that.tail);
    }

    @Override
    public int hashCode() {
        int result = head.hashCode();
        result = 31 * result + tail.hashCode();
        return result;
    }
}
