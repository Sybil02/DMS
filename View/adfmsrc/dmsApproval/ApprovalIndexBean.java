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
    private boolean disableClose = true;
    private String firstName;
    private Key key ;

    Map<String,RichSelectOneChoice> paraSocMap = new LinkedHashMap<String,RichSelectOneChoice>();

    public ApprovalIndexBean() {
        //初始化时不显示状态，因为entityList没有初始化出来，显示有问题。
        ViewObject vo = ADFUtils.findIterator("DmsApprovalStatusVOIterator").getViewObject();
        String where = " 1 = 2";
        vo.setWhereClause(where);
        vo.executeQuery();
        vo.setWhereClause(null);
        this.setStartStatus();
    }
    
    private List<AppParamBean> paramList;
    private List<SelectItem> entityList = new ArrayList<SelectItem>();
    private List<String> comList = new ArrayList<String>();
    
    public void setStartStatus(){
        DBTransaction trans = (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        Person curUser =(Person)ADFContext.getCurrent().getSessionScope().get("cur_user");
        String sql = "SELECT T.APPROVAL_NAME,T.APPROVAL_STATUS,ENABLE_RUN FROM DMS_APPROVALFLOW_INFO T,DMS_ROLE_APPROVAL R," +
            "DMS_GROUP_ROLE G,DMS_USER_GROUP U WHERE T.ID = R.APPROVAL_ID AND R.ROLE_ID = G.ROLE_ID " +
            "AND G.GROUP_ID = U.GROUP_ID AND U.USER_ID = '"+curUser.getId()+"' ORDER BY \"SEQ\"";
        try {
            ResultSet rs = stat.executeQuery(sql);
            if(rs.next()){
                String enableRun = rs.getString("ENABLE_RUN");
                Object status = rs.getString("APPROVAL_STATUS");
                if("Y".equals(enableRun) && !"Y".equals(status)){
                    this.disableRun = false; 
                    this.disableClose = true;
                }else if("Y".equals(enableRun) && !"N".equals(status)){
                    this.disableClose = false; 
                    this.disableRun = true;
                }else if("N".equals(enableRun)){
                    this.disableClose = true;
                    this.disableRun = true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void makeCurrentRow(SelectionEvent selectionEvent) {
        //重置StatusVO，此时查询生成entityList
        ViewObject vo = ADFUtils.findIterator("DmsApprovalStatusVOIterator").getViewObject();
        vo.executeQuery();
        RichTable wfTable = (RichTable)selectionEvent.getSource();
        CollectionModel cm = (CollectionModel)wfTable.getValue();
        JUCtrlHierBinding tableBinding = (JUCtrlHierBinding)cm.getWrappedData();
        DCIteratorBinding iter = tableBinding.getDCIteratorBinding();
        JUCtrlHierNodeBinding selectedRowData = (JUCtrlHierNodeBinding)wfTable.getSelectedRowData();
        Key rowKey = selectedRowData.getRowKey();
        iter.setCurrentRowWithKey(rowKey.toStringFormat(true));
        Row row = selectedRowData.getRow();
        String enableRun  = row.getAttribute("EnableRun").toString();
        String comId = row.getAttribute("ValueSetId").toString();
        Object status = row.getAttribute("ApprovalStatus");
        if("Y".equals(enableRun) && !"Y".equals(status)){
            this.disableRun = false; 
            this.disableClose = true;
        }else if("Y".equals(enableRun) && !"N".equals(status)){
            this.disableClose = false; 
            this.disableRun = true;
        }else if("N".equals(enableRun)){
            this.disableClose = true;
            this.disableRun = true;
        }
        
        //已经查询过的不再查询
        if(!this.comList.contains(comId)){
            this.getEntityList(comId);  
            this.comList.add(comId);
        }

        
    }
    
    public List<SelectItem> getEntityList(String comId){
        List<SelectItem> list = new ArrayList<SelectItem>();
        DBTransaction trans = (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(1);
        String sql = "SELECT V.ID,V.SOURCE FROM DCM_COM_VS T,DMS_VALUE_SET V WHERE T.IS_APPROVAL = 'Y' "
            + "AND T.VALUE_SET_ID = V.ID AND V.LOCALE = 'zh_CN' AND T.COMBINATION_ID = '" + comId + "'";
        ResultSet rs;
        String source = "";
        try {
            rs = stat.executeQuery(sql);
            if(rs.next()){
                source = rs.getString("SOURCE");    
            }else{
                rs.close();
                stat.close();
                return list;
            }
            rs.close();
            
            String sql1 = "SELECT T.CODE,T.MEANING FROM " + source + " T WHERE T.LOCALE = '" 
                          + ADFContext.getCurrent().getLocale() + "'";
            ResultSet eRs = stat.executeQuery(sql1);    
            while(eRs.next()){
                SelectItem sim = new SelectItem();
                sim.setValue(eRs.getString("CODE"));
                sim.setLabel(eRs.getString("MEANING"));
                System.out.println(eRs.getString("MEANING"));
                this.entityList.add(sim);
            }
            eRs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    public void startApproval(ActionEvent actionEvent) {
        ViewObject vo = ADFUtils.findIterator("DmsUserApprovalVOIterator").getViewObject();
        this.combination = vo.getCurrentRow().getAttribute("ValueSetId").toString();
        this.curAppId = vo.getCurrentRow().getAttribute("Id").toString();
        key = vo.getCurrentRow().getKey();
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
        vo.setCurrentRow(vo.getRow(key));
        this.firstName = "";
        RichPopup.PopupHints hints = new RichPopup.PopupHints();
        this.paramPop.show(hints);
        
    }
    
    public void initValues(){
        DBTransaction trans = (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        String locale = ((Person)ADFContext.getCurrent().getSessionScope().get("cur_user")).getLocale();
        Statement stat = trans.createStatement(1);
        try {
        for(AppParamBean app : this.paramList){
            String sql = "SELECT T.CODE,T.MEANING FROM " + app.getPSource() + " T WHERE T.ENABLED = 'Y'" +
                " AND T.LOCALE = '"+locale+"'";
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
        ViewObject vo = ADFUtils.findIterator("DmsUserApprovalVOIterator").getViewObject();
        vo.executeQuery();
        vo.setCurrentRow(vo.getRow(key));
        this.disableRun = true;
        this.disableClose = false;
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
        Key key = vo.getCurrentRow().getKey();
        String id = vo.getCurrentRow().getAttribute("Id").toString();
        String runId = vo.getCurrentRow().getAttribute("RunId").toString();
        //关闭组合
        String cSql = "UPDATE DCM_TEMPLATE_COMBINATION T SET T.STATUS = 'CLOSE' WHERE EXISTS (SELECT 1 FROM APPROVAL_TEMPLATE_STATUS A "
            + "WHERE A.COM_ID = T.COM_RECORD_ID AND A.RUN_ID = '" + runId + "')";
        //更新状态
        DBTransaction trans = (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        String sql = "UPDATE DMS_APPROVALFLOW_INFO T SET T.APPROVAL_STATUS = 'N' WHERE T.ID = '" + id + "'";
        try {
            stat.executeUpdate(sql);
            stat.executeUpdate(cSql);
            trans.commit();
            stat.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //刷新Table
        vo.executeQuery();
        vo.setCurrentRow(vo.getRow(key));
        this.disableRun = false;
        this.disableClose = true;
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

    public void setEntityList(List<SelectItem> entityList) {
        this.entityList = entityList;
    }

    public List<SelectItem> getEntityList() {
        return entityList;
    }

    public void setDisableClose(boolean disableClose) {
        this.disableClose = disableClose;
    }

    public boolean isDisableClose() {
        return disableClose;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getFirstName() {
        return firstName;
    }
}
