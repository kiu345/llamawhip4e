package com.github.kiu345.eclipse.eclipseai.prompt.token;

public class ContextToken implements Token {
    public static final String START_TOKEN = "<|ContextStart|>";
    public static final String END_TOKEN = "<|ContextEnd|>";

    private MatchType lastResult = MatchType.NONE;

    @Override
    public MatchType match(String input) {
        if (START_TOKEN.equals(input)) {
            lastResult = MatchType.BEGIN;
            return MatchType.BEGIN;
        }

        if (END_TOKEN.equals(input)) {
            lastResult = MatchType.END;
            return MatchType.END;
        }

        lastResult = MatchType.NONE;
        return MatchType.NONE;

    }

    public MatchType lastResult() {
        return lastResult;
    }

}
