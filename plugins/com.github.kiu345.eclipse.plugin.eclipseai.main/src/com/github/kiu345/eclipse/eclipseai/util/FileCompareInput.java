package com.github.kiu345.eclipse.eclipseai.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.compare.IEditableContent;
import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;

class FileCompareInput implements IStreamContentAccessor, ITypedElement, IEditableContent {
    private IFile file;

    public FileCompareInput(IFile file) {
        this.file = file;
    }

    @Override
    public InputStream getContents() throws CoreException {
        return file.getContents();
    }

    @Override
    public String getName() {
        return file.getName();
    }

    @Override
    public String getType() {
        return ITypedElement.TEXT_TYPE;
    }

    @Override
    public Image getImage() {
        return null;
    }

    @Override
    public boolean isEditable() {
        return true;
    }

    @Override
    public ITypedElement replace(ITypedElement arg0, ITypedElement arg1) {
        return null;
    }

    @Override
    public void setContent(byte[] arg0) {
        try {
            if (file.exists()) {
                file.setContents(new ByteArrayInputStream(arg0), IFile.FORCE, null);
            }
            else {
                file.create(new ByteArrayInputStream(arg0), IFile.FORCE, null);
            }
        }
        catch (CoreException e) {

        }
    }
}
