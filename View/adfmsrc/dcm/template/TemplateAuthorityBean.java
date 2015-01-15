package dcm.template;

import common.ADFUtils;

import java.util.Iterator;
import java.util.List;

import javax.faces.event.ActionEvent;

import javax.faces.event.ValueChangeEvent;

import oracle.adf.view.rich.component.rich.RichPopup;
import oracle.adf.view.rich.component.rich.data.RichTable;

import oracle.adf.view.rich.context.AdfFacesContext;

import oracle.jbo.Key;
import oracle.jbo.Row;
import oracle.jbo.RowSetIterator;
import oracle.jbo.ViewObject;

public class TemplateAuthorityBean {
    private RichTable assignedtemplateTable;
    private RichTable unassignedTemplateTable;
    private RichPopup popup;

    public TemplateAuthorityBean() {
    }

    public void showAddPopup(ActionEvent actionEvent) {
        Row roleRow =
            ADFUtils.findIterator("DmsEnabledRoleIterator").getViewObject().getCurrentRow();
        if (roleRow != null) {
            ViewObject unassignedTempalteView =
                ADFUtils.findIterator("DcmUnAssignedTemplateIterator").getViewObject();
            unassignedTempalteView.setNamedWhereClauseParam("roleId",
                                                            roleRow.getAttribute("Id"));
            unassignedTempalteView.executeQuery();
            RichPopup.PopupHints hint = new RichPopup.PopupHints();
            this.popup.show(hint);
        }
    }

    public void removeAuthority(ActionEvent actionEvent) {
        if (this.assignedtemplateTable.getSelectedRowKeys() != null) {
            Iterator itr =
                this.assignedtemplateTable.getSelectedRowKeys().iterator();
            RowSetIterator rowSetIterator =
                ADFUtils.findIterator("DcmRoleTemplateViewIterator").getRowSetIterator();
            while(itr.hasNext()){
                List key = (List)itr.next();
                Row row = rowSetIterator.getRow((Key)key.get(0));
                if(row!=null){
                    row.remove();
                }
            }
            ADFUtils.findIterator("DcmRoleTemplateViewIterator").getViewObject().getApplicationModule().getTransaction().commit();
            AdfFacesContext.getCurrentInstance().addPartialTarget(this.assignedtemplateTable);
        }  
    }

    public void setAssignedtemplateTable(RichTable assignedtemplateTable) {
        this.assignedtemplateTable = assignedtemplateTable;
    }

    public RichTable getAssignedtemplateTable() {
        return assignedtemplateTable;
    }

    public void setUnassignedTemplateTable(RichTable unassignedTemplateTable) {
        this.unassignedTemplateTable = unassignedTemplateTable;
    }

    public RichTable getUnassignedTemplateTable() {
        return unassignedTemplateTable;
    }

    public void setPopup(RichPopup popup) {
        this.popup = popup;
    }

    public RichPopup getPopup() {
        return popup;
    }

    public void addTemplate(ActionEvent actionEvent) {
        if (this.unassignedTemplateTable.getSelectedRowKeys() != null) {
            ViewObject roleTemplateVo =
                ADFUtils.findIterator("DcmRoleTemplateViewIterator").getViewObject();
            String roleId =
                (String)ADFUtils.findIterator("DmsEnabledRoleIterator").getViewObject().getCurrentRow().getAttribute("Id");
            Iterator itr =
                this.unassignedTemplateTable.getSelectedRowKeys().iterator();
            RowSetIterator rowSetIterator =
                ADFUtils.findIterator("DcmUnAssignedTemplateIterator").getRowSetIterator();
            while (itr.hasNext()) {
                List key = (List)itr.next();
                Row templateRow = rowSetIterator.getRow((Key)key.get(0));
                Row row = roleTemplateVo.createRow();
                row.setAttribute("RoleId", roleId);
                row.setAttribute("TemplateId", templateRow.getAttribute("Id"));
                roleTemplateVo.insertRow(row);
            }
            roleTemplateVo.getApplicationModule().getTransaction().commit();
            ADFUtils.findIterator("DcmRoleTemplateViewIterator").getViewObject().executeQuery();
            ADFUtils.findIterator("DcmUnAssignedTemplateIterator").getViewObject().executeQuery();
            AdfFacesContext.getCurrentInstance().addPartialTarget(this.unassignedTemplateTable);
            AdfFacesContext.getCurrentInstance().addPartialTarget(this.assignedtemplateTable);
        }
    }

    public void roleChangeListener(ValueChangeEvent valueChangeEvent) {
        AdfFacesContext.getCurrentInstance().addPartialTarget(this.assignedtemplateTable);    
    }
}
