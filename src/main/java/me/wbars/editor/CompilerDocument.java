package me.wbars.editor;

import me.wbars.compiler.Compiler;
import me.wbars.compiler.scanner.models.PartOfSpeech;
import me.wbars.compiler.scanner.models.Token;
import me.wbars.compiler.semantic.models.ASTNode;
import me.wbars.compiler.utils.MutablePair;
import me.wbars.compiler.utils.ObjectsUtils;
import me.wbars.compiler.utils.Pair;
import me.wbars.compiler.utils.Registry;
import me.wbars.editor.quickfix.ConstantFoldingQuickFix;
import me.wbars.editor.quickfix.QuickFix;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Collections.reverseOrder;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.*;
import static me.wbars.compiler.parser.models.Tokens.*;
import static me.wbars.compiler.utils.CollectionsUtils.findSublistIndex;

/**
 * Class is not thread safe, these fields should be reset before highlighting via {@link #resetState()}
 *
 * @see #registeredNodesTextPos
 * @see #registeredQuickfixes
 * @see #registeredNodesTextPos
 */
public class CompilerDocument extends DefaultStyledDocument {
    private final Compiler compiler;
    private final Function<Token, AttributeSet> tokenStyleMapper;
    private final Map<PartOfSpeech, AttributeSet> tokensAttributes = getTokensAttributes();
    private final AttributeSet defaultAttribute = getDefaultAttribute();
    private final Registry<QuickFix> quickFixRegistry = createOptimizeProcessor();

    private final Map<ASTNode, QuickFix> registeredQuickfixes = new HashMap<>();
    private final Map<ASTNode, Pair<Integer, Integer>> registeredNodesTokenPos = new HashMap<>();
    private final Map<ASTNode, Pair<Integer, Integer>> registeredNodesTextPos = new HashMap<>();
    private static final List<String> keywords = asList(
            PROGRAM,
            TYPE,
            ARRAY,
            OF,
            VAR,
            PROCEDURE,
            BEGIN,
            ASSIGNMENT,
            WHILE,
            RELOP,
            DO,
            THEN,
            END,
            IF,
            ELSE
    );

    private Registry<QuickFix> createOptimizeProcessor() {
        Registry<QuickFix> quickFixRegistry = new Registry<>();
        quickFixRegistry.register("constant folding", new ConstantFoldingQuickFix());
        return quickFixRegistry;
    }

    private AttributeSet getDefaultAttribute() {
        StyleContext cont = StyleContext.getDefaultStyleContext();

        SimpleAttributeSet attr = new SimpleAttributeSet();
        attr.addAttribute(StyleConstants.Foreground, EditorStyle.fontColor);
        attr.addAttribute(StyleConstants.Background, EditorStyle.backgroundColor);
        return cont.addAttributes(cont.getEmptySet(), attr);
    }

    CompilerDocument(Compiler compiler) {
        this.compiler = compiler;
        this.tokenStyleMapper = getTokenStyleMapper();
    }

    private Function<Token, AttributeSet> getTokenStyleMapper() {
        return token -> token != null ? tokensAttributes.getOrDefault(token.getPos(), defaultAttribute) : defaultAttribute;
    }

    private Map<PartOfSpeech, AttributeSet> getTokensAttributes() {
        Map<AttributeSet, Set<PartOfSpeech>> reverseTokensAttributes = new HashMap<>();

        AttributeSet keywordColor = coloredAttribute(EditorStyle.keywordColor);
        AttributeSet numberColor = coloredAttribute(EditorStyle.numberColor);
        AttributeSet stringColor = coloredAttribute(EditorStyle.stringColor);

        reverseTokensAttributes.put(keywordColor, newPartOfSpeechSet(keywords.toArray(new String[]{})));
        reverseTokensAttributes.put(numberColor, newPartOfSpeechSet(
                UNSIGNED_INTEGER,
                REALNUMBER
        ));
        reverseTokensAttributes.put(stringColor, newPartOfSpeechSet(
                STRING_VAR
        ));
        return reverseTokensAttributes.entrySet().stream()
                .flatMap(e -> e.getValue().stream().map(token -> new Pair<>(token, e.getKey())))
                .collect(toMap(Pair::first, Pair::second));

    }

    private Set<PartOfSpeech> newPartOfSpeechSet(String... tokens) {
        return stream(tokens).map(PartOfSpeech::getOrCreate).collect(toSet());
    }

    private AttributeSet coloredAttribute(Color color) {
        return singleAttribute(StyleConstants.Foreground, color);
    }

    private AttributeSet coloredBackgroundAttribute(Color color) {
        return singleAttribute(StyleConstants.Background, color);
    }

    private AttributeSet singleAttribute(Object property, Color color) {
        StyleContext cont = StyleContext.getDefaultStyleContext();
        return cont.addAttribute(cont.getEmptySet(), property, color);
    }

