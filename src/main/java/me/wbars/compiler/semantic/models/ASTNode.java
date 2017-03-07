package me.wbars.compiler.semantic.models;

import me.wbars.compiler.generator.JvmBytecodeGenerator;
import me.wbars.compiler.parser.models.Tokens;
import me.wbars.compiler.scanner.models.Token;
import me.wbars.compiler.scanner.models.TokenFactory;
import me.wbars.compiler.semantic.models.types.Type;
import me.wbars.compiler.semantic.models.types.TypeRegistry;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Collections.singletonList;
import static me.wbars.compiler.utils.CollectionsUtils.flatten;
import static me.wbars.compiler.utils.CollectionsUtils.merge;

public abstract class ASTNode {
    protected String value;
    protected Type type;
    protected ASTNode parent;
    private TypeRegistry typeRegistry;

    public void setValue(String value) {
        this.value = value;
    }

    public ASTNode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public Type getType() {
        return type;
    }

    protected Type getType(TypeRegistry typeRegistry) {
        return null;
    }

    public final Type getProcessedType(TypeRegistry typeRegistry) {
        this.typeRegistry = typeRegistry;
        if (type == null) {
            type = getType(typeRegistry);
        }
        return type;
    }

    public int generateCode(JvmBytecodeGenerator codeGenerator) {
        throw new UnsupportedOperationException();
    }

    public ASTNode getParent() {
        return parent;
    }

    public void setParent(ASTNode parent) {
        this.parent = parent;
    }

    public ASTNode child(int i) {
        return children().get(i);
    }

    public ASTNode replace(ASTNode node) {
        int thisIndex = IntStream.range(0, parent.children().size()).boxed()
                .filter(i -> parent.child(i) == this).findAny()
                .orElseThrow(IllegalStateException::new);
        parent.replaceChild(thisIndex, node);
        return node;
    }

    protected abstract void replaceChild(int index, ASTNode node);

    public abstract List<ASTNode> children();

    public TypeRegistry getTypeRegistry() {
        return typeRegistry;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName().replaceAll("Node", "") + "(" + value + ")";
    }

    @SuppressWarnings("unused")
    public String pretty() {
        return pretty(0);
    }

    public String pretty(int ident) {
        String result = "|";
        for (int i = 0; i < ident; i++) {
            result += "\t";
        }
        result += "|" + toString();
        if (children() != null) {
            result += children().stream()
                    .filter(Objects::nonNull)
                    .map(child -> child.pretty(ident + 1))
                    .reduce((s, s2) -> s + "\n" + s2).orElse("");

        }
        return result;
    }

    public Map<ASTNode, List<Token>> getNodesTokens() {
        return getNodesTokens(new HashMap<>());
    }

    public Map<ASTNode, List<Token>> getNodesTokens(Map<ASTNode, List<Token>> acc) {
        acc.put(this, tokens());
        if (children() != null) children().stream().filter(Objects::nonNull).forEach(c -> c.getNodesTokens(acc));
        return acc;
    }

    public abstract List<Token> tokens();

    protected List<Token> nestedTokens(List<? extends ASTNode> identifiers, Supplier<Token> delimerSupplier) {
        return identifiers.stream()
                .map(ASTNode::tokens)
                .reduce((t, t2) -> merge(t, singletonList(delimerSupplier.get()), t2))
                .orElse(Collections.emptyList());
    }

    protected List<Token> nestedTokens(List<? extends ASTNode> identifiers) {
        return flatten(identifiers.stream()
                .map(ASTNode::tokens)
                .collect(Collectors.toList()));
    }

    protected List<Token> ifNotEmpty(List<? extends ASTNode> nodes, Function<List<? extends ASTNode>, List<Token>> s) {
        return nodes.isEmpty() ? Collections.emptyList() : s.apply(nodes);
    }


    protected List<Token> nestedStatements(List<? extends ASTNode> nodes) {
        return flatten(nodes.stream()
                .map(n -> merge(n.tokens(), singletonList(TokenFactory.createSemicolon())))
                .collect(Collectors.toList()));
    }

    protected List<Token> compoundStatement(List<? extends ASTNode> nodes) {
        return nodes.size() > 1 ? (
                merge(
                        Collections.singletonList(Token.keyword(Tokens.BEGIN)),
                        nestedStatements(nodes),
                        Collections.singletonList(Token.keyword(Tokens.END))
                )) : nestedStatements(nodes);
    }


    public List<Token> branch(List<ASTNode> branch) {
        return branch.size() > 1 ? compoundStatement(branch) : nestedTokens(branch);
    }
}
