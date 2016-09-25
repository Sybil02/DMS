package dms.groupUser;

import common.ADFUtils;

import common.DmsUtils;

import common.lov.DmsComBoxLov;

import common.lov.ValueSetRow;

import dms.login.Person;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.sql.Statement;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;

import javax.faces.model.SelectItem;

import oracle.adf.share.ADFContext;
import oracle.adf.share.logging.ADFLogger;
import oracle.adf.view.rich.component.rich.RichPopup;
import oracle.adf.view.rich.component.rich.data.RichTable;

import oracle.adf.view.rich.context.AdfFacesContext;

import oracle.jbo.Key;
import oracle.jbo.Row;

import oracle.jbo.RowSetIterator;
import oracle.jbo.ViewObject;
import oracle.jbo.server.DBTransaction;

public class UserGroupBean {
    private static ADFLogger logger =
        ADFLogger.createADFLogger(UserGroupBean.class);
    private RichTable groupedUserTable;
    private RichPopup popup;
    private RichTable ungroupedUserTable;
    private DmsComBoxLov groupLov;
    private String groupName;
    public UserGroupBean() {
        this.initGroupLov();
    }

    public void initGroupLov() {
            List<SelectItem> values = new ArrayList<SelectItem>();
            DBTransaction trans =
                (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
            Statement stat = trans.createStatement(DBTransaction.DEFAULT);
            Person person = (Person)ADFContext.getCurrent().getSessionScope().get("cur_user");
            String sql =
                "SELECT T.ID,T.NAME FROM DMS_GROUP T WHERE T.LOCALE='" + person.getLocale() +
                "'  AND T.ENABLE_FLAG = 'Y' ORDER BY T.NAME";
            ResultSet rs;
            try {
                rs = stat.executeQuery(sql);
                List<ValueSetRow> list = new ArrayList<ValueSetRow>();
                while (rs.next()) {
                    SelectItem item = new SelectItem();
                    item.setLabel(rs.getString("ID"));
                    item.setValue(rs.getString("NAME"));
                    values.add(item);
                    //模糊搜索框
                    ValueSetRow vsr =
                        new ValueSetRow(rs.getString("ID"), rs.getString("NAME"),
                                        rs.getString("ID"));
                    list.add(vsr);
                    this.groupName = rs.getString("NAME");
                }
                this.groupLov = new DmsComBoxLov(list);
                ViewObject vo =
                    ADFUtils.findIterator("DmsGroupViewIterator").getViewObject();
                String wc = " NAME = '" + this.groupName + "'";
                vo.setWhereClause(wc);
                vo.executeQuery();
                if (vo.hasNext()) {
                    Row row = vo.first();
                    vo.setCurrentRow(row);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            //get vo
            //curRow.getAtt();
            //
        }
    
    public void groupChangeListener(ValueChangeEvent valueChangeEvent) {
        ViewObject vo =
                ADFUtils.findIterator("DmsGroupViewIterator").getViewObject();
            String wc = " NAME = '" + valueChangeEvent.getNewValue() + "'";
            vo.setWhereClause(wc);
            vo.executeQuery();
            if (vo.hasNext()) {
                Row row = vo.first();
                vo.setCurrentRow(row);
            }
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

    public void setGroupLov(DmsComBoxLov groupLov) {
        this.groupLov = groupLov;
    }

    public DmsComBoxLov getGroupLov() {
        return groupLov;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupName() {
        return groupName;
    }
}
