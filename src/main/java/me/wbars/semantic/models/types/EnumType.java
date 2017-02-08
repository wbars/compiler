package me.wbars.semantic.models.types;

public class EnumType {
    private final String[] variants;

    EnumType(String[] variants) {
        this.variants = variants;
    }

    public String[] getVariants() {
        return variants;
    }
}
