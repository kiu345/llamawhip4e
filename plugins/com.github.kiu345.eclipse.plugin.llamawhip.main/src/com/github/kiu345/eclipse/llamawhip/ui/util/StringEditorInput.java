package com.github.kiu345.eclipse.llamawhip.ui.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;

public class StringEditorInput implements IStorageEditorInput {
    private final String name;
    private final String content;

    public StringEditorInput(String name, String content) {
        this.name = name;
        this.content = content;
    }

    @Override
    public boolean exists() {
        return true;
    }

    @Override
    public ImageDescriptor getImageDescriptor() {
        return IDEUtils.pluginIcon();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public IPersistableElement getPersistable() {
        return null;
    }

    @Override
    public String getToolTipText() {
        return name;
    }

    @Override
    public <T> T getAdapter(Class<T> adapter) {
        return null;
    }

    @Override
    public IStorage getStorage() throws CoreException {
        return new IStorage() {
            @Override
            public InputStream getContents() {
                return new ByteArrayInputStream(content.getBytes());
            }

            @Override
            public IPath getFullPath() {
                return null;
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public boolean isReadOnly() {
                return false;
            }

            @Override
            public <T> T getAdapter(Class<T> adapter) {
                return null;
            }
        };
    }
}

