package me.wbars;

import me.wbars.scanner.models.DfaNode;
import me.wbars.scanner.models.Ridge;
import me.wbars.scanner.models.State;
import me.wbars.scanner.models.StateComponent;

import java.util.*;

import static java.util.Collections.singletonList;

public class DFA {
    public static DfaNode transfortm(StateComponent component, Set<Character> alphabet) {
        Queue<DfaNode> queue = new ArrayDeque<>();
        Map<Integer, DfaNode> visitedNodes = new HashMap<>();
        DfaNode startNode = new DfaNode(new HashSet<>(singletonList(component.getHead())));
        queue.add(startNode);
        visitedNodes.put(startNode.hashCode(), startNode);

        while (!queue.isEmpty()) {
            DfaNode nextNode = queue.poll();
            for (char letter : alphabet) {
                Set<State> accessibleStates = getAccessibleStates(nextNode, letter);
                if (accessibleStates.isEmpty()) continue;

                DfaNode node = visitedNodes.getOrDefault(DfaNode.computeHashCode(accessibleStates), new DfaNode(accessibleStates));
                if (!visitedNodes.containsKey(DfaNode.computeHashCode(accessibleStates))) {
                    queue.add(node);
                    visitedNodes.put(node.hashCode(), node);
                }
                if (!node.getStates().isEmpty()) {
                    nextNode.getEdges().add(new DfaNode.Edge(letter, node));
                }
            }
        }
        return startNode;
    }


    private static Set<State> getAccessibleStates(DfaNode node, char ridge) {
        Set<State> accessibleStates = new HashSet<>();
        for (State state : node.getStates()) {
            Set<State> visited = new HashSet<>();
            dfs(state, ridge, accessibleStates, visited);
        }
        return accessibleStates;
    }

    private static void dfs(State state, char ridge, Set<State> accessibleStates, Set<State> visited) {
        for (Ridge r : state.getRidges()) {
            if (visited.contains(r.getTo()) || r.getCh() != ridge) continue;
            visited.add(r.getTo());
            accessibleStates.add(r.getTo());
            dfs(r.getTo(), ridge, accessibleStates, visited);

        }
    }
}
