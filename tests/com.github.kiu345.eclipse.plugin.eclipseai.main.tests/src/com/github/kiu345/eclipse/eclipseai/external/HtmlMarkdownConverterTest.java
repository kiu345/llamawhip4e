package com.github.kiu345.eclipse.eclipseai.external;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import com.github.kiu345.eclipse.eclipseai.prompt.UIParser;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;

class HtmlMarkdownConverterTest {

    @Test
    void testHtmlToMarkdown() {
        String html = "<h3>Hello World</h3>";

        var options = FlexmarkHtmlConverter.builder().toImmutable();
        var converter = FlexmarkHtmlConverter.builder(options).build();
        String markdown = converter.convert(html);

        assertThat(markdown.trim()).contains("### Hello World");
    }

    @Test
    void testMarkdownToHtml() {
        String markdown = "### Hello World";

        MutableDataSet options = new MutableDataSet();
        // options.set(Parser.EXTENSIONS, Arrays.asList(TablesExtension.create(), StrikethroughExtension.create()));
        // options.set(HtmlRenderer.SOFT_BREAK, "<br />\n");
        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options)
                .attributeProviderFactory(UIParser.createAttributeProviderFactory())
                .build();

        Node document = parser.parse(markdown);
        String html = renderer.render(document); // "<p>This is <em>Sparta</em></p>\n"

        assertThat(html.trim()).contains("<h3>Hello World</h3>");
    }

    @Test
    void testMarkdownTable() {
        String markdown = """
                | Header 1 | Header 2 | Header 3 |
                |----------|----------|----------|
                | Inhalt 1 | Inhalt 2 | Inhalt 3 |
                | Inhalt 4 | Inhalt 5 | Inhalt 6 |
                """;

        MutableDataSet options = new MutableDataSet();
        options.set(Parser.EXTENSIONS, Arrays.asList(TablesExtension.create()));
        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options)
                .attributeProviderFactory(UIParser.createAttributeProviderFactory())
                .build();

        Node document = parser.parse(markdown);
        String html = renderer.render(document);

//        System.out.println(html);
        assertThat(html.trim())
                .contains("<table class=\"chat\">")
                .contains("Header 1");
    }
}
