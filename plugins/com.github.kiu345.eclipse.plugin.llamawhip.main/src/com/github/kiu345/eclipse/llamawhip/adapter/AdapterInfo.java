package com.github.kiu345.eclipse.llamawhip.adapter;

import java.util.Set;

public record AdapterInfo(String name, String description, String url, String version, Set<Type> types) {
    public enum Type {
        CLOUD,
        ONPREM,
        FILE
    }
}
