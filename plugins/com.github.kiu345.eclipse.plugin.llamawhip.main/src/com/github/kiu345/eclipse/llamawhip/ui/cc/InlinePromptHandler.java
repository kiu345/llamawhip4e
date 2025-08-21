package com.github.kiu345.eclipse.llamawhip.ui.cc;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.ITextEditor;

import com.github.kiu345.eclipse.llamawhip.Activator;

public class InlinePromptHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IEditorPart editor = HandlerUtil.getActiveEditor(event);
        if (editor instanceof ITextEditor editorinstance) {
            IDocument document = editorinstance
                    .getDocumentProvider()
                    .getDocument(editor.getEditorInput());

            StyledText styledText = (StyledText) ((ITextEditor) editor).getAdapter(StyledText.class);

            InlinePromptProposal proposal = new InlinePromptProposal(Platform.getLog(Activator.getDefault().getBundle()));
            if (editorinstance.getSelectionProvider().getSelection() instanceof ITextSelection ts) {
                proposal.showInlinePrompt(document, ts, styledText);
            }
        }
        return null;
    }
}
