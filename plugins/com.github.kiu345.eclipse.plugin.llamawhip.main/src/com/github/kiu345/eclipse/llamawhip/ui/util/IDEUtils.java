package com.github.kiu345.eclipse.llamawhip.ui.util;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

import com.github.kiu345.eclipse.llamawhip.Activator;
import com.github.kiu345.eclipse.llamawhip.ui.AIChatViewPart;

/**
 * Utility class for IDE related operations.
 */
public class IDEUtils {
    private static final ILog LOG = Activator.getDefault().getLog();

    private static final String CHATVIEW_ID = "com.github.kiu345.eclipse.llamawhip.chatview";

    /**
     * Returns a image descriptor for the plugin icon.
     */
    public static ImageDescriptor pluginIcon() {
        try {
            var bundle = Activator.getDefault().getBundle();
            URL url = FileLocator.find(bundle, new Path("/icons/llamawhip16.png"), null);
            ImageDescriptor desc = ImageDescriptor.createFromURL(url);
            return desc;
        }
        catch (Exception e) {
            return null;
        }
    }

    /**
     * Retrieves problem markers for the given project and file.
     */
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

    /**
     * Checks if an editor with the given ID is available in the workbench.
     */
    public static boolean isEditorAvailable(String editorId) {
        IEditorDescriptor desc = PlatformUI.getWorkbench()
                .getEditorRegistry()
                .findEditor(editorId);
        return desc != null;
    }

    /**
     * Opens a new editor with the given filename and content.
     */
    public static void createEditor(String filename, String fileContent) {
        IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
        if (windows.length == 0) {
            return;
        }

        final IWorkbenchPage page = windows[0].getActivePage();

        final List<String> editorPriorityList = new ArrayList<>(5);
        editorPriorityList.add("org.eclipse.ui.DefaultTextEditor");
        editorPriorityList.add("org.eclipse.ui.editors.text.TextEditor");

        // only on valid filenames and a dot as a separator, ignoring Unix dot files
        if (filename != null && filename.lastIndexOf(".") > 0) {
            var fileExtension = StringUtils.substringAfterLast(filename, ".").toLowerCase();
            switch (fileExtension) {
                case "java" -> {
                    editorPriorityList.addFirst("org.eclipse.jdt.ui.CompilationUnitEditor");
                }
            }
        }

        StringEditorInput input = new StringEditorInput(filename, fileContent);
        Display.getDefault().asyncExec(() -> {
            for (String editorId : editorPriorityList) {
                try {
                    // check if the editor plugin is installed and try to open the content with it
                    if (isEditorAvailable(editorId)) {
                        page.openEditor(input, editorId);
                        return;
                    }
                }
                catch (PartInitException e) {
                    LOG.error("failed to open editor:" + e.getMessage(), e);
                }
            }
        });
    }

    /**
     * Returns the currently active editor in the workbench.
     */
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

    /**
     * Returns the file of the currently active editor, or {@code null} if none.
     */
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

    /**
     * Returns the chat view. Opens it if not in workspace.
     *
     * @return the chat view part
     * 
     * @throws IllegalStateException it creating or opening the component failed
     */
    public static AIChatViewPart getOrOpenChatView() {
        IWorkbenchPage page = PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow()
                .getActivePage();
        if (page == null) {
            return null;
        }

        IViewPart view = page.findView(CHATVIEW_ID);
        if (view == null) {
            try {
                view = page.showView(CHATVIEW_ID);
            }
            catch (PartInitException e) {
                throw new IllegalStateException("Could not create " + CHATVIEW_ID, e);
            }
        }
        if (view instanceof AIChatViewPart chatView) {
            return chatView;
        }
        throw new IllegalStateException(CHATVIEW_ID + " provided unexpected object of type " + view.getClass().getSimpleName());
    }
}
