package com.github.kiu345.eclipse.eclipseai.prompt;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.github.kiu345.eclipse.eclipseai.part.helper.UIInputHandler;

class MessageParserTest {
    private static final String TEST1 = "<p>Leo's chest ached. \"How do I fix it?\"</p><br/>2>1";
    private static final String TEST2 = "Leo's chest ached. \"How do I fix it?\"\\\n2>1";

    private static final String TEST3 = """
            **The Fox of Ember Hollow**
            <p>In a village where the rivers ran <strong>silver</strong> and the skies hummed with whispers, there lived a boy named Leo who feared the woods.</p>
            <div class="forest">
                <p>One autumn, he stumbled upon a fox with eyes like molten gold, its fur shimmering with the faint glow of a dying star.</p>
                <p><strong>The fox</strong> asked, "Why do you fear the woods, child?"</p>
                <p>Leo muttered about shadows and the tales of the Wraithwood, where lost souls lingered.</p>
            </div>
            <div class="boat">
                <p>Curious, Leo followed the fox deep into the woods. The trees grew taller, their branches weaving a canopy of twilight.</p>
                <p>The fox led him to a clearing where a child's toy boat floated on a pond of starlight.</p>
                <p>The boat was frozen, its sail tattered.</p>
            </div>
            <p><strong>The fox</strong> said, "Once, this boat carried a boy who sought answers. He asked the woods for a wish. The woods gave him a path to escape his troubles. But the boy forgot to return. The boat is a tomb."</p>
            <p>Leo's chest ached. "How do I fix it?"</p>
            <p>The fox licked its lips, the sound like distant thunder. "You must sail the boat back. But the stars will not guide you unless you let go of your fear."</p>
            <p>Leo knelt, brushing the dust from the boat. As he touched it, the pond rippled, and the boat drifted toward the trees.</p>
            <p>When Leo returned home, the woods felt different—less ominous, more alive. He never spoke of the fox, but the feather, tucked in his pocket, glowed whenever he faced a choice.</p>""";

    private static final String TEST4 = """
            <div class="boat">
                <p>Curious, Leo followed the fox deep into the woods. The trees grew taller, their branches weaving a canopy of twilight.</p>
                <p>The fox led him to a clearing where a child's toy boat floated on a pond of starlight.</p>
                <p>The boat was frozen, its sail tattered.</p>
            </div>""";
    private static final String TEST5_1 = """
            ```json
            {
              "name": "John Doe",
              "age": 30,
              "address": {
                "street": "123 Main St",
                "city": "Anytown",
                "zipCode": "12345"
              },
              "hobbies": ["reading", "hiking", "photography"]
            }
            ```

            and

            ```java
            public class BlubbTest {

                @Test
                public void testBlubbClass() {
                    String html = "<html>Test</html>";
                }
            }
            ```""";
    private static final String TEST5_2 = """
            ```json
            {
              "name": "John Doe",
              "age": 30,
              "email": "john.doe@example.com",
              "is_employee": true,
              "address": {
                "street": "123 Main St",
                "city": "Anytown",
                "zipcode": "12345"
              },
              "hobbies": ["reading", "hiking", "photography"],
              "account_balance": 1500.75
            }
            ```
            """;

    private static final String TEST6 = """
            <think>
            Okay, the user asked for a unit test for the Blubb class. I need to generate a test class using JUnit. Let me check the existing code. The Blubb class has a main method but no actual functionality. So the test should probably focus on any methods that might be added later.
            </think>

            Here's a JUnit 5 unit test template for your `Blubb` class:

            ```java
            public class BlubbTest {

                @Test
                public void testBlubbClass() {
                }
            }
            ```

            **Notes:**
            1. This test class is currently empty since the `Blubb` class has no implemented functionality""";
    private static final String TEST7 = """
                        Hier ist ein Beispiel für eine Markdown-Tabelle:

            ```markdown
            | Header 1 | Header 2 | Header 3 |
            |----------|----------|----------|
            | Inhalt 1 | Inhalt 2 | Inhalt 3 |
            | Inhalt A | Inhalt B | Inhalt C |
            ```

            Die Tabelle wird in der IDE oder im Chat so angezeigt:

            | Header 1 | Header 2 | Header 3 |
            |----------|----------|----------|
            | Inhalt 1 | Inhalt 2 | Inhalt 3 |
            | Inhalt A | Inhalt B | Inhalt C |

            """;
    private MessageParser parser;

