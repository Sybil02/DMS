package workapproveflow.workflow;

import common.ADFUtils;

import common.DmsUtils;

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

import oracle.jbo.Row;
import oracle.jbo.ViewObject;

import oracle.jbo.server.DBTransaction;

import workapproveflow.WorkflowEngine;

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

}
