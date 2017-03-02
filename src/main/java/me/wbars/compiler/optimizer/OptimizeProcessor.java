package me.wbars.compiler.optimizer;

import me.wbars.compiler.semantic.models.ASTNode;
import me.wbars.compiler.utils.Registry;

import java.util.HashSet;
import java.util.Set;

public class OptimizeProcessor extends Registry<Optimizer> {
    public void process(ASTNode node) {
        for (Optimizer optimizer : values()) {
            Set<ASTNode> visited = new HashSet<>();
            dfs(node, visited, optimizer);
        }
    }

    private void dfs(ASTNode node, Set<ASTNode> visited, Optimizer optimizer) {
        visited.add(node);
        optimizer.traverse(node);
        if (node.children() == null) return;
        for (ASTNode child : node.children()) {
            if (child != null && !visited.contains(child)) dfs(child, visited, optimizer);
        }
    }
}
