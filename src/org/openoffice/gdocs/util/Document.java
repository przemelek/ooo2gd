package org.openoffice.gdocs.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Document {
    private boolean convertable;
    private String title;
    private String updated;
    private String documentLink;
    private String editMediaLink;
    private String contentType;
    private String id;
    private Set<String> folders = new HashSet<String>();

    public String getDocumentLink() {
        return this.documentLink;
    }

    public String getId() {
        return this.id;
    }
    
    public String getTitle() {
        return this.title;
    }

    public String getUpdated() {
        return this.updated;
    }

    public Set<String> getFolders() {
        return Collections.unmodifiableSet(folders);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }

    public void setDocumentLink(String documentLink) {
        this.documentLink = documentLink;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void addFolder(String folder) {
        folders.add(folder);
    }

    @Override
    public String toString() {
        return title;
    }

    public boolean isConvertable() {
        return convertable;
    }

    public void setConvertable(boolean convertable) {
        this.convertable = convertable;
    }

    /**
     * @return the editMediaLink
     */
    public String getEditMediaLink() {
        return editMediaLink;
    }

    /**
     * @param editMediaLink the editMediaLink to set
     */
    public void setEditMediaLink(String editMediaLink) {
        this.editMediaLink = editMediaLink;
    }

    /**
     * @return the contentType
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * @param contentType the contentType to set
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
