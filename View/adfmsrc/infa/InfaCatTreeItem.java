package infa;

import java.util.List;

public class InfaCatTreeItem {
    
    private String id;
    private String label;
    private List<InfaCatTreeItem> childList;
    
    public InfaCatTreeItem() {
        super();
    }

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

    public void setChildList(List<InfaCatTreeItem> childList) {
        this.childList = childList;
    }

    public List<InfaCatTreeItem> getChildList() {
        return childList;
    }
}
