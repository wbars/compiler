package me.wbars;

import me.wbars.scanner.models.CharacterIterator;
import me.wbars.scanner.models.Ridge;
import me.wbars.scanner.models.State;
import me.wbars.scanner.models.StateComponent;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class NFA {
    private static final Set<Character> reservedChars = new HashSet<>();

    static {
        reservedChars.add('(');
        reservedChars.add(')');
        reservedChars.add('*');
        reservedChars.add('|');
    }

    public static StateComponent parse(String regexp) {
        return parse(new CharacterIterator(regexp), getTrivialComponents(regexp));
    }

    private static StateComponent getNextComponent(Map<Character, StateComponent> trivialComponents,
                                                   CharacterIterator iterator,
                                                   StateComponent currentComponent) {
        char ch = iterator.next();
        if (!reservedChars.contains(ch)) {
            StateComponent component = trivialComponents.get(ch);
            return currentComponent != null ? concat(currentComponent, component) : component;
        } else if (ch == '(') {
            StateComponent component = extractComponentFromParanthesis(iterator);
            return currentComponent != null ? concat(currentComponent, component) : component;
        } else if (ch == '|') {
            StateComponent component = parse(iterator, trivialComponents);
            return currentComponent != null ? or(currentComponent, component) : component;
        } else if (ch == '*') {
            return closure(currentComponent);
        }
        return null;
    }

    private static StateComponent parse(CharacterIterator iterator, Map<Character, StateComponent> trivialComponents) {
        StateComponent currentComponent = null;
        while (iterator.hasNext()) {

            currentComponent = getNextComponent(trivialComponents, iterator, currentComponent);
        }
        return currentComponent;
    }

    private static StateComponent extractComponentFromParanthesis(CharacterIterator iterator) {
        int parenthsCounter = 1;
        StringBuilder sb = new StringBuilder();
        while (parenthsCounter != 0 && iterator.hasNext()) {
            char c = iterator.next();
            if (c == '(') parenthsCounter++;
            if (c == ')') parenthsCounter--;
            sb.append(c);
        }
        if (parenthsCounter != 0) return null;
        return parse(sb.deleteCharAt(sb.length() - 1).toString());
    }

    private static Map<Character, StateComponent> getTrivialComponents(String regexp) {
        return regexp.chars().boxed()
                .map(c -> (char) c.intValue())
                .distinct()
                .filter(c -> !reservedChars.contains(c))
                .collect(Collectors.toMap(c -> c, NFA::create));
    }


    private static StateComponent create(char ch) {
        State head = State.nonTerminal();
        State tail = State.nonTerminal();
        head.getRidges().add(Ridge.ridge(tail, ch));
        return StateComponent.create(head, tail);
    }

    private static StateComponent concat(StateComponent first, StateComponent second) {
        first.getTail().getRidges().add(Ridge.empty(second.getHead()));
        return StateComponent.create(first.getHead(), second.getTail());
    }

    private static StateComponent or(StateComponent first, StateComponent second) {
        State head = State.nonTerminal();
        State tail = State.nonTerminal();

        head.getRidges().add(Ridge.empty(first.getHead()));
        head.getRidges().add(Ridge.empty(second.getHead()));

        first.getTail().getRidges().add(Ridge.empty(tail));
        second.getTail().getRidges().add(Ridge.empty(tail));

        return StateComponent.create(head, tail);
    }

    private static StateComponent closure(StateComponent component) {
        State head = State.nonTerminal();
        State tail = State.nonTerminal();

        head.getRidges().add(Ridge.empty(component.getHead()));
        head.getRidges().add(Ridge.empty(tail));

        component.getTail().getRidges().add(Ridge.empty(component.getHead()));
        return StateComponent.create(head, tail);
    }
}
