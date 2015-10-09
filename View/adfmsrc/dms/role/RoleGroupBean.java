package dms.role;

import com.sun.faces.context.FacesContextImpl;

import common.ADFUtils;
import common.DmsUtils;


import common.JSFUtils;

import java.util.Iterator;
import java.util.List;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import oracle.adf.share.logging.ADFLogger;
import oracle.adf.view.rich.component.rich.RichPopup;
import oracle.adf.view.rich.component.rich.data.RichTable;
import oracle.adf.view.rich.context.AdfFacesContext;
import oracle.adfinternal.view.faces.model.binding.FacesCtrlListBinding;
import oracle.jbo.Key;
import oracle.jbo.Row;
import oracle.jbo.RowSetIterator;
import oracle.jbo.ViewObject;

public class RoleGroupBean {

    private static ADFLogger logger =
        ADFLogger.createADFLogger(RoleGroupBean.class);


    private RichTable selectedRoleTable;
    private RichPopup popup;
    private RichTable unSelectedRoleTable;

    public RoleGroupBean() {


    }

    public void setSelectedRoleTable(RichTable selectedRoleTable) {
        this.selectedRoleTable = selectedRoleTable;
    }

    public RichTable getSelectedRoleTable() {
        return selectedRoleTable;
    }

    public void groupChangeListener(ValueChangeEvent valueChangeEvent) {
        FacesCtrlListBinding groupName =
            (FacesCtrlListBinding)JSFUtils.resolveExpression("#{bindings.GroupName}");
        groupName.setInputValue(valueChangeEvent.getNewValue());
        AdfFacesContext.getCurrentInstance().addPartialTarget(this.selectedRoleTable);
    }

    public void setPopup(RichPopup popup) {
        this.popup = popup;
    }

    public RichPopup getPopup() {
        return popup;
    }

    public void showPopup(ActionEvent actionEvent) {
        Row curGroup =
            ADFUtils.findIterator("DmsEnabledGroupViewIterator").getCurrentRow();
        if (curGroup != null) {
            ViewObject vo =
                ADFUtils.findIterator("DmsUnGroupedRoleViewIterator").getViewObject();
            vo.setNamedWhereClauseParam("groupId",
                                        curGroup.getAttribute("Id"));
            vo.executeQuery();
            RichPopup.PopupHints hint = new RichPopup.PopupHints();
            this.popup.show(hint);
        }
    }

    public void removeRole(ActionEvent actionEvent) {
        if (this.selectedRoleTable.getSelectedRowKeys() != null) {
            ViewObject vo =
                DmsUtils.getDmsApplicationModule().getDmsGroupRoleView();
            Object[] rowKeySetArray = selectedRoleTable.getSelectedRowKeys().toArray();

            RowSetIterator rowSetIterator =
                ADFUtils.findIterator("DmsGroupedRoleViewIterator").getRowSetIterator();
            
            for (Object obj : rowKeySetArray) {
                List key = (List)obj;
                Row roleRow = rowSetIterator.getRow((Key)key.get(0));
                if (roleRow == null)
                    return ;
                
                Key k=new Key(new Object[]{roleRow.getAttribute("Id")});
                Row[] rows=vo.findByKey(k, 1);
                if(rows!=null&&rows.length>0){
                    rows[0].remove();
                }
            }
            vo.getApplicationModule().getTransaction().commit();
            ADFUtils.findIterator("DmsGroupedRoleViewIterator").getViewObject().executeQuery();
            AdfFacesContext.getCurrentInstance().addPartialTarget(this.selectedRoleTable);
        }
    }

    public void addRoleToGroup(ActionEvent actionEvent) {
        long now = System.currentTimeMillis();
        if (this.unSelectedRoleTable.getSelectedRowKeys() != null) {
            ViewObject vo =
                DmsUtils.getDmsApplicationModule().getDmsGroupRoleView();
            String groupId =
                (String)ADFUtils.findIterator("DmsEnabledGroupViewIterator").getViewObject().getCurrentRow().getAttribute("Id");
            Iterator itr =
                this.unSelectedRoleTable.getSelectedRowKeys().iterator();
            RowSetIterator rowSetIterator =
                ADFUtils.findIterator("DmsUnGroupedRoleViewIterator").getRowSetIterator();
            while (itr.hasNext()) {
                List key = (List)itr.next();
                Row roleRow = rowSetIterator.getRow((Key)key.get(0));
                if (roleRow == null)
                    return ;
                
                Row row = vo.createRow();
                row.setAttribute("GroupId", groupId);
                row.setAttribute("RoleId", roleRow.getAttribute("Id"));
                vo.insertRow(row);
            }
            vo.getApplicationModule().getTransaction().commit();
            ADFUtils.findIterator("DmsUnGroupedRoleViewIterator").getViewObject().executeQuery();
            ADFUtils.findIterator("DmsGroupedRoleViewIterator").getViewObject().executeQuery();
            AdfFacesContext.getCurrentInstance().addPartialTarget(this.selectedRoleTable);
            AdfFacesContext.getCurrentInstance().addPartialTarget(this.unSelectedRoleTable);
        }
        System.out.println(System.currentTimeMillis() - now);
    }

    public void setUnSelectedRoleTable(RichTable unSelectedRoleTable) {
        this.unSelectedRoleTable = unSelectedRoleTable;
    }

    public RichTable getUnSelectedRoleTable() {
        return unSelectedRoleTable;
    }
}
