package me.wbars.editor;

import java.util.function.BiConsumer;

/**
 * First param: token position
 * Second param: text position
 */
interface SourceCodeProcessor extends BiConsumer<Integer, Integer> {}
