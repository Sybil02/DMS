package infa;

import common.ADFUtils;

import java.util.Iterator;

import java.util.List;

import javax.faces.event.ActionEvent;

import oracle.adf.view.rich.component.rich.RichPopup;
import oracle.adf.view.rich.component.rich.data.RichTable;

import oracle.adf.view.rich.context.AdfFacesContext;

import oracle.jbo.Key;
import oracle.jbo.Row;
import oracle.jbo.RowSetIterator;
import oracle.jbo.ViewObject;

public class InfaAuthrityBean {
    private RichTable authedTable;
    private RichPopup workflowPop;
    private RichTable unAuthedTable;

    public InfaAuthrityBean() {
        super();
    }

    public void remove(ActionEvent actionEvent) {
        if(this.authedTable.getSelectedRowKeys() != null){
            Iterator iter = this.authedTable.getSelectedRowKeys().iterator();    
            RowSetIterator rsi = ADFUtils.findIterator("InfaRoleWorkflowVOIterator").getRowSetIterator();
            while(iter.hasNext()){
                List key = (List)iter.next();
                Row row = rsi.getRow((Key)key.get(0));
                if(row != null){
                    row.remove();    
                }
            }
            ADFUtils.findIterator("InfaRoleWorkflowVOIterator").getViewObject().getApplicationModule().getTransaction().commit();
            AdfFacesContext.getCurrentInstance().addPartialTarget(this.authedTable);
        }
    }

    public void showAddPop(ActionEvent actionEvent) {
        ViewObject unroleWfVO=ADFUtils.findIterator("InfaUnauthedWorkflowVOIterator").getViewObject();
        Row roleRow=ADFUtils.findIterator("DmsEnabledRoleIterator").getViewObject().getCurrentRow();
        if(roleRow!=null){
            unroleWfVO.setNamedWhereClauseParam("roleId", roleRow.getAttribute("Id"));
            unroleWfVO.executeQuery();
            RichPopup.PopupHints hint =new RichPopup.PopupHints();
            this.workflowPop.show(hint);
        }
    }

    public void setAuthedTable(RichTable authedTable) {
        this.authedTable = authedTable;
    }

    public RichTable getAuthedTable() {
        return authedTable;
    }

    public void setWorkflowPop(RichPopup workflowPop) {
        this.workflowPop = workflowPop;
    }

    public RichPopup getWorkflowPop() {
        return workflowPop;
    }

    public void addWf(ActionEvent actionEvent) {
        if(this.unAuthedTable.getSelectedRowKeys() != null){
            Iterator iter = this.unAuthedTable.getSelectedRowKeys().iterator();   
            ViewObject vo = ADFUtils.findIterator("InfaRoleWorkflowVOIterator").getViewObject();
            String roleId =(String)ADFUtils.findIterator("DmsEnabledRoleIterator").getViewObject().getCurrentRow().getAttribute("Id");
            RowSetIterator rsi = ADFUtils.findIterator("InfaUnauthedWorkflowVOIterator").getRowSetIterator();
            while(iter.hasNext()){
                List key = (List)iter.next();
                Row unRow = rsi.getRow((Key)key.get(0));
                Row row = vo.createRow();
                row.setAttribute("RoleId", roleId);
                row.setAttribute("WorkflowId", unRow.getAttribute("Id"));
                vo.insertRow(row);
            }
            vo.getApplicationModule().getTransaction().commit();
            ADFUtils.findIterator("InfaRoleWorkflowVOIterator").getViewObject().executeQuery();
            ADFUtils.findIterator("InfaUnauthedWorkflowVOIterator").getViewObject().executeQuery();
            AdfFacesContext.getCurrentInstance().addPartialTarget(this.authedTable);
            AdfFacesContext.getCurrentInstance().addPartialTarget(this.unAuthedTable);
        }
    }

    public void setUnAuthedTable(RichTable unAuthedTable) {
        this.unAuthedTable = unAuthedTable;
    }

    public RichTable getUnAuthedTable() {
        return unAuthedTable;
    }
}
