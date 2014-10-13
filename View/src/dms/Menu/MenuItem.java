package dms.Menu;

import java.util.List;

import team.epm.dms.view.DmsMenuTreeViewRowImpl;

public class MenuItem {
    private String id;
    private String pid;
    private String label;
    private String functionId;
    private String action;
    private List<MenuItem> children;

   public MenuItem(DmsMenuTreeViewRowImpl row) {
        this.id=row.getId();
        this.pid=row.getPId();
        this.label=row.getLabel();
        this.functionId=row.getFunctionId();
        this.action=row.getAction();
    }

    public MenuItem() {
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getPid() {
        return pid;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public void setFunctionId(String functionId) {
        this.functionId = functionId;
    }

    public String getFunctionId() {
        return functionId;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getAction() {
        return action;
    }

    public void setChildren(List<MenuItem> children) {
        this.children = children;
    }

    public List<MenuItem> getChildren() {
        return children;
    }
}
