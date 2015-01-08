package dms.function;

import common.ADFUtils;
import common.DmsUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import oracle.adf.share.ADFContext;
import oracle.adf.view.rich.component.rich.RichPopup;
import oracle.adf.view.rich.component.rich.data.RichTable;
import oracle.adf.view.rich.component.rich.input.RichSelectManyShuttle;
import oracle.adf.view.rich.context.AdfFacesContext;

import oracle.jbo.Key;
import oracle.jbo.Row;

import oracle.jbo.RowSetIterator;
import oracle.jbo.ViewObject;

import org.apache.commons.lang.ObjectUtils;
import org.apache.myfaces.trinidad.event.SelectionEvent;

import team.epm.module.DmsModuleImpl;

public class FunctionAuthorityBean{

    private RichTable assignedFunctionTable;
    private RichPopup popup;
    private RichTable unassignedFunctionTable;

    public void roleChangeListener(ValueChangeEvent valueChangeEvent) {
        AdfFacesContext.getCurrentInstance().addPartialTarget(this.assignedFunctionTable);
    }

    public void setAssignedFunctionTable(RichTable assignedFunctionTable) {
        this.assignedFunctionTable = assignedFunctionTable;
    }

    public RichTable getAssignedFunctionTable() {
        return assignedFunctionTable;
    }

    public void showPopup(ActionEvent actionEvent) {
        Row curGroup=ADFUtils.findIterator("DmsEnabledRoleIterator").getCurrentRow();
        if(curGroup!=null){
            ViewObject vo=ADFUtils.findIterator("DmsUnassignedFunctionViewIterator").getViewObject();
            vo.setNamedWhereClauseParam("roleId", curGroup.getAttribute("Id"));
            vo.executeQuery();
            RichPopup.PopupHints hint=new RichPopup.PopupHints();
            this.popup.show(hint);
        }
    }

    public void removeFunction(ActionEvent actionEvent) {
        if (this.assignedFunctionTable.getSelectedRowKeys() != null) {
            Iterator itr =
                this.assignedFunctionTable.getSelectedRowKeys().iterator();
            RowSetIterator rowSetIterator =
                ADFUtils.findIterator("DmsRoleFunctionViewIterator").getRowSetIterator();
            while(itr.hasNext()){
                List key = (List)itr.next();
                Row row = rowSetIterator.getRow((Key)key.get(0));
                if(row!=null){
                    row.remove();
                }
            }
            ADFUtils.findIterator("DmsRoleFunctionViewIterator").getViewObject().getApplicationModule().getTransaction().commit();
            AdfFacesContext.getCurrentInstance().addPartialTarget(this.assignedFunctionTable);
        }        
    }

    public void setPopup(RichPopup popup) {
        this.popup = popup;
    }

    public RichPopup getPopup() {
        return popup;
    }

    public void add_function(ActionEvent actionEvent) {
        if (this.unassignedFunctionTable.getSelectedRowKeys() != null) {
            ViewObject roleFunctionVo =ADFUtils.findIterator("DmsRoleFunctionViewIterator").getViewObject();
            String roleId =(String)ADFUtils.findIterator("DmsEnabledRoleIterator").getViewObject().getCurrentRow().getAttribute("Id");
            Iterator itr = this.unassignedFunctionTable.getSelectedRowKeys().iterator();
            RowSetIterator rowSetIterator =
                ADFUtils.findIterator("DmsUnassignedFunctionViewIterator").getRowSetIterator();
            while (itr.hasNext()) {
                List key = (List)itr.next();
                Row functionRow = rowSetIterator.getRow((Key)key.get(0));
                Row row = roleFunctionVo.createRow();
                row.setAttribute("RoleId", roleId);
                row.setAttribute("FunctionId", functionRow.getAttribute("Id"));
                roleFunctionVo.insertRow(row);
            }
            roleFunctionVo.getApplicationModule().getTransaction().commit();
            ADFUtils.findIterator("DmsRoleFunctionViewIterator").getViewObject().executeQuery();
            ADFUtils.findIterator("DmsUnassignedFunctionViewIterator").getViewObject().executeQuery();
            AdfFacesContext.getCurrentInstance().addPartialTarget(this.assignedFunctionTable);
            AdfFacesContext.getCurrentInstance().addPartialTarget(this.unassignedFunctionTable);
        }
    }

    public void setUnassignedFunctionTable(RichTable unassignedFunctionTable) {
        this.unassignedFunctionTable = unassignedFunctionTable;
    }

    public RichTable getUnassignedFunctionTable() {
        return unassignedFunctionTable;
    }
}
