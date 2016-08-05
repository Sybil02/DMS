package dms.role;

import common.ADFUtils;
import common.DmsUtils;


import common.lov.DmsComBoxLov;

import common.lov.ValueSetRow;

import dms.login.Person;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.sql.Statement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;

import javax.faces.model.SelectItem;

import oracle.adf.model.BindingContext;
import oracle.adf.model.binding.DCIteratorBinding;
import oracle.adf.share.ADFContext;
import oracle.adf.share.logging.ADFLogger;
import oracle.adf.view.rich.component.rich.RichPopup;
import oracle.adf.view.rich.component.rich.data.RichTable;
import oracle.adf.view.rich.component.rich.input.RichSelectManyShuttle;
import oracle.adf.view.rich.context.AdfFacesContext;

import oracle.binding.BindingContainer;

import oracle.jbo.Key;
import oracle.jbo.Row;
import oracle.jbo.RowSetIterator;
import oracle.jbo.ViewObject;
import oracle.jbo.server.DBTransaction;
import oracle.jbo.uicli.binding.JUCtrlListBinding;

import org.apache.commons.lang.ObjectUtils;
import org.apache.myfaces.trinidad.event.SelectionEvent;

import team.epm.dms.view.DmsGroupRoleViewImpl;
import team.epm.dms.view.DmsRoleViewImpl;
import team.epm.dms.view.DmsUserGroupViewImpl;
import team.epm.module.DmsModuleImpl;

public class RoleGroupBean {
 
    private static ADFLogger logger=ADFLogger.createADFLogger(RoleGroupBean.class);
    private Person person =
        (Person)ADFContext.getCurrent().getSessionScope().get("cur_user");
    private DmsComBoxLov groupLov;
    private String groupName;
    private RichTable selectedRoleTable;
    private RichPopup popup;
    private RichTable unSelectedRoleTable;

    public RoleGroupBean(){
            this.initGroupLov();
        }

        public void initGroupLov() {
            List<SelectItem> values = new ArrayList<SelectItem>();
            DBTransaction trans =
                (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
            Statement stat = trans.createStatement(DBTransaction.DEFAULT);
            String sql =
                "SELECT T.ID,T.NAME FROM DMS_GROUP T WHERE T.LOCALE='" + this.person.getLocale() +
                "'  AND T.ENABLE_FLAG = 'Y'";
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
                    ADFUtils.findIterator("DmsEnabledGroupViewIterator").getViewObject();
                String wc = " NAME = '"+this.groupName+"'";
                vo.setWhereClause(wc);
                vo.executeQuery();
                if (vo.hasNext()) {
                    Row row = vo.first();
                    vo.setCurrentRow(row);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    public void setSelectedRoleTable(RichTable selectedRoleTable) {
        this.selectedRoleTable = selectedRoleTable;
    }

    public RichTable getSelectedRoleTable() {
        return selectedRoleTable;
    }

    public void groupChangeListener(ValueChangeEvent valueChangeEvent) {
        ViewObject vo =
            ADFUtils.findIterator("DmsEnabledGroupViewIterator").getViewObject();
        String wc = " NAME = '"+ valueChangeEvent.getNewValue() +"'";
        vo.setWhereClause(wc);
        vo.executeQuery();
        if (vo.hasNext()) {
            Row row = vo.first();
            vo.setCurrentRow(row);
        }
        AdfFacesContext.getCurrentInstance().addPartialTarget(this.selectedRoleTable);
    }

    public void setPopup(RichPopup popup) {
        this.popup = popup;
    }

    public RichPopup getPopup() {
        return popup;
    }

    public void showPopup(ActionEvent actionEvent) {
        Row curGroup=ADFUtils.findIterator("DmsEnabledGroupViewIterator").getCurrentRow();
        if(curGroup!=null){
            ViewObject vo=ADFUtils.findIterator("DmsUnGroupedRoleViewIterator").getViewObject();
            vo.setNamedWhereClauseParam("groupId", curGroup.getAttribute("Id"));
            vo.executeQuery();
            RichPopup.PopupHints hint=new RichPopup.PopupHints();
            this.popup.show(hint);
        }
    }

    public void removeRole(ActionEvent actionEvent) {
        if (this.selectedRoleTable.getSelectedRowKeys() != null) {
            ViewObject vo =
                DmsUtils.getDmsApplicationModule().getDmsGroupRoleView();
            Iterator itr =
                this.selectedRoleTable.getSelectedRowKeys().iterator();
            RowSetIterator rowSetIterator =
                ADFUtils.findIterator("DmsGroupedRoleViewIterator").getRowSetIterator();
            while(itr.hasNext()){
                List key = (List)itr.next();
                Row roleRow = rowSetIterator.getRow((Key)key.get(0));
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
    }

    public void setUnSelectedRoleTable(RichTable unSelectedRoleTable) {
        this.unSelectedRoleTable = unSelectedRoleTable;
    }

    public RichTable getUnSelectedRoleTable() {
        return unSelectedRoleTable;
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
