package com.github.kiu345.eclipse.eclipseai.prompt;

public enum Prompts {
    SYSTEM("system-prompt.txt", "System"),
    DISCUSS("discuss-prompt.txt", "Discuss"),
    DOCUMENT("document-prompt.txt", "Document"),
    FIX_ERRORS("fix-errors-prompt.txt", "Fix Errors"),
    GIT_COMMENT("gitcomment-prompt.txt", "Git Comment"),
    REFACTOR("refactor-prompt.txt", "Refactor"),
    TEST_CASE("testcase-prompt.txt", "JUnit Test case"),
    DISCUSS_SELECTED("discuss-selected.txt", "Discuss Selected code");

    private final String fileName;
    private final String description;

    private Prompts(String fileName, String description) {
        this.fileName = fileName;
        this.description = description;
    }

    public String preferenceName() {
        return "preference.prompt." + name();
    }

    public String getFileName() {
        return fileName;
    }

    public String getDescription() {
        return description;
    }

}
