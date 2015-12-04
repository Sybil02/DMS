package dms.workflow;

import common.ADFUtils;
import common.DmsUtils;
import common.JSFUtils;
import java.util.Iterator;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import oracle.adf.view.rich.component.rich.RichPopup;
import oracle.adf.view.rich.component.rich.data.RichTable;
import oracle.adf.view.rich.context.AdfFacesContext;
import oracle.adfinternal.view.faces.model.binding.FacesCtrlListBinding;
import oracle.jbo.Key;
import oracle.jbo.Row;
import oracle.jbo.RowSetIterator;
import oracle.jbo.ViewObject;
import oracle.jbo.uicli.binding.JUCtrlHierNodeBinding;

import org.apache.myfaces.trinidad.model.CollectionModel;
import org.apache.myfaces.trinidad.model.RowKeySet;


public class WorkflowAuthorityBean {
    private RichTable assignedworkflowTable;
    private RichTable unassignedworkflowTable;
    private RichPopup popup;


    public WorkflowAuthorityBean() {
        super();
    }

    public void showAddPopup(ActionEvent actionEvent) {
        Row roleRow =
            ADFUtils.findIterator("DmsEnabledRoleIterator").getViewObject().getCurrentRow();
        if (roleRow != null) {
            ViewObject unassignedWorkflowView =
                ADFUtils.findIterator("DmsSetWorkflowInfoVOIterator").getViewObject();
            unassignedWorkflowView.setNamedWhereClauseParam("roleId",
                                                            roleRow.getAttribute("Id"));
            unassignedWorkflowView.executeQuery();
            RichPopup.PopupHints hint = new RichPopup.PopupHints();
            this.popup.show(hint);
        }
    }

    public void removeAuthority(ActionEvent actionEvent) {

        if (this.assignedworkflowTable.getSelectedRowKeys() != null) {
            RowKeySet rowKeys =
                this.assignedworkflowTable.getSelectedRowKeys();
            Object[] rowKeySetArray = rowKeys.toArray();
            CollectionModel cm =
                (CollectionModel)assignedworkflowTable.getValue();

            for (Object key : rowKeySetArray) {
                assignedworkflowTable.setRowKey(key);
                JUCtrlHierNodeBinding rowData =
                    (JUCtrlHierNodeBinding)cm.getRowData();
                if (rowData == null)
                    return;
                
                rowData.getRow().remove();
            }
            ADFUtils.findIterator("DmsRoleWorkflowVOIterator").getViewObject().getApplicationModule().getTransaction().commit();
            AdfFacesContext.getCurrentInstance().addPartialTarget(this.assignedworkflowTable);
        }
    }


    public void setPopup(RichPopup popup) {
        this.popup = popup;
    }

    public RichPopup getPopup() {
        return popup;
    }

    public void addWorkflow(ActionEvent actionEvent) {
        if (this.unassignedworkflowTable.getSelectedRowKeys() != null) {
            ViewObject roleWorkflowVo =
                ADFUtils.findIterator("DmsRoleWorkflowVOIterator").getViewObject();
            String roleId =
                (String)ADFUtils.findIterator("DmsEnabledRoleIterator").getViewObject().getCurrentRow().getAttribute("Id");
            Iterator itr =
                this.unassignedworkflowTable.getSelectedRowKeys().iterator();
            RowSetIterator rowSetIterator =
                ADFUtils.findIterator("DmsSetWorkflowInfoVOIterator").getRowSetIterator();
            while (itr.hasNext()) {
                List key = (List)itr.next();
                Row workflowRow = rowSetIterator.getRow((Key)key.get(0));
                if (workflowRow == null)
                    return;

                Row row = roleWorkflowVo.createRow();
                row.setAttribute("RoleId", roleId);
                row.setAttribute("WorkflowId",
                                 workflowRow.getAttribute("Id").toString());
                roleWorkflowVo.insertRow(row);
            }
            roleWorkflowVo.getApplicationModule().getTransaction().commit();
            ADFUtils.findIterator("DmsRoleWorkflowVOIterator").getViewObject().executeQuery();
            ADFUtils.findIterator("DmsSetWorkflowInfoVOIterator").getViewObject().executeQuery();
            AdfFacesContext.getCurrentInstance().addPartialTarget(this.unassignedworkflowTable);
            AdfFacesContext.getCurrentInstance().addPartialTarget(this.assignedworkflowTable);
        }
    }

    public void roleChangeListener(ValueChangeEvent valueChangeEvent) {
        FacesCtrlListBinding roleName =
            (FacesCtrlListBinding)JSFUtils.resolveExpression("#{bindings.RoleName}");
        roleName.setInputValue(valueChangeEvent.getNewValue());
        AdfFacesContext.getCurrentInstance().addPartialTarget(this.assignedworkflowTable);
    }

    public void setAssignedworkflowTable(RichTable assignedworkflowTable) {
        this.assignedworkflowTable = assignedworkflowTable;
    }

    public RichTable getAssignedworkflowTable() {
        return assignedworkflowTable;
    }

    public void setUnassignedworkflowTable(RichTable unassignedworkflowTable) {
        this.unassignedworkflowTable = unassignedworkflowTable;
    }

    public RichTable getUnassignedworkflowTable() {
        return unassignedworkflowTable;
    }

    public void enableRunChange(ValueChangeEvent valueChangeEvent) {
        //更新模型层
        valueChangeEvent.getComponent().processUpdates(FacesContext.getCurrentInstance());
        //提交之前要更新模型层，valueChangeEvent发生在更新模型层之前，commit只会提交模型层的数据，直接提交不会把新的值提交到数据库
        ADFUtils.findIterator("DmsRoleWorkflowVOIterator").getViewObject().getApplicationModule().getTransaction().commit();
        
    }
}


