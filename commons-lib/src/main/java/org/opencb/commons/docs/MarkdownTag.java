package org.opencb.commons.docs;

import com.sun.javadoc.Tag;

public class MarkdownTag {

    private Tag tag;
    private String name;
    private String description;

    public MarkdownTag(Tag tag) {
        this.tag = tag;
        initialize();
    }

    private void initialize() {
        name = tag.name();
        description = tag.text();
    }

    public Tag getTag() {
        return tag;
    }

    public MarkdownTag setTag(Tag tag) {
        this.tag = tag;
        return this;
    }

    public String getName() {
        return name;
    }

    public MarkdownTag setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public MarkdownTag setDescription(String description) {
        this.description = description;
        return this;
    }
}
