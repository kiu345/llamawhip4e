package com.github.kiu345.eclipse.eclipseai.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.e4.core.commands.ExpressionContext;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class FixErrorHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        System.out.println("FixErrorHandler.execute()");
        System.out.println(event.getApplicationContext());
        System.out.println(event.getParameters());
        if (event.getApplicationContext() instanceof org.eclipse.e4.core.commands.ExpressionContext exprContext) {
            System.out.println(exprContext.getDefaultVariable());
        }
        return null;
    }

    @Override
    public void setEnabled(Object evaluationContext) {
        System.out.println("FixErrorHandler.setEnabled()");
        System.out.println(evaluationContext);
        if (evaluationContext instanceof ExpressionContext context) {
            System.out.println(context.getDefaultVariable());
        }
        super.setEnabled(evaluationContext);
    }

    @Override
    public boolean isEnabled() {
        System.out.println("FixErrorHandler.isEnabled()");
        System.out.println(getSelectedFile());
        return true;
    }

    @Override
    public boolean isHandled() {
        System.out.println("FixErrorHandler.isHandled()");
        return true;
    }

    protected IFile getSelectedFile() {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window == null) {
            return null;
        }

        ISelection selection = window.getSelectionService().getSelection();
        if (!(selection instanceof IStructuredSelection)) {
            return null;
        }
        IStructuredSelection structuredSelection = (IStructuredSelection) selection;

        Object firstElement = structuredSelection.getFirstElement();
        if (!(firstElement instanceof IAdaptable)) {
            return null;
        }

        IFile file = (IFile) ((IAdaptable) firstElement).getAdapter(IFile.class);
        return file;
    }

}
