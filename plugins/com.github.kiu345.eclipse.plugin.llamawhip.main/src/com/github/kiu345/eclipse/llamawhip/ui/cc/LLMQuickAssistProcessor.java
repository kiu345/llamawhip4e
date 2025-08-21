package com.github.kiu345.eclipse.llamawhip.ui.cc;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext;
import org.eclipse.jface.text.source.Annotation;

public class LLMQuickAssistProcessor extends LLMProcessorBase implements
        org.eclipse.jface.text.quickassist.IQuickAssistProcessor,
        org.eclipse.jdt.ui.text.java.IQuickAssistProcessor {

    public LLMQuickAssistProcessor() {
        super();
    }

    @Override
    public boolean canAssist(IQuickAssistInvocationContext context) {
        return true;
    }

    @Override
    public boolean hasAssists(IInvocationContext context) throws CoreException {
        return true;
    }

    @Override
    public boolean canFix(Annotation annotation) {
        return false;
    }

    @Override
    public String getErrorMessage() {
        return null;
    }

    @Override
    public ICompletionProposal[] computeQuickAssistProposals(IQuickAssistInvocationContext context) {
//        String userInput = openInlineInput(context);
//        String editorContent = context.getSourceViewer().getDocument().get();
//      return generateProposals(editorContent, userInput);
      return generateProposals("123", "abc");
    }

    @Override
    public IJavaCompletionProposal[] getAssists(IInvocationContext context, IProblemLocation[] locations) throws CoreException {
        ask(context);
//        context.getASTRoot().get
        IJavaCompletionProposal proposal = new CompletionProposal(
                "System.out.println(\"Sample\");",
                context.getSelectionOffset(),
                "System.out.println(\"Sample\");".length(),
                null,
                "Insert sample println",
                100000,
                "Sample insert"
        );

        return new IJavaCompletionProposal[] { proposal };
    }

}
