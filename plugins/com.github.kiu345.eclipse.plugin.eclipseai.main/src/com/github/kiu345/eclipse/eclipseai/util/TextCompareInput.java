package com.github.kiu345.eclipse.eclipseai.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;

public class TextCompareInput implements IStreamContentAccessor, ITypedElement {
    private String document;

    public TextCompareInput(String document) {
        this.document = document;
    }

    @Override
    public InputStream getContents() throws CoreException {
        return new ByteArrayInputStream(document.getBytes());
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
