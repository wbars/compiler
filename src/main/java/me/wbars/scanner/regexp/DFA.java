package me.wbars.scanner.regexp;

import me.wbars.scanner.regexp.models.DfaNode;
import me.wbars.scanner.regexp.models.Ridge;
import me.wbars.scanner.regexp.models.State;
import me.wbars.scanner.regexp.models.StateComponent;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;

public class DFA {
    public static DfaNode transform(StateComponent component) {
        Queue<DfaNode> queue = new ArrayDeque<>();
        Set<DfaNode> visitedNodes = new HashSet<>();
        DfaNode startNode = DfaNode.create(new HashSet<>(singletonList(component.getHead())));
        queue.add(startNode);
        visitedNodes.add(startNode);

        Set<Character> alphabet = NFA.alphabet();
        while (!queue.isEmpty()) {
            DfaNode nextNode = queue.poll();
            for (char letter : alphabet) {
                Set<State> accessibleStates = getAccessibleStates(nextNode, letter);
                if (accessibleStates.isEmpty()) continue;

                DfaNode node;
                DfaNode newNode = DfaNode.create(accessibleStates);
                if (!visitedNodes.contains(newNode)) {
                    node = newNode;
                } else {
                    node = visitedNodes.stream().filter(newNode::equals).findAny().orElse(newNode);
                }

                if (!visitedNodes.contains(node)) {
                    visitedNodes.add(node);
                    queue.add(node);
                }
                if (!node.getStates().isEmpty()) {
                    nextNode.getEdges().add(new DfaNode.Edge(letter, node));
                }
            }
        }
        return startNode;
    }


    private static Set<State> getAccessibleStates(DfaNode node, char ridge) {
        return node.getStates().stream()
                .flatMap(s -> s.getRidges().stream())
                .filter(r -> r.getCh() == ridge)
                .map(Ridge::getTo)
                .collect(Collectors.toSet());
    }

    public static Set<DfaNode> getNodes(DfaNode root) {
        Set<DfaNode> result = new HashSet<>();
        dfsDfa(root, result, new HashSet<>());
        return result;
    }

    private static void dfsDfa(DfaNode node, Set<DfaNode> result, Set<DfaNode> visited) {
        result.add(node);
        visited.add(node);
        for (DfaNode.Edge edge : node.getEdges()) {
            if (visited.contains(edge.getNode())) continue;
            dfsDfa(edge.getNode(), result, visited);
        }
    }

    public static Set<DfaNode> getNodesWithAnyOfState(Set<State> states, Set<DfaNode> nodes) {
        return nodes.stream()
                .filter(node -> states.stream().anyMatch(node.getStates()::contains))
                .collect(Collectors.toSet());
    }
}