    @BeforeEach
    void setUp() {
        parser = new MessageParser();
    }

    @Test
    void testParseToHtml1() {
        System.out.println("MessageParserTest.testParseToHtml1()");
        String result = parser.parseToHtml(UUID.randomUUID(), TEST1);
//      System.out.println(result);
        assertThat(result)
                .isNotNull()
                .describedAs("< and > should be escaped in content")
                .matches("[\\s]*<p>[^<>]*</p>[\\s]*")
                .describedAs("No newline should exist")
                .doesNotContain("<br/>", "<br />");
    }

    @Test
    void testParseToHtml2() {
        System.out.println("MessageParserTest.testParseToHtml2()");
        System.out.println(TEST2 + "\n---");
        String result = parser.parseToHtml(UUID.randomUUID(), TEST2);
//      System.out.println(result);
        assertThat(result)
                .isNotNull()
                .containsAnyOf("<br/>", "<br />");
    }

    @Test
    void testParseToHtml3() {
//        System.out.println("MessageParserTest.testParseToHtml3()");
//        System.out.println(TEST3 + "\n---");
        String result = parser.parseToHtml(UUID.randomUUID(), TEST3);
        assertThat(result)
                .isNotNull()
                .describedAs("Missing content")
                .contains("<strong>The Fox of Ember Hollow</strong>")
                .contains("&quot;Why do you fear the woods, child?&quot;")
                .describedAs("HTML not encoded")
                .doesNotContain("<div class=\"forest\">");
//        System.out.println(result);
    }

    @Test
    void testParseToHtmlWithOtherFunctions() {
        System.out.println("MessageParserTest.testParseToHtmlWithOtherFunctions()");
        System.out.println(TEST4 + "\n---");
        String result = UIInputHandler.escapeHtmlQuotes(UIInputHandler.fixLineBreaks(parser.parseToHtml(UUID.randomUUID(), TEST4)));
        assertThat(result).isNotNull();
        System.out.println(result);
    }

    @Test
    void testParseToHtml5() {
//        System.out.println("MessageParserTest.testParseToHtml5()");
//        System.out.println(TEST5_1 + "\n---");
        String result1 = parser.parseToHtml(UUID.randomUUID(), TEST5_1);
        assertThat(result1)
                .isNotNull()
                .matches(Pattern.compile(".*<pre.*&quot;age&quot;.*</pre>.*and.*<pre.*</pre>.*", Pattern.MULTILINE | Pattern.DOTALL))
                .contains("&quot;John Doe&quot;")
                .contains("public class BlubbTest {")
                .contains("public void testBlubbClass() {")
                .contains("&quot;&lt;html&gt;Test&lt;/html&gt;&quot;");

//        System.out.println(result1);
//        System.out.println("----");
        String result2 = parser.parseToHtml(UUID.randomUUID(), TEST5_2);
        assertThat(result2)
                .isNotNull()
                .matches(Pattern.compile("<input type=\"button\".*<pre.*&quot;age&quot;.*</pre>", Pattern.MULTILINE | Pattern.DOTALL));
//        System.out.println(result2);
    }

    @Test
    void testParseToHtml6() {
//        System.out.println("MessageParserTest.testParseToHtml6()");
//        System.out.println(TEST6 + "\n---");
        String result = parser.parseToHtml(UUID.randomUUID(), TEST6);
        assertThat(result).isNotNull();
        System.out.println("----");
        System.out.println(result);
        System.out.println("----");
    }

    @Test
    void testParseToHtml7() {
//        System.out.println("MessageParserTest.testParseToHtml6()");
//        System.out.println(TEST6 + "\n---");
        String result = parser.parseToHtml(UUID.randomUUID(), TEST7);
        assertThat(result).isNotNull();
        System.out.println("----");
        System.out.println(result);
        System.out.println("----");
    }

