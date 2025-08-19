package com.github.kiu345.eclipse.eclipseai.ui.cc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

import com.github.kiu345.eclipse.llamawhip.Activator;

public class LLMContentAssistProcessor extends LLMProcessorBase implements IContentAssistProcessor, IJavaCompletionProposalComputer {

    public LLMContentAssistProcessor() {
        super();
    }

    @Override
    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
        ask(viewer, offset);
        String context = viewer.getDocument().get();
        return generateProposals(context, "");
    }

    @Override
    public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public char[] getCompletionProposalAutoActivationCharacters() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public char[] getContextInformationAutoActivationCharacters() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getErrorMessage() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IContextInformationValidator getContextInformationValidator() {
        return null;
    }

    @Override
    public void sessionStarted() {
    }

    @SuppressWarnings("unused")
    @Override
    public List<ICompletionProposal> computeCompletionProposals(
            ContentAssistInvocationContext context,
            IProgressMonitor monitor
    ) {

        IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        List<ICompletionProposal> proposals = new ArrayList<>();
        IDocument doc = context.getDocument();
        ITextSelection textSelection = context.getTextSelection();
        int cursorOffset = context.getInvocationOffset();
        int lineOfCursor;
        try {
            lineOfCursor = doc.getLineOfOffset(cursorOffset);
            int lineOfCursorOffset = doc.getLineOffset(lineOfCursor);
            proposals.add(new CompletionProposal("Test", lineOfCursorOffset, cursorOffset - lineOfCursorOffset, null, "Hello", 10000, "")); // List.of(res);
        }
        catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
        catch (RuntimeException e) {
//            Activator.logError("Error when requesting completion: " + e.getMessage(), e);
            throw e;
        }
        finally {
//            if (debugPromptLoggingEnabled) {
//                Activator.logInfo(debugPromptSB.toString());
//            }
        }

        return proposals;
    }

    @Override
    public List<IContextInformation> computeContextInformation(ContentAssistInvocationContext context, IProgressMonitor monitor) {
        return Collections.emptyList();
    }

    @Override
    public void sessionEnded() {
    }
}
