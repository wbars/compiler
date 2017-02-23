package me.wbars.generator;

import java.util.function.Function;

/**
 * Second generic parameters denotes last bytecode index of function calling
 * @param <T> argument type
 */
@FunctionalInterface
interface NativeFunction<T> extends Function<T, Integer> {}
