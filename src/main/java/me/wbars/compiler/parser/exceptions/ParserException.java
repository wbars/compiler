package me.wbars.compiler.parser.exceptions;

public class ParserException extends RuntimeException {
    public ParserException(String got, String... expected) {
        super("Invalid token! Got: " + got + ". Expected one of: " + String.join(", ", expected));
    }
}
