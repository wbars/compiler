package me.wbars.compiler.optimizer;

import me.wbars.compiler.semantic.models.types.Type;
import me.wbars.compiler.semantic.models.types.TypeRegistry;

public class ArithmeticProcessor {
    public static String execute(String operation, String leftValue, String rightValue, Type type) {
        if (type == TypeRegistry.INTEGER) {
            int left = Integer.parseInt(leftValue);
            int right = Integer.parseInt(rightValue);
            if (operation.equals("+")) return String.valueOf(left + right);
            if (operation.equals("-")) return String.valueOf(left - right);
            if (operation.equals("*")) return String.valueOf(left * right);
            if (operation.equals("/")) return String.valueOf(left / right);
        }
        throw new IllegalArgumentException();
    }
}
