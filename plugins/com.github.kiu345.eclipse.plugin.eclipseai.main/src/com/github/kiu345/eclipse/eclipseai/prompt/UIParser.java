package com.github.kiu345.eclipse.eclipseai.prompt;

import java.util.Arrays;
import java.util.Stack;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import com.github.kiu345.eclipse.eclipseai.prompt.flexext.CodeExtension;
import com.github.kiu345.eclipse.eclipseai.prompt.token.Token;
import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ast.Link;
import com.vladsch.flexmark.ext.definition.DefinitionExtension;
import com.vladsch.flexmark.ext.emoji.EmojiExtension;
import com.vladsch.flexmark.ext.footnotes.FootnoteExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.ins.InsExtension;
import com.vladsch.flexmark.ext.plantuml.PlantUmlExtension;
import com.vladsch.flexmark.ext.superscript.SuperscriptExtension;
import com.vladsch.flexmark.ext.tables.TableBlock;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.toc.TocExtension;
import com.vladsch.flexmark.html.AttributeProvider;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.html.IndependentAttributeProviderFactory;
import com.vladsch.flexmark.html.renderer.AttributablePart;
import com.vladsch.flexmark.html.renderer.LinkResolverContext;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.ParserEmulationProfile;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import com.vladsch.flexmark.util.html.MutableAttributes;


public abstract class UIParser {

    protected final Stack<Token> tokenStack = new Stack<>();

    public static String escapeBackSlashes(String input) {
        return input.replace("\\", "\\\\");
    }

    public static String markdown(String input) {
        MutableDataSet options = new MutableDataSet();
        options.set(
                Parser.EXTENSIONS, Arrays.asList(
                        CodeExtension.create(),
                        DefinitionExtension.create(),
                        EmojiExtension.create(),
                        FootnoteExtension.create(),
                        InsExtension.create(),
                        PlantUmlExtension.create(),
                        SuperscriptExtension.create(),
                        StrikethroughExtension.create(),
                        TablesExtension.create(),
                        TocExtension.create()
                )
        );

        options.set(Parser.PARSER_EMULATION_PROFILE, ParserEmulationProfile.GITHUB_DOC);

        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options)
                .attributeProviderFactory(createAttributeProviderFactory())
                .escapeHtml(true)
                .build();

        Node document = parser.parse(input);
        String html = renderer.render(document);

        return html;
    }

    /*
     * open links in new window
     */
    public static IndependentAttributeProviderFactory createAttributeProviderFactory() {
        return new IndependentAttributeProviderFactory() {
            @Override
            public AttributeProvider apply(LinkResolverContext context) {
                return new AttributeProvider() {
                    @Override
                    public void setAttributes(@NotNull Node node, @NotNull AttributablePart part, @NotNull MutableAttributes attributes) {
                        if (node instanceof Link && part == AttributablePart.LINK) {
                            attributes.replaceValue("target", "_blank");
                            attributes.replaceValue("rel", "noopener noreferrer");
                        }
                        if (node instanceof TableBlock && part == AttributablePart.NODE) {
                            attributes.addValue("class", "chat");
                        }
                        if (node instanceof Heading && part == AttributablePart.NODE) {
                            attributes.remove("id");
                        }
                    }
                };
            }
        };
    }

    /**
     * Converts the text to an HTML formatted string.
     *
     * @return An HTML formatted string representation of the prompt text.
     */
    abstract public String parseToHtml(UUID msgUuid, String prompt);

}
