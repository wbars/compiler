package me.wbars.compiler.semantic.models.types;

import java.util.List;

public class EnumType implements Type {
    private final List<String> variants;

    EnumType(List<String> variants) {
        this.variants = variants;
    }

    public List<String> getVariants() {
        return variants;
    }

    @Override
    public String name() {
        return "Enum";
    }
}
