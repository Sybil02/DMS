package dcm.template;

import common.ADFUtils;

import common.JSFUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.event.ActionEvent;

import javax.faces.event.ValueChangeEvent;

import javax.faces.model.SelectItem;

import oracle.adf.view.rich.component.rich.RichPopup;
import oracle.adf.view.rich.component.rich.data.RichTable;

import oracle.adf.view.rich.context.AdfFacesContext;

import oracle.adfinternal.view.faces.model.binding.FacesCtrlListBinding;

import oracle.jbo.Key;
import oracle.jbo.Row;
import oracle.jbo.RowSetIterator;
import oracle.jbo.ViewObject;

import oracle.jbo.uicli.binding.JUCtrlHierNodeBinding;
import oracle.jbo.uicli.binding.JUCtrlValueBinding;

import org.apache.myfaces.trinidad.model.CollectionModel;
import oracle.jbo.uicli.binding.JUCtrlValueBinding.*;

import org.apache.myfaces.trinidad.model.RowKeySet;

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
                RowKeySet rowKeys = this.assignedtemplateTable.getSelectedRowKeys();
                Object[] rowKeySetArray = rowKeys.toArray();
                CollectionModel cm =
                    (CollectionModel)assignedtemplateTable.getValue();

            for (Object key : rowKeySetArray) {
                assignedtemplateTable.setRowKey(key);
                JUCtrlHierNodeBinding rowData =
                    (JUCtrlHierNodeBinding)cm.getRowData();
                if (rowData == null)
                    return;
                
                rowData.getRow().remove();
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
                if (templateRow == null)
                    return ;
                
                Row row = roleTemplateVo.createRow();
                row.setAttribute("RoleId", roleId);
                row.setAttribute("TemplateId", templateRow.getAttribute("Id"));
                roleTemplateVo.insertRow(row);
            }
            roleTemplateVo.getApplicationModule().getTransaction().commit();
            ADFUtils.findIterator("DcmUnAssignedTemplateIterator").getViewObject().executeQuery();
            AdfFacesContext.getCurrentInstance().addPartialTarget(this.unassignedTemplateTable);
            AdfFacesContext.getCurrentInstance().addPartialTarget(this.assignedtemplateTable);
        }

    }

    public void roleChangeListener(ValueChangeEvent valueChangeEvent) {
        FacesCtrlListBinding roleName =  (FacesCtrlListBinding) JSFUtils.resolveExpression("#{bindings.RoleName}");
        roleName.setInputValue(valueChangeEvent.getNewValue());
        AdfFacesContext.getCurrentInstance().addPartialTarget(this.assignedtemplateTable);    
    }


    public void filterModuleName(ValueChangeEvent valueChangeEvent) {
        
        String filterVal = (String) valueChangeEvent.getNewValue();
        FacesCtrlListBinding roleName = (FacesCtrlListBinding) JSFUtils.resolveExpression("#{bindings.RoleName}");
       
        ViewObject vo = (ViewObject) ADFUtils.findIterator("DcmRoleTemplateViewIterator").getViewObject();

        
        vo.setWhereClause("exists (select 1  from dcm_template " + 
                                  "where dcm_template.id = DcmRoleTemplate.Template_Id " + 
                                  "and dcm_template.name like '%"+filterVal+"%')");
       
        
        vo.executeQuery(); 
        AdfFacesContext.getCurrentInstance().addPartialTarget(this.assignedtemplateTable);    
        
    }

    public void filterAddModuleName(ValueChangeEvent valueChangeEvent) {
   
        String filterVal = (String) valueChangeEvent.getNewValue();
        ViewObject vo = (ViewObject) ADFUtils.findIterator("DcmUnAssignedTemplateIterator").getViewObject();

        vo.setWhereClause("QRSLT.name like '%" + filterVal + "%'");
         
        vo.executeQuery(); 
        AdfFacesContext.getCurrentInstance().addPartialTarget(this.unassignedTemplateTable);  
    }
}
