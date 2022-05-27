package org.opencb.commons.api.config;

import java.util.List;

public class ApiConfig {
    private List<CategoryConfig> categoryConfigList;
    private List<Shortcut> shortcuts;

    public ApiConfig() {
    }

    public ApiConfig(List<CategoryConfig> categoryConfigList) {
        this.categoryConfigList = categoryConfigList;
    }

    public List<CategoryConfig> getCategoryConfigList() {
        return categoryConfigList;
    }

    public ApiConfig setCategoryConfigList(List<CategoryConfig> categoryConfigList) {
        this.categoryConfigList = categoryConfigList;
        return this;
    }

    public List<Shortcut> getShortcuts() {
        return shortcuts;
    }

    public ApiConfig setShortcuts(List<Shortcut> shortcuts) {
        this.shortcuts = shortcuts;
        return this;
    }
}
