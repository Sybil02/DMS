package dms.groupUser;

import common.ADFUtils;

import common.DmsUtils;

import java.util.Iterator;
import java.util.List;

import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;

import oracle.adf.share.logging.ADFLogger;
import oracle.adf.view.rich.component.rich.RichPopup;
import oracle.adf.view.rich.component.rich.data.RichTable;

import oracle.adf.view.rich.context.AdfFacesContext;

import oracle.jbo.Key;
import oracle.jbo.Row;

import oracle.jbo.RowSetIterator;
import oracle.jbo.ViewObject;

public class UserGroupBean {
    private static ADFLogger logger =
        ADFLogger.createADFLogger(UserGroupBean.class);
    private RichTable groupedUserTable;
    private RichPopup popup;
    private RichTable ungroupedUserTable;

    public UserGroupBean() {
    }

    public void groupChangeListener(ValueChangeEvent valueChangeEvent) {
        AdfFacesContext.getCurrentInstance().addPartialTarget(this.groupedUserTable);
    }

    public void setGroupedUserTable(RichTable groupedUserTable) {
        this.groupedUserTable = groupedUserTable;
    }

    public RichTable getGroupedUserTable() {
        return groupedUserTable;
    }

    public void showAddUserPopup(ActionEvent actionEvent) {
        ViewObject groupVo =
            ADFUtils.findIterator("DmsGroupViewIterator").getViewObject();
        ViewObject ungroupedView =
            ADFUtils.findIterator("DmsUnGroupedUserViewIterator").getViewObject();
        Row curRow = groupVo.getCurrentRow();
        if (curRow != null) {
            String groupId = (String)curRow.getAttribute("Id");
            ungroupedView.setNamedWhereClauseParam("groupId", groupId);
            ungroupedView.executeQuery();
            RichPopup.PopupHints hints = new RichPopup.PopupHints();
            this.popup.show(hints);
        }
    }

    public void removeUserFromGroup(ActionEvent actionEvent) {
        if (this.groupedUserTable.getSelectedRowKeys() != null) {
            ViewObject vo =
                DmsUtils.getDmsApplicationModule().getDmsUserGroupView();
            Iterator itr =
                this.groupedUserTable.getSelectedRowKeys().iterator();
            RowSetIterator rowSetIterator =
                ADFUtils.findIterator("DmsGroupedUserViewIterator").getRowSetIterator();
            while(itr.hasNext()){
                List key = (List)itr.next();
                Row usrRow = rowSetIterator.getRow((Key)key.get(0));
                if(usrRow == null){
                    continue;    
                }
                Key k=new Key(new Object[]{usrRow.getAttribute("Id")});
                Row[] rows=vo.findByKey(k, 1);
                if(rows!=null&&rows.length>0){
                    rows[0].remove();
                }
            }
            vo.getApplicationModule().getTransaction().commit();
            ADFUtils.findIterator("DmsGroupedUserViewIterator").getViewObject().executeQuery();
            AdfFacesContext.getCurrentInstance().addPartialTarget(this.groupedUserTable);
        }
    }

    public void setPopup(RichPopup popup) {
        this.popup = popup;
    }

    public RichPopup getPopup() {
        return popup;
    }

    public void addUserToGroup(ActionEvent actionEvent) {
        if (this.ungroupedUserTable.getSelectedRowKeys() != null) {
            ViewObject vo =
                DmsUtils.getDmsApplicationModule().getDmsUserGroupView();
            String groupId =
                (String)ADFUtils.findIterator("DmsGroupViewIterator").getViewObject().getCurrentRow().getAttribute("Id");
            Iterator itr =
                this.ungroupedUserTable.getSelectedRowKeys().iterator();
            RowSetIterator rowSetIterator =
                ADFUtils.findIterator("DmsUnGroupedUserViewIterator").getRowSetIterator();
            while (itr.hasNext()) {
                List key = (List)itr.next();
                Row usrRow = rowSetIterator.getRow((Key)key.get(0));
                Row row = vo.createRow();
                row.setAttribute("GroupId", groupId);
                row.setAttribute("UserId", usrRow.getAttribute("Id"));
                vo.insertRow(row);
            }
            vo.getApplicationModule().getTransaction().commit();
            ADFUtils.findIterator("DmsUnGroupedUserViewIterator").getViewObject().executeQuery();
            ADFUtils.findIterator("DmsGroupedUserViewIterator").getViewObject().executeQuery();
            AdfFacesContext.getCurrentInstance().addPartialTarget(this.groupedUserTable);
            AdfFacesContext.getCurrentInstance().addPartialTarget(this.ungroupedUserTable);
        }
    }

    public void setUngroupedUserTable(RichTable ungroupedUserTable) {
        this.ungroupedUserTable = ungroupedUserTable;
    }

    public RichTable getUngroupedUserTable() {
        return ungroupedUserTable;
    }
}