    private void processSource(String text, List<Token> tokens, SourceCodeProcessor highlighter) {
        for (int textPos = 0, tokenPos = 0; textPos < text.length(); textPos++, tokenPos++) {
            textPos = skipSpaces(text, textPos);
            highlighter.accept(tokenPos, textPos);
            textPos += tokens.get(tokenPos).valueLength() - 1;
        }
    }

    private int skipSpaces(String text, int textPos) {
        while (textPos < text.length() && !notWhitespace(text.charAt(textPos))) textPos++;
        return textPos;
    }

    private SourceCodeProcessor createNodesPosProcessor(ASTNode program, List<Token> tokens) {
        Map<Integer, Integer> highlights = getHighlights(program, tokens);
        final MutablePair<Integer, Integer> startPosEndToken = new MutablePair<>(-1, -1);
        final ASTNode[] currentNode = {null};

        BiConsumer<Integer, Integer> startHighlighting = (tokenPos, currentPos) -> {
            int endPos = highlights.get(tokenPos);

            startPosEndToken.setFirst(currentPos);
            startPosEndToken.setSecond(endPos);
            currentNode[0] = findRegisteredNodeByPos(tokenPos, endPos);
        };

        BiConsumer<Integer, Integer> registerHighlighting = (tokenPos, currentPos) -> {
            registeredNodesTextPos.put(currentNode[0], new Pair<>(startPosEndToken.first(), currentPos));

            startPosEndToken.setFirst(-1);
            startPosEndToken.setSecond(-1);
        };

        Predicate<Integer> canStartHighlightingFromPos = tokenPos -> startPosEndToken.first() < 0 && highlights.containsKey(tokenPos);
        Predicate<Integer> canRegisterHighlightAtPos = tokenPos -> startPosEndToken.second() > 0 && tokenPos.equals(startPosEndToken.second());

        return (tokenPos, currentPos) -> {
            if (canStartHighlightingFromPos.test(tokenPos)) {
                startHighlighting.accept(tokenPos, currentPos);
                return;
            }
            if (canRegisterHighlightAtPos.test(tokenPos)) registerHighlighting.accept(tokenPos, currentPos);
        };
    }

    private ASTNode findRegisteredNodeByPos(int startTokenPos, int endTokenPos) {
        Pair<Integer, Integer> pos = new Pair<>(startTokenPos, endTokenPos);
        return registeredNodesTokenPos.entrySet().stream()
                .filter(e -> e.getValue().equals(pos))
                .map(Map.Entry::getKey)
                .findAny().orElseThrow(RuntimeException::new);
    }

    private SourceCodeProcessor createWordHighlightProcessor(List<Token> tokens) {
        return (tokenPos, currentPos) -> {
            if (tokenPos >= tokens.size()) return;

            Token wordToken = tokens.get(tokenPos);
            setCharacterAttributes(currentPos, wordToken.getValue().length(), tokenStyleMapper.apply(wordToken), false);
        };
    }

    private void highlightSyntax() {
        resetState();

        String text = tryGetText();
        if (text == null) return;
        List<Token> tokens = compiler.getScanner().scan(text);

        processSource(text, tokens, createWordHighlightProcessor(tokens));
        processSource(text, tokens, createNodesPosProcessor(compiler.getASTNode(text), tokens));
        highlightQuickfixes();
    }

    private void resetState() {
        registeredQuickfixes.clear();
        registeredNodesTokenPos.clear();
        registeredNodesTextPos.clear();
        setCharacterAttributes(0, getLength(), defaultAttribute, false);
    }

    private void highlightQuickfixes() {
        registeredNodesTextPos.values()
                .forEach(pair -> setCharacterAttributes(pair.first(), pair.second() - pair.first(), coloredBackgroundAttribute(Color.lightGray), false));
    }

