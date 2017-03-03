package me.wbars.editor;

import me.wbars.compiler.Compiler;
import me.wbars.compiler.parser.models.Tokens;
import me.wbars.compiler.scanner.models.PartOfSpeech;
import me.wbars.compiler.scanner.models.Token;
import me.wbars.compiler.utils.Pair;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.*;
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

    private AttributeSet coloredAttribute(Color orange) {
        StyleContext cont = StyleContext.getDefaultStyleContext();
        return cont.addAttribute(cont.getEmptySet(), StyleConstants.Foreground, orange);
    }

    private void highlightSyntax() {
        String text = tryGetText();
        if (text == null) return;
        Iterator<Token> tokenIterator = compiler.getScanner().scan(text).iterator();
        for (int currentPos = 0; currentPos < text.length(); currentPos++) {
            Token token = tokenIterator.next();
            while (!notWhitespace(text.charAt(currentPos))) {
                currentPos++;
                if (currentPos >= text.length()) break;
            }
            int tokenLength = token.getValue().length();
            String highlightedWord = text.substring(currentPos, currentPos + tokenLength);
            setCharacterAttributes(currentPos, tokenLength, tokenStyleMapper.apply(highlightedWord), false);
            currentPos += tokenLength - 1;
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
