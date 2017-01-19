package me.wbars.scanner.models;

import java.util.Iterator;

public class CharacterIterator implements Iterator<Character> {

    private final String str;
    private int pos = 0;

    public CharacterIterator(String str) {
        this.str = str;
    }

    public boolean hasNext() {
        return pos < str.length();
    }

    public Character next() {
        return str.charAt(pos++);
    }

    public Character peek() {
        return str.charAt(pos + 1);
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}
