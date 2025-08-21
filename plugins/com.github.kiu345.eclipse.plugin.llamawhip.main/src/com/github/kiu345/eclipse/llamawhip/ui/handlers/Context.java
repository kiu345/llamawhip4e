package com.github.kiu345.eclipse.llamawhip.ui.handlers;

public record Context(
        String fileName,
        String fileContents,
        String selectedContent,
        String selectedItem,
        String selectedItemType,
        String lang,
        int selectedLineFrom,
        int selectedLineTo,
        Marker[] marker) {}
