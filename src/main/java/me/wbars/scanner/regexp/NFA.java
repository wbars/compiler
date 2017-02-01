package me.wbars.scanner.regexp;

import me.wbars.scanner.regexp.models.CharacterIterator;
import me.wbars.scanner.regexp.models.Ridge;
import me.wbars.scanner.regexp.models.State;
import me.wbars.scanner.regexp.models.StateComponent;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.IntStream.rangeClosed;

public class NFA {
    public static Set<Character> alphabet() {
        return trivialComponents.keySet();
    }

    public static Set<State> getTerminalStates(StateComponent component) {
        return getStates(component).stream()
                .filter(State::isTerminal)
                .collect(Collectors.toSet());
    }

    private interface ComponentMapper {
        StateComponent apply(CharacterIterator iterator, StateComponent currentComponent);

        char getMeta();
    }

    private static final Set<Character> reservedChars = new HashSet<>();
    private static final Map<Character, StateComponent> trivialComponents;
    private static final List<ComponentMapper> reservedMappers;
    private static final Map<Character, String> macros;

    static {
        reservedChars.add('(');
        reservedChars.add(')');
        reservedChars.add('*');
        reservedChars.add('|');
        reservedChars.add('+');
        reservedChars.add('\\');

        trivialComponents = getVisibleAsciiSymbols()
                .mapToObj(c -> (char) c)
                .collect(Collectors.toMap(c -> c, NFA::create));

        macros = new HashMap<>();
        macros.put('d', "0|1|2|3|4|5|6|7|8|9");
        macros.put('w', "a|b|c|d|e|f|g|h|i|j|k|l|m|n|o|p|q|r|s|t|u|v|w|x|y|z|A|B|C|D|E|F|G|H|I|J|K|L|M|N|O|P|Q|R|S|T|U|V|W|X|Y|Z|_");
        macros.put('.', "\\w|\\d|\\s|!|\"|#|$|%|&|\\(|\\)|\\*|\\+|,|\\-|.|/|:|;|<|=|>|?|@|[|\\\\|]|^|_|`|{|\\|}|~");
        macros.put('s', " ");

        reservedMappers = new ArrayList<>();
        reservedMappers.add(new ComponentMapper() {
            @Override
            public StateComponent apply(CharacterIterator iterator, StateComponent currentComponent) {
                char reserved = iterator.next();
                StateComponent component = macros.containsKey(reserved) ? parse(macros.get(reserved)) : create(reserved);
                return currentComponent != null ? concat(currentComponent, component) : component;
            }

            @Override
            public char getMeta() {
                return '\\';
            }
        });

        reservedMappers.add(new ComponentMapper() {
            @Override
            public StateComponent apply(CharacterIterator iterator, StateComponent currentComponent) {
                StateComponent component = extractComponentFromParanthesis(iterator);
                return currentComponent != null ? concat(currentComponent, component) : component;
            }

            @Override
            public char getMeta() {
                return '(';
            }
        });

        reservedMappers.add(new ComponentMapper() {
            @Override
            public StateComponent apply(CharacterIterator iterator, StateComponent currentComponent) {
                return closure(currentComponent);
            }

            @Override
            public char getMeta() {
                return '*';
            }
        });

        reservedMappers.add(new ComponentMapper() {
            @Override
            public StateComponent apply(CharacterIterator iterator, StateComponent currentComponent) {
                return atLeastOnce(currentComponent);
            }

            @Override
            public char getMeta() {
                return '+';
            }
        });

        reservedMappers.add(new ComponentMapper() {
            @Override
            public StateComponent apply(CharacterIterator iterator, StateComponent currentComponent) {
                StateComponent component = parse(iterator);
                return currentComponent != null ? or(currentComponent, component) : component;
            }

            @Override
            public char getMeta() {
                return '|';
            }
        });
    }

    private static IntStream getVisibleAsciiSymbols() {
        return rangeClosed(32, 126);
    }

    private static String orAllChars() {
        return trivialComponents.keySet().stream().map(String::valueOf).reduce((c, c2) -> c + "|" + c2).orElse("");
    }

    public static StateComponent parse(String regexp) {
        return parse(new CharacterIterator(regexp));
    }

    public static StateComponent parseWithoutEpsilon(String regexp) {
        StateComponent component = parse(new CharacterIterator(regexp));
        toNonEpsilonNfa(component);
        return component;
    }

    private static StateComponent getNextComponent(CharacterIterator iterator, StateComponent currentComponent) {
        char ch = iterator.next();
        if (!reservedChars.contains(ch)) {
            return currentComponent != null ? concat(currentComponent, create(ch)) : create(ch);
        }
        return reservedMappers.stream()
                .filter(m -> m.getMeta() == ch)
                .findFirst().orElseThrow(RuntimeException::new)
                .apply(iterator, currentComponent);
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
        getStates(first).forEach(state -> state.setTerminal(false));
        return StateComponent.create(first.getHead(), second.getTail());
    }

    public static StateComponent or(StateComponent first, StateComponent second) {
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

    public static Set<State> getStates(StateComponent component) {
        Set<State> result = new HashSet<>();
        dfs(component.getHead(), result, new HashSet<>());
        return result;
    }

    private static void dfs(State state, Set<State> result, Set<State> visitedStates) {
        visitedStates.add(state);
        result.add(state);
        state.getRidges().stream()
                .filter(r -> !visitedStates.contains(r.getTo()))
                .forEach(r -> dfs(r.getTo(), result, visitedStates));
    }

    public static void toNonEpsilonNfa(StateComponent component) {
        addDirectRidges(component);
        addTerminalStates(component);
        getTransitiveRidges(component);
        removeEmptyRidges(component);
    }

    private static void removeEmptyRidges(StateComponent component) {
        getRidges(component).stream()
                .filter(Ridge::isEmpty)
                .forEach(Ridge::remove);
    }

    private static void getTransitiveRidges(StateComponent component) {
        getStates(component).forEach(state -> getTransitiveRidges(state)
                .forEach(transitiveRidge -> state.addRidge(transitiveRidge.getTo(), transitiveRidge.getCh()))
        );
    }

    private static Set<Ridge> getTransitiveRidges(State state) {
        return state.getRidges().stream()
                .filter(Ridge::isEmpty)
                .flatMap(firstRidge -> firstRidge.getTo().getRidges().stream())
                .filter(secondRidge -> !secondRidge.isEmpty())
                .map(secondRidge -> Ridge.ridge(state, secondRidge.getTo(), secondRidge.getCh()))
                .collect(Collectors.toSet());
    }

    private static void addTerminalStates(StateComponent component) {
        getStates(component).stream()
                .filter(s -> !s.isTerminal() && existsEmptyRidgeToTerminal(s))
                .forEach(s -> s.setTerminal(true));
    }

    private static boolean existsEmptyRidgeToTerminal(State state) {
        return state.getRidges().stream()
                .anyMatch(r -> r.isEmpty() && r.getTo().isTerminal());
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
