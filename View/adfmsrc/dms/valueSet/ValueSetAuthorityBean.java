package dms.valueSet;

import common.ADFUtils;
import common.DmsUtils;

import common.JSFUtils;

import dcm.ComboboxLOVBean;

import dcm.DcmQueryDescriptor;

import dms.login.Person;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;


import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import oracle.adf.model.BindingContext;
import oracle.adf.model.binding.DCBindingContainer;
import oracle.adf.model.binding.DCIteratorBinding;
import oracle.adf.share.ADFContext;
import oracle.adf.share.logging.ADFLogger;
import oracle.adf.view.rich.component.rich.RichPopup;
import oracle.adf.view.rich.component.rich.data.RichTable;
import oracle.adf.view.rich.component.rich.input.RichInputText;
import oracle.adf.view.rich.component.rich.input.RichSelectManyShuttle;
import oracle.adf.view.rich.context.AdfFacesContext;

import oracle.adf.view.rich.model.FilterableQueryDescriptor;

import oracle.adfinternal.view.faces.context.AdfFacesContextImpl;
import oracle.adfinternal.view.faces.model.binding.FacesCtrlListBinding;

import oracle.binding.BindingContainer;

import oracle.jbo.ApplicationModule;
import oracle.jbo.Key;
import oracle.jbo.Row;

import oracle.jbo.RowSetIterator;
import oracle.jbo.ViewObject;
import oracle.jbo.server.DBTransaction;
import oracle.jbo.uicli.binding.JUCtrlHierNodeBinding;
import oracle.jbo.uicli.binding.JUCtrlListBinding;

import org.apache.commons.lang.ObjectUtils;
import org.apache.myfaces.trinidad.event.SelectionEvent;

import org.apache.myfaces.trinidad.model.CollectionModel;
import org.apache.myfaces.trinidad.model.RowKeySet;
import org.apache.myfaces.trinidad.model.RowKeySetImpl;

import team.epm.dms.view.DmsGroupRoleViewImpl;
import team.epm.module.DmsModuleImpl;

public class ValueSetAuthorityBean {
    private RowKeySet selectedRows = new RowKeySetImpl();
    private Map valueMap;
    private static ADFLogger logger =
        ADFLogger.createADFLogger(ValueSetAuthorityBean.class);
    private Person person =
        (Person)ADFContext.getCurrent().getSessionScope().get("cur_user");
    private RichTable assignedValueTable;
    private RichPopup popup;
    private RichTable unassignedValueTable;
    private ComboboxLOVBean nameLov;
    private RichInputText filterValueInput; //搜索查找框，集值的


    public ValueSetAuthorityBean() {
        nameLov = ComboboxLOVBean.createByBinding("Name");
    }


    public void setAssignedValueTable(RichTable assignedValueTable) {
        this.assignedValueTable = assignedValueTable;
    }

    public RichTable getAssignedValueTable() {
        return assignedValueTable;
    }

    public void groupChangeListener(ValueChangeEvent valueChangeEvent) {

        FacesCtrlListBinding groupName =
            (FacesCtrlListBinding)JSFUtils.resolveExpression("#{bindings.GroupName}");
        groupName.setInputValue(valueChangeEvent.getNewValue());

        AdfFacesContext.getCurrentInstance().addPartialTarget(this.assignedValueTable);
        AdfFacesContext.getCurrentInstance().addPartialTarget(this.unassignedValueTable);


    }

    public void valuesetChangeListener(ValueChangeEvent valueChangeEvent) {

        String setMap = valueChangeEvent.getNewValue().toString();

        FacesCtrlListBinding name =
            (FacesCtrlListBinding)JSFUtils.resolveExpression("#{bindings.Name}");
        Integer rowid = (Integer)getNameLov().getMapItem().get(setMap);
        if (rowid == null)
            return;

        name.setInputValue(setMap);
        System.out.println("row id == " + rowid);

        Row row =
            ADFUtils.findIterator("DmsValueSetViewIterator").getRowSetIterator().getRowAtRangeIndex(rowid);
        this.refreshValueMap(row);

        //ADFUtils.findIterator("DmsGroupValueViewIterator").getViewObject().executeQuery();
        AdfFacesContext.getCurrentInstance().addPartialTarget(this.assignedValueTable);
        AdfFacesContext.getCurrentInstance().addPartialTarget(this.unassignedValueTable);
    }

    public void setValueMap(Map valueMap) {
        this.valueMap = valueMap;
    }

    public Map getValueMap() {
        if (this.valueMap == null) {
            this.refreshValueMap(null);
        }
        return valueMap;
    }

    private void refreshValueMap(Row row) {
        this.valueMap = new HashMap();
        ViewObject valueSetVo =
            ADFUtils.findIterator("DmsValueSetViewIterator").getViewObject();
        if (row == null) {
            row = valueSetVo.getCurrentRow();
        }
        if (row != null) {
            String valueSetSrc = (String)row.getAttribute("Source");
            if (valueSetSrc == null)
                return;

            valueSetSrc = valueSetSrc.toUpperCase();

            String sql =
                "select t.code,t.meaning from \"" + valueSetSrc + "\" t where t.locale='" +
                person.getLocale() + "'";
            DBTransaction trans =
                (DBTransaction)valueSetVo.getApplicationModule().getTransaction();
            Statement stmt = trans.createStatement(DBTransaction.DEFAULT);
            try {
                ResultSet rs = stmt.executeQuery(sql);
                while (rs.next()) {
                    String code = rs.getString("code");
                    String meaning = rs.getString("meaning");
                    this.valueMap.put(code, meaning);
                }
                rs.close();
                stmt.close();
            } catch (SQLException e) {
                this.logger.severe(e);
            }
        }
    }

