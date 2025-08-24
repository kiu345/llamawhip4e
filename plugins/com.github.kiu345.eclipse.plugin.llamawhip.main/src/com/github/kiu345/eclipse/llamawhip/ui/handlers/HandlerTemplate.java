package com.github.kiu345.eclipse.llamawhip.ui.handlers;

import java.nio.file.Files;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.ILog;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

import com.github.kiu345.eclipse.llamawhip.prompt.Prompts;
import com.github.kiu345.eclipse.llamawhip.ui.ChatPresenter;

import jakarta.inject.Inject;

abstract public class HandlerTemplate extends AbstractHandler {
    @Inject
    protected ChatPresenter viewPresenter;
    @Inject
    private ILog logger;

    protected final Prompts type;

    public HandlerTemplate(Prompts type) {
        this.type = type;
    }

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
//        runPrompt();
        return null;
    }

    protected String editor2String(ITextEditor input) {
        try {
            var file = input.getEditorInput().getAdapter(IFile.class);
            return new String(Files.readAllBytes(file.getLocation().toFile().toPath()), file.getCharset());
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void runPrompt() {
        logger.info("running prompt");
        // Get the active editor
        var activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        var activeEditor = activePage.getActiveEditor();

        // Check if it is a text editor
        if (activeEditor instanceof ITextEditor) {
            ITextEditor textEditor = (ITextEditor) activeEditor;

            // Retrieve the document and text selection
            ITextSelection textSelection = (ITextSelection) textEditor.getSelectionProvider().getSelection();
            var compilationUnit = JavaUI.getEditorInputJavaElement(textEditor.getEditorInput());

            var selectedText = textSelection.getText();

            // Read the content from the file
            // this fixes skipped empty lines issue
            var file = textEditor.getEditorInput().getAdapter(IFile.class);
            var documentText = "";
            try {
                documentText = new String(Files.readAllBytes(file.getLocation().toFile().toPath()), file.getCharset());
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
            var fileName = file.getProjectRelativePath().toString(); // use project relative path
            var ext = file.getFileExtension().toString();

            // get java elements
            var selectedJavaElement = "";
            var selectedJavaType = "code snippet";

            if (compilationUnit instanceof ICompilationUnit) {
                IJavaElement selectedElement;
                try {
                    selectedElement = ((ICompilationUnit) compilationUnit).getElementAt(textSelection.getOffset());
                    if (selectedElement != null) {
                        switch (selectedElement.getElementType()) {
                            case IJavaElement.METHOD:
                            case IJavaElement.FIELD:
                            case IJavaElement.LOCAL_VARIABLE:
                                selectedJavaElement = selectedElement.toString();
                                selectedJavaType = javaElementTypeToString(selectedElement);
                                break;
                            case IJavaElement.TYPE:
                                selectedJavaElement = "";
                                selectedJavaType = "class declaration";
                            default:
                        }
                    }
                    selectedJavaElement = selectedJavaElement.replaceAll("\\[.*\\]", "");
                }
                catch (JavaModelException e) {
                    throw new RuntimeException(e);
                }
            }
            @SuppressWarnings("unused")
            var context = new Context(
                    fileName,
                    documentText,
                    selectedText,
                    selectedJavaElement,
                    selectedJavaType,
                    ext,
                    textSelection.getStartLine(),
                    textSelection.getEndLine(),
                    null
            );
//            var message = chatMessageFactory.createUserChatMessage(type, context);
//            viewPresenter.onSendPredefinedPrompt(type, message);
        }
    }

    public String javaElementTypeToString(IJavaElement element) {
        switch (element.getElementType()) {
            case IJavaElement.ANNOTATION:
                return "annotation";
            case IJavaElement.CLASS_FILE:
                return "class file";
            case IJavaElement.COMPILATION_UNIT:
                return "compilation unit";
            case IJavaElement.FIELD:
                return "field";
            case IJavaElement.IMPORT_CONTAINER:
                return "import container";
            case IJavaElement.IMPORT_DECLARATION:
                return "import declaration";
            case IJavaElement.INITIALIZER:
                return "initializer";
            case IJavaElement.JAVA_MODEL:
                return "java model";
            case IJavaElement.JAVA_MODULE:
                return "java module";
            case IJavaElement.JAVA_PROJECT:
                return "java project";
            case IJavaElement.LOCAL_VARIABLE:
                return "local variable";
            case IJavaElement.METHOD:
                return "method";
            case IJavaElement.PACKAGE_DECLARATION:
                return "package declaration";
            case IJavaElement.PACKAGE_FRAGMENT:
                return "package fragment";
            case IJavaElement.PACKAGE_FRAGMENT_ROOT:
                return "package fragment root";
            case IJavaElement.TYPE:
                return "type";
            case IJavaElement.TYPE_PARAMETER:
                return "type parameter";
            default:
                return "";
        }

    }

}
