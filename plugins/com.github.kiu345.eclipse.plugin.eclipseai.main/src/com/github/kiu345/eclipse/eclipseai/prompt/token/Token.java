package com.github.kiu345.eclipse.eclipseai.prompt.token;

public interface Token {
    public enum MatchType {
        NONE,
        BEGIN,
        END,
        TOGGEL,
        LOCATION
    }

    public MatchType match(String input);

    public MatchType lastResult();

}
