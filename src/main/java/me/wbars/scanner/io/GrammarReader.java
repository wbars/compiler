package me.wbars.scanner.io;

import me.wbars.scanner.models.PartOfSpeech;
import me.wbars.scanner.models.TransitionTable;
import me.wbars.scanner.regexp.DFA;
import me.wbars.scanner.regexp.NFA;
import me.wbars.scanner.regexp.models.DfaNode;
import me.wbars.scanner.regexp.models.StateComponent;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;

public abstract class GrammarReader {
    abstract public String getGrammarContent();

    private Map<PartOfSpeech, StateComponent> nfaGrammars() {
        return stream(getGrammarContent().split("\n"))
                .filter(s -> !s.isEmpty() && !s.startsWith("#"))
                .map(s -> s.split(" ", 2))
                .collect(Collectors.toMap(
                        s -> PartOfSpeech.getOrCreate(s[0].trim()),
                        s -> NFA.parseWithoutEpsilon(s[1].trim()))
                );
    }

    public final TransitionTable readTable() {
        Map<PartOfSpeech, StateComponent> grammars = nfaGrammars();
        StateComponent orComponent = grammars.values().stream().reduce(NFA::or).orElse(null);
        NFA.toNonEpsilonNfa(orComponent);

        DfaNode dfa = DFA.transform(orComponent);
        Set<DfaNode> nodes = DFA.getNodes(dfa);
        Map<PartOfSpeech, Set<Integer>> dfaPos = grammars.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> toInts(DFA.getNodesWithAnyOfState(NFA.getTerminalStates(e.getValue()), nodes)))
                );
        return TransitionTable.create(transitionsToInts(getTransitions(nodes)), dfaPos, toInt(dfa));
    }

    private static Map<DfaNode, Map<Character, DfaNode>> getTransitions(Set<DfaNode> nodes) {
        return nodes.stream()
                .collect(Collectors.toMap(Function.identity(), GrammarReader::getTransitions));
    }

    private static Map<Character, DfaNode> getTransitions(DfaNode node) {
        return node.getEdges().stream()
                .collect(Collectors.toMap(DfaNode.Edge::getCh, DfaNode.Edge::getNode));
    }

    private Map<Integer, Map<Character, Integer>> transitionsToInts(Map<DfaNode, Map<Character, DfaNode>> transitions) {
        return transitions.entrySet().stream()
                .collect(Collectors.toMap(e -> toInt(e.getKey()), e -> edgesToInts(e.getValue())));
    }

    private Map<Character, Integer> edgesToInts(Map<Character, DfaNode> edges) {
        return edges.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, o -> toInt(o.getValue())));
    }

    private int toInt(DfaNode node) {
        return node.getId();
    }

    private Set<Integer> toInts(Set<DfaNode> nodes) {
        return nodes.stream()
                .map(this::toInt)
                .collect(Collectors.toSet());
    }
}
