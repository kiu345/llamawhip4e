package com.github.kiu345.eclipse.llamawhip.messaging.attachment;

import java.util.Objects;

import org.eclipse.core.resources.IFile;

public class FileAttachment {
    IFile file;
    String shortName;

    public FileAttachment() {
    }

    public FileAttachment(IFile file) {
        super();
        setFile(file);
    }

    public FileAttachment(IFile file, String shortName) {
        super();
        this.file = file;
        this.shortName = shortName;
    }

    public IFile getFile() {
        return file;
    }

    public void setFile(IFile file) {
        if (shortName == null) {
            try {
                shortName = file.getFullPath().lastSegment();
            }
            catch (Exception ex) {
                shortName = file.getName();
            }
        }
        this.file = file;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    @Override
    public String toString() {
        return shortName;
    }

    @Override
    public int hashCode() {
        return Objects.hash(file);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof FileAttachment)) {
            return false;
        }
        FileAttachment other = (FileAttachment) obj;
        return Objects.equals(file, other.file);
    }

}
