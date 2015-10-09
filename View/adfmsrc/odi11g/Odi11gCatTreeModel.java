package odi11g;

import common.DmsUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.util.Map;

import oracle.adf.share.ADFContext;

import oracle.jbo.Row;
import oracle.jbo.ViewCriteria;
import oracle.jbo.ViewCriteriaRow;
import oracle.jbo.ViewObject;
import oracle.jbo.domain.Number;

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
    
    /*
     * 判断当前节点是否显示，如果子节点没有内容则不显示，递归搜索
     */
    private boolean isMenuVisible(String id) {
        //缓存机制，如果搜索过则不搜索
        if (nodeVisible.containsKey(id)) {
            return nodeVisible.get(id);
        }

        ViewObject catVo =
            DmsUtils.getOdi11gApplicationModule().getOdi11SceneAuthorityView(ADFContext.getCurrent().getLocale().toString());

        catVo.setWhereClause("P_ID = '" + id + "'");
        catVo.executeQuery();
        
        //如果是没有子节点，那么这个就是端点，要判断下这个端点是否有内容
        if (catVo.getRowCount() < 1) {
            catVo.reset();
            catVo.setWhereClause("ID = '" + id + "'");
            catVo.executeQuery();
            Row row = catVo.first();
            if (((Number)(row.getAttribute("AUTHORITY"))).intValue() > 0) {
                nodeVisible.put(id, true);
                return true;
            }else { 
                nodeVisible.put(id, false);
                return false;
            }
        }
        //用来保存起数据，后面循环递归判断这个节点是否有内容
        List<String> list = new ArrayList<String>();

        while (catVo.hasNext()) {

            Row row = catVo.next();
            String nid = (String)row.getAttribute("ID");
            int isVisible = ((Number)row.getAttribute("AUTHORITY")).intValue();

            //如果包含有内容的子节点，则马上返回，因为这个节点肯定有内容，如果没有则
            if (isVisible == 0)
                list.add(nid);
            else {
                nodeVisible.put(nid, true);
                return true;
            }
        }

        for (String npid : list) {
            boolean isVisible = isMenuVisible(npid);

            if (isVisible == true) {
                nodeVisible.put(npid, isVisible);
                return true;
            }
        }
        nodeVisible.put(id, false);
        return false;
    }

    //判断这个节点是否出现
    Map<String, Boolean> nodeVisible = new HashMap<String, Boolean>();

    private List<Odi11gCatTreeItem> getChildTreeItem(String pid) {
        pid = pid == null ? "is null" : "='" + pid + "'";
        List<Odi11gCatTreeItem> items = new ArrayList<Odi11gCatTreeItem>();
        ViewObject catVo =
            DmsUtils.getOdi11gApplicationModule().getOdi11SceneCatView();

        ViewCriteria vc = catVo.createViewCriteria();
        ViewCriteriaRow vcRow = vc.createViewCriteriaRow();
        vcRow.setAttribute("PId", pid);

        vc.addElement(vcRow);

        catVo.applyViewCriteria(vc);
        catVo.executeQuery();
        catVo.getViewCriteriaManager().setApplyViewCriteriaNames(null);

        while (catVo.hasNext()) {
            Row row = catVo.next();
            String id = ObjectUtils.toString(row.getAttribute("Id"));
            String label = ObjectUtils.toString(row.getAttribute("CatName"));
            if (isMenuVisible(id)) {
                Odi11gCatTreeItem item = new Odi11gCatTreeItem();
                item.setId(id);
                item.setLabel(label);
                items.add(item);
            }
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
