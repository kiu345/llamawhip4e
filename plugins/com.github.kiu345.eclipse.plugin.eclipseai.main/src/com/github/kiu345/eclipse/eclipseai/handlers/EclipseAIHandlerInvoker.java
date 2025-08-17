package com.github.kiu345.eclipse.eclipseai.handlers;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;

import com.github.kiu345.eclipse.eclipseai.Activator;
import com.github.kiu345.eclipse.eclipseai.prompt.Prompts;

public class EclipseAIHandlerInvoker {
    public static void Invoke(String command) {
        Invoke(Prompts.valueOf(command.replace(' ', '_').toUpperCase().substring(1)));
    }

    public static void Invoke(Prompts prompt) {
        try {
            switch (prompt) {
                case SYSTEM:
                    break;
                case DISCUSS:
                    ((EclipseAIDiscussCodeHandler) ContextInjectionFactory.make(
                            Activator.getBundleContext().getBundle().loadClass(EclipseAIDiscussCodeHandler.class.getName()),
                            EclipseContextFactory.getServiceContext(Activator.getBundleContext())
                    )).runPrompt();
                    ;
                    break;
                case DOCUMENT:
                    ((EclipseAIJavaDocHandler) ContextInjectionFactory.make(
                            Activator.getBundleContext().getBundle().loadClass(EclipseAIJavaDocHandler.class.getName()),
                            EclipseContextFactory.getServiceContext(Activator.getBundleContext())
                    )).runPrompt();
                    ;
                    break;
                case FIX_ERRORS:
                    ((EclipseAIFixErrorsHandler) ContextInjectionFactory.make(
                            Activator.getBundleContext().getBundle().loadClass(EclipseAIFixErrorsHandler.class.getName()),
                            EclipseContextFactory.getServiceContext(Activator.getBundleContext())
                    )).runPrompt();
                    ;
                    break;
                case GIT_COMMENT:
                    ((EclipseAIGenerateGitCommentHandler) ContextInjectionFactory.make(
                            Activator.getBundleContext().getBundle().loadClass(EclipseAIGenerateGitCommentHandler.class.getName()),
                            EclipseContextFactory.getServiceContext(Activator.getBundleContext())
                    )).runPrompt();
                    ;
                    break;
                case REFACTOR:
                    ((EclipseAICodeRefactorHandler) ContextInjectionFactory.make(
                            Activator.getBundleContext().getBundle().loadClass(EclipseAICodeRefactorHandler.class.getName()),
                            EclipseContextFactory.getServiceContext(Activator.getBundleContext())
                    )).runPrompt();
                    ;
                    break;
                case TEST_CASE:
                    ((EclipseAIUnitTestHandler) ContextInjectionFactory.make(
                            Activator.getBundleContext().getBundle().loadClass(EclipseAIUnitTestHandler.class.getName()),
                            EclipseContextFactory.getServiceContext(Activator.getBundleContext())
                    )).runPrompt();
                    ;
                    break;
                case DISCUSS_SELECTED:
                    break;
            }
        }
        catch (ClassNotFoundException e) {
        }

    }
}
