package com.github.kiu345.eclipse.eclipseai.part;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.graphics.Image;

class TextCompareInput implements IStreamContentAccessor, ITypedElement {
    private IDocument document;

    public TextCompareInput(IDocument document) {
        this.document = document;
    }

    @Override
    public InputStream getContents() throws CoreException {
        return new ByteArrayInputStream(document.get().getBytes());
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public String getType() {
        return ITypedElement.TEXT_TYPE;
    }

    @Override
    public Image getImage() {
        return null;
    }
}
