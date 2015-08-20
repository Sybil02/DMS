package workapproveflow.workflow;

import common.ADFUtils;

import common.DmsUtils;

import common.JSFUtils;

import dms.login.Person;

import dms.workflow.WorkflowEditBean;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import oracle.adf.model.binding.DCIteratorBinding;
import oracle.adf.share.ADFContext;
import oracle.adf.share.logging.ADFLogger;
import oracle.adf.view.rich.component.rich.RichPopup;
import oracle.adf.view.rich.component.rich.data.RichTable;

import oracle.adf.view.rich.component.rich.input.RichSelectOneChoice;
import oracle.adf.view.rich.context.AdfFacesContext;

import oracle.jbo.Key;
import oracle.jbo.Row;
import oracle.jbo.ViewObject;

import oracle.jbo.server.DBTransaction;

import oracle.jbo.uicli.binding.JUCtrlHierBinding;
import oracle.jbo.uicli.binding.JUCtrlHierNodeBinding;

import org.apache.myfaces.trinidad.event.SelectionEvent;

import workapproveflow.WorkflowEngine;

import org.apache.myfaces.trinidad.model.CollectionModel;

public class WorkflowDisplayBean {

    //日志
    private static ADFLogger _logger =ADFLogger.createADFLogger(WorkflowEditBean.class);
    private Person curUser;
    private RichTable wfTable;
    private List<WorkflowValueSet> wfValueSetList = new ArrayList<WorkflowValueSet>();
    //存放值集的CODE和选择的值ID
    private Map<String,Map<String,String>> comSelectMap = new HashMap<String,Map<String,String>>();
    //WORFKFLOW_ENGINE
    WorkflowEngine wfEngine = new WorkflowEngine();
    private RichPopup runPop;
    private RichPopup tempPop;
    private RichPopup approvePop;
    private RichPopup runInterPop;
    boolean isflag = false;
    boolean isflagtwo = true;
    List<SelectItem> tempItemList = new ArrayList<SelectItem>();
   
