package com.football.model;

/**
 * Trend direction for club ranking changes.
 */
public enum TrendDirection {
    UP("+"),
    DOWN("-"),
    STABLE("=");

    private final String symbol;

    TrendDirection(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    public static TrendDirection fromSymbol(String symbol) {
        return switch (symbol) {
            case "+" -> UP;
            case "-" -> DOWN;
            default -> STABLE;
        };
    }
}
