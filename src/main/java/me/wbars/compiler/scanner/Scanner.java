package me.wbars.compiler.scanner;

import me.wbars.compiler.scanner.models.Token;
import me.wbars.compiler.scanner.models.TransitionTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Scanner {
    private Scanner() {

    }

    public static List<Token> scan(String content, TransitionTable transitionTable) {
        List<Token> result = new ArrayList<>();
        int state;
        Stack<Integer> states = new Stack<>();
        int i = 0;
        while (i < content.length()) {
            char ch = content.charAt(i);
            if (isEmpty(ch)) {
                i++;
                continue;
            }

            state = transitionTable.getStartState();
            String lexeme = "";
            states.clear();
            states.push(-1);

            while (state != -1 && i < content.length()) {
                char nextChar = content.charAt(i);
                lexeme += nextChar;
                if (state > 0 && transitionTable.getPos(state) != null) states.clear();
                states.push(state);
                state = transitionTable.getTransitions().get(state).getOrDefault(nextChar, -1);
                i++;
            }

            while (!states.isEmpty() && lexeme.length() > 0 && transitionTable.getPos(state) == null) {
                state = states.pop();
                lexeme = lexeme.substring(0, lexeme.length() - 1);
                i--;
            }
            if (state < 0 || lexeme.isEmpty()) {
                throw new RuntimeException(String.valueOf(ch));
            }
            result.add(new Token(transitionTable.getPos(state), lexeme));
        }
        return result;
    }

    private static boolean isEmpty(char ch) {
        return ch == ' ' || ch == '\n' || ch == '\t';
    }
}
