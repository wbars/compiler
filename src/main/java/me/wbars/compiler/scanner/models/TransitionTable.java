package me.wbars.compiler.scanner.models;

import java.util.*;

import static java.util.Collections.singleton;
import static java.util.Comparator.comparingInt;

public class TransitionTable {
    private final Map<Integer, Map<Character, Integer>> transitions;
    private final Map<PartOfSpeech, Set<Integer>> dfaPos;
    private final Integer startDfa;
    private final Map<Integer, Set<PartOfSpeech>> invertedPos;

    private TransitionTable(Map<Integer, Map<Character, Integer>> transitions, Map<PartOfSpeech, Set<Integer>> dfaPos, Integer startDfa, Map<Integer, Set<PartOfSpeech>> invertedPos) {
        this.transitions = transitions;
        this.dfaPos = dfaPos;
        this.startDfa = startDfa;
        this.invertedPos = invertedPos;
    }

    public static TransitionTable create(Map<Integer, Map<Character, Integer>> transitions, Map<PartOfSpeech, Set<Integer>> dfaPos, Integer startDfa) {
        return new TransitionTable(transitions, dfaPos, startDfa, getInvertedPos(dfaPos));
    }

    private static Map<Integer, Set<PartOfSpeech>> getInvertedPos(Map<PartOfSpeech, Set<Integer>> posMap) {
        Map<Integer, Set<PartOfSpeech>> result = new HashMap<>();
        posMap.forEach((partOfSpeech, states) -> {
            states.forEach(state -> {
                result.merge(state, new HashSet<>(singleton(partOfSpeech)), (pos, pos2) -> {
                    pos.addAll(pos2);
                    return pos;
                });
            });
        });
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

    public Map<Integer, Set<PartOfSpeech>> getInvertedPos() {
        return invertedPos;
    }

    public PartOfSpeech getPos(int state) {
        return invertedPos.getOrDefault(state, Collections.emptySet()).stream()
                .min(comparingInt(PartOfSpeech::getId)).orElse(null);
    }
}
