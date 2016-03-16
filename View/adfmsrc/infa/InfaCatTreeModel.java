package infa;

import common.DmsUtils;

import java.util.ArrayList;
import java.util.List;


import oracle.jbo.Row;
import oracle.jbo.ViewCriteria;
import oracle.jbo.ViewCriteriaRow;
import oracle.jbo.ViewObject;

import org.apache.commons.lang.ObjectUtils;
import org.apache.myfaces.trinidad.model.ChildPropertyTreeModel;

public class InfaCatTreeModel extends ChildPropertyTreeModel{

    public InfaCatTreeModel() {
        super();        
        List<InfaCatTreeItem> root = this.getChildTreeItem(null);
        for (InfaCatTreeItem itm : root) {
                itm.setChildList(this.getChildTreeItem(itm.getId()));
        }
        this.setChildProperty("childList");
        this.setWrappedData(root);
    }

    private List<InfaCatTreeItem> getChildTreeItem(String pid) {
        pid=pid==null ? "is null" : "='"+pid+"'";
        List<InfaCatTreeItem> items = new ArrayList<InfaCatTreeItem>();
        ViewObject catVo = DmsUtils.getInfaApplicationModule().getInfaWorkflowCatVO();

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
            InfaCatTreeItem item = new InfaCatTreeItem();
            item.setId(id);
            item.setLabel(label);
            items.add(item);
        }
        return items;
    }

    @Override
    public Object getRowData() {
        InfaCatTreeItem item = (InfaCatTreeItem)super.getRowData();
        if (item.getChildList() == null) {
            item.setChildList(this.getChildTreeItem(item.getId()));
        }
        return super.getRowData();
    }
}
