package me.wbars.compiler.scanner;

import me.wbars.compiler.scanner.models.Token;
import me.wbars.compiler.scanner.models.TransitionTable;
import me.wbars.compiler.utils.MyIterator;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

public class Scanner {
    private static final int INVALID_STATE = -1;
    private final TransitionTable transitionTable;

    public static List<Token> scan(String content, TransitionTable transitionTable) {
        return new Scanner(transitionTable).scan(content);
    }

    public Scanner(TransitionTable transitionTable) {
        this.transitionTable = transitionTable;
    }

    public List<Token> scan(String content) {
        List<Token> result = new ArrayList<>();
        MyIterator<Character> iterator = new MyIterator<>(getChars(content));
        while (iterator.notFinished()) {
            if (isEmpty(iterator.current())) {
                iterator.advance();
                continue;
            }
            Token token = scanWord(iterator);
            if (token == null) throw new RuntimeException(String.valueOf((char) iterator.current()));
            result.add(token);
        }
        return result;
    }

    private Token scanWord(MyIterator<Character> iterator) {
        int state = transitionTable.getStartState();
        String lexeme = "";
        Stack<Integer> states = new Stack<>();
        states.push(INVALID_STATE);

        while (state != INVALID_STATE && iterator.notFinished()) {
            char nextChar = iterator.current();
            lexeme += nextChar;
            if (state > 0 && transitionTable.getPos(state) != null) states.clear();
            states.push(state);
            state = transitionTable.getNextState(state, nextChar);
            iterator.advance();
        }

        while (!states.isEmpty() && lexeme.length() > 0 && transitionTable.getPos(state) == null) {
            state = states.pop();
            lexeme = lexeme.substring(0, lexeme.length() - 1);
            iterator.descent();
        }
        return state != INVALID_STATE && !lexeme.isEmpty() ? new Token(transitionTable.getPos(state), lexeme) : null;
    }

    private static List<Character> getChars(String content) {
        return content.chars().mapToObj(e -> (char) e).collect(Collectors.toList());
    }

    private static boolean isEmpty(char ch) {
        return ch == ' ' || ch == '\n' || ch == '\t';
    }
}
