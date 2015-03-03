package odi11g;

import common.DmsUtils;

import java.util.ArrayList;
import java.util.List;

import oracle.jbo.Row;
import oracle.jbo.ViewCriteria;
import oracle.jbo.ViewCriteriaRow;
import oracle.jbo.ViewObject;

import org.apache.commons.lang.ObjectUtils;
import org.apache.myfaces.trinidad.model.ChildPropertyTreeModel;
import org.apache.myfaces.trinidad.model.CollectionModel;
import org.apache.myfaces.trinidad.model.SortCriterion;

public class Odi11gCatTreeModel extends ChildPropertyTreeModel {
    public Odi11gCatTreeModel() {
        super();        
        List<Odi11gCatTreeItem> root = this.getChildTreeItem(null);
        for (Odi11gCatTreeItem itm : root) {
                itm.setChildren(this.getChildTreeItem(itm.getId()));
        }
        this.setChildProperty("children");
        this.setWrappedData(root);
    }

    private List<Odi11gCatTreeItem> getChildTreeItem(String pid) {
        pid=pid==null ? "is null" : "='"+pid+"'";
        List<Odi11gCatTreeItem> items = new ArrayList<Odi11gCatTreeItem>();
        ViewObject catVo = DmsUtils.getOdi11gApplicationModule().getOdi11SceneCatView();

        ViewCriteria vc=catVo.createViewCriteria();
        ViewCriteriaRow vcRow = vc.createViewCriteriaRow();
        vcRow.setAttribute("PId", pid);
        vc.addElement(vcRow);
        
        catVo.applyViewCriteria(vc);
        catVo.executeQuery();
        catVo.getViewCriteriaManager().setApplyViewCriteriaNames(null);
        while (catVo.hasNext()) {
            Row row = catVo.next();
            String id=ObjectUtils.toString(row.getAttribute("Id"));
            String label=ObjectUtils.toString(row.getAttribute("CatName"));
            Odi11gCatTreeItem item = new Odi11gCatTreeItem();
            item.setId(id);
            item.setLabel(label);
            items.add(item);
        }
        return items;
    }

    @Override
    protected CollectionModel createChildModel(Object object) {
        return super.createChildModel(object);
    }

    @Override
    public void enterContainer() {
        super.enterContainer();
    }

    @Override
    public void exitContainer() {
        super.exitContainer();
    }

    @Override
    protected Object getChildData(Object object) {
        return super.getChildData(object);
    }

    @Override
    public Object getContainerRowKey(Object object) {
        return super.getContainerRowKey(object);
    }

    @Override
    public int getRowCount() {
        return super.getRowCount();
    }

    @Override
    public Object getRowData() {
        Odi11gCatTreeItem item = (Odi11gCatTreeItem)super.getRowData();
        if (item.getChildren() == null) {
            item.setChildren(this.getChildTreeItem(item.getId()));
        }
        return super.getRowData();
    }

    @Override
    public int getRowIndex() {
        return super.getRowIndex();
    }

    @Override
    public Object getRowKey() {
        return super.getRowKey();
    }

    @Override
    public List<SortCriterion> getSortCriteria() {
        return super.getSortCriteria();
    }

    @Override
    public Object getWrappedData() {
        return super.getWrappedData();
    }

    @Override
    public boolean isContainer() {
        return super.isContainer();
    }

    @Override
    public boolean isRowAvailable() {
        return super.isRowAvailable();
    }

    @Override
    public boolean isSortable(String string) {
        return super.isSortable(string);
    }

    @Override
    public void setRowIndex(int i) {
        super.setRowIndex(i);
    }

    @Override
    public void setRowKey(Object object) {
        super.setRowKey(object);
    }

    @Override
    public void setSortCriteria(List<SortCriterion> list) {
        super.setSortCriteria(list);
    }

    @Override
    public void setWrappedData(Object object) {
        super.setWrappedData(object);
    }
}
