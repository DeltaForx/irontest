package io.irontest.models;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Created by Zheng on 10/09/2016.
 */
public enum FolderTreeNodeType {
    FOLDER("folder"), TESTCASE("testcase");

    private final String text;

    private FolderTreeNodeType(String text) {
        this.text = text;
    }

    @Override
    @JsonValue
    public String toString() {
        return text;
    }

    public static FolderTreeNodeType getByText(String text) {
        for (FolderTreeNodeType e : values()) {
            if (e.text.equals(text)) {
                return e;
            }
        }
        return null;
    }
}
