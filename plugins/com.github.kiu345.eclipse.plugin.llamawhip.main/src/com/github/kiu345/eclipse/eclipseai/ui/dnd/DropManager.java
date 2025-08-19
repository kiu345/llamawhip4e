package com.github.kiu345.eclipse.eclipseai.ui.dnd;

import java.util.Objects;
import java.util.function.Consumer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.ILog;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.part.EditorInputTransfer;
import org.eclipse.ui.part.EditorInputTransfer.EditorInputData;
import org.eclipse.ui.part.FileEditorInput;

import jakarta.inject.Inject;

/**
 * Handles drag-and-drop operations for files and editor inputs within the Eclipse UI.
 *
 * <p>
 * This manager registers a {@link DropTarget} on a given {@link Control} and
 * processes dropped data. It supports file drops via {@link FileTransfer}
 * and editor input drops via {@link EditorInputTransfer}. When a file is
 * dropped, the configured {@link Consumer} is invoked with the corresponding
 * {@link IFile} instance.
 * </p>
 *
 * <p>
 * Clients can set a custom {@link Consumer} using {@link #setFileConsumler(Consumer)}.
 * The default consumer performs no action.
 * </p>
 */
@Creatable
public class DropManager {
    private class DropTargetAdapterImpl extends DropTargetAdapter {

        @Override
        public void dragEnter(DropTargetEvent event) {
            // Always indicate a copy operation.
            event.detail = DND.DROP_COPY;
            event.feedback = DND.FEEDBACK_NONE;
        }

        @Override
        public void drop(DropTargetEvent event) {
            if (event.currentDataType != null) {
                handleTransfer(event.currentDataType, event.data);
            }
        }
    }

    @Inject
    private ILog log;

    private final DropTargetAdapter dropTargetAdapter = new DropTargetAdapterImpl();

    private Consumer<IFile> fileConsumler = (file) -> {};

    public void setFileConsumler(Consumer<IFile> fileConsumler) {
        this.fileConsumler = Objects.requireNonNull(fileConsumler);
    }

    public Consumer<IFile> getFileConsumler() {
        return fileConsumler;
    }

    /** Add DnD capabilities to the given control. */
    public void registerDropTarget(Control targetControl) {
        DropTarget dropTarget = new DropTarget(targetControl, DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK | DND.DROP_DEFAULT);
        dropTarget.setTransfer(new Transfer[] { FileTransfer.getInstance(), EditorInputTransfer.getInstance() });
        dropTarget.addDropListener(dropTargetAdapter);
    }

    private void handleTransfer(TransferData currentDataType, Object data) {
        log.info("DropManager.handleTransfer(" + currentDataType + ")");
        if (data instanceof EditorInputData[] editorInputDatas) {
            for (EditorInputData inputData : editorInputDatas) {
                if (inputData.input instanceof FileEditorInput fileEditor) {
                    IFile file = fileEditor.getFile();
                    fileConsumler.accept(file);
                }
                else {
                    log.warn("unhadled input:" + inputData.input);
                }
            }
        }
    }

}
