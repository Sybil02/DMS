package dmsApproval;

import common.ADFUtils;

import common.DmsUtils;

import common.JSFUtils;

import dms.login.Person;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import java.util.UUID;

import javax.faces.event.ActionEvent;

import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import oracle.adf.model.binding.DCIteratorBinding;
import oracle.adf.share.ADFContext;
import oracle.adf.view.rich.component.rich.RichPopup;

import oracle.adf.view.rich.component.rich.data.RichTable;
import oracle.adf.view.rich.component.rich.input.RichSelectOneChoice;

import oracle.jbo.Key;
import oracle.jbo.Row;
import oracle.jbo.ViewObject;
import oracle.jbo.server.DBTransaction;

import oracle.jbo.uicli.binding.JUCtrlHierBinding;
import oracle.jbo.uicli.binding.JUCtrlHierNodeBinding;

import org.apache.myfaces.trinidad.event.SelectionEvent;
import org.apache.myfaces.trinidad.model.CollectionModel;

import team.epm.dcm.view.DcmTemplateCombinationVOImpl;
import team.epm.dcm.view.DcmTemplateCombinationVORowImpl;

public class ApprovalIndexBean {
    private RichPopup paramPop;
    private String entityCode;
    private String combination;
    private String curAppId;
    private boolean disableRun = true;

    Map<String,RichSelectOneChoice> paraSocMap = new LinkedHashMap<String,RichSelectOneChoice>();

    public ApprovalIndexBean() {
        super();
    }
    
    private List<AppParamBean> paramList;
    
    public void makeCurrentRow(SelectionEvent selectionEvent) {
        RichTable wfTable = (RichTable)selectionEvent.getSource();
        CollectionModel cm = (CollectionModel)wfTable.getValue();
        JUCtrlHierBinding tableBinding = (JUCtrlHierBinding)cm.getWrappedData();
        DCIteratorBinding iter = tableBinding.getDCIteratorBinding();
        JUCtrlHierNodeBinding selectedRowData = (JUCtrlHierNodeBinding)wfTable.getSelectedRowData();
        Key rowKey = selectedRowData.getRowKey();
        iter.setCurrentRowWithKey(rowKey.toStringFormat(true));
        Row row = selectedRowData.getRow();
        String enableRun  = row.getAttribute("EnableRun").toString();
        if("Y".equals(enableRun)){
            this.disableRun = false; 
        }else{
            this.disableRun = true; 
        }
        
    }
    
