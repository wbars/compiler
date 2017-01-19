package me.wbars;

import me.wbars.scanner.models.StateComponent;

public class Main {

    public static void main(String[] args) {
        StateComponent abcdf = NFA.parse("a(b*)");
        System.out.println("Done");
    }
}
