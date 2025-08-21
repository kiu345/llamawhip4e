package com.github.kiu345.eclipse.llamawhip.ui.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.ILog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.ITextEditor;

import com.github.kiu345.eclipse.llamawhip.Activator;
import com.github.kiu345.eclipse.llamawhip.config.PluginConfiguration;
import com.github.kiu345.eclipse.llamawhip.prompt.PromptLoader;
import com.github.kiu345.eclipse.llamawhip.prompt.Prompts;
import com.github.kiu345.eclipse.llamawhip.ui.util.IDEUtils;

public class CodeReviewHandler extends HandlerTemplate implements IHandler {
    public CodeReviewHandler() {
        super(Prompts.REVIEW);
    }

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IEditorPart editor = HandlerUtil.getActiveEditor(event);
        ILog log = Activator.getDefault().getLog();
        try {
            if (editor != null) {
                IEditorInput input = editor.getEditorInput();
                if (editor instanceof ITextEditor textEditor) {
                    IDocument doc = textEditor.getDocumentProvider().getDocument(input);

                    String content = doc.get();
                    String type = "";

                    String reviewPrompt = PluginConfiguration.instance().getPrompt(Prompts.REVIEW)
                            .replace(PromptLoader.TEMPLATE_TYPE, type)
                            .replace(PromptLoader.TEMPLATE_CONTENT, content);
                    System.out.println(reviewPrompt);
                    var chat = IDEUtils.getOrOpenChatView();
                    if (chat != null) {
                        chat.sendInCurrentChat(reviewPrompt);
                    }
                }
            }
        }
        catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        return null;
    }
}
