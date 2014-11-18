package dcm.template;

import java.util.List;

public class TemplateTreeItem {
    private String id;
    private String label;
    private String type;
    private List<TemplateTreeItem> children;
    public static final String TYPE_CATEGORY="CATEGORY";
    public static final String TYPE_TEMPLATE="TEMPLATE";
    
    public TemplateTreeItem(String id,String label,String type ){
        this.id=id;
        this.label=label;
        this.type=type;
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

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setChildren(List<TemplateTreeItem> children) {
        this.children = children;
    }

    public List<TemplateTreeItem> getChildren() {
        return children;
    }
}
