package com.github.kiu345.eclipse.llamawhip.config;

public enum UITheme {
    LIGHT,
    DARK;

    public String title() {
        return switch (this) {
            case DARK -> "Dark";
            case LIGHT -> "Light";
            default -> "Undefined";
        };
    }

    public String[] cssFiles() {
        return switch (this) {
            case DARK -> new String[] {  "textview.dark.css", "dark.min.css" };
            case LIGHT -> new String[] {  "textview.css", "hjthemes.css" };
            default -> new String[] {  "textview.css", "hjthemes.css" }; // fallback, we always have some CSS
        };
    }
}
