package me.wbars.parser.models;

import java.util.List;

public class Rule {
    private final String element;
    private final List<List<String>> derivations;
    private final boolean isToken;

    public Rule(String element, List<List<String>> derivations) {
        this.element = element;
        this.derivations = derivations;
        this.isToken = element.toUpperCase().equals(element);
    }

    public String getElement() {
        return element;
    }

    public List<List<String>> getDerivations() {
        return derivations;
    }

    public boolean isToken() {
        return isToken;
    }
}
