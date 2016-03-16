package infa;

import common.ADFUtils;

import oracle.adf.view.rich.component.rich.data.RichTable;
import oracle.adf.view.rich.component.rich.data.RichTree;

import oracle.adf.view.rich.context.AdfFacesContext;

import oracle.jbo.ViewObject;

import org.apache.myfaces.trinidad.event.SelectionEvent;
import org.apache.myfaces.trinidad.model.RowKeySet;

public class InfaIndexBean {
    
    private InfaCatTreeModel infaTreeModel;
    private RichTable wfTable;

    public InfaIndexBean() {
        super();
    }

    public void setInfaTreeModel(InfaCatTreeModel infaTreeModel) {
        this.infaTreeModel = infaTreeModel;
    }

    public InfaCatTreeModel getInfaTreeModel() {
        if(this.infaTreeModel == null){
            infaTreeModel = new InfaCatTreeModel();        
        }
        return infaTreeModel;
    }

    public void infaTreeSelection(SelectionEvent event) {
        RichTree tree = (RichTree)event.getSource();
        RowKeySet rsk = event.getAddedSet();
        if(rsk != null){
            if(rsk.size()==0){
                return;    
            }    
            Object rowKey = null;
            InfaCatTreeItem treeItem = null;
            rowKey = rsk.iterator().next();
            tree.setRowKey(rowKey);
            treeItem = (InfaCatTreeItem)tree.getRowData();
            String catId = treeItem.getId();
            ViewObject vo = ADFUtils.findIterator("InfaUserWorkflowVOIterator").getViewObject();
            vo.setNamedWhereClauseParam("catId", catId);
            vo.executeQuery();
            AdfFacesContext.getCurrentInstance().addPartialTarget(this.wfTable);
        }
    }

    public void setWfTable(RichTable wfTable) {
        this.wfTable = wfTable;
    }

    public RichTable getWfTable() {
        return wfTable;
    }
}
