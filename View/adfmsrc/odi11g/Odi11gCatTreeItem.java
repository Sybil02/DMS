package odi11g;

import java.util.List;

public class Odi11gCatTreeItem {
    private String id;
    private String label;
    private List<Odi11gCatTreeItem> children;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public void setChildren(List<Odi11gCatTreeItem> children) {
        this.children = children;
    }

    public List<Odi11gCatTreeItem> getChildren() {
        return children;
    }
}