    public void startApproval(ActionEvent actionEvent) {
        ViewObject vo = ADFUtils.findIterator("DmsUserApprovalVOIterator").getViewObject();
        this.combination = vo.getCurrentRow().getAttribute("ValueSetId").toString();
        this.curAppId = vo.getCurrentRow().getAttribute("Id").toString();
        
        DBTransaction trans = (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(1);
        String sql = "SELECT V.ID,V.SOURCE,V.NAME,V.CODE,T.IS_APPROVAL,T.SEQ FROM DCM_COM_VS T,DMS_VALUE_SET V WHERE "
            + "T.VALUE_SET_ID = V.ID AND V.LOCALE = 'zh_CN' AND T.COMBINATION_ID = '" + combination + "' ORDER BY T.SEQ";
        ResultSet rs;
        this.paramList = new ArrayList<AppParamBean>();
        try {
            rs = stat.executeQuery(sql);
            while(rs.next()){
                String isApp = rs.getString("IS_APPROVAL");
                if(!"Y".equals(isApp)){
                    AppParamBean param = new AppParamBean();
                    param.setPName(rs.getString("NAME"));
                    param.setPSource(rs.getString("SOURCE"));
                    param.setPCode(rs.getString("CODE"));
                    this.paramList.add(param);
                }else{
                    this.entityCode = rs.getString("CODE");
                }
            }
            rs.close();
            stat.close();
            
        } catch (SQLException e) {
                e.printStackTrace();
        }
        
        this.initValues();
        RichPopup.PopupHints hints = new RichPopup.PopupHints();
        this.paramPop.show(hints);
        
    }
    
    public void initValues(){
        DBTransaction trans = (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(1);
        try {
        for(AppParamBean app : this.paramList){
            String sql = "SELECT T.CODE,T.MEANING FROM " + app.getPSource() + " T WHERE T.ENABLED = 'Y'";
            ResultSet rs;
            List<SelectItem> valueList = new ArrayList<SelectItem>();
            rs = stat.executeQuery(sql);
            while(rs.next()){
                SelectItem sim = new SelectItem();
                sim.setLabel(rs.getString("MEANING"));
                sim.setValue(rs.getString("CODE"));
                valueList.add(sim);
            }
            rs.close();
            app.setValueList(valueList);
        }
            stat.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void paramSelectListener(ValueChangeEvent valueChangeEvent) {
        System.out.println(valueChangeEvent.getNewValue()+"........");
        
        RichSelectOneChoice pSoc = (RichSelectOneChoice)valueChangeEvent.getSource();
        for(Map.Entry entry : this.paraSocMap.entrySet()){
            if(pSoc.equals(entry.getValue())){
                for(AppParamBean app : this.paramList){
                    if(app.getPName().equals(entry.getKey())){
                        app.setPChoiced(valueChangeEvent.getNewValue().toString());  
                    }    
                }
            }    
        }
        
    }
    
    public void runApproval(ActionEvent actionEvent) {
        for(AppParamBean app : this.paramList){
            if(app.getPChoiced() == null || "".equals(app.getPChoiced())){
                JSFUtils.addFacesErrorMessage("参数不能为空！");
                return;    
            }   
        }
        
        DBTransaction trans = (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(1);
        
        //初始化状态
        String runId = UUID.randomUUID().toString().replace("-", "");
        String userId = ((Person)ADFContext.getCurrent().getSessionScope().get("cur_user")).getId();
        String comSource = this.getComSource();
        //更改审批了runId
        String uSql = "UPDATE DMS_APPROVALFLOW_INFO T SET T.APPROVAL_STATUS = 'Y',T.RUN_ID = '" + runId +"' WHERE T.ID = '" + this.curAppId + "'";
        //插入状态表
        String sql = "INSERT INTO APPROVAL_TEMPLATE_STATUS SELECT T.ID,'" + runId + "',T.TEMP_ID,T.ENTITY_CODE,T.USER_ID,"
            + "'CLOSE',NULL,SYSDATE,SYSDATE,'" + userId + "','" + userId + "',C.ID,T.SEQ,'' FROM DMS_APPROVALFLOW_ENTITYS T,"
            + comSource + " C WHERE C." + this.entityCode + " = T.ENTITY_CODE AND T.APPROVAL_ID = '" + this.curAppId
            + "' ";
        for(AppParamBean app : this.paramList){
           sql = sql + "AND C." + app.getPCode() + " ='" + app.getPChoiced() + "' ";
        }
        try {
            stat.executeUpdate(sql);
            trans.commit();   
            //先删除组合状态
            String deleteSql = "DELETE DCM_TEMPLATE_COMBINATION D WHERE EXISTS (SELECT 1 FROM APPROVAL_TEMPLATE_STATUS T " 
                + "WHERE T.RUN_ID = '" + runId + "' AND T.TEMPLATE_ID = D.TEMPLATE_ID AND T.COM_ID = D.COM_RECORD_ID)";
            stat.executeUpdate(deleteSql);
            stat.executeUpdate(uSql);
            trans.commit();
            //再新增组合状态
            String sSql = "SELECT DISTINCT T.TEMPLATE_ID,T.COM_ID FROM APPROVAL_TEMPLATE_STATUS T WHERE T.RUN_ID = '" + runId + "'";
            ResultSet rs = stat.executeQuery(sSql);
            Map<String,List<String>> tempComMap = new HashMap<String,List<String>>();
            List<String> comList = new ArrayList<String>();
            String tId = "";
            
            while(rs.next()){
                tId = rs.getString("TEMPLATE_ID");
                comList.add(rs.getString("COM_ID"));
            }
            rs.close();
            stat.close();
            //打开组合
            tempComMap.put(tId, comList);
            this.openTempCom(tempComMap);
            
            //发送邮件
            this.sendMail(runId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        ADFUtils.findIterator("DmsUserApprovalVOIterator").getViewObject().executeQuery();
        this.paramPop.cancel();
        JSFUtils.addFacesInformationMessage("启动成功！");

    }
    
    private void sendMail(String runId){
        
    }
    
    private void openTempCom(Map<String,List<String>> tempComMap){
        DcmTemplateCombinationVOImpl tempComVo =DmsUtils.getDcmApplicationModule().getDcmTemplateCombinationVO();
        for(Map.Entry<String,List<String>> tempEntry : tempComMap.entrySet()){
            for(String comId : tempEntry.getValue()){
                //不存在则新建一条
                DcmTemplateCombinationVORowImpl newRow = (DcmTemplateCombinationVORowImpl)tempComVo.createRow();
                newRow.setComRecordId(comId);
                newRow.setTemplateId(tempEntry.getKey());
                newRow.setStatus("OPEN");
                tempComVo.insertRow(newRow);
            }
        }
        tempComVo.getApplicationModule().getTransaction().commit();
    }
    
    public String getComSource(){
        ViewObject vo = DmsUtils.getDcmApplicationModule().getDcmCombinationView();
        String whereClause = " ID = '" + this.combination + "'";
        vo.setWhereClause(whereClause);
        vo.executeQuery();
        String source = "";
        if(vo.hasNext()){
            source = vo.first().getAttribute("Code").toString();   
        }
        return source;        
    }

    public void closeApproval(ActionEvent actionEvent) {
        ViewObject vo = ADFUtils.findIterator("DmsUserApprovalVOIterator").getViewObject();
        String id = vo.getCurrentRow().getAttribute("Id").toString();
        //更新状态
        DBTransaction trans = (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        String sql = "UPDATE DMS_APPROVALFLOW_INFO T SET T.APPROVAL_STATUS = 'N' WHERE T.ID = '" + id + "'";
        try {
            stat.executeUpdate(sql);
            trans.commit();
            stat.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setParamPop(RichPopup paramPop) {
        this.paramPop = paramPop;
    }

    public RichPopup getParamPop() {
        return paramPop;
    }

    public void setParamList(List<AppParamBean> paramList) {
        this.paramList = paramList;
    }

    public List<AppParamBean> getParamList() {
        return paramList;
    }

    public void setParaSocMap(Map<String, RichSelectOneChoice> paraSocMap) {
        this.paraSocMap = paraSocMap;
    }

    public Map<String, RichSelectOneChoice> getParaSocMap() {
        return paraSocMap;
    }

    public void setDisableRun(boolean disableRun) {
        this.disableRun = disableRun;
    }

    public boolean isDisableRun() {
        return disableRun;
    }
}
