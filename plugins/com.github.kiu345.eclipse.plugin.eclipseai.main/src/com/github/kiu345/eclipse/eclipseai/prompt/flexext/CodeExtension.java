package com.github.kiu345.eclipse.eclipseai.prompt.flexext;

import org.jetbrains.annotations.NotNull;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.util.data.MutableDataHolder;

public class CodeExtension implements HtmlRenderer.HtmlRendererExtension {

    private CodeExtension() {
    }

    public static CodeExtension create() {
        return new CodeExtension();
    }

    @Override
    public void rendererOptions(@NotNull MutableDataHolder options) {

    }

    @Override
    public void extend(@NotNull HtmlRenderer.Builder htmlRendererBuilder, @NotNull String rendererType) {
        if (htmlRendererBuilder.isRendererType("HTML")) {
            htmlRendererBuilder.nodeRendererFactory(new CodeRenderer.Factory());
        } else if (htmlRendererBuilder.isRendererType("JIRA")) {
        }
    }
}

