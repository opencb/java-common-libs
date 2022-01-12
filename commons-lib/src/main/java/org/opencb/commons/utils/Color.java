package org.opencb.commons.utils;

public enum Color {

    BLACK("BLACK"),
    RED("RED"),
    GREEN("GREEN"),
    YELLOW("YELLOW"),
    BLUE("BLUE"),
    MAGENTA("MAGENTA"),
    CYAN("CYAN"),
    WHITE("WHITE"),
    DEFAULT("DEFAULT");

    private final String text;

    Color(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
