package me.wbars.parser;

import java.util.function.Supplier;

public interface NamedSupplier<T> extends Supplier<T> {
    String getName();
}
