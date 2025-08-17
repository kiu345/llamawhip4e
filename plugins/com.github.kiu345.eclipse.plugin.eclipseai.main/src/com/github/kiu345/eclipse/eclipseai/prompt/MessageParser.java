package com.github.kiu345.eclipse.eclipseai.prompt;

import java.util.Scanner;
import java.util.UUID;

import org.apache.commons.text.StringEscapeUtils;

import com.github.kiu345.eclipse.eclipseai.prompt.token.ThinkToken;

/**
 * A utility class for parsing and converting a text prompt to an HTML formatted string.
 */
public class MessageParser extends UIParser {

    private static final int DEFAULT_STATE = 0;
    private static final int CODE_BLOCK_STATE = 1;
//    private static final int TEXT_ATTACHMENT_STATE = 2;

    private int state = DEFAULT_STATE;

    @Override
    public String parseToHtml(UUID msgUuid, String promptInput) {
        tokenStack.clear();
        var out = new StringBuilder();
        state = DEFAULT_STATE;
        String prompt = promptInput;

        var thinkString = "";

        if (prompt.trim().startsWith("</think>")) {
            out.append("<div class=\"thinking\">Thinking...</div>");
            prompt = prompt.replaceFirst("</think>", "");
        }

        if (prompt.startsWith(ThinkToken.START_TOKEN)) {
            int think_end = prompt.indexOf(ThinkToken.END_TOKEN);
            out.append("<div class=\"thinking\">");
            out.append("<div class=\"header\">Thought<a class=\"headertools\" onClick=\"toggelView('thought_" + msgUuid.toString() + "')\">Show/hide</a></div>");
            out.append("<div id=\"thought_" + msgUuid.toString() + "\" class=\"thought\" style=\"display: none;\">");
            if (think_end >= 0) {
                thinkString = prompt.substring(ThinkToken.START_TOKEN.length(), think_end);
                prompt = prompt.substring(think_end + ThinkToken.END_TOKEN.length());
                out.append(StringEscapeUtils.escapeHtml4(thinkString));
                out.append("</div></div>");
            }
            else {
                thinkString = prompt.substring(ThinkToken.START_TOKEN.length());
                out.append(StringEscapeUtils.escapeHtml4(thinkString));
                out.append("</div></div>");
                return out.toString();
            }
        }
//        prompt = StringEscapeUtils.escapeHtml4(prompt);

        try (var scanner = new Scanner(prompt)) {
            scanner.useDelimiter("\n");
            StringBuilder textBuffer = new StringBuilder();
            while (scanner.hasNext()) {
                var line = scanner.next();
//                var codeBlockToken = new CodeToken();
//                if (false && codeBlockToken.match(line) != MatchType.NONE) {
//                    switch (codeBlockToken.lastResult()) {
//                        case BEGIN: {
//                            if (inCodeBlock()) {
//                                if (!textBuffer.isEmpty()) {
//                                    handleNonCodeBlock(out, textBuffer.toString(), !scanner.hasNext());
//                                    textBuffer = new StringBuilder();
//                                }
//                                handleCodeBlock(out, codeBlockToken.lastMatchedLang(), codeBlockToken.lastMatchedIndent());
//                            }
//                            else {
//                                if (!textBuffer.isEmpty()) {
//                                    handleNonCodeBlock(out, textBuffer.toString(), !scanner.hasNext());
//                                    textBuffer = new StringBuilder();
//                                }
//                                handleCodeBlock(out, null, codeBlockToken.lastMatchedIndent());
//                            }
//                            break;
//                        }
//                        case TOGGEL: {
//                            if (inCodeBlock()) {
//                                if (!textBuffer.isEmpty()) {
//                                    handleNonCodeBlock(out, textBuffer.toString(), !scanner.hasNext());
//                                    textBuffer = new StringBuilder();
//                                }
//                                handleCodeBlock(out, null, codeBlockToken.lastMatchedIndent());
//                            }
//                            else {
//                                if (!textBuffer.isEmpty()) {
//                                    handleNonCodeBlock(out, textBuffer.toString(), !scanner.hasNext());
//                                    textBuffer = new StringBuilder();
//                                }
//                                handleCodeBlock(out, null, codeBlockToken.lastMatchedIndent());
//                            }
//                            break;
//                        }
//                        default:
//                            throw new IllegalArgumentException("Unexpected value: " + codeBlockToken.lastResult());
//                    }
//                }
//                else {
                textBuffer.append(line);
                if (scanner.hasNext()) {
                    textBuffer.append("\n");
//                    }
                }
            }
            if (!textBuffer.isEmpty()) {
                handleNonCodeBlock(out, textBuffer.toString(), !scanner.hasNext());
            }
        }
//        if ((state & CODE_BLOCK_STATE) == CODE_BLOCK_STATE) {
//            out.append("</code></pre>\n");
//            state ^= CODE_BLOCK_STATE;
//        }

        return out.toString().trim();
    }

//    private void handleTextAttachmentStart(StringBuilder out, String line) {
//        if ((state & TEXT_ATTACHMENT_STATE) != TEXT_ATTACHMENT_STATE) {
//            out.append("<div class=\"context\">");
//            state ^= TEXT_ATTACHMENT_STATE;
//        }
//
//    }
//
    private void handleNonCodeBlock(StringBuilder out, String line, boolean lastLine) {
        if ((state & CODE_BLOCK_STATE) == CODE_BLOCK_STATE) {
            out.append(escapeBackSlashes(line));
        }
//        else if ((state & TEXT_ATTACHMENT_STATE) == TEXT_ATTACHMENT_STATE) {
//            handleTextAttachmentLine(out, line);
//            return;
//        }
        else {
            out.append(markdown(line));
        }

        if (lastLine && (state & CODE_BLOCK_STATE) == CODE_BLOCK_STATE) // close opened code blocks
        {
//            out.append("</code></pre>\n");
            // state ^= CODE_BLOCK_STATE;
        }
        if ((state & CODE_BLOCK_STATE) == CODE_BLOCK_STATE) {
//            out.append("\n");
        }
        else if (!lastLine) {
            out.append("<br/>");
        }
    }
//
//    private void handleTextAttachmentLine(StringBuilder out, String line) {
//        if (line.startsWith(TATT_CONTEXTEND)) {
//            out.append("\n</div>\n");
//            state ^= TEXT_ATTACHMENT_STATE;
//        }
//        else {
//            out.append(StringEscapeUtils.escapeHtml4(line) + "<br/>");
//        }
//    }

//    private void handleCodeBlock(StringBuilder out, String lang, int indent) {
//        if ((state & CODE_BLOCK_STATE) != CODE_BLOCK_STATE) {
//            String codeBlockId = UUID.randomUUID().toString();
//            out.append(
//                    """
//                            <input type="button" onClick="eclipseCopyCode(document.getElementById('${codeBlockId}').innerText)" value="Copy" />
//                            <input type="button" onClick="eclipseSaveCode(document.getElementById('${codeBlockId}').innerText)" value="Save" />
//                            <input type="${showApplyPatch}" onClick="eclipseApplyPatch(document.getElementById('${codeBlockId}').innerText)" value="ApplyPatch"/>
//                            <pre style="margin-left: ${indent}pt;"><code lang="${lang}" id="${codeBlockId}">"""
//                            .replace("${indent}", "" + (indent * 5))
//                            .replace("${lang}", lang)
//                            .replace("${codeBlockId}", codeBlockId)
//                            .replace("${showApplyPatch}", "diff".equals(lang) ? "button" : "hidden") // show "Apply Patch" button for diffs
//            );
//            state ^= CODE_BLOCK_STATE;
//        }
//        else {
//            out.append("</code></pre>\n");
//            state ^= CODE_BLOCK_STATE;
//        }
//    }
}
