package com.github.kiu345.eclipse.eclipseai.ui.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

public class IDEUtils {

    public static Collection<IMarker> getMarkers(IProject project, IFile file) throws CoreException {
        if (file == null) {
            return Collections.emptyList();
        }
        List<IMarker> resultList = new ArrayList<>();
        var filePath = file.getProjectRelativePath().toString();

        var markers = project.findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
        for (var marker : markers) {
            int severity = marker.getAttribute(IMarker.SEVERITY, -1);

            if (severity == IMarker.SEVERITY_ERROR) {
                var fileName = marker.getResource().getName();
                if (filePath.equals(fileName)) {
                    resultList.add(marker);
                }
            }
        }
        return resultList;
    }

    public static IEditorPart getCurrentEditor() {
        IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
        for (IWorkbenchWindow window : windows) {
            IWorkbenchPage page = window.getActivePage();
            if (page == null) {
                continue;
            }
            IEditorPart editorPart = page.getActiveEditor();
            if (editorPart == null) {
                continue;
            }

            return editorPart;
        }
        return null;

    }

    public static IFile getCurrentEditorFile() {
        var editor = getCurrentEditor();
        if (editor == null) {
            return null;
        }
        if (editor.getEditorInput() instanceof FileEditorInput fileEditor) {
            return fileEditor.getFile();
        }
        return null;
    }
}
