package dms.approvalFlow;

import common.ADFUtils;

import common.DmsUtils;

import common.JSFUtils;

import dcm.DcmDataTableModel;

import dcm.DcmQueryDescriptor;

import dcm.template.TemplateBackingBean;

import dms.login.Person;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import java.util.Map;

import java.util.Set;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import javax.faces.event.PhaseEvent;
import javax.faces.event.ValueChangeEvent;

import javax.faces.model.SelectItem;


import oracle.adf.model.BindingContext;
import oracle.adf.model.binding.DCBindingContainer;
import oracle.adf.model.binding.DCIteratorBinding;
import oracle.adf.model.binding.DCIteratorBindingDef;
import oracle.adf.share.ADFContext;
import oracle.adf.share.logging.ADFLogger;
import oracle.adf.view.rich.component.rich.RichPopup;
import oracle.adf.view.rich.component.rich.data.RichColumn;
import oracle.adf.view.rich.component.rich.data.RichTable;

import oracle.adf.view.rich.component.rich.input.RichInputListOfValues;
import oracle.adf.view.rich.component.rich.input.RichInputText;
import oracle.adf.view.rich.component.rich.input.RichSelectOneChoice;
import oracle.adf.view.rich.component.rich.nav.RichCommandButton;
import oracle.adf.view.rich.component.rich.output.RichOutputText;
import oracle.adf.view.rich.context.AdfFacesContext;

import oracle.adf.view.rich.event.QueryEvent;

import oracle.adf.view.rich.event.ReturnPopupEvent;
import oracle.adf.view.rich.model.FilterableQueryDescriptor;

import oracle.adf.view.rich.model.ListOfValuesModel;


import oracle.binding.BindingContainer;

import oracle.jbo.ApplicationModule;
import oracle.jbo.Key;
import oracle.jbo.Row;
import oracle.jbo.RowSetIterator;
import oracle.jbo.ViewCriteria;
import oracle.jbo.ViewCriteriaRow;
import oracle.jbo.ViewObject;
import oracle.jbo.server.DBTransaction;
import oracle.jbo.server.ViewRowImpl;

import oracle.jbo.uicli.binding.JUCtrlHierBinding;

import oracle.jbo.uicli.binding.JUCtrlHierNodeBinding;

import org.apache.commons.lang.ObjectUtils;

import org.apache.myfaces.trinidad.event.SelectionEvent;

import org.apache.myfaces.trinidad.model.CollectionModel;

import utils.system;

public class ApprovalBackingBean {
    private static ADFLogger logger =
        ADFLogger.createADFLogger(ApprovalBackingBean.class);
    private Person curUser;
    private String sources;
    private List<SelectItem> approvalObjItemList = new ArrayList<SelectItem>();

    public ApprovalBackingBean() {
        curUser = (Person)ADFContext.getCurrent().getSessionScope().get("cur_user");
       getBeforePhaseQuery();//第一次打开页面初始化
    }
        public void setApprovalObjItemList(List<SelectItem> approvalObjItemList) {
            this.approvalObjItemList = approvalObjItemList;
        }

        public List<SelectItem> getApprovalObjItemList() {
            return approvalObjItemList;
        }
    //  public ListOfValuesModel
//创建审批流，根据当前行刷新数据
    public void makeCurrent(SelectionEvent selectionEvent) {
        // Add event code here...
        this.approvalObjItemList.clear();
        RichTable rt = (RichTable)selectionEvent.getSource();
        CollectionModel cm = (CollectionModel)rt.getValue();
        JUCtrlHierBinding tableBinding = (JUCtrlHierBinding)cm.getWrappedData();
        DCIteratorBinding iter = tableBinding.getDCIteratorBinding();
        
        JUCtrlHierNodeBinding selectedRowData = (JUCtrlHierNodeBinding)rt.getSelectedRowData();
        Key rowKey = selectedRowData.getRowKey();
        iter.setCurrentRowWithKey(rowKey.toStringFormat(true));
        Row row = selectedRowData.getRow();
        String valueSetId = row.getAttribute("ValueSetId").toString();
        pubDome(valueSetId);
    }

//LOV值改变调用此方法
    public void valueSetChange(ValueChangeEvent valueChangeEvent) {
        this.approvalObjItemList.clear();
        // Add event code here...
        valueChangeEvent.getComponent().processUpdates(FacesContext.getCurrentInstance());   //值改变刷新模型     
        DCIteratorBinding it = ADFUtils.findIterator("DmsApprovalFlowInfoVOIterator");
        String valueSetId = it.getCurrentRow().getAttribute("ValueSetId").toString();
        pubDome(valueSetId);
        }
//第一次打开页面得到第一行数据   
     public void getBeforePhaseQuery() {
        //table =valueset
        DCIteratorBinding it = ADFUtils.findIterator("DmsApprovalFlowInfoVOIterator");
        ViewObject vo = it.getViewObject();
        Row row = vo.first();
        //row.getAttribute("valueSetId");
        String valueSetId =  it.getCurrentRow().getAttribute("ValueSetId").toString();
            //row.getAttribute("valueSetId").toString();
        pubDome(valueSetId);
    }
    //得到动态表名查询数据
    private void pubDome(String valueSetId){
        DBTransaction db = (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stmt = db.createStatement(DBTransaction.DEFAULT);
        StringBuffer sql = new StringBuffer();
        sql.append("select upper(t.SOURCE) \"SOURCE_NAME\" from dms_value_set t");
        sql.append(" where upper(t.ID)=upper(\'").append(valueSetId).append("\')");
        sql.append(" and t.locale= \'");
        sql.append(curUser.getLocale()).append("\'");
        try {
            ResultSet rs = stmt.executeQuery(sql.toString());
            while(rs.next()){
            this.sources = rs.getString("SOURCE_NAME");
            }
            StringBuffer sql2 = new StringBuffer();
            sql2.append("select s.code \"code_num\",s.meaning \"mean_name\" from ");
            sql2.append(this.sources);
            sql2.append(" s");
          //  System.out.println(sql2);
            rs = stmt.executeQuery(sql2.toString());
            while(rs.next()){
                SelectItem item = new SelectItem();
                item.setLabel(rs.getString("mean_name"));
                item.setValue(rs.getString("code_num"));
                this.approvalObjItemList.add(item);
            }
            rs.close();
            stmt.close();
            AdfFacesContext adfFace = AdfFacesContext.getCurrentInstance();
            adfFace.addPartialTarget(JSFUtils.findComponentInRoot("t2"));
            adfFace.addPartialTarget(JSFUtils.findComponentInRoot("t1"));
        } catch (Exception e) {
        this.logger.severe(e);
        JSFUtils.addFacesErrorMessage(DmsUtils.getMsg("dms.approval.create_error_msg"));
        return;
        }
    }

}



