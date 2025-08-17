package com.github.kiu345.eclipse.eclipseai.prompt.flexext;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import com.vladsch.flexmark.ast.FencedCodeBlock;
import com.vladsch.flexmark.html.HtmlRendererOptions;
import com.vladsch.flexmark.html.HtmlWriter;
import com.vladsch.flexmark.html.renderer.AttributablePart;
import com.vladsch.flexmark.html.renderer.NodeRenderer;
import com.vladsch.flexmark.html.renderer.NodeRendererContext;
import com.vladsch.flexmark.html.renderer.NodeRendererFactory;
import com.vladsch.flexmark.html.renderer.NodeRenderingHandler;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.sequence.BasedSequence;

public class CodeRenderer implements NodeRenderer {
    final private boolean codeContentBlock;
    final public static AttributablePart CODE_CONTENT = new AttributablePart("FENCED_CODE_CONTENT");

    public CodeRenderer(DataHolder options) {
        codeContentBlock = Parser.FENCED_CODE_CONTENT_BLOCK.get(options);
    }

    @Override
    public Set<NodeRenderingHandler<?>> getNodeRenderingHandlers() {
        HashSet<NodeRenderingHandler<?>> set = new HashSet<>();
        set.add(new NodeRenderingHandler<>(FencedCodeBlock.class, CodeRenderer.this::render));
        return set;
    }

    private String addStartBlock(HtmlWriter html) {
        String codeBlockId = UUID.randomUUID().toString();
        html.append("<input type=\"button\" onClick=\"eclipseCopyCode(document.getElementById('%s').innerText)\" value=\"Copy\" />".formatted(codeBlockId));
        html.append("<input type=\"button\" onClick=\"eclipseSaveCode(document.getElementById('%s').innerText)\" value=\"Save\" />".formatted(codeBlockId));
//        html.append("<input type=\"${showApplyPatch}\" onClick=\"eclipseApplyPatch(document.getElementById('${codeBlockId}').innerText)\" value=\"Apply\"/>".formatted(codeBlockId));
//                """
//                        <input type="button" onClick="eclipseCopyCode(document.getElementById('${codeBlockId}').innerText)" value="Copy" />
//                        <input type="button" onClick="eclipseSaveCode(document.getElementById('${codeBlockId}').innerText)" value="Save" />
//                        <input type="${showApplyPatch}" onClick="eclipseApplyPatch(document.getElementById('${codeBlockId}').innerText)" value="ApplyPatch"/>
//                        <pre style="margin-left: ${indent}pt;"><code lang="${lang}" id="${codeBlockId}">"""
//                        .replace("${indent}", "" + (indent * 5))
//                        .replace("${lang}", lang)
//                        .replace("${codeBlockId}", codeBlockId)
//                        .replace("${showApplyPatch}", "diff".equals(lang) ? "button" : "hidden") // show "Apply Patch" button for diffs
//        );
        return codeBlockId;
    }

    void render(FencedCodeBlock node, NodeRendererContext context, HtmlWriter html) {
        html.line();

        String blockId = addStartBlock(html);

        html.srcPosWithTrailingEOL(node.getChars()).withAttr().tag("pre").attr("id", blockId).openPre();

        BasedSequence info = node.getInfo();
        HtmlRendererOptions htmlOptions = context.getHtmlOptions();
        if (info.isNotNull() && !info.isBlank()) {
            String language = node.getInfoDelimitedByAny(htmlOptions.languageDelimiterSet).unescape();
            String languageClass = htmlOptions.languageClassMap.getOrDefault(language, htmlOptions.languageClassPrefix + language);
            html.attr("class", languageClass);
        }
        else {
            String noLanguageClass = htmlOptions.noLanguageClass.trim();
            if (!noLanguageClass.isEmpty()) {
                html.attr("class", noLanguageClass);
            }
        }
//        html.attr("title", "Test");

        html.srcPosWithEOL(node.getContentChars()).withAttr(CODE_CONTENT).tag("code");
        if (codeContentBlock) {
            context.renderChildren(node);
        }
        else {
            html.text(node.getContentChars().normalizeEOL());
        }
        html.tag("/code");
        html.tag("/pre").closePre();
        html.lineIf(htmlOptions.htmlBlockCloseTagEol);
    }

    public static class Factory implements NodeRendererFactory {
        @NotNull
        @Override
        public NodeRenderer apply(@NotNull DataHolder options) {
            return new CodeRenderer(options);
        }
    }
}
