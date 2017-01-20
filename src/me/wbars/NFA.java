package me.wbars;

import me.wbars.scanner.models.CharacterIterator;
import me.wbars.scanner.models.Ridge;
import me.wbars.scanner.models.State;
import me.wbars.scanner.models.StateComponent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class NFA {
    @FunctionalInterface
    private interface ComponentMapper {
        StateComponent apply(CharacterIterator iterator, StateComponent currentComponent);
    }

    private static final Set<Character> reservedChars = new HashSet<>();
    public static final Map<Character, StateComponent> trivialComponents;
    private static final Map<Character, ComponentMapper> reservedMappers;

    static {
        reservedChars.add('(');
        reservedChars.add(')');
        reservedChars.add('*');
        reservedChars.add('|');
        reservedChars.add('+');
        reservedChars.add('\\');

        trivialComponents = IntStream.concat(IntStream.rangeClosed(65, 90),
                IntStream.concat(IntStream.rangeClosed(97, 122),
                        IntStream.rangeClosed(48, 57)))
                .mapToObj(c -> (char) c)
                .distinct()
                .filter(c -> !reservedChars.contains(c))
                .collect(Collectors.toMap(c -> c, NFA::create));

        reservedMappers = new HashMap<>();
        reservedMappers.put('(', (iterator, currentComponent) -> {
            StateComponent component = extractComponentFromParanthesis(iterator);
            return currentComponent != null ? concat(currentComponent, component) : component;
        });
        reservedMappers.put('|', (iterator, currentComponent) -> {
            StateComponent component = parse(iterator);
            return currentComponent != null ? or(currentComponent, component) : component;
        });
        reservedMappers.put('\\', (iterator, currentComponent) -> {
            char reserved = iterator.next();
            StateComponent component = anyFromRange(getCharRange(reserved));
            return currentComponent != null ? concat(currentComponent, component) : component;
        });
        reservedMappers.put('*', (iterator, currentComponent) -> closure(currentComponent));
        reservedMappers.put('+', (iterator, currentComponent) -> atLeastOnce(currentComponent));
    }

    private static IntStream getCharRange(char macro) {
        if (macro == 'd') return IntStream.rangeClosed(48, 57);
        if (macro == 'w') return IntStream.rangeClosed(97, 122);
        if (macro == 'W') return IntStream.rangeClosed(65, 90);
        return null;
    }

    public static StateComponent parse(String regexp) {
        return parse(new CharacterIterator(regexp));
    }

    private static StateComponent getNextComponent(CharacterIterator iterator, StateComponent currentComponent) {
        char ch = iterator.next();
        if (!reservedChars.contains(ch)) {
            return currentComponent != null ? concat(currentComponent, trivialComponents.get(ch)) : trivialComponents.get(ch);
        }
        if (!reservedMappers.containsKey(ch)) throw new RuntimeException();
        return reservedMappers.get(ch).apply(iterator, currentComponent);
    }

    private static StateComponent anyFromRange(IntStream chars) {
        return chars.boxed()
                .map(c -> trivialComponents.get((char) c.intValue()))
                .reduce(NFA::or).orElseThrow(RuntimeException::new);
    }

    private static StateComponent atLeastOnce(StateComponent component) {
        return concat(component, closure(component));
    }

    private static StateComponent parse(CharacterIterator iterator) {
        StateComponent currentComponent = null;
        while (iterator.hasNext()) {
            currentComponent = getNextComponent(iterator, currentComponent);
        }
        currentComponent.getTail().setTerminal(true);
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


    private static StateComponent create(char ch) {
        State head = State.create();
        State tail = State.create();
        head.addRidge(tail, ch);
        return StateComponent.create(head, tail);
    }

    private static StateComponent concat(StateComponent first, StateComponent second) {
        first.getTail().addEmptyRidge(second.getHead());
        return StateComponent.create(first.getHead(), second.getTail());
    }

    private static StateComponent or(StateComponent first, StateComponent second) {
        State head = State.create();
        State tail = State.create();

        head.addEmptyRidge(first.getHead());
        head.addEmptyRidge(second.getHead());

        first.getTail().addEmptyRidge(tail);
        second.getTail().addEmptyRidge(tail);

        return StateComponent.create(head, tail);
    }

    private static StateComponent closure(StateComponent component) {
        State head = State.create();
        State tail = State.create();

        head.addEmptyRidge(component.getHead());
        head.addEmptyRidge(tail);
        component.getTail().addEmptyRidge(component.getHead());
        component.getTail().addEmptyRidge(tail);

        return StateComponent.create(head, tail);
    }

    public static Set<Ridge> getRidges(StateComponent component) {
        return getStates(component).stream()
                .flatMap(s -> s.getRidges().stream())
                .collect(Collectors.toSet());
    }

    private static Set<State> getStates(StateComponent component) {
        Set<State> result = new HashSet<>();
        dfs(component.getHead(), result, new HashSet<>());
        return result;
    }

    private static void dfs(State state, Set<State> result, Set<State> visitedStates) {
        visitedStates.add(state);
        result.add(state);
        for (Ridge ridge : state.getRidges()) {
            if (!visitedStates.contains(ridge.getTo())) {
                dfs(ridge.getTo(), result, visitedStates);
            }
        }
    }

    public static void toNonEpsilonNfa(StateComponent component) {
        addDirectRidges(component);
        addTerminalStates(component);
        addTransitiveRidges(component);
        removeEmptyRidges(component);
    }

    private static void removeEmptyRidges(StateComponent component) {
        for (Ridge ridge : getRidges(component)) {
            if (ridge.isEmpty()) {
                ridge.remove();
            }
        }
    }

    private static void addTransitiveRidges(StateComponent component) {
        Set<State> states = getStates(component);
        for (State state : states) {
            Set<Ridge> transitiveRidges = new HashSet<>();
            for (Ridge ridge : state.getRidges()) {
                for (Ridge ridge1 : ridge.getTo().getRidges()) {
                    if (ridge.isEmpty() && !ridge1.isEmpty()) {
                        transitiveRidges.add(Ridge.ridge(state, ridge1.getTo(), ridge1.getCh()));
                    }
                }
            }
            transitiveRidges.forEach(r -> state.addRidge(r.getTo(), r.getCh()));
        }
    }

    private static void addTerminalStates(StateComponent component) {
        Set<State> states = getStates(component);
        for (State state : states) {
            if (state.isTerminal()) continue;
            for (Ridge ridge : state.getRidges()) {
                if (ridge.isEmpty() && ridge.getTo().isTerminal()) {
                    state.setTerminal(true);
                }
            }
        }
    }

    private static void addDirectRidges(StateComponent component) {
        Set<State> states = getStates(component);
        Map<State, Set<State>> closures = new HashMap<>();
        for (State state : states) {
            Set<State> accessibleStates = new HashSet<>();
            epsilonDfs(state, accessibleStates, new HashSet<>());
            closures.put(state, accessibleStates);
        }
        closures.forEach((state, closure) -> closure.forEach(state::addEmptyRidge));
    }

    private static void epsilonDfs(State state, Set<State> accessibleStates, Set<State> visited) {
        for (Ridge r : state.getRidges()) {
            if (visited.contains(r.getTo()) || !r.isEmpty()) continue;

            visited.add(r.getTo());
            accessibleStates.add(r.getTo());
            epsilonDfs(r.getTo(), accessibleStates, visited);
        }
    }
}
