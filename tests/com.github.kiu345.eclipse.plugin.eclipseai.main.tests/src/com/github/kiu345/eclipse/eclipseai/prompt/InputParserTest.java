package com.github.kiu345.eclipse.eclipseai.prompt;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class InputParserTest {

    private static final String TEST1 = "<p>Leo's chest ached. \"How do I fix it?\"</p><br/>2>1";
    private static final String TEST2 = "Leo's chest ached. \"How do I fix it?\"\n2>1";

    private InputParser parser;

    @Test
    void testParseToHtml1() {
        System.out.println("InputParserTest.testParseToHtml1()");
        parser = new InputParser();
        String result = parser.parseToHtml(UUID.randomUUID(), TEST1);
//        System.out.println(result);
        assertThat(result)
                .isNotNull()
                .describedAs("< and > should be escaped in content")
                .matches("[\\s]*<p>[^<>]*</p>[\\s]*")
                .describedAs("No newline should exist")
                .doesNotContain("<br/>", "<br />");
    }

    @Test
    void testParseToHtml2() {
        System.out.println("InputParserTest.testParseToHtml2()");
        parser = new InputParser();
        String result = parser.parseToHtml(UUID.randomUUID(), TEST2);
//        System.out.println(result);
        assertThat(result)
                .isNotNull();
    }

}
