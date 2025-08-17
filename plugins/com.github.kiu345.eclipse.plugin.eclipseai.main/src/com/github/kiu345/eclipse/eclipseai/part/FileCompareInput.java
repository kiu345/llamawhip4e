package com.github.kiu345.eclipse.eclipseai.part;

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
        return file.getName(); // 필요에 따라 이름을 지정할 수 있습니다.
    }

    @Override
    public String getType() {
        return ITypedElement.TEXT_TYPE;
    }

    @Override
    public Image getImage() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isEditable() {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public ITypedElement replace(ITypedElement arg0, ITypedElement arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setContent(byte[] arg0) {
        // TODO Auto-generated method stub
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
