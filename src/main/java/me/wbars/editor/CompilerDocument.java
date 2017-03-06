package me.wbars.editor;

import me.wbars.compiler.Compiler;
import me.wbars.compiler.parser.models.Tokens;
import me.wbars.compiler.scanner.models.PartOfSpeech;
import me.wbars.compiler.scanner.models.Token;
import me.wbars.compiler.semantic.models.ASTNode;
import me.wbars.compiler.semantic.models.BinaryOpNode;
import me.wbars.compiler.utils.CollectionsUtils;
import me.wbars.compiler.utils.Pair;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CompilerDocument extends DefaultStyledDocument {
    private final Compiler compiler;
    private final Function<String, AttributeSet> tokenStyleMapper;

    public CompilerDocument(Compiler compiler) {
        this.compiler = compiler;
        tokenStyleMapper = getTokenStyleMapper();
    }

    private Function<String, AttributeSet> getTokenStyleMapper() {
        Map<PartOfSpeech, AttributeSet> tokensAttributes = getTokensAttributes();
        AttributeSet defaultAttribute = coloredAttribute(EditorStyle.fontColor);
        return s -> {
            Token token = compiler.getScanner().scanWord(s);
            return token != null ? tokensAttributes.getOrDefault(token.getPos(), defaultAttribute) : defaultAttribute;
        };
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
                PartOfSpeech.getOrCreate(Tokens.END)
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

    private void highlightSyntax() {
        String text = tryGetText();
        if (text == null) return;
        ASTNode program = compiler.getASTNode(text);
        List<Token> tokens = program.getNodesTokens().get(program);
        HashMap<Integer, Integer> optimizationHighlights = new HashMap<>();
        dfs(program, tokens, optimizationHighlights);

        Iterator<Token> tokenIterator = tokens.iterator();
        int currentToken = 0;
        int startBackgroundPos = -1;
        int endBackgroundToken = -1;

        //todo refactor
        for (int currentPos = 0; currentPos < text.length(); currentPos++) {
            Token token = tokenIterator.next();
            while (!notWhitespace(text.charAt(currentPos))) {
                currentPos++;
                if (currentPos >= text.length()) break;
            }
            int tokenLength = token.getValue().length();

            if (startBackgroundPos < 0 && optimizationHighlights.containsKey(currentToken)) {
                startBackgroundPos = currentPos;
                endBackgroundToken = currentToken + optimizationHighlights.get(currentToken);
            }

            String highlightedWord = text.substring(currentPos, currentPos + tokenLength);
            setCharacterAttributes(currentPos, tokenLength, tokenStyleMapper.apply(highlightedWord), false);
            if (endBackgroundToken > 0 && currentToken == endBackgroundToken) {
                setCharacterAttributes(startBackgroundPos, currentPos - startBackgroundPos, coloredBackgroundAttrubute(Color.pink), false);
                startBackgroundPos = -1;
                endBackgroundToken = -1;
            }
            currentPos += tokenLength - 1;
            currentToken++;
        }

    }

    private void dfs(ASTNode node, List<Token> allTokens, Map<Integer, Integer> optimizationHighlights) {
        if (node instanceof BinaryOpNode && ((BinaryOpNode)node).isWithParens()) {
            List<Token> tokens = node.tokens();
            int sublistIndex = CollectionsUtils.findSublistIndex(allTokens, tokens);
            if (sublistIndex > 0) optimizationHighlights.put(sublistIndex, tokens.size());
            return;
        }
        if (node == null || node.children() == null) return;
        List<ASTNode> children = node.children();
        for (ASTNode child : children) {
            if (child == null) continue;
            dfs(child, allTokens, optimizationHighlights);
        }
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
}