    @Test
    void testEscapeBackSlashes() {
        System.out.println("MessageParserTest.testEscapeBackSlashes()");
        String result = MessageParser.escapeBackSlashes("\\b");
        assertThat(result)
                .isNotNull()
                .isEqualTo("\\\\b", result);
    }

    @Test
    void testMarkdown() {
        String result = MessageParser.markdown("*bold*");
        assertThat(result)
                .isNotNull()
                .isEqualToIgnoringNewLines("<p><em>bold</em></p>");

        result = MessageParser.markdown("# Header\n## Header2");
        assertThat(result)
                .isNotNull()
                .isEqualToIgnoringNewLines("<h1>Header</h1><h2>Header2</h2>");
    }

    @Test
    void testEscapeHtmlEntities() {
        String input = "Ümlaut & <tag> » test";
        String output = parser.parseToHtml(UUID.randomUUID(), input);
        assertThat(output)
                .contains("Ümlaut &amp; &lt;tag&gt; » test")
                .doesNotContain("&amp;amp;"); // no double escape
    }

    @Nested
    @DisplayName("Codeblock handling")
    class CodeBlockTests {

        @Test
        void testOpenCodeBlock() {
            String input = "```\nSystem.out.println(\"Hi\");\n```";
            String output = parser.parseToHtml(UUID.randomUUID(), input);
            assertThat(output)
                    .containsPattern("<pre.*><code.*>")
                    .contains("System.out.println(&quot;Hi&quot;);")
                    .containsPattern("</code></pre>");
        }

        @Test
        void testUnclosedCodeBlock() {
            String input = "```\nLine 1\nLine 2"; // no closing ```
            String output = parser.parseToHtml(UUID.randomUUID(), input);
            assertThat(output)
                    .containsPattern("<pre.*><code.*>")
                    .contains("Line 1\nLine 2")
                    .containsPattern("</code></pre>"); // closing at EOF
        }
    }

    @Nested
    @DisplayName("Sample message handling")
    class MessageTests {
        private static final String MSG1 = """
                In den meisten Fällen kann Hamcrest durch AssertJ ersetzt werden:

                | Aspekt | Hamcrest | AssertJ |
                |--------|----------|---------|
                | **Syntax** | `assertThat(value).is(...)` mit Matchers (z. B. `containsString`, `hasItem`) | `assertThat(value).isEqualTo(...)`, `contains`, `hasSize`, etc. |
                | **Lesbarkeit** | Matcher-Logik, oft `assertThat(list, hasItem("x"))` | Fließende, leicht lesbare Aufrufe: `assertThat(list).contains("x")` |
                | **Fehlermeldungen** | Standard-Message, kann erweitert werden | Sehr aussagekräftige, kontextbezogene Fehlermeldungen |
                | **Einfacher Einstieg** | Für kleine Projekte reicht Matchers aus | Umfassender, aber auch leichtgewichtig |
                | **Kompatibilität** | Gut mit JUnit 4 und JUnit 5 | Direkte Integration in JUnit 5, aber auch mit JUnit 4 |

                **Kurz gesagt:** Ja, AssertJ kann Hamcrest in den meisten Unit‑Tests ersetzen und bietet oft noch bessere Fehlermeldungen und eine flüssigere API. Wenn du jedoch spezielle Matcher benötigst, die in AssertJ nicht vorhanden sind, könntest du weiterhin Hamcrest einsetzen oder eigene Matcher schreiben.
                """;

        @Test
        void testMsg1() {
            String output = parser.parseToHtml(UUID.randomUUID(), MSG1);
            assertThat(output)
                    .isNotBlank()
                    .contains("<td>Fließende, leicht lesbare Aufrufe: <code>assertThat(list).contains(&quot;x&quot;)</code></td>")
                    .endsWith("</p>");
        }
    }

    @Nested
    @DisplayName("Diagram handling")
    class MarkdownTests {
        private static final String PLANTUML = """
                Demo Diagram

                @startuml
                Alice -> Bob: Hallo
                Bob -> Alice: Hi!
                @enduml
                """;

        @Test
        void testPlantUML() {
            String output = parser.parseToHtml(UUID.randomUUID(), PLANTUML);
            assertThat(output)
                    .isNotBlank();
            System.out.println(output);
        }
    }

}
