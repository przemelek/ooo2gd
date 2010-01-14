package org.openoffice.gdocs.util;

public class Document {
    private String title;
    private String updated;
    private String documentLink;
    private String id;

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

    @Override
    public String toString() {
        return title;
    }
}
