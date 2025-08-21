package com.github.kiu345.eclipse.llamawhip.ui.cc;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

import com.github.kiu345.eclipse.llamawhip.Activator;
import com.github.kiu345.eclipse.llamawhip.adapter.ChatAdapterFactory;
import com.github.kiu345.eclipse.llamawhip.adapter.ModelDescriptor;
import com.github.kiu345.eclipse.llamawhip.config.PluginConfiguration;
import com.github.kiu345.eclipse.llamawhip.messaging.UserMsg;

public abstract class LLMProcessorBase {

    protected final PluginConfiguration config = PluginConfiguration.instance();
    protected final ILog log;

    public LLMProcessorBase() {
        log = Platform.getLog(Activator.getDefault().getBundle());
    }

    protected ICompletionProposal[] generateProposals(String context, String userPrompt) {
//        String result = client.requestCompletion(context, userPrompt);
        CompletionProposal proposal = new CompletionProposal("Blubb", 0, 0, "Blubb".length());
        return new ICompletionProposal[] { proposal };
    }

    protected void ask(ITextViewer viewer, int offset) {
        var profile = config.getDefaultProfile();
        var modelName = config.getDefaultModel();
        if (profile == null || modelName == null) {
            return;
        }

        IDocument doc = viewer.getDocument();
        String content = doc.get();
        String queryDoc = StringUtils.substring(content, 0, offset) + "${{CURSOR}}" + StringUtils.substring(content, offset);

//        var adapter = ChatAdapterFactory.create(log, profile);
//        var ollamaModels = adapter.getModels();
//        var model = ollamaModels.stream().filter(e -> modelName.equals(e.name())).findAny().get();

        @SuppressWarnings("unused")
        UserMsg message = new UserMsg("""
                This is a code completion request form a IDE.
                Create a code completion ideas for code at the location ${{CURSOR}}!
                Only return the code without any markup! File type is %s
                <|CONTEXT|>
                %s
                </|CONTEXT|>
                """.formatted("text/plain", queryDoc));
    }

    protected void ask(IInvocationContext context) {
        var profile = config.getDefaultProfile();
        var modelName = config.getDefaultModel();
        if (profile == null || modelName == null) {
            return;
        }
        
        String content;
        try {
            content = context.getCompilationUnit().getSource();
        }
        catch (JavaModelException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        }
        int offset = context.getSelectionOffset();

        String queryDoc = StringUtils.substring(content, 0, offset) + "${{CURSOR}}" + StringUtils.substring(content, offset);

        var adapter = ChatAdapterFactory.create(log, profile);
        List<ModelDescriptor> ollamaModels = adapter.getModels();
        var model = ollamaModels.stream().filter(e -> modelName.equals(e.name())).findAny().get();

        UserMsg message = new UserMsg("""
                This is a code completion request form a IDE.
                Create a code completion ideas for code at the location ${{CURSOR}}!
                Only return the code without any markup! File type is %s
                <|CONTEXT|>
                %s
                </|CONTEXT|>
                """.formatted("text/plain", queryDoc));
        @SuppressWarnings("unused")
        var response = adapter.generate(model, message.toString());
//
        
    }
}
