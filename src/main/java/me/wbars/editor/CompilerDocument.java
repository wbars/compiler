package me.wbars.editor;

import me.wbars.compiler.Compiler;
import me.wbars.compiler.parser.models.Tokens;
import me.wbars.compiler.scanner.models.PartOfSpeech;
import me.wbars.compiler.scanner.models.Token;
import me.wbars.compiler.semantic.models.ASTNode;
import me.wbars.compiler.utils.Pair;
import me.wbars.compiler.utils.Registry;
import me.wbars.editor.quickfix.ConstantFoldingQuickFix;
import me.wbars.editor.quickfix.QuickFix;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Collections.reverseOrder;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toList;
import static me.wbars.compiler.utils.CollectionsUtils.findSublistIndex;

public class CompilerDocument extends DefaultStyledDocument {
    private final Compiler compiler;
    private final Function<Token, AttributeSet> tokenStyleMapper;
    private final Map<PartOfSpeech, AttributeSet> tokensAttributes = getTokensAttributes();
    private final AttributeSet defaultAttribute = getDefaultAttribute();
    private final Registry<QuickFix> quickFixRegistry = createOptimizeProcessor();
    private final Map<ASTNode, QuickFix> registeredQuickfixes = new HashMap<>();
    private final Map<ASTNode, Pair<Integer, Integer>> nodesPos = new HashMap<>();


