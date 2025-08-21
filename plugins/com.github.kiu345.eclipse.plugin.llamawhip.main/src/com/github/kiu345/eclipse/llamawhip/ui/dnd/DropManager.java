package com.github.kiu345.eclipse.llamawhip.ui.dnd;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.ILog;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.part.EditorInputTransfer.EditorInputData;
import org.eclipse.ui.part.FileEditorInput;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * Controls can register to be enabled to act as a "drop target", so it can
 * accept e.g. files from the Project Explorer.
 */
@Creatable
@Singleton
public class DropManager {
    private class DropTargetAdapterImpl extends DropTargetAdapter {

        @Override
        public void dragEnter(DropTargetEvent event) {
            event.detail = DND.DROP_COPY;
            event.feedback = DND.FEEDBACK_NONE;
        }

        @Override
        public void dragOperationChanged(DropTargetEvent event) {
            event.detail = DND.DROP_COPY;
            event.feedback = DND.FEEDBACK_NONE;
        }

        @Override
        public void dragOver(DropTargetEvent event) {
            event.detail = DND.DROP_COPY;
            event.feedback = DND.FEEDBACK_NONE;
        }

        @Override
        public void drop(DropTargetEvent event) {
            // Prevent deleting stuff from the source when the user just
            // moves content instead of using copy.
            if (event.detail == DND.DROP_MOVE) {
                event.detail = DND.DROP_COPY;
            }
//            transferHandlerFactory.getTransferHandler(event.currentDataType)
//                    .ifPresentOrElse(
//                            handler -> handler.handleTransfer(event.data),
//                            () -> log.warn("Unsupported data type: " + event.data.getClass().getName())
//                    );
            if (event.currentDataType != null) {
                handleTransfer(event.currentDataType, event.data);
            }
        }
    }

    @Inject
    private ILog log;

    @Inject
    private TransferHandlerFactory transferHandlerFactory;

    private final DropTargetAdapter dropTargetAdapter;

    private Consumer<IFile> fileConsumler = (file) -> {};

    public void setFileConsumler(Consumer<IFile> fileConsumler) {
        this.fileConsumler = Objects.requireNonNull(fileConsumler);
    }

    public Consumer<IFile> getFileConsumler() {
        return fileConsumler;
    }

    public DropManager() {
        dropTargetAdapter = new DropTargetAdapterImpl();
    }

    /** Add DnD capabilities to the given control. */
    public void registerDropTarget(Control targetControl) {
        DropTarget dropTarget = new DropTarget(targetControl, DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK | DND.DROP_DEFAULT);
        dropTarget.setTransfer(transferHandlerFactory.getSupportedTransfers());
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
        if (data instanceof TreeSelection treeSel) {
            List<IFile> files = treeSel.stream().filter(IFile.class::isInstance).map(IFile.class::cast).toList();
            for(var element: files) {
                fileConsumler.accept(element);
            }
        }
    }
}
