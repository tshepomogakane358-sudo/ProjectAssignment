package com.smartqueue.fx;

public class StyleManager {

    public static final String PRIMARY        = "#1a73e8";
    public static final String PRIMARY_DARK   = "#1558b0";
    public static final String SUCCESS        = "#34a853";
    public static final String DANGER         = "#ea4335";
    public static final String BACKGROUND     = "#f0f4f8";
    public static final String CARD           = "#ffffff";
    public static final String TEXT_PRIMARY   = "#202124";
    public static final String TEXT_SECONDARY = "#5f6368";
    public static final String BORDER         = "#dadce0";

    public static String button(String bg, String hover) {
        return "-fx-background-color: " + bg + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 14px;" +
                "-fx-padding: 10 24 10 24;" +
                "-fx-background-radius: 6;" +
                "-fx-cursor: hand;";
    }

    public static String primaryButton() {
        return button(PRIMARY, PRIMARY_DARK);
    }

    public static String dangerButton() {
        return button(DANGER, "#c62828");
    }

    public static String successButton() {
        return button(SUCCESS, "#2d8e47");
    }

    public static String card() {
        return "-fx-background-color: " + CARD + ";" +
                "-fx-background-radius: 10;" +
                "-fx-padding: 30;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 2);";
    }

    public static String textField() {
        return "-fx-background-color: white;" +
                "-fx-border-color: " + BORDER + ";" +
                "-fx-border-radius: 5;" +
                "-fx-background-radius: 5;" +
                "-fx-padding: 8 12 8 12;" +
                "-fx-font-size: 14px;";
    }

    public static String label(String color, int size, boolean bold) {
        return "-fx-text-fill: " + color + ";" +
                "-fx-font-size: " + size + "px;" +
                (bold ? "-fx-font-weight: bold;" : "");
    }
}