    public void setPopup(RichPopup popup) {
        this.popup = popup;
    }

    public RichPopup getPopup() {
        return popup;
    }

    public void add_value(ActionEvent actionEvent) {
        //更新未选值集的显示问题，如果没有设置，值集仍然处于筛选状态
        try {
            ViewObject vo =
                ADFUtils.findIterator("getDmsValueViewIterator").getViewObject();
            vo.setWhereClause("MEANING like '%'");
            vo.executeQuery();
            //清空弹出框的值集过滤框的内容
            filterValueInput.setValue("");
            RichPopup.PopupHints hint = new RichPopup.PopupHints();
            this.popup.show(hint);
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                                                         new FacesMessage("值集源出错"));
            e.printStackTrace();
        }
    }

    public void remove_value(ActionEvent actionEvent) {

        if (this.assignedValueTable.getSelectedRowKeys() != null) {
            RowKeySet rowKeys = this.assignedValueTable.getSelectedRowKeys();
            Object[] rowKeySetArray = rowKeys.toArray();
            CollectionModel cm =
                (CollectionModel)assignedValueTable.getValue();

            for (Object key : rowKeySetArray) {
                assignedValueTable.setRowKey(key);
                JUCtrlHierNodeBinding rowData =
                    (JUCtrlHierNodeBinding)cm.getRowData();
                rowData.getRow().remove();
            }

            ADFUtils.findIterator("getDmsValueViewIterator").getViewObject().getApplicationModule().getTransaction().commit();
            ADFUtils.findIterator("DmsGroupValueViewIterator").getViewObject().executeQuery();
            AdfFacesContext.getCurrentInstance().addPartialTarget(this.assignedValueTable);
        }
    }

    public void add_value_authority(ActionEvent actionEvent) {
        if (this.unassignedValueTable.getSelectedRowKeys() != null) {
            ViewObject groupValueVo =
                ADFUtils.findIterator("DmsGroupValueViewIterator").getViewObject();
            String groupId =
                (String)ADFUtils.findIterator("DmsEnabledGroupViewIterator").getViewObject().getCurrentRow().getAttribute("Id");
            String valueSetId =
                (String)ADFUtils.findIterator("DmsValueSetViewIterator").getViewObject().getCurrentRow().getAttribute("Id");
            Iterator itr =
                this.unassignedValueTable.getSelectedRowKeys().iterator();
            RowSetIterator rowSetIterator =
                ADFUtils.findIterator("getDmsValueViewIterator").getRowSetIterator();
            while (itr.hasNext()) {
                List key = (List)itr.next();
                Row valueRow = rowSetIterator.getRow((Key)key.get(0));

                if (valueRow == null)
                    continue;

                Row row = groupValueVo.createRow();
                row.setAttribute("GroupId", groupId);
                row.setAttribute("ValueSetId", valueSetId);
                row.setAttribute("ValueId", valueRow.getAttribute("CODE"));
                groupValueVo.insertRow(row);
            }

            groupValueVo.getApplicationModule().getTransaction().commit();
            ADFUtils.findIterator("getDmsValueViewIterator").getViewObject().executeQuery();
            ADFUtils.findIterator("DmsGroupValueViewIterator").getViewObject().executeQuery();
            AdfFacesContext.getCurrentInstance().addPartialTarget(this.assignedValueTable);
            AdfFacesContext.getCurrentInstance().addPartialTarget(this.unassignedValueTable);
        }
    }

    public void setUnassignedValueTable(RichTable unassignedValueTable) {
        this.unassignedValueTable = unassignedValueTable;
    }

    public RichTable getUnassignedValueTable() {
        return unassignedValueTable;
    }

    public void setSelectedRows(RowKeySet selectedRows) {
        this.selectedRows = selectedRows;
    }

    public RowKeySet getSelectedRows() {
        return selectedRows;
    }


    //过滤值集权限表中的值的方法

    public void filterValueSetItem(ValueChangeEvent valueChangeEvent) {

        String filter = (String)valueChangeEvent.getNewValue();

        ViewObject vo =
            ADFUtils.findIterator("DmsGroupValueViewIterator").getViewObject();


        vo.executeQuery();

        Row row = vo.first();

        while (row != null) {

            if (!valueMap.get(row.getAttribute("ValueId")).toString().contains(filter))
                vo.removeCurrentRowAndRetain();
            else
                vo.next();

            row = vo.getCurrentRow();
        }
    }

    public void unassignedValueChangeListener(ValueChangeEvent valueChangeEvent) {
        String filter = (String)valueChangeEvent.getNewValue();

        ViewObject vo =
            ADFUtils.findIterator("getDmsValueViewIterator").getViewObject();

        vo.setWhereClause("MEANING like '%" + filter + "%'");
        vo.executeQuery();


        AdfFacesContext.getCurrentInstance().addPartialTarget(unassignedValueTable);
    }

    public void setFilterValueInput(RichInputText filterValueInput) {
        this.filterValueInput = filterValueInput;
    }

    public RichInputText getFilterValueInput() {
        return filterValueInput;
    }

    public void setNameLov(ComboboxLOVBean nameLov) {
        this.nameLov = nameLov;
    }

    public ComboboxLOVBean getNameLov() {
        return nameLov;
    }
}