    public Registry<QuickFix> createOptimizeProcessor() {
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

    public CompilerDocument(Compiler compiler) {
        this.compiler = compiler;
        tokenStyleMapper = getTokenStyleMapper();
    }

    private Function<Token, AttributeSet> getTokenStyleMapper() {
        return token -> token != null ? tokensAttributes.getOrDefault(token.getPos(), defaultAttribute) : defaultAttribute;
    }

    private Map<PartOfSpeech, AttributeSet> getTokensAttributes() {
        Map<AttributeSet, Set<PartOfSpeech>> reverseTokensAttributes = new HashMap<>();

        AttributeSet keywordColor = coloredAttribute(EditorStyle.keywordColor);
        AttributeSet numberColor = coloredAttribute(EditorStyle.numberColor);
        AttributeSet stringColor = coloredAttribute(EditorStyle.stringColor);

        reverseTokensAttributes.put(keywordColor, setFrom(
                PartOfSpeech.getOrCreate(Tokens.PROGRAM),
                PartOfSpeech.getOrCreate(Tokens.TYPE),
                PartOfSpeech.getOrCreate(Tokens.ARRAY),
                PartOfSpeech.getOrCreate(Tokens.OF),
                PartOfSpeech.getOrCreate(Tokens.VAR),
                PartOfSpeech.getOrCreate(Tokens.PROCEDURE),
                PartOfSpeech.getOrCreate(Tokens.BEGIN),
                PartOfSpeech.getOrCreate(Tokens.ASSIGNMENT),
                PartOfSpeech.getOrCreate(Tokens.WHILE),
                PartOfSpeech.getOrCreate(Tokens.RELOP),
                PartOfSpeech.getOrCreate(Tokens.DO),
                PartOfSpeech.getOrCreate(Tokens.THEN),
                PartOfSpeech.getOrCreate(Tokens.END),
                PartOfSpeech.getOrCreate(Tokens.IF),
                PartOfSpeech.getOrCreate(Tokens.ELSE)
                )
        );
        reverseTokensAttributes.put(numberColor, setFrom(
                PartOfSpeech.getOrCreate(Tokens.UNSIGNED_INTEGER),
                PartOfSpeech.getOrCreate(Tokens.REALNUMBER)
        ));
        reverseTokensAttributes.put(stringColor, setFrom(
                PartOfSpeech.getOrCreate(Tokens.STRING_VAR)
        ));
        return reverseTokensAttributes.entrySet().stream()
                .flatMap(e -> e.getValue().stream().map(token -> new Pair<>(token, e.getKey())))
                .collect(Collectors.toMap(Pair::first, Pair::second));

    }

    private Set<PartOfSpeech> setFrom(PartOfSpeech... tokens) {
        return new HashSet<>(Arrays.asList(tokens));
    }

    private AttributeSet coloredAttribute(Color color) {
        StyleContext cont = StyleContext.getDefaultStyleContext();
        return cont.addAttribute(cont.getEmptySet(), StyleConstants.Foreground, color);
    }

    private AttributeSet coloredBackgroundAttrubute(Color color) {
        StyleContext cont = StyleContext.getDefaultStyleContext();
        return cont.addAttribute(cont.getEmptySet(), StyleConstants.Background, color);
    }

    private void processSource(String text, List<Token> tokens, SourceCodeProcessor highlighter) {

        for (int textPos = 0, tokenPos = 0; textPos < text.length(); textPos++, tokenPos++) {
            textPos = skipSpaces(text, textPos);
            highlighter.accept(tokenPos, textPos);
            textPos += tokens.get(tokenPos).valueLength() - 1;
        }
    }

    private int skipSpaces(String text, int textPos) {
        while (textPos < text.length() && !notWhitespace(text.charAt(textPos))) {
            textPos++;
        }
        return textPos;
    }

    private SourceCodeProcessor createNodesPosProcessor(ASTNode program, List<Token> tokens) {
        Map<Integer, Pair<Integer, ASTNode>> optimizationHighlights = getOptimizationHighlights(program, tokens);
        final int[] startBackgroundPos = {-1};
        final int[] endBackgroundToken = {-1};
        final ASTNode[] currentNode = {null};

        return (tokenPos, currentPos) -> {
            if (startBackgroundPos[0] < 0 && optimizationHighlights.containsKey(tokenPos)) {
                startBackgroundPos[0] = currentPos;
                Pair<Integer, ASTNode> pair = optimizationHighlights.get(tokenPos);
                endBackgroundToken[0] = tokenPos + pair.first();
                currentNode[0] = pair.second();
            } else if (endBackgroundToken[0] > 0 && tokenPos == endBackgroundToken[0]) {
                nodesPos.put(currentNode[0], new Pair<>(startBackgroundPos[0], currentPos));
                startBackgroundPos[0] = -1;
                endBackgroundToken[0] = -1;
            }
        };
    }

    private SourceCodeProcessor createWordHighlightProcessor(List<Token> tokens) {
        return (tokenPos, currentPos) -> {
            Token wordToken = tokens.get(tokenPos);
            setCharacterAttributes(currentPos, wordToken.getValue().length(), tokenStyleMapper.apply(wordToken), false);
        };
    }

    private void highlightSyntax() {
        String text = tryGetText();
        if (text == null) return;
        //        List<Token> tokens = program.getNodesTokens().get(program);
        List<Token> tokens = compiler.getScanner().scan(text);


        registeredQuickfixes.clear();
        nodesPos.clear();

        setCharacterAttributes(0, text.length(), defaultAttribute, false);
        processSource(text, tokens, createWordHighlightProcessor(tokens));
        processSource(text, tokens, createNodesPosProcessor(compiler.getASTNode(text), tokens));
        highlightQuickfixes();
    }

    private void highlightQuickfixes() {
        nodesPos.values()
                .forEach(pair -> setCharacterAttributes(pair.first(), pair.second() - pair.first(), coloredBackgroundAttrubute(Color.lightGray), false));
    }

    private Map<Integer, Pair<Integer, ASTNode>> getOptimizationHighlights(ASTNode node, List<Token> allTokens) {
        Map<Integer, Pair<Integer, ASTNode>> result = new HashMap<>();
        quickFixRegistry.values().forEach(quickFix -> {
            List<ASTNode> allNodes = collectNodes(node, quickFix).stream()
                    .sorted(reverseOrder(comparingInt(value -> value.tokens().size())))
                    .collect(toList());

            List<Pair<Integer, Integer>> highlightedTokensPos = new ArrayList<>();
            allNodes.forEach(n -> registerQuickfix(n, allTokens, quickFix, result, highlightedTokensPos));
        });
        return result;
    }

    private List<ASTNode> collectNodes(ASTNode node, QuickFix quickFix) {
        List<ASTNode> result = new ArrayList<>();
        dfs(node, result, quickFix);
        return result;
    }

    private void dfs(ASTNode node,
                     List<ASTNode> acc,
                     QuickFix quickFix
    ) {
        if (node == null || node.children() == null) return;
        if (quickFix.isAcceptable(node)) {
            acc.add(node);
            return;
        }
        node.children().stream()
                .filter(Objects::nonNull)
                .forEach(child -> dfs(child, acc, quickFix));
    }

    private void registerQuickfix(ASTNode node, List<Token> allTokens, QuickFix quickFix, Map<Integer, Pair<Integer, ASTNode>> optimizationHighlights, List<Pair<Integer, Integer>> highlightedTokensPos) {
        List<Token> tokens = node.tokens();
        optimizationHighlights.put(getNodeTokensStartIndex(allTokens, highlightedTokensPos, tokens), new Pair<>(tokens.size(), node));
        registeredQuickfixes.put(node, quickFix);
        return;
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

    public void runOptimizations() {
        if (registeredQuickfixes.isEmpty()) return;
        Pair<Integer, Integer> nodePos = nodesPos.values().iterator().next();
        runQuickFix(nodePos);
        highlightSyntax();
        runOptimizations();
    }

    public void runSelectedOptimisation(int caretPosition) {
        nodesPos.entrySet().stream()
                .filter(e -> e.getValue().first() <= caretPosition && caretPosition <= e.getValue().second())
                .map(Map.Entry::getValue)
                .findAny().ifPresent(this::runQuickFix);
    }


    public void runQuickFix(Pair<Integer, Integer> nodePos) {
        ASTNode node = nodesPos.entrySet().stream()
                .filter(e -> e.getValue().equals(nodePos))
                .map(Map.Entry::getKey)
                .findAny().orElse(null);
        QuickFix quickFix = registeredQuickfixes.get(node);
        try {
            replace(nodePos.first(), nodePos.second() - nodePos.first(), getText(quickFix.apply(node)), SimpleAttributeSet.EMPTY);
        } catch (BadLocationException ignored) {
        }
    }

    private String getText(ASTNode node) {
        return node.tokens().stream()
                .map(Token::getValue).reduce((s, s2) -> s + " " + s2)
                .orElse("");
    }
}
