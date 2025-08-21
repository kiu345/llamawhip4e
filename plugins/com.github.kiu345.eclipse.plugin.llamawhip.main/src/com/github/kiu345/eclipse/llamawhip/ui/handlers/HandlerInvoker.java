package com.github.kiu345.eclipse.llamawhip.ui.handlers;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;

import com.github.kiu345.eclipse.llamawhip.Activator;
import com.github.kiu345.eclipse.llamawhip.prompt.Prompts;

public class HandlerInvoker {
    public static void Invoke(String command) {
        Invoke(Prompts.valueOf(command.replace(' ', '_').toUpperCase().substring(1)));
    }

    public static void Invoke(Prompts prompt) {
        try {
            switch (prompt) {
                case SYSTEM:
                    break;
                case DISCUSS:
                    ((DiscussCodeHandler) ContextInjectionFactory.make(
                            Activator.getDefault().getBundle().loadClass(DiscussCodeHandler.class.getName()),
                            EclipseContextFactory.getServiceContext(Activator.getBundleContext())
                    )).runPrompt();
                    ;
                    break;
                case DOCUMENT:
                    ((JavaDocHandler) ContextInjectionFactory.make(
                            Activator.getBundleContext().getBundle().loadClass(JavaDocHandler.class.getName()),
                            EclipseContextFactory.getServiceContext(Activator.getBundleContext())
                    )).runPrompt();
                    ;
                    break;
                case FIX_ERRORS:
                    ((FixErrorsHandler) ContextInjectionFactory.make(
                            Activator.getBundleContext().getBundle().loadClass(FixErrorsHandler.class.getName()),
                            EclipseContextFactory.getServiceContext(Activator.getBundleContext())
                    )).runPrompt();
                    ;
                    break;
                case REFACTOR:
                    ((CodeRefactorHandler) ContextInjectionFactory.make(
                            Activator.getBundleContext().getBundle().loadClass(CodeRefactorHandler.class.getName()),
                            EclipseContextFactory.getServiceContext(Activator.getBundleContext())
                    )).runPrompt();
                    ;
                    break;
                case TEST_CASE:
                    ((UnitTestHandler) ContextInjectionFactory.make(
                            Activator.getBundleContext().getBundle().loadClass(UnitTestHandler.class.getName()),
                            EclipseContextFactory.getServiceContext(Activator.getBundleContext())
                    )).runPrompt();
                    ;
                    break;
                case DISCUSS_SELECTED:
                    break;
                case BASE:
                    break;
                case COMPLETE:
                    break;
                default:
                    break;
            }
        }
        catch (ClassNotFoundException e) {
        }

    }
}
