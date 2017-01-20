package me.wbars;

import me.wbars.scanner.models.DfaNode;
import me.wbars.scanner.models.StateComponent;

public class Main {

    public static void main(String[] args) {
        StateComponent abcdf = NFA.parse("a((b|c)*)");
        NFA.toNonEpsilonNfa(abcdf);
        DfaNode dfa = DFA.transfortm(abcdf, NFA.alphabet("abc"));
        System.out.println("Done");
    }
}