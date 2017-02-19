package me.wbars.generator;

import java.util.HashMap;
import java.util.Map;

public class RegistersTable {
    private final RegistersTable prev;

    private final Map<String, Integer> identifiersRegisters = new HashMap<>();
    private int maxRegister = 0;

    public RegistersTable(RegistersTable prev) {
        this.prev = prev;
        if (prev != null) this.maxRegister = prev.maxRegister;
    }

    public RegistersTable getPrev() {
        return prev;
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

    public int current() { return maxRegister; }
}
