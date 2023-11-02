package com.salkcoding.server;

public enum Operator {
    ADD, SUB, MUL, DIV;

    //Get Operator enum if matched
    public static Operator getOperator(String value) {
        return switch (value.toLowerCase()) {
            case "+":
            case "add":
                yield ADD;
            case "-":
            case "sub":
                yield SUB;
            case "*":
            case "mul":
                yield MUL;
            case "/":
            case "div":
                yield DIV;
            default:
                yield null;
        };
    }

    public static boolean isOperator(String value) {
        return getOperator(value) != null;
    }
}
