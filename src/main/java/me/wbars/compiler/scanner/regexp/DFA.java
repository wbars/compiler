package me.wbars.compiler.scanner.regexp;

import me.wbars.compiler.scanner.regexp.models.DfaNode;
import me.wbars.compiler.scanner.regexp.models.Ridge;
import me.wbars.compiler.scanner.regexp.models.State;
import me.wbars.compiler.scanner.regexp.models.StateComponent;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toSet;

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
            alphabet.forEach(letter -> addNewNodeToQueue(queue, visitedNodes, nextNode, getAccessibleStates(nextNode, letter), (node) -> DfaNode.Edge.create(letter, node)));
            addNewNodeToQueue(queue, visitedNodes, nextNode, getAccessibleStatesViaAnyRidge(nextNode), DfaNode.Edge::any);
        }
        return startNode;
    }

    public static void addNewNodeToQueue(Queue<DfaNode> queue, Set<DfaNode> visitedNodes, DfaNode nextNode, Set<State> accessibleStates, Function<DfaNode, DfaNode.Edge> edgeSupplier) {
        if (accessibleStates.isEmpty()) return;

        DfaNode newNode = getOrCreate(visitedNodes, accessibleStates);
        if (!visitedNodes.contains(newNode)) {
            visitedNodes.add(newNode);
            queue.add(newNode);
        }
        if (!newNode.getStates().isEmpty()) {
            nextNode.getEdges().add(edgeSupplier.apply(newNode));
        }
    }

    private static Set<State> getAccessibleStatesViaAnyRidge(DfaNode node) {
        return node.getStates().stream()
                .flatMap(s -> s.getRidges().stream())
                .filter(Ridge::isAny)
                .map(Ridge::getTo)
                .collect(toSet());
    }

    private static DfaNode getOrCreate(Set<DfaNode> visitedNodes, Set<State> accessibleStates) {
        DfaNode newNode = DfaNode.create(accessibleStates);
        return visitedNodes.contains(newNode) ? visitedNodes.stream().filter(newNode::equals).findAny().orElse(newNode) : newNode;
    }


    private static Set<State> getAccessibleStates(DfaNode node, char ridge) {
        return node.getStates().stream()
                .flatMap(s -> s.getRidges().stream())
                .filter(r -> r.getCh() == ridge)
                .map(Ridge::getTo)
                .collect(toSet());
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
                .collect(toSet());
    }
}
