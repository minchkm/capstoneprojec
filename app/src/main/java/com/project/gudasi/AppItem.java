package com.project.gudasi;

public class AppItem {
    private int iconRes;
    private String name;

    public AppItem(int iconRes, String name) {
        this.iconRes = iconRes;
        this.name = name;
    }

    public int getIconRes() {
        return iconRes;
    }

    public String getName() {
        return name;
    }
}
