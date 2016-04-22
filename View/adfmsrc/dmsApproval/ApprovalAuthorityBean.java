package dmsApproval;

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

public class ApprovalAuthorityBean {
    private RichPopup popup;
    private RichTable authorityTable;
    private RichTable unAuthorityTable;

    public ApprovalAuthorityBean() {
        super();
    }

    public void setPopup(RichPopup popup) {
        this.popup = popup;
    }

    public RichPopup getPopup() {
        return popup;
    }

    public void addApproval(ActionEvent actionEvent) {
        if (this.unAuthorityTable.getSelectedRowKeys() != null) {
            ViewObject roleTemplateVo =
                ADFUtils.findIterator("DmsRoleApprovalVOIterator").getViewObject();
            String roleId =
                (String)ADFUtils.findIterator("DmsEnabledRoleIterator").getViewObject().getCurrentRow().getAttribute("Id");
            Iterator itr =
                this.unAuthorityTable.getSelectedRowKeys().iterator();
            RowSetIterator rowSetIterator =
                ADFUtils.findIterator("DmsUnAssignedAppVOIterator").getRowSetIterator();
            while (itr.hasNext()) {
                List key = (List)itr.next();
                Row templateRow = rowSetIterator.getRow((Key)key.get(0));
                Row row = roleTemplateVo.createRow();
                row.setAttribute("RoleId", roleId);
                row.setAttribute("ApprovalId", templateRow.getAttribute("Id"));
                row.setAttribute("EnableRun", "N");
                roleTemplateVo.insertRow(row);
            }
            roleTemplateVo.getApplicationModule().getTransaction().commit();
            ADFUtils.findIterator("DmsRoleApprovalVOIterator").getViewObject().executeQuery();
            ADFUtils.findIterator("DmsUnAssignedAppVOIterator").getViewObject().executeQuery();
            AdfFacesContext.getCurrentInstance().addPartialTarget(this.unAuthorityTable);
            AdfFacesContext.getCurrentInstance().addPartialTarget(this.authorityTable);
        }
    }

    public void showAddPop(ActionEvent actionEvent) {
        Row roleRow =
            ADFUtils.findIterator("DmsEnabledRoleIterator").getViewObject().getCurrentRow();
        if (roleRow != null) {
            ViewObject unassignedTempalteView =
                ADFUtils.findIterator("DmsUnAssignedAppVOIterator").getViewObject();
            unassignedTempalteView.setNamedWhereClauseParam("roleId",
                                                            roleRow.getAttribute("Id"));
            unassignedTempalteView.executeQuery();
            RichPopup.PopupHints hint = new RichPopup.PopupHints();
            this.popup.show(hint);
        }
    }

    public void removeApp(ActionEvent actionEvent) {
        if (this.authorityTable.getSelectedRowKeys() != null) {
            Iterator itr =
                this.authorityTable.getSelectedRowKeys().iterator();
            RowSetIterator rowSetIterator =
                ADFUtils.findIterator("DmsRoleApprovalVOIterator").getRowSetIterator();
            while(itr.hasNext()){
                List key = (List)itr.next();
                Row row = rowSetIterator.getRow((Key)key.get(0));
                if(row!=null){
                    row.remove();
                }
            }
            ADFUtils.findIterator("DmsRoleApprovalVOIterator").getViewObject().getApplicationModule().getTransaction().commit();
            AdfFacesContext.getCurrentInstance().addPartialTarget(this.authorityTable);
        }
    }

    public void setAuthorityTable(RichTable authorityTable) {
        this.authorityTable = authorityTable;
    }

    public RichTable getAuthorityTable() {
        return authorityTable;
    }

    public void setUnAuthorityTable(RichTable unAuthorityTable) {
        this.unAuthorityTable = unAuthorityTable;
    }

    public RichTable getUnAuthorityTable() {
        return unAuthorityTable;
    }
}
