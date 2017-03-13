package me.wbars.compiler.scanner.models;

import java.util.*;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singleton;
import static java.util.Comparator.comparingInt;

public class TransitionTable {
    private final Map<Integer, Map<Character, Integer>> transitions;
    private final Map<PartOfSpeech, Set<Integer>> dfaPos;
    private final Integer startDfa;
    private final Map<Integer, Set<PartOfSpeech>> invertedPos;
    private final Map<Integer, Integer> anyTransitions;

    private TransitionTable(Map<Integer, Map<Character, Integer>> transitions, Map<PartOfSpeech, Set<Integer>> dfaPos, Integer startDfa, Map<Integer, Set<PartOfSpeech>> invertedPos, Map<Integer, Integer> anyTransitions) {
        this.transitions = transitions;
        this.dfaPos = dfaPos;
        this.startDfa = startDfa;
        this.invertedPos = invertedPos;
        this.anyTransitions = anyTransitions;
    }

    public static TransitionTable create(Map<Integer, Map<Character, Integer>> transitions, Map<PartOfSpeech, Set<Integer>> dfaPos, Integer startDfa, Map<Integer, Integer> anyTransitions) {
        return new TransitionTable(transitions, dfaPos, startDfa, getInvertedPos(dfaPos), anyTransitions);
    }

    private static Map<Integer, Set<PartOfSpeech>> getInvertedPos(Map<PartOfSpeech, Set<Integer>> posMap) {
        Map<Integer, Set<PartOfSpeech>> result = new HashMap<>();
        posMap.forEach((partOfSpeech, states) -> states.forEach(state -> {
            result.merge(state, new HashSet<>(singleton(partOfSpeech)), (pos, pos2) -> {
                pos.addAll(pos2);
                return pos;
            });
        }));
        return result;
    }

    public Map<Integer, Map<Character, Integer>> getTransitions() {
        return transitions;
    }

    public Map<PartOfSpeech, Set<Integer>> getPosMap() {
        return dfaPos;
    }

    public Integer getStartState() {
        return startDfa;
    }

    public PartOfSpeech getPos(int state) {
        return invertedPos.getOrDefault(state, Collections.emptySet()).stream()
                .min(comparingInt(PartOfSpeech::getId)).orElse(null);
    }

    public Map<Integer, Integer> getAnyTransitions() {
        return anyTransitions;
    }

    public int getNextState(int state, char ch) {
        int transition = transitions.getOrDefault(state, emptyMap()).getOrDefault(ch, -1);
        return transition >= 0 ? transition : anyTransitions.getOrDefault(state, -1);
    }
}