    private Map<Integer, Integer> getHighlights(ASTNode node, List<Token> allTokens) {
        Function<QuickFix, Map<Integer, Integer>> getHighlights = quickFix -> {
            List<Pair<Integer, Integer>> highlightedTokensPosAccumulator = new ArrayList<>();
            return collectNodes(node, quickFix::isAcceptable).stream()
                    .sorted(reverseOrder(comparingInt(value -> value.tokens().size())))
                    .map(n -> registerQuickfix(n, allTokens, quickFix, highlightedTokensPosAccumulator))
                    .collect(toMap(Pair::first, Pair::second));
        };
        return quickFixRegistry.values().stream()
                .map(getHighlights)
                .flatMap(highlights -> highlights.entrySet().stream())
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private List<ASTNode> collectNodes(ASTNode node, Predicate<ASTNode> filter) {
        List<ASTNode> result = new ArrayList<>();
        dfs(node, result, filter);
        return result;
    }

    private void dfs(ASTNode node, List<ASTNode> acc, Predicate<ASTNode> until) {
        if (node == null || node.children() == null) return;
        if (until.test(node)) {
            acc.add(node);
            return;
        }
        node.children().stream()
                .filter(Objects::nonNull)
                .forEach(child -> dfs(child, acc, until));
    }

    private Pair<Integer, Integer> registerQuickfix(ASTNode node,
                                                    List<Token> allTokens,
                                                    QuickFix quickFix,
                                                    List<Pair<Integer, Integer>> highlightedTokensPos) {
        List<Token> tokens = node.tokens();
        registeredQuickfixes.put(node, quickFix);

        int nodeTokensStartIndex = getNodeTokensStartIndex(allTokens, highlightedTokensPos, tokens);
        Pair<Integer, Integer> tokenPos = new Pair<>(nodeTokensStartIndex, nodeTokensStartIndex + tokens.size());
        registeredNodesTokenPos.put(node, tokenPos);
        return tokenPos;
    }

    private int getNodeTokensStartIndex(List<Token> allTokens, List<Pair<Integer, Integer>> highlightedTokensPos, List<Token> tokens) {
        int tokensStartIndex = findSublistIndex(allTokens, tokens, highlightedTokensPos);
        highlightedTokensPos.add(new Pair<>(tokensStartIndex, tokensStartIndex + tokens.size()));
        return tokensStartIndex;
    }

    private String tryGetText() {
        try {
            return getText(0, getLength());
        } catch (BadLocationException e) {
            return null;
        }
    }

    public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
        super.insertString(offset, str, a);
        SwingUtilities.invokeLater(this::highlightSyntax);
    }

    private boolean notWhitespace(char c) {
        return String.valueOf(c).matches("\\S");
    }

    public void remove(int offs, int len) throws BadLocationException {
        super.remove(offs, len);
        SwingUtilities.invokeLater(this::highlightSyntax);
    }

    void runOptimizations() {
        if (registeredQuickfixes.isEmpty()) return;
        Pair<Integer, Integer> nodePos = registeredNodesTextPos.values().iterator().next();
        runQuickFix(nodePos);
        highlightSyntax();
        runOptimizations();
    }

    void runSelectedOptimisation(int caretPosition) {
        registeredNodesTextPos.entrySet().stream()
                .filter(e -> e.getValue().first() <= caretPosition && caretPosition <= e.getValue().second())
                .map(Map.Entry::getValue)
                .findAny().ifPresent(this::runQuickFix);
    }


    private void runQuickFix(Pair<Integer, Integer> nodePos) {
        ASTNode node = registeredNodesTextPos.entrySet().stream()
                .filter(e -> e.getValue().equals(nodePos))
                .map(Map.Entry::getKey)
                .findAny().orElse(null);
        QuickFix quickFix = registeredQuickfixes.get(node);
        if (quickFix == null) return;
        try {
            replace(nodePos.first(), nodePos.second() - nodePos.first(), getText(quickFix.apply(node)), SimpleAttributeSet.EMPTY);
        } catch (BadLocationException ignored) {
        }
    }

    private String getText(ASTNode node) {
        return node.tokens().stream()
                .map(Token::getValue)
                .reduce(ObjectsUtils::spaceConcat)
                .orElse("");
    }

    List<String> getCurrentAutocompleteList(int caretPosition) {
        String name = getCurrentChunk(caretPosition);
        return !name.isEmpty() ? getAutocompleteTerms().stream()
                .filter(term -> term.toLowerCase().contains(name.toLowerCase()))
                .map(String::toLowerCase)
                .collect(toList()) : emptyList();
    }

    private Pair<Integer, Integer> getCurrentChunkBounds(String text, int caretPosition) {
        int startPosition = caretPosition;
        while (startPosition >= 0 && notWhitespace(text.charAt(startPosition))) startPosition--;

        int endPosition = caretPosition;
        while (endPosition < text.length() && notWhitespace(text.charAt(endPosition))) endPosition++;

        return new Pair<>(startPosition + 1, endPosition - 1);
    }

    private String getCurrentChunk(int caretPosition) {
        String text = tryGetText();
        if (text == null) return " ";
        Pair<Integer, Integer> bounds = getCurrentChunkBounds(text, caretPosition);
        return text.substring(bounds.first(), bounds.second() + 1);
    }

    private List<String> getAutocompleteTerms() {
        return keywords;
    }

    void replaceCurrentChunk(int caretPosition, String replace) {
        String text = tryGetText();
        if (text == null) return;

        Pair<Integer, Integer> bounds = getCurrentChunkBounds(text, caretPosition);
        try {
            replace(bounds.first(), bounds.second() - bounds.first() + 1, replace, SimpleAttributeSet.EMPTY);
        } catch (BadLocationException ignored) {
        }
    }
}
