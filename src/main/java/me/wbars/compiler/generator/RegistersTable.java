package me.wbars.compiler.generator;

import java.util.HashMap;
import java.util.Map;

public class RegistersTable {
    private final RegistersTable prev;

    private final Map<String, Integer> identifiersRegisters = new HashMap<>();
    private int maxRegister = 0;
    private int forBlockEnd;

    public RegistersTable(RegistersTable prev, int forBlockEnd) {
        this.prev = prev;
        this.forBlockEnd = forBlockEnd;
        if (prev != null) this.maxRegister = prev.maxRegister;
    }

    public Integer register(String identifier) {
        int nextRegister = nextRegister();
        identifiersRegisters.put(identifier, nextRegister);
        return nextRegister;
    }

    public Integer lookupStack(String identifier) {
        Integer currentRegister = identifiersRegisters.get(identifier);
        if (currentRegister != null || prev == null) return currentRegister;
        return prev.lookupStack(identifier);
    }

    public Integer lookupOrRegister(String identifier) {
        Integer register = lookupStack(identifier);
        return register != null ? register : register(identifier);
    }

    public int nextRegister() {
        return maxRegister++;
    }

    public int blockEndIndex() {
        return forBlockEnd;
    }

    public void setForBlockEnd(int forBlockEnd) {
        this.forBlockEnd = forBlockEnd;
    }
}
