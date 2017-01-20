package me.wbars;

import me.wbars.scanner.models.DfaNode;
import me.wbars.scanner.models.StateComponent;

public class Main {

    public static void main(String[] args) {
        StateComponent abcdf = NFA.parse("((b|c)+)");
        NFA.toNonEpsilonNfa(abcdf);
        DfaNode dfa = DFA.transform(abcdf);
        System.out.println("Done");
    }
}
