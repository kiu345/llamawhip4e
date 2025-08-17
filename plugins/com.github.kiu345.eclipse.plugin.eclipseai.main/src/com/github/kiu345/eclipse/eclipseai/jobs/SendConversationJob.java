package com.github.kiu345.eclipse.eclipseai.jobs;

import java.util.concurrent.CompletableFuture;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.di.annotations.Creatable;

import com.github.kiu345.eclipse.eclipseai.model.Conversation;
import com.github.kiu345.eclipse.eclipseai.services.OllamaHttpClientProvider;

import jakarta.inject.Inject;

@Creatable
public class SendConversationJob extends Job
{
    @Inject
    private ILog logger;
    
    @Inject
    private OllamaHttpClientProvider clientProvider;

    @Inject
    private Conversation conversation;

    public SendConversationJob() {
        super(EclipseAIJobConstants.JOB_PREFIX + " ask AI for help");
    }

    @Override
    protected IStatus run(IProgressMonitor progressMonitor) {
        var openAIClient = clientProvider.get();
        openAIClient.setCancelProvider(() -> progressMonitor.isCanceled());

        try {
            var future = CompletableFuture.runAsync(openAIClient.run(conversation))
                    .thenApply(v -> Status.OK_STATUS)
                    .exceptionally(e -> Status.error("Unable to run the task: " + e.getMessage(), e));
            return future.get();
        }
        catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Status.error(e.getMessage(), e);
        }
    }
}