    public WorkflowDisplayBean() {
        super();
        this.curUser =(Person)ADFContext.getCurrent().getSessionScope().get("cur_user");
    }
    //show popup ,显示选择的组合
    public void showRunPop(ActionEvent actionEvent) {
        //clear list
        this.wfValueSetList.clear();
        this.comSelectMap.clear();
        DCIteratorBinding wfIter = ADFUtils.findIterator("DmsUserWorkflowVOIterator");
        ViewObject wfVo = wfIter.getViewObject();
        Row curRow = wfVo.getCurrentRow();
        if(curRow.getAttribute("WfStatus").equals("Y")){
            FacesContext.getCurrentInstance().addMessage(null,new FacesMessage("工作流已经启动，不能重复启动！"));
            return;
        }
        DBTransaction trans = (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat=trans.createStatement(DBTransaction.DEFAULT);
        Row row = wfVo.getCurrentRow();
        String wfComId = row.getAttribute("WfCom").toString();
        String sql = "SELECT T1.VALUE_SET_ID,T2.NAME,T2.SOURCE,T2.CODE FROM DCM_COM_VS T1,DMS_VALUE_SET T2 "
                        + "WHERE T1.VALUE_SET_ID = T2.ID AND T2.LOCALE = '" + this.curUser.getLocale()+"' "
                        + "AND T1.COMBINATION_ID = '" + wfComId + "' ORDER BY T1.SEQ";

        try {
            //查询所有值集源
            ResultSet vsRs = stat.executeQuery(sql);
            Map<String,String> sourceMap = new LinkedHashMap<String,String>();
            while(vsRs.next()){
                String valueSetName = vsRs.getString("NAME");
                String source = vsRs.getString("SOURCE");
                String code = vsRs.getString("CODE");
                sourceMap.put(valueSetName,source);
                //将值集名称put到map中
                Map<String,String> codeMap = new HashMap<String,String>();
                codeMap.put(code, "");
                this.comSelectMap.put(valueSetName, codeMap);
            }
            vsRs.close();
            for(Map.Entry<String,String> entry : sourceMap.entrySet()){
                String valueSql = "SELECT CODE,MEANING FROM " + entry.getValue();
                //查询每个值集源中的值，并封装成selectitem
                ResultSet valueRs = stat.executeQuery(valueSql);
                List<SelectItem> itemList = new ArrayList<SelectItem>();
                while(valueRs.next()){
                    SelectItem item = new SelectItem();
                    item.setValue(valueRs.getString("CODE"));
                    item.setLabel(valueRs.getString("MEANING"));
                    itemList.add(item);
                }
                //每个值集生成一个bean，加入list中
                WorkflowValueSet wvs = new WorkflowValueSet(entry.getKey(),null,itemList);
                this.wfValueSetList.add(wvs);   
                valueRs.close();
            }
            stat.close();
        } catch (SQLException e) {
            this._logger.severe(e);
        }
        //显示pop
        RichPopup.PopupHints hints = new RichPopup.PopupHints();
        this.runPop.show(hints);
    }
   
    public void showDetails(ActionEvent actionEvent) {
      //   Add event code here...
        tempItemList.clear();
        
        DCIteratorBinding wfsIter = ADFUtils.findIterator("DmsWorkflowStatusVOIterator");
        ViewObject wfsVo = wfsIter.getViewObject();
        Row curRow = wfsVo.getCurrentRow();
       
        String stepTask = curRow.getAttribute("StepTask").toString();
       ;
        if(stepTask.equals("OPEN TEMPLATES")){
            String stepObject = curRow.getAttribute("StepObject").toString();
            openTemp(stepObject);
        }else if(stepTask.equals("APPROVE")){
            String runId = curRow.getAttribute("RunId").toString();
            String StepNo = curRow.getAttribute("StepNo").toString();
            approveflow(runId,StepNo);
        }else if(stepTask.equals("ETL")){
            
        }
        
    }
    public void approveflow(String runId,String StepNo){
        DBTransaction trans = (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat=trans.createStatement(DBTransaction.DEFAULT);
        StringBuffer sql = new StringBuffer();
        sql.append("select apptem.TEMPLATE_ID \"tempId\", temp.name \"name\" from dcm_template temp , approve_template_status apptem " +
            "where temp.id = apptem.TEMPLATE_ID and apptem.run_id = \'");
        sql.append(runId).append("\'");
        sql.append(" and apptem.step_no = \'");
        sql.append(StepNo).append("\'");
        sql.append(" and temp.locale = apptem.locale and temp.locale= \'");
        sql.append(curUser.getLocale()).append("\'");
        ResultSet rs;

        try {
            rs =  stat.executeQuery(sql.toString());
            while(rs.next()){
                SelectItem item = new SelectItem();
                item.setValue(rs.getString("tempId"));
                item.setLabel(rs.getString("name"));
                tempItemList.add(item);
            }
            rs.close();;
            stat.close();
        } catch (SQLException e) {
            this._logger.severe(e);
        }
        //显示pop
        RichPopup.PopupHints hints = new RichPopup.PopupHints();
        this.approvePop.show(hints);
    }
    public void openTemp(String stepObject){
        DBTransaction trans = (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat=trans.createStatement(DBTransaction.DEFAULT);
        StringBuffer sql = new StringBuffer();
        sql.append("select temp.id \"tempId\", temp.name \"name\" from dcm_template temp where temp.TEMPLATE_LABEL = \'");
        sql.append(stepObject);
        sql.append("\'");
        sql.append(" and temp.locale= \'");
        sql.append(curUser.getLocale()).append("\'");
        ResultSet rs;

        try {
            rs = stat.executeQuery(sql.toString());
           
            while(rs.next()){
                SelectItem item = new SelectItem();
                item.setValue(rs.getString("tempId"));
                item.setLabel(rs.getString("name"));
                tempItemList.add(item);
            }
            rs.close();;
            stat.close();
        } catch (SQLException e) {
            this._logger.severe(e);
        }
        
        //显示pop
        RichPopup.PopupHints hints = new RichPopup.PopupHints();
        this.tempPop.show(hints);
    }
    //执行接口
    public void execInterface(ActionEvent actionEvent) {
        // Add event code here...
        RichPopup.PopupHints hints = new RichPopup.PopupHints();
        this.runInterPop.show(hints);
        
    }
    //工作流启动，初始化工作流步骤信息
    public void runWorkflow(ActionEvent actionEvent) {
        this.runPop.cancel();
        DCIteratorBinding wfIter = ADFUtils.findIterator("DmsUserWorkflowVOIterator");
        ViewObject wfVo = wfIter.getViewObject();
        Row row = wfVo.getCurrentRow();
        String wfId = row.getAttribute("WorkflowId").toString();
        String wfStatus = row.getAttribute("WfStatus").toString();
        //改变工作流状态
        this.wfEngine.changeWfStatus(wfId, wfStatus);
        //初始化工作流状态
        this.wfEngine.initWfSteps(wfId,this.comSelectMap);
        FacesContext.getCurrentInstance().addMessage(null,new FacesMessage("工作流启动完成！"));
        //通过sql直接修改表数据，需要查询VO改变迭代器数据，刷新table才有效
        wfVo.executeQuery();
        AdfFacesContext adfFacesContext = AdfFacesContext.getCurrentInstance();
        adfFacesContext.addPartialTarget(this.wfTable);
    }

    public void vsValueChange(ValueChangeEvent valueChangeEvent) {
        RichSelectOneChoice comSoc = (RichSelectOneChoice)valueChangeEvent.getSource();
        String valuesName = comSoc.getLabel();
        String valueCode = comSoc.getValue().toString();
        //找到对应的值集，将选择值的CODE（对应模板组合表中的列名）,VALUE的编码保存进去
        if(this.comSelectMap.containsKey(valuesName)){
            Map<String,String> valueMap = this.comSelectMap.get(valuesName);
            for(Map.Entry<String,String> entry : valueMap.entrySet()){
                  valueMap.put(entry.getKey(), valueCode);
            }
            this.comSelectMap.put(valuesName, valueMap);
        }
    }
    public void valueChangeApprove(ValueChangeEvent valueChangeEvent) {
        // Add event code here...
        RichSelectOneChoice comSoc = (RichSelectOneChoice)valueChangeEvent.getSource();
        String tempId = comSoc.getValue().toString();
       // DCIteratorBinding wfsIter = ADFUtils.findIterator("DmsWorkflowStatusVOIterator");
        DCIteratorBinding atsIter = ADFUtils.findIterator("DmsApproveTemplateStatusVOIterator");
        StringBuffer sql = new StringBuffer();
        sql.append("TEMPLATE_ID = \'");
        sql.append(tempId).append("\'");
        sql.append(" locale = \'");
        sql.append(curUser.getLocale()).append("\'");
        ViewObject wftsVo = atsIter.getViewObject();
        wftsVo.setWhereClause(sql.toString());
        wftsVo.executeQuery();
        // 刷新表格
        AdfFacesContext adfFace = AdfFacesContext.getCurrentInstance();
        adfFace.addPartialTarget(JSFUtils.findComponentInRoot("t5"));
    }
    public void valueChangeTemp(ValueChangeEvent valueChangeEvent) {
        // Add event code here...
        RichSelectOneChoice comSoc = (RichSelectOneChoice)valueChangeEvent.getSource();
        String tempId = comSoc.getValue().toString();
        DCIteratorBinding wfsIter = ADFUtils.findIterator("DmsWorkflowStatusVOIterator");
        DCIteratorBinding wftsIter = ADFUtils.findIterator("DmsWorkflowTemplateStatusVOIterator");
        ViewObject wfsVo = wfsIter.getViewObject();
        Row curRow = wfsVo.getCurrentRow();
        String runId = curRow.getAttribute("RunId").toString();
        String stepNo = curRow.getAttribute("StepNo").toString();
        StringBuffer sql = new StringBuffer();
        sql.append("RUN_ID = \'");
        sql.append(runId).append("\'");
        sql.append(" and TEMPLATE_ID = \'");
        sql.append(tempId).append("\'");
        sql.append(" and STEP_NO = \'");
        sql.append(stepNo).append("\'");
        ViewObject wftsVo = wftsIter.getViewObject();
        wftsVo.setWhereClause(sql.toString());
        wftsVo.executeQuery();
       // 刷新表格
        AdfFacesContext adfFace = AdfFacesContext.getCurrentInstance();
        adfFace.addPartialTarget(JSFUtils.findComponentInRoot("t5"));
       
    }
    public void makeCurrent(SelectionEvent selectionEvent) {
        // Add event code here...
        isflag = true;
        isflagtwo = true;
        RichTable rt = (RichTable)selectionEvent.getSource();
        CollectionModel cm = (CollectionModel)rt.getValue();
        JUCtrlHierBinding tableBinding = (JUCtrlHierBinding)cm.getWrappedData();
        DCIteratorBinding iter = tableBinding.getDCIteratorBinding();
        
        JUCtrlHierNodeBinding selectedRowData = (JUCtrlHierNodeBinding)rt.getSelectedRowData();
        Key rowKey = selectedRowData.getRowKey();
        iter.setCurrentRowWithKey(rowKey.toStringFormat(true));
        Row row = selectedRowData.getRow();
        String StepTask = row.getAttribute("StepTask").toString();
        String StepStatus = row.getAttribute("StepStatus").toString();
        if(StepTask.equals("ETL")&&StepStatus.equals("WORKING")){
           isflag = false;
        }else if(StepTask.equals("OPEN TEMPLATES")||StepTask.equals("APPROVE")&&StepStatus.equals("WORKING")){
            isflagtwo = false;
        }
        
    }
    public void setWfTable(RichTable wfTable) {
        this.wfTable = wfTable;
    }

    public RichTable getWfTable() {
        return wfTable;
    }

    public void setRunPop(RichPopup runPop) {
        this.runPop = runPop;
    }

    public RichPopup getRunPop() {
        return runPop;
    }

    public void setWfValueSetList(List<WorkflowValueSet> wfValueSetList) {
        this.wfValueSetList = wfValueSetList;
    }

    public List<WorkflowValueSet> getWfValueSetList() {
        return wfValueSetList;
    }






    public void setTempPop(RichPopup tempPop) {
        this.tempPop = tempPop;
    }

    public RichPopup getTempPop() {
        return tempPop;
    }

    public void setTempItemList(List<SelectItem> tempItemList) {
        this.tempItemList = tempItemList;
    }

    public List<SelectItem> getTempItemList() {
        return tempItemList;
    }

    public void setApprovePop(RichPopup approvePop) {
        this.approvePop = approvePop;
    }

    public RichPopup getApprovePop() {
        return approvePop;
    }


    public void setRunInterPop(RichPopup runInterPop) {
        this.runInterPop = runInterPop;
    }

    public RichPopup getRunInterPop() {
        return runInterPop;
    }


    public void setIsflag(boolean isflag) {
        this.isflag = isflag;
    }

    public boolean isIsflag() {
        return isflag;
    }

    public void setIsflagtwo(boolean isflagtwo) {
        this.isflagtwo = isflagtwo;
    }

    public boolean isIsflagtwo() {
        return isflagtwo;
    }
}
