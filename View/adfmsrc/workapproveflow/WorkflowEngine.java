package workapproveflow;

import common.DmsUtils;

import dms.login.Person;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.sql.Types;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import java.util.UUID;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import oracle.jbo.domain.Number;
import oracle.adf.share.ADFContext;
import oracle.adf.share.logging.ADFLogger;
import oracle.jbo.server.DBTransaction;

import team.epm.dcm.view.DcmTemplateCombinaVOImpl;
import team.epm.dcm.view.DcmTemplateCombinaVORowImpl;
import team.epm.dms.view.DmsWorkflowTemplateStatusVOImpl;
import team.epm.dms.view.DmsWorkflowTemplateStatusVORowImpl;

public class WorkflowEngine {
    //日志
    private static ADFLogger _logger =
        ADFLogger.createADFLogger(WorkflowEngine.class);
    private Person curUser;

    public WorkflowEngine() {
        super();
        this.curUser =
                (Person)ADFContext.getCurrent().getSessionScope().get("cur_user");
    }

    //改变工作流状态

    public boolean changeWfStatus(String wfId, String wfStatus) {
        try {
            DBTransaction trans =
                (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
            String newStatus = "Y";
            if (wfStatus.equals("Y")) {
                newStatus = "N";
            }
            String sql =
                "UPDATE DMS_WORKFLOWINFO SET WF_STATUS = ? WHERE ID = ? ";
            PreparedStatement state = trans.createPreparedStatement(sql, 0);
            state.setString(1, newStatus);
            state.setString(2, wfId);
            int i = state.executeUpdate();
            trans.commit();
            state.close();
            return true;
        } catch (SQLException e) {
            this._logger.severe(e);
            return false;
        }
    }

    //初始化工作流步骤表

    public void initWfSteps(String wfId,String runId,
                            Map<String, Map<String, String>> comSelectMap) {
        StringBuffer openComPara = new StringBuffer();
        for (Map.Entry<String, Map<String, String>> entry :
             comSelectMap.entrySet()) {
            Map<String, String> map = entry.getValue();
            for (Map.Entry<String, String> mapEntry : map.entrySet()) {
                openComPara.append(mapEntry.getKey()).append(":").append(mapEntry.getValue()).append("#");
            }
        }
        DBTransaction trans =
            (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        //查询工作流步骤信息
        String stepSql =
            "SELECT STEP_NO,STEP_TASK,PRE_STEP,APPROVE_OBJECT,ETL_OBJECT,LABEL_OBJECT FROM DMS_WORKFLOW_STEPS WHERE LOCALE = '" +
            this.curUser.getLocale() + "' AND WF_ID = '" + wfId + "'";

        //插入生成的工作流步骤状态信息
        //INSERT INTO DMS_WORKFLOW_STATUS VALUES(WFID,RUNID,STEPNO,STEPTASK,STEPSTATUS,STARTAT,FINISHAT,SYSDATE,CURUSER,OPENCOM,STEPOBJ,PRESTEP)
        String insertSql =
            "INSERT INTO DMS_WORKFLOW_STATUS VALUES(?,?,?,?,?,NULL,NULL,SYSDATE,?,?,?,?)";
        PreparedStatement preStat =
            trans.createPreparedStatement(insertSql, 0);
        
        //更新工作流信息表，将最新启动的runId关联到工作流
        String updateSql =
            "UPDATE DMS_WORKFLOWINFO SET WF_RUNID = '" + runId + "'" +
            " WHERE ID ='" + wfId + "'";
        try {
            ResultSet rs = stat.executeQuery(stepSql);
            while (rs.next()) {
                int stepNo = rs.getInt("STEP_NO");
                String stepTask = rs.getString("STEP_TASK");
                int preStep = rs.getInt("PRE_STEP");
                String approveObj = rs.getString("APPROVE_OBJECT");
                String etlObj = rs.getString("ETL_OBJECT");
                String labelObj = rs.getString("LABEL_OBJECT");
                System.out.println(stepNo + "#" + stepTask + "#" + preStep +
                                   "#" + approveObj + "#" + etlObj + "#" +
                                   labelObj);
                //设置sql中插入的值
                preStat.setString(1, wfId);
                preStat.setString(2, runId);
                preStat.setInt(3, stepNo);
                preStat.setString(4, stepTask);
                preStat.setString(5, "N");
                preStat.setString(6, this.curUser.getId());
                preStat.setString(7, openComPara.toString());
                if (stepTask.equals("OPEN TEMPLATES")) {
                    preStat.setString(8, labelObj);
                } else if (stepTask.equals("ETL")) {
                    preStat.setString(8, etlObj);
                } else {
                    //APPROVE
                    preStat.setString(8, approveObj);
                }
                preStat.setInt(9, preStep);
                preStat.addBatch();
            }
            preStat.executeBatch();
            stat.executeUpdate(updateSql);
            trans.commit();
            rs.close();
            stat.close();
            preStat.close();
            //初始化步骤状态后，自动启动第一个步骤
            this.startFirstStep(wfId, runId);
        } catch (SQLException e) {
            this._logger.severe(e);
        }
    }

    //启动工作流第一个步骤
    
    private void startFirstStep(String wfId, String runId) {
        DBTransaction trans =
            (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        ApproveflowEngine afe = new ApproveflowEngine();
        //query first step
        String sql =
            "SELECT STEP_NO,STEP_TASK,OPEN_COM,STEP_OBJECT,PRE_STEP FROM DMS_WORKFLOW_STATUS WHERE STEP_NO = 1 AND WF_ID = '" +
            wfId + "'" + " AND RUN_ID ='" + runId + "'";
        try {
            ResultSet rs = stat.executeQuery(sql);
            String stepTask = "";
            String openCom = "";
            String stepObj = "";
            int preStep = 0;
            if (rs.next()) {
                stepTask = rs.getString("STEP_TASK");
                stepObj = rs.getString("STEP_OBJECT");
                openCom = rs.getString("OPEN_COM");
                preStep = rs.getInt("PRE_STEP");
            }
            if (stepTask.equals("OPEN TEMPLATES")) {
                this.openTemplates(stepObj, openCom,wfId,runId,1);
                //打开模板完成，更新步骤状态表为working
                String udSql = "UPDATE DMS_WORKFLOW_STATUS SET STEP_STATUS = 'WORKING',START_AT = SYSDATE WHERE WF_ID = '" + wfId + "'"
                    + " AND RUN_ID = '" + runId + "'" + " AND STEP_NO = 1 ";
                stat.executeUpdate(udSql);
                trans.commit();
            }
            if (stepTask.equals("ETL")) {
                //初始化ODI执行状态表
                this.initOdiStatus(wfId,runId, stepObj, 1, true);
                
                //更新ETL步骤为开始
                String ueSql = "UPDATE DMS_WORKFLOW_STATUS SET STEP_STATUS = 'WORKING',START_AT = SYSDATE WHERE WF_ID = '" + wfId + "'"
                    + " AND RUN_ID = '" + runId + "'" + " AND STEP_NO = 1 ";
                stat.executeUpdate(ueSql);
                trans.commit();

                //发送邮件提醒用户
//                String uSql = "SELECT S.SCENE_ALIAS,USER_ID FROM ODI11_USER_SCENE_V T,ODI11_SCENE S WHERE T.SCENE_ID " +
//                    "= S.ID AND S.LOCALE = 'zh_CN' AND T.SCENE_ID = '"
//                    + stepObj + "'";
//                ResultSet uRs = stat.executeQuery(uSql);
//                while(uRs.next()){
//                    afe.sendMail(uRs.getString("SCENE_ALIAS"),"", uRs.getString("USER_ID"), "工作流启动", "执行接口！", "工作流启动，请及时执行接口！", "");        
//                }
//                uRs.close();
                //跳过ETL
//                int stepNo = 1 ;
//                while(true){
//                    Map<String,String> nextMap = this.queryNextStep(wfId, runId, stepNo);   
//                    if("".equals(nextMap.get("STEP_TASK")) || nextMap.get("STEP_TASK")==null){
//                        return;        
//                    }
//                    if("OPEN TEMPLATES".equals(nextMap.get("STEP_TASK"))){
//                        this.openTemplates(nextMap.get("STEP_OBJECT"), openCom,wfId,runId,stepNo+1);
//                        //打开模板完成，更新步骤状态表为working
//                        String udSql = "UPDATE DMS_WORKFLOW_STATUS SET STEP_STATUS = 'WORKING',START_AT = SYSDATE WHERE WF_ID = '" + wfId + "'"
//                            + " AND RUN_ID = '" + runId + "'" + " AND STEP_NO = " + (stepNo + 1);
//                        stat.executeUpdate(udSql);
//                        trans.commit();       
//                        break;
//                    }else if("ETL".equals(nextMap.get("STEP_TASK"))){
//                        //更新ETL步骤为开始
//                        String ueSql0 = "UPDATE DMS_WORKFLOW_STATUS SET STEP_STATUS = 'WORKING',START_AT = SYSDATE WHERE WF_ID = '" + wfId + "'"
//                            + " AND RUN_ID = '" + runId + "'" + " AND STEP_NO = " + (stepNo + 1);
//                        stat.executeUpdate(ueSql0);
//                        trans.commit();    
//                        stepNo = stepNo + 1;
//                        //发送邮件提醒用户
//                        String uSql0 = "SELECT S.SCENE_ALIAS,USER_ID FROM ODI11_USER_SCENE_V T,ODI11_SCENE S WHERE T.SCENE_ID " +
//                            "= S.ID AND S.LOCALE = 'zh_CN' AND T.SCENE_ID = '"
//                            + stepObj + "'";
//                        ResultSet uRs0 = stat.executeQuery(uSql0);
//                        while(uRs0.next()){
//                            afe.sendMail(uRs0.getString("SCENE_ALIAS"),"", uRs0.getString("USER_ID"), "工作流启动", "执行接口！", "工作流，请及时执行接口！", "");        
//                        }
//                        uRs.close();
//                    }
//                }
            }
            if (stepTask.equals("APPROVE")) {
                //提示没有审批对象
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("第一步不能为审批，找不到审批对象，请检查工作流配置！"));
            }
            stat.close();
        } catch (SQLException e) {
            this._logger.severe(e);
        }
    }

    public Map<String,String> getWfCom(String wfId,String runId){
        DBTransaction trans =
            (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        String sql = "SELECT DISTINCT T.OPEN_COM FROM DMS_WORKFLOW_STATUS T WHERE T.WF_ID = '" + wfId + "' AND T.RUN_ID = '"
            + runId + "'";
        String wSql = "SELECT W.WF_NAME,D.NAME,D.CODE,D.SOURCE FROM DCM_COM_VS T,DMS_VALUE_SET D,DMS_WORKFLOWINFO W "
            + "WHERE T.COMBINATION_ID = W.WF_COM AND W.LOCALE = D.LOCALE AND T.VALUE_SET_ID = D.ID AND D.LOCALE = 'zh_CN' "
            + "AND W.ID = '" + wfId + "' ORDER BY T.SEQ";
        String openCom = "";
        Map<String,String> comMap = new HashMap<String,String>();
        try {
            ResultSet rs = stat.executeQuery(sql);
            if(rs.next()){
                openCom = rs.getString("OPEN_COM");        
            }
            if("".equals(openCom) || openCom == null){
                return comMap;
            }
            rs.close();
            
            ResultSet wRs = stat.executeQuery(wSql);
            String wfName = "";
            Map<String,String> codeSourceMap = new LinkedHashMap<String,String>();
            while(wRs.next()){
                String vcode = wRs.getString("CODE");       
                String vsource = wRs.getString("SOURCE");
                wfName = wRs.getString("WF_NAME");
                codeSourceMap.put(vcode, vsource);
            }
            wRs.close();
            
            //YEARS:FY16#SCENARIO:Budget#VERSION:V01#
            String comName = "";
            String comCode = "";
            String [] args = openCom.split("#");
            
            for(int i = 0;i<args.length ; i++){

                String [] cv = args[i].split(":");
                comName = comName + "#" + this.getValueName(codeSourceMap.get(cv[0]), cv[1]);
                comCode = comCode + "#" + cv[1];   
            }
            comMap.put("comCode", comCode);
            comMap.put("comName", comName);
            comMap.put("wfName", wfName);
            stat.close();
        } catch (SQLException e) { 
            this._logger.severe(e);
        }
        return comMap;
    }
    
    public String getValueName(String source,String value){
        DBTransaction trans =
            (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        String sql = "SELECT MEANING FROM " + source + " WHERE LOCALE = 'zh_CN' AND CODE = '" + value + "'";
        String meaning = "";
        ResultSet rs;
        try {
            rs = stat.executeQuery(sql);
            if(rs.next()){
                meaning = rs.getString("MEANING");        
            }
            rs.close();
            stat.close();
        } catch (SQLException e) {
            this._logger.severe(e);
        } 
        return meaning;
    }
    
    public void changeEtlStatus(String wfId,String runId,String sceneId,String comId,String status){
        DBTransaction trans =
            (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        String sql = "UPDATE WORKFLOW_ODI_STATUS T SET T.EXEC_STATUS = '" + status + "' WHERE T.RUN_ID = '"
            + runId + "' AND T.SCENE_ID = '" + sceneId 
            + "' AND T.ENTITY_PARENT = ( SELECT DISTINCT D.PARENT FROM WORKFLOW_TEMPLATE_STATUS W,DCM_ENTITY_PARENT D WHERE W.ENTITY_CODE = D.ENTITY AND W.COM_ID = '"
            + comId + "')";
        
        String eSql =  "SELECT DISTINCT T.ODI_PARAM,T.EXEC_BY FROM DMS_WORKFLOW_ODI_PARAM T WHERE T.RUN_ID = '"
            + runId + "' AND T.SCENE_ID = '" + sceneId 
            + "' AND T.ENTITY_PARENT = ( SELECT DISTINCT D.PARENT FROM WORKFLOW_TEMPLATE_STATUS W,DCM_ENTITY_PARENT D WHERE W.ENTITY_CODE = D.ENTITY AND W.COM_ID = '"
            + comId + "')";
        
        try {
            stat.executeUpdate(sql);
            trans.commit();
            
            ResultSet rs = stat.executeQuery(eSql);
            while(rs.next()){
                this.sendEtlMail(wfId,runId,sceneId, rs.getString("ODI_PARAM"), rs.getString("EXEC_BY"));    
            }
            rs.close();
            stat.close();
        } catch (SQLException e) {
            this._logger.severe(e);
        }
    }
    
    public void initOdiStatus(String wfId,String runId,String sceneId,int stepNo,boolean isFull){
        
        DBTransaction trans =
            (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        String iSql = "INSERT INTO WORKFLOW_ODI_STATUS VALUES(?,?,?,?,?,?,?,?,SYSDATE,?,?)";
        PreparedStatement pstat = trans.createPreparedStatement(iSql, 0);
 
        //查询接口前一个填写表单步骤
        String lSql = "SELECT T.STEP_NO FROM DMS_WORKFLOW_STATUS T WHERE T.STEP_TASK = 'OPEN TEMPLATES' AND T.STEP_NO < " + stepNo
            + " AND T.RUN_ID = '" + runId + "' ORDER BY T.STEP_NO DESC";
        
        ResultSet lRs;
        try {
            lRs = stat.executeQuery(lSql);
            if(lRs.next()){
                //前面有填写模板的步骤   
                int wNo = lRs.getInt("STEP_NO");
                //查询填写模板的步骤存在的父节点，且部门父节点对应的ODI参数节点
                String aSql = "SELECT DISTINCT T.ENTITY_PARENT,T.ODI_PARAM FROM WORKFLOW_TEMPLATE_STATUS W,DCM_ENTITY_PARENT D,DMS_WORKFLOW_ODI_PARAM T "
                    + "WHERE W.ENTITY_CODE = D.ENTITY AND T.ENTITY_PARENT = D.PARENT AND W.RUN_ID = '" + runId + "' AND W.STEP_NO = " + wNo
                    + " AND T.SCENE_ID = '" + sceneId + "'";
                lRs.close();
                ResultSet rs = stat.executeQuery(aSql);
                while(rs.next()){
                //INSERT INTO WORKFLOW_ODI_STATUS VALUES('ID','RUNID',1,'SID','ARGS','STATUS','EXEC_BY','FAT',SYSDATE,'CUSER','')
                    pstat.setString(1, UUID.randomUUID().toString().replace("-", ""));
                    pstat.setString(2, runId);
                    pstat.setInt(3, stepNo);
                    pstat.setString(4, sceneId);
                    pstat.setString(5, rs.getString("ODI_PARAM"));
                    pstat.setString(6, "CLOSE");
                    pstat.setString(7, null);
                    pstat.setDate(8, null);
                    pstat.setString(9, this.curUser.getId());
                    pstat.setString(10, rs.getString("ENTITY_PARENT"));
                    pstat.addBatch();
                }
                pstat.executeBatch();
                trans.commit();
                rs.close();
            }else{
                //前面没有填写模板的步骤，接口为跑全部门,从dms_workflow_odi_param中查找参数
                String sql1 = "SELECT DISTINCT T.ODI_PARAM,T.EXEC_BY FROM DMS_WORKFLOW_ODI_PARAM T WHERE T.SCENE_ID = '" + sceneId + "'";
                ResultSet rs1 = stat.executeQuery(sql1);
                while(rs1.next()){
                    pstat.setString(1, UUID.randomUUID().toString().replace("-", ""));
                    pstat.setString(2, runId);
                    pstat.setInt(3, stepNo);
                    pstat.setString(4, sceneId);
                    pstat.setString(5, rs1.getString("ODI_PARAM"));
                    pstat.setString(6, "N");
                    pstat.setString(7, null);
                    pstat.setDate(8, null);
                    pstat.setString(9, this.curUser.getId());
                    pstat.setString(10, "ALLENTITY");
                    pstat.addBatch(); 
                    
                    //发送邮件
                    this.sendEtlMail(wfId,runId,sceneId, rs1.getString("ODI_PARAM"), rs1.getString("EXEC_BY"));
                }
                pstat.executeBatch();
                trans.commit();
                rs1.close();
            }
            stat.close();
            pstat.close();
        } catch (SQLException e) {
            this._logger.severe(e);
        }
    }
    
    public void sendEtlMail(String wfId,String runId,String sceneId,String odiParam,String execUser){
        ApproveflowEngine afe = new ApproveflowEngine();
        DBTransaction trans =
            (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        
        String sql0 = "SELECT T.SCENE_ALIAS FROM ODI11_SCENE T WHERE T.LOCALE = 'zh_CN' AND T.ID = '" + sceneId + "'";
        String sql1 = "SELECT T.MEANING FROM DIM_ENTITYS T WHERE T.LOCALE = 'zh_CN' AND T.CODE = '" + odiParam + "'";
        try {
            String odiName = "";
            String paraName = "";

            ResultSet rs0 = stat.executeQuery(sql0);
            if(rs0.next()){
                 odiName = rs0.getString("SCENE_ALIAS");   
            }
            rs0.close();
            
            ResultSet rs1 = stat.executeQuery(sql1);
            if(rs1.next()){
                 paraName = rs1.getString("MEANING");   
            }
            rs1.close();
            
            Map<String,String> wfMap = this.getWfCom(wfId, runId);
            String comName = wfMap.get("comName")+"#"+paraName;
            String wfName = "【" + wfMap.get("wfName") + "】" ;
            
            afe.sendMail(odiName, comName, execUser, "系统", wfName + "执行接口！", "请及时执行接口！", "");
            
            stat.close();
        } catch (SQLException e) {
            this._logger.severe(e);
        }
    }
    
    //打开模板，打开所有组合
    //stepObj 模板标签  openCom打开组合
    public void openTemplates(String stepObj, String openCom,String wfId,String runId,int stepNo) {
        //分割组合参数的值和列名
        Map<String, String> valuesMap = new HashMap<String, String>();
        //去掉拼接时最后一个#
        if(openCom == null || "".equals(openCom)){
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("工作流未配置打开模板组合！"));
            return;
        }
        openCom = openCom.substring(0, openCom.length() - 1);
        String[] str = openCom.split("#");
        for (int i = 0; i < str.length; i++) {
            String[] s = str[i].split(":");
            //s[0]:列名，s[1]：值
            valuesMap.put(s[0], s[1]);
        }
        //保存所有模板，和模板对应要打开的组合ID
        Map<String, List<String>> tempComMap =
            new HashMap<String, List<String>>();
        //保存所有模板，和模板要打开的部门编码,组合ID
        Map<String,Map<String,List<String>>> tempEntityMap = new HashMap<String,Map<String,List<String>>>();
        DBTransaction trans =
            (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        //tempMap<template_id,com_id>
        Map<String, String> tempMap = new HashMap<String, String>();
        //query templates
        String tempSql =
            "SELECT T.ID,D.CODE FROM DCM_TEMPLATE T,DCM_COMBINATION D WHERE T.COMBINATION_ID = D.ID AND T.LOCALE = D.LOCALE AND T.LOCALE = '" +
            this.curUser.getLocale() + "'" + " AND T.TEMPLATE_LABEL = '" +
            stepObj + "'";
        ResultSet tempRs;
        try {
            tempRs = stat.executeQuery(tempSql);
            while (tempRs.next()) {
                String tempId = tempRs.getString("ID");
                //每个模板对应的组合ID的源表
                String comSource = tempRs.getString("CODE");
                tempMap.put(tempId, comSource);
            }
            tempRs.close();
            //获得每个模板要打开的组合ID
            for (Map.Entry<String, String> entry : tempMap.entrySet()) {
                // 查询模板对应的组合和值集CODE
                String comVsSql = "select t2.is_entity ,t3.code ,t4.code as source from dcm_template t1 , dcm_com_vs t2 , dms_value_set t3 , dcm_combination t4 "
                    + "where t1.combination_id = t2.combination_id and t2.value_set_id = t3.id and t1.combination_id = t4.id and t1.locale = t4.locale " 
                    + "and t1.locale = t3.locale and t1.locale = 'zh_CN' and t1.id = '" + entry.getKey() + "'";
                //存模板组合对应的值集编码
                Map<String,String> tempComVsMap = new HashMap<String,String>();
                boolean isEntity = false;//组合是否含有实体 
                ResultSet comVsRs = stat.executeQuery(comVsSql);
                while(comVsRs.next()){
                    tempComVsMap.put(comVsRs.getString("CODE"), comVsRs.getString("SOURCE"));
                    if("Y".equals(comVsRs.getString("IS_ENTITY"))){
                        isEntity = true ;    
                    }
                }
                //查询每个模板在dcm_tem_entity_com中维护了的部门和部门对应的列名
                String entitySql =
                    "SELECT ENTITY,ENTITY_CODE FROM DCM_TEM_ENTITY_COM WHERE DCM_TEMPLATE_ID = '" +
                    entry.getKey() + "'";
                System.out.println("entitySql:::"+entitySql);
                ResultSet entityRs = stat.executeQuery(entitySql);
                //存部门和该部门对应查询组合ID的sql
                Map<String,String> sqlMap = new HashMap<String,String>();
                //部门编码,和该部门对应的组合ID
                Map<String,List<String>> entityMap = new HashMap<String,List<String>>();
                StringBuffer cis = new StringBuffer();
                System.out.println("SELECT ID FROM!!!!"+entry.getValue());
                cis.append("SELECT ID FROM ").append(entry.getValue());
                boolean tempFlag = true;//只拼一个WHERE
                //拼接模板组合中和工作流组合相同的值集条件(满足条件的为含组合的模板)
                for(Map.Entry<String,String> cEntry : tempComVsMap.entrySet()){
                    if(valuesMap.containsKey(cEntry.getKey())){
                        //System.out.println("sssssss:"+tempFlag);
                        if(tempFlag){
                            cis.append(" WHERE ");
                            tempFlag = false;
                        }
                        //System.out.println(cEntry.getValue()+" = '"+valuesMap.get(cEntry.getValue()));
                        cis.append(cEntry.getKey()).append(" = '").append(valuesMap.get(cEntry.getKey())).append("' AND ");
                    }  
                }
                //System.out.println(cis.toString());
                boolean openEntity = false ;
                //循环模板要打开的部门（满足条件的为含组合且组合中含部门的模板）
                while(entityRs.next()){
                    StringBuffer cisStr = new StringBuffer();
                    cisStr.append(cis);
                    if(tempComVsMap.containsKey(entityRs.getString("ENTITY_CODE"))){
                        if(tempFlag){
                            cisStr.append(" WHERE ");
                            tempFlag = false;
                        }        
                        cisStr.append(entityRs.getString("ENTITY_CODE")).append(" = '").append(entityRs.getString("ENTITY")).append("' AND ");
                        openEntity = true ;
                    }     
                    //去掉最后一个多出的"AND ",加空格共四位
                    String sqlStr =
                        cisStr.substring(0, cisStr.length() - 4);
                    sqlMap.put(entityRs.getString("ENTITY"),sqlStr);
                }
                
                if(!isEntity && !openEntity){
                    //组合不存在实体
                    //去掉最后一个多出的"AND ",加空格共四位
                    String sqlStr =
                        cis.substring(0, cis.length() - 4);
                    sqlMap.put(entry.getKey().substring(0, entry.getKey().length()-10)+"_NotEntity",sqlStr);
                }
                if(isEntity && !openEntity){
                    //组合存在实体，但是没有打开任何实体
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("检测到工作流存在未维护部门的表单，请维护完整表单要打开的部门！"));
                }
                if(tempFlag){
                    //存在组合，但是跟工作流组合没有任何交集  
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("检测到模板组合跟工作流组合没有任何交集，已自动忽略！"));
                }
                //根据工作流选择的组合，查询出在模板组合表中的ID
                List<String> comIdList = new ArrayList<String>();
                for (Map.Entry<String,String> sqlEntry : sqlMap.entrySet()) {
                    System.out.println("query:"+sqlEntry.getValue());
                    ResultSet comIdRs = stat.executeQuery(sqlEntry.getValue());
                    List<String> comEntityList = new ArrayList<String>();
                    while(comIdRs.next()) {
                        String comId = comIdRs.getString("ID");
                        comIdList.add(comId);
                        comEntityList.add(comId);
                    }
                    entityMap.put(sqlEntry.getKey(),comEntityList);
                    comIdRs.close();
                }
                tempComMap.put(entry.getKey(), comIdList);
                //put模板ID 和 对应要打开的部门Map
                tempEntityMap.put(entry.getKey(), entityMap);
                entityRs.close();
            }
            //打开每张模板对应的组合
            this.openTempCom(tempComMap);
            //初始化每张模板的输入状态tempEntityMap<temp_id,<entity_code,com_id>>
            this.initTemplateStatus(runId, stepNo, tempEntityMap,true);
            //发送填写表单邮件
            this.sendWriteMail(wfId,runId, stepNo, tempComMap,true);
            //查询审批步骤
            StringBuffer stepNoSql = new StringBuffer();
            int approveNo = stepNo;
            stepNoSql.append("select STEP_NO from dms_workflow_status where step_task = 'APPROVE' ");
            stepNoSql.append("and step_no > ").append(stepNo);
            stepNoSql.append("order by step_no");
            ResultSet snRs = stat.executeQuery(stepNoSql.toString());
            if(snRs.next()){
                approveNo = snRs.getInt("STEP_NO");        
            }
            //初始化每张模板的审批状态
            this.initApproveStatus(runId, approveNo, tempEntityMap);
            stat.close();
        } catch (SQLException e) {
            this._logger.severe(e);
        }
    }
    
    //打开模板，不打开组合
    public void onlyOpenTemplates(String stepObj, String openCom,String runId,int stepNo){
        //分割组合参数的值和列名
        Map<String, String> valuesMap = new HashMap<String, String>();
        //去掉拼接时最后一个#
        openCom = openCom.substring(0, openCom.length() - 1);
        String[] str = openCom.split("#");
        for (int i = 0; i < str.length; i++) {
            String[] s = str[i].split(":");
            //s[0]:列名，s[1]：值
            valuesMap.put(s[0], s[1]);
        }
        //保存所有模板，和模板对应要打开的组合ID
        Map<String, List<String>> tempComMap =
            new HashMap<String, List<String>>();
        //保存所有模板，和模板要打开的部门编码,组合ID
        Map<String,Map<String,List<String>>> tempEntityMap = new HashMap<String,Map<String,List<String>>>();
        DBTransaction trans =
            (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        //tempMap<template_id,com_id>
        Map<String, String> tempMap = new HashMap<String, String>();
        //query templates
        String tempSql =
            "SELECT T.ID,D.CODE FROM DCM_TEMPLATE T,DCM_COMBINATION D WHERE T.COMBINATION_ID = D.ID AND T.LOCALE = D.LOCALE AND T.LOCALE = '" +
            this.curUser.getLocale() + "'" + " AND T.TEMPLATE_LABEL = '" +
            stepObj + "'";
        ResultSet tempRs;
        try {
            tempRs = stat.executeQuery(tempSql);
            while (tempRs.next()) {
                String tempId = tempRs.getString("ID");
                //每个模板对应的组合ID的源表
                String comSource = tempRs.getString("CODE");
                tempMap.put(tempId, comSource);
            }
            tempRs.close();
            //获得每个模板要打开的组合ID
            for (Map.Entry<String, String> entry : tempMap.entrySet()) {
                // 查询模板对应的组合和值集CODE
                String comVsSql = "select t2.is_entity ,t3.code ,t4.code as source from dcm_template t1 , dcm_com_vs t2 , dms_value_set t3 , dcm_combination t4 "
                    + "where t1.combination_id = t2.combination_id and t2.value_set_id = t3.id and t1.combination_id = t4.id and t1.locale = t4.locale " 
                    + "and t1.locale = t3.locale and t1.locale = 'zh_CN' and t1.id = '" + entry.getKey() + "'";
                //存模板组合对应的值集编码
                Map<String,String> tempComVsMap = new HashMap<String,String>();
                boolean isEntity = false;//组合是否含有实体 
                ResultSet comVsRs = stat.executeQuery(comVsSql);
                while(comVsRs.next()){
                    tempComVsMap.put(comVsRs.getString("CODE"), comVsRs.getString("SOURCE"));
                    if("Y".equals(comVsRs.getString("IS_ENTITY"))){
                        isEntity = true ;    
                    }
                }
                //查询每个模板在dcm_tem_entity_com中维护了的部门和部门对应的列名
                String entitySql =
                    "SELECT ENTITY,ENTITY_CODE FROM DCM_TEM_ENTITY_COM WHERE DCM_TEMPLATE_ID = '" +
                    entry.getKey() + "'";
                System.out.println("entitySql:::"+entitySql);
                ResultSet entityRs = stat.executeQuery(entitySql);
                //存部门和该部门对应查询组合ID的sql
                Map<String,String> sqlMap = new HashMap<String,String>();
                //部门编码,和该部门对应的组合ID
                Map<String,List<String>> entityMap = new HashMap<String,List<String>>();
                StringBuffer cis = new StringBuffer();
                System.out.println("SELECT ID FROM!!!!"+entry.getValue());
                cis.append("SELECT ID FROM ").append(entry.getValue());
                boolean tempFlag = true;//只拼一个WHERE
                //拼接模板组合中和工作流组合相同的值集条件(满足条件的为含组合的模板)
                for(Map.Entry<String,String> cEntry : tempComVsMap.entrySet()){
                    if(valuesMap.containsKey(cEntry.getKey())){
                        //System.out.println("sssssss:"+tempFlag);
                        if(tempFlag){
                            cis.append(" WHERE ");
                            tempFlag = false;
                        }
                        //System.out.println(cEntry.getValue()+" = '"+valuesMap.get(cEntry.getValue()));
                        cis.append(cEntry.getKey()).append(" = '").append(valuesMap.get(cEntry.getKey())).append("' AND ");
                    }  
                }
                //System.out.println(cis.toString());
                boolean openEntity = false ;
                //循环模板要打开的部门（满足条件的为含组合且组合中含部门的模板）
                while(entityRs.next()){
                    StringBuffer cisStr = new StringBuffer();
                    cisStr.append(cis);
                    if(tempComVsMap.containsKey(entityRs.getString("ENTITY_CODE"))){
                        if(tempFlag){
                            cisStr.append(" WHERE ");
                            tempFlag = false;
                        }        
                        cisStr.append(entityRs.getString("ENTITY_CODE")).append(" = '").append(entityRs.getString("ENTITY")).append("' AND ");
                        openEntity = true ;
                    }     
                    //去掉最后一个多出的"AND ",加空格共四位
                    String sqlStr =
                        cisStr.substring(0, cisStr.length() - 4);
                    sqlMap.put(entityRs.getString("ENTITY"),sqlStr);
                }
                
                if(!isEntity && !openEntity){
                    //组合不存在实体
                    //去掉最后一个多出的"AND ",加空格共四位
                    String sqlStr =
                        cis.substring(0, cis.length() - 4);
                    sqlMap.put(entry.getKey().substring(0, entry.getKey().length()-10)+"_NotEntity",sqlStr);
                }
                if(isEntity && !openEntity){
                    //组合存在实体，但是没有打开任何实体
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("检测到模板没有打开任何部门，请维护完整模板要打开的部门！"));
                }
                if(tempFlag){
                    //存在组合，但是跟工作流组合没有任何交集  
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("检测到模板组合跟工作流组合没有任何交集，已自动忽略！"));
                }
                //根据工作流选择的组合，查询出在模板组合表中的ID
                List<String> comIdList = new ArrayList<String>();
                for (Map.Entry<String,String> sqlEntry : sqlMap.entrySet()) {
                    System.out.println("query:"+sqlEntry.getValue());
                    ResultSet comIdRs = stat.executeQuery(sqlEntry.getValue());
                    List<String> comEntityList = new ArrayList<String>();
                    while(comIdRs.next()) {
                        String comId = comIdRs.getString("ID");
                        comIdList.add(comId);
                        comEntityList.add(comId);
                    }
                    entityMap.put(sqlEntry.getKey(),comEntityList);
                    comIdRs.close();
                }
                tempComMap.put(entry.getKey(), comIdList);
                //put模板ID 和 对应要打开的部门Map
                tempEntityMap.put(entry.getKey(), entityMap);
                entityRs.close();
            }
            //打开每张模板对应的组合
            //this.openTempCom(tempComMap);
            //初始化每张模板的输入状态tempEntityMap<temp_id,<entity_code,com_id>>
            this.initTemplateStatus(runId, stepNo, tempEntityMap,false);
            //查询审批步骤
            StringBuffer stepNoSql = new StringBuffer();
            int approveNo = stepNo;
            stepNoSql.append("select STEP_NO from dms_workflow_status where step_task = 'APPROVE' ");
            stepNoSql.append("and step_no > ").append(stepNo);
            stepNoSql.append("order by step_no");
            ResultSet snRs = stat.executeQuery(stepNoSql.toString());
            if(snRs.next()){
                approveNo = snRs.getInt("STEP_NO");        
            }
            //初始化每张模板的审批状态
            this.initApproveStatus(runId, approveNo, tempEntityMap);
            stat.close();
        } catch (SQLException e) {
            this._logger.severe(e);
        }
    }
    
    //打开模板组合 tempComMap<模板ID，List<组合ID>>
    private void openTempCom(Map<String,List<String>> tempComMap){
        DcmTemplateCombinaVOImpl tempComVo =DmsUtils.getDcmApplicationModule().getDcmTemplateCombinaVO();
        for(Map.Entry<String,List<String>> tempEntry : tempComMap.entrySet()){
            for(String comId : tempEntry.getValue()){
                tempComVo.queryById(tempEntry.getKey(), comId);
                DcmTemplateCombinaVORowImpl row = (DcmTemplateCombinaVORowImpl)tempComVo.first();
                if(row != null){
                    if(row.getStatus().equals("CLOSE")){
                        //存在则修改为 OPEN
                        row.setStatus("OPEN");
                    }
                }else{
                    //不存在则新建一条
                    DcmTemplateCombinaVORowImpl newRow = (DcmTemplateCombinaVORowImpl)tempComVo.createRow();
                    newRow.setComRecordId(comId);
                    newRow.setTemplateId(tempEntry.getKey());
                    newRow.setStatus("OPEN");
                    tempComVo.insertRow(newRow);
                }
            }
        }
        tempComVo.getApplicationModule().getTransaction().commit();
    }
    
    public void sendWriteMail(String wfId,String runId,int stepNo,Map<String,List<String>> tempComMap,boolean isFirst){
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT DISTINCT T.WRITE_BY,E.MEANING,T.DCM_TEMPLATE_ID FROM DCM_TEM_ENTITY_COM T ,WORKFLOW_TEMPLATE_STATUS W ,DCM_TEMPLATE D ,DIM_ENTITYS E ");
        sql.append("WHERE T.ENTITY = W.ENTITY_CODE AND T.DCM_TEMPLATE_ID = W.TEMPLATE_ID AND T.DCM_TEMPLATE_ID = D.ID AND T.ENTITY = E.CODE ");
        if(isFirst){
            //第一个步骤批量打开模板状态直接为N
            sql.append("AND D.LOCALE = E.LOCALE AND D.LOCALE = 'zh_CN' AND W.WRITE_STATUS = 'N' ");
        }else{
            //回退一个子部门，再次打开时，不提醒没有回退的字部门
            sql.append("AND D.LOCALE = E.LOCALE AND D.LOCALE = 'zh_CN' AND W.WRITE_STATUS = 'CLOSE' ");   
        }
        sql.append("AND W.RUN_ID = '").append(runId).append("' ");
        sql.append("AND W.STEP_NO = ").append(stepNo);
        String tempSql = " AND T.DCM_TEMPLATE_ID IN (";
        String comSql = "AND W.COM_ID IN (";
        for(Map.Entry<String,List<String>> tempEntry : tempComMap.entrySet()){
            tempSql = tempSql + "'" + tempEntry.getKey() + "',";
            for(String comId : tempEntry.getValue()){
                comSql = comSql + "'" + comId + "',";
            }
        }
        sql.append(tempSql).append("'') ").append(comSql).append("'')");
        
        DBTransaction trans =
            (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);

        ApproveflowEngine afe = new ApproveflowEngine();
        try {
            Map<String,String> wfMap = this.getWfCom(wfId, runId);
            ResultSet rs = stat.executeQuery(sql.toString());
            while(rs.next()){
                String writeUser = rs.getString("WRITE_BY");
                String entityName = rs.getString("MEANING");
                String templateId = rs.getString("DCM_TEMPLATE_ID");
                String wfName = "【" + wfMap.get("wfName") + "】";
                entityName = wfMap.get("comName")+"#"+entityName;
                afe.sendMail(templateId, entityName, writeUser, "系统", wfName + "填写表单!", "请及时填写表单并提交！", "");
            }
            rs.close();
            stat.close();
        } catch (SQLException e) {
            this._logger.severe(e);
        }
    }
    
    //打开子部门的输入状态
    private void openWirteStatus(Map<String,List<String>> tempComMap,String runId,int stepNo){
        DBTransaction trans =
            (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        try{
            for(Map.Entry<String,List<String>> tempEntry : tempComMap.entrySet()){
                for(String comId : tempEntry.getValue()){
                    StringBuffer upWs = new StringBuffer();
                    upWs.append("UPDATE WORKFLOW_TEMPLATE_STATUS T SET T.WRITE_STATUS = 'N' WHERE ");
                    upWs.append("T.RUN_ID = '").append(runId).append("' ");
                    upWs.append("AND T.TEMPLATE_ID = '").append(tempEntry.getKey()).append("' ");
                    upWs.append("AND T.STEP_NO = ").append(stepNo);
                    upWs.append(" AND T.COM_ID = '").append(comId).append("' ");
                    upWs.append("AND T.WRITE_STATUS <> 'Y'");
                    stat.addBatch(upWs.toString());
                }
            }
            stat.executeBatch();
            trans.commit();
            stat.clearBatch();
            
            //查询回退涉及到的子部门中已经输入完成
            StringBuffer sql = new StringBuffer();
            sql.append("SELECT T.TEMPLATE_ID,T.COM_ID FROM WORKFLOW_TEMPLATE_STATUS T WHERE T.WRITE_STATUS = 'Y' ");
            sql.append("AND T.RUN_ID = '").append(runId).append("' ");
            sql.append("AND T.STEP_NO = ").append(stepNo);
            sql.append(" AND T.COM_ID IN (");
            for(Map.Entry<String,List<String>> tempEntry : tempComMap.entrySet()){
                for(String comId : tempEntry.getValue()){
                    sql.append("'").append(comId).append("',");
                }
            }
            sql.append("'')");
            ResultSet rs = stat.executeQuery(sql.toString());
            while(rs.next()){
                String comSql = "UPDATE DCM_TEMPLATE_COMBINATION T SET T.STATUS = 'CLOSE',T.UPDATED_AT = SYSDATE " 
                    + "WHERE T.TEMPLATE_ID = '" + rs.getString("TEMPLATE_ID") + "' AND T.COM_RECORD_ID = '" + rs.getString("COM_ID") + "'";
                stat.addBatch(comSql);
            }
            stat.executeBatch();
            trans.commit();
            
            stat.close();
        }catch(Exception e){
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("开启子部门输入系统异常！"));
            this._logger.severe("开启子部门输入系统异常"+e);    
        }
    }
    
    //打开所有步骤中子部门对应的所有模板输入状态
    public void openComByChild(String wfId,String runId ,String templateId,String comId , int stepNo){
        DBTransaction trans =
            (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        //查询部门的父节点下的所有子部门
        List<String> childList = new ArrayList<String>();
        StringBuffer pSql = new StringBuffer();
        pSql.append("select ENTITY from dcm_entity_parent d where d.parent = ( ");
        pSql.append("select distinct parent from workflow_template_status t1 , dcm_entity_parent t2 ");
        pSql.append("where t1.entity_code = t2.entity and t1.template_id = '").append(templateId).append("' ");
        pSql.append("and t1.com_id = '").append(comId).append("')");
        try {
            ResultSet rs = stat.executeQuery(pSql.toString());
            while(rs.next()){
                childList.add(rs.getString("ENTITY"));      
            }
            rs.close();
            //查询模板状态表中所有子部门对应的模板，组合ID
            StringBuffer comSql = new StringBuffer();
            comSql.append("select template_id,com_id from workflow_template_status ");
            comSql.append("where run_id = '").append(runId).append("' ");
            comSql.append("and step_no = ").append(stepNo);
            comSql.append(" and entity_code in (");
            for(String etStr : childList){
                comSql.append("'").append(etStr).append("',");    
            }
            comSql.append("'')");
            ResultSet comRs = stat.executeQuery(comSql.toString());
            Map<String,List<String>> tempComMap = new HashMap<String,List<String>>();
            List<String> comList ;
            while(comRs.next()){
                String temp_Id = comRs.getString("TEMPLATE_ID");
                String com_Id = comRs.getString("COM_ID");
                if(tempComMap.containsKey(temp_Id)){
                    comList = tempComMap.get(temp_Id);
                    comList.add(com_Id);
                }else{
                    comList = new ArrayList<String>();
                    comList.add(com_Id);
                }
                tempComMap.put(temp_Id, comList);
            }
            comRs.close();
            stat.close();
            //打开子部门对应的模板组合
            this.openTempCom(tempComMap);
            //发送邮件
            this.sendWriteMail(wfId,runId, stepNo, tempComMap,false);
            //开启模板输入状态,并将已经输入的子部门关闭（回退）
            this.openWirteStatus(tempComMap,runId, stepNo);
        } catch (SQLException e) {
            this._logger.severe(e);
        }
    }
    
    //初始化模板部门数据输入状态表
    private void initTemplateStatus(String runId,int stepNo,Map<String,Map<String,List<String>>> tempEntityMap,boolean initAll){
        DmsWorkflowTemplateStatusVOImpl vo = DmsUtils.getDmsApplicationModule().getDmsWorkflowTemplateStatusVO();
        for(Map.Entry<String,Map<String,List<String>>> tempEntry : tempEntityMap.entrySet()){
            for(Map.Entry<String,List<String>> entry : tempEntry.getValue().entrySet()){
                for(String comId : entry.getValue()){
                    System.out.println(runId+":"+stepNo+":"+tempEntry.getKey()+":"+entry.getKey()+":"+comId+":N");
                    DmsWorkflowTemplateStatusVORowImpl wtsRow = (DmsWorkflowTemplateStatusVORowImpl)vo.createRow();
                    wtsRow.setRunId(runId);
                    wtsRow.setStepNo(new Number(stepNo));
                    wtsRow.setTemplateId(tempEntry.getKey());
                    wtsRow.setEntityCode(entry.getKey());
                    wtsRow.setComId(comId);
                    wtsRow.setWriteBy("");
                    if(initAll){
                        wtsRow.setWriteStatus("N");
                    }else{
                        wtsRow.setWriteStatus("CLOSE");
                    }
                    wtsRow.setFinishAt(null);
                    vo.insertRow(wtsRow);    
                }
            }
        }
        vo.getApplicationModule().getTransaction().commit();
    }
    //初始化每张模板的审批状态
    private void initApproveStatus(String runId,int stepNo,Map<String,Map<String,List<String>>> tempEntityMap){
        DBTransaction trans =
            (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        //遍历模板和对应的部门
        try{
        for(Map.Entry<String,Map<String,List<String>>> tempEntry : tempEntityMap.entrySet()){
            for(Map.Entry<String,List<String>> entityEntry : tempEntry.getValue().entrySet()){
                Map<String,List<String>> entityUserMap = new HashMap<String,List<String>>();
                List<String> userList = new ArrayList<String>();
                String tempId = tempEntry.getKey();
                String entityCode = entityEntry.getKey();
                if(entityCode.equals(tempId.substring(0, tempId.length()-10)+"_NotEntity")){
                    continue;   //不需要审批的模板    
                }
                StringBuffer tempSql = new StringBuffer();
                //查询每个部门对应的审核人
                tempSql.append("SELECT T2.CODE,T2.USER_ID,T2.SEQ FROM DMS_APPROVALFLOWINFO T1 , DMS_APPROVALFLOW_ENTITYS T2 ");
                tempSql.append("WHERE  T1.ID = T2.APPROVAL_ID AND T1.LOCALE =T2.LOCALE ");
                tempSql.append("AND T1.LOCALE = '").append(this.curUser.getLocale()).append("' ");
                tempSql.append("AND T1.TEMPLATE_ID = '").append(tempId).append("' ");
                tempSql.append("AND T2.CODE ='").append(entityCode).append("' ");
                tempSql.append("ORDER BY T2.CODE,T2.SEQ");
                try {
                    ResultSet rs = stat.executeQuery(tempSql.toString());
                    while(rs.next()){
                        String userId = rs.getString("USER_ID");
                        userList.add(userId);
                        //put部门和部门审核人的List，已在sql中排序
                        entityUserMap.put(entityCode, userList);
                    }
                    rs.close();
                } catch (SQLException e) {
                    this._logger.severe(e);
                }
                //遍历同一个部门的不同组合
                for(String comId : entityEntry.getValue()){
                    //遍历部门和部门审核人list，每个审核人插入一条记录
                    for(Map.Entry<String,List<String>> euEntry : entityUserMap.entrySet()){
                        for(String user : euEntry.getValue()){
                            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                            StringBuffer insert = new StringBuffer();
                            insert.append("INSERT INTO APPROVE_TEMPLATE_STATUS VALUES(");
                            insert.append("'").append(java.util.UUID.randomUUID().toString().replace("-", "")).append("',");
                            insert.append("'").append(runId).append("',");
                            insert.append("'").append(tempEntry.getKey()).append("',");
                            insert.append("'").append(euEntry.getKey()).append("',");
                            insert.append(stepNo).append(",");
                            insert.append("'").append(user).append("',");
                            insert.append("'").append("CLOSE").append("',");
                            insert.append("'").append(this.curUser.getLocale()).append("',");
                            insert.append("null").append(",");
                            insert.append("to_date('").append(df.format(new Date())).append("','yyyy-MM-dd'),");
                            insert.append("to_date('").append(df.format(new Date())).append("','yyyy-MM-dd'),");
                            insert.append("'").append(this.curUser.getId()).append("',");
                            insert.append("'").append(this.curUser.getId()).append("',");
                            insert.append("'").append(comId).append("')");
                            stat.addBatch(insert.toString());
                        }    
                    }    
                }
                stat.executeBatch();
                stat.clearBatch();
            } 
            trans.commit();
        }
            stat.close();
        }catch(SQLException e){
            this._logger.severe(e);
        }
    }
    //检测下一步
    public Map<String,String> queryNextStep(String wfId,String runId ,int stepNo){
        stepNo = stepNo + 1;
        DBTransaction trans = (DBTransaction)DmsUtils.getDcmApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        StringBuffer nextSql = new StringBuffer();
        nextSql.append("SELECT STEP_TASK,STEP_STATUS,OPEN_COM,STEP_OBJECT FROM DMS_WORKFLOW_STATUS WHERE RUN_ID = '");
        nextSql.append(runId).append("' ");
        nextSql.append("AND WF_ID = '").append(wfId).append("' ");
        nextSql.append("AND STEP_NO = ").append(stepNo);
        Map<String,String> nextMap = new HashMap<String,String>();
        ResultSet rs;
        try {
            rs = stat.executeQuery(nextSql.toString());
            if(rs.next()){
                nextMap.put("STEP_TASK", rs.getString("STEP_TASK"));   
                nextMap.put("STEP_STATUS", rs.getString("STEP_STATUS"));
                nextMap.put("OPEN_COM", rs.getString("OPEN_COM"));
                nextMap.put("STEP_OBJECT", rs.getString("STEP_OBJECT"));
            }
            rs.close();
            stat.close();
        } catch (SQLException e) {
            this._logger.severe(e);
        }
        return nextMap;
    }
    
    //检测步骤是否完成，启动下一步
    public void stepIsFinish(String wfId ,String runId , int stepNo , String stepTask){
        boolean isFinish = false;
        DBTransaction trans = (DBTransaction)DmsUtils.getDcmApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        StringBuffer sql = new StringBuffer();

        if(stepTask.equals("OPEN TEMPLATES")){
            sql.append("SELECT 1 FROM WORKFLOW_TEMPLATE_STATUS WHERE WRITE_STATUS <> 'Y' ");
        }else if(stepTask.equals("ETL")){
            sql.append("SELECT 1 FROM WORKFLOW_ODI_STATUS WHERE EXEC_STATUS <> 'Y' ");
        }else{
            sql.append("SELECT 1 FROM APPROVE_TEMPLATE_STATUS WHERE APPROVAL_STATUS <> 'Y' ");
        }
        sql.append("AND RUN_ID = '").append(runId).append("' ");
        sql.append("AND STEP_NO = ").append(stepNo);
        try {
            ResultSet rs = stat.executeQuery(sql.toString());
            if(rs.next()){
                //步骤未完成
                isFinish = false;
            }else{
                //步骤完成
                isFinish = true;
                //更新工作流状态表
                StringBuffer finishSql = new StringBuffer();
                finishSql.append("UPDATE DMS_WORKFLOW_STATUS SET STEP_STATUS = 'Y',FINISH_AT = SYSDATE WHERE ");
                finishSql.append("WF_ID = '").append(wfId).append("' ").append("AND RUN_ID = '").append(runId).append("' ").append("AND STEP_NO = ").append(stepNo);
                stat.executeUpdate(finishSql.toString());
                trans.commit();
            }
            rs.close();
            stat.close();
        } catch (SQLException e) {
            this._logger.severe(e);
        }
    }
    
    public void startNext(String wfId,String runId,String templateId,String comId ,String comName,
                          int stepNo,String lastTask,String commitUser){
        List<String> proList = new ArrayList<String>();
        //检查父节点是否完成输入或审批
        if(lastTask.equals("OPEN TEMPLATES")){
            if(!this.checkParent(runId, templateId, comId, stepNo)){
                return;    
            }else{
                //获取执行程序
                proList = this.getStepPro(stepNo, wfId);     
            }    
        }else{
            ApproveflowEngine afe = new ApproveflowEngine();
            if(!(afe.checkParentApprove(runId, templateId, comId, stepNo) && this.checkParent(runId, templateId, comId, stepNo-1))){
                return;
            }else{
                //获取执行程序
                proList = this.getStepPro(stepNo-1, wfId);
            }    
        }
        
        //执行
        
        if(proList.size() > 0){
            if(lastTask.equals("OPEN TEMPLATES")){
                //更改状态-正在计算
                this.changeCalc(runId,templateId, comId, stepNo, "START");
            }else{
                this.changeCalc(runId,templateId, comId, stepNo-1, "START");
            }
            //查询当前步骤中的所有子部门
            List<String> childList = this.getChildEntity(templateId, comId);
            
            for(String calc : proList){
                if(this.executePro(calc, childList,wfId,runId,stepNo)){
                    continue;    
                }else{
                    break;
                }        
            }   
            
            if(lastTask.equals("OPEN TEMPLATES")){
                //更改状态-完成
                this.changeCalc(runId, templateId, comId, stepNo, "END");
            }else{
                this.changeCalc(runId, templateId, comId, stepNo-1, "END");
            }
        }
        
        DBTransaction trans =
            (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        ApproveflowEngine afe = new ApproveflowEngine();
        try{
        while(true){
            Map<String,String> nextMap = this.queryNextStep(wfId, runId, stepNo);  
            String nextTask = nextMap.get("STEP_TASK");
            String stepObj = nextMap.get("STEP_OBJECT");
            String openCom = nextMap.get("OPEN_COM");
            if("ETL".equals(nextTask)){           
                //新增odi逻辑
                String etlStatus = nextMap.get("STEP_STATUS");
                if("N".equals(etlStatus)){
                    this.initOdiStatus(wfId,runId, stepObj, stepNo+1, false);    
                }
                this.changeEtlStatus(wfId,runId, stepObj, comId, "N");
                //新增odi逻辑
                
                //跟新ETL步骤状态，发送邮件
                StringBuffer etlSql = new StringBuffer();
                etlSql.append("UPDATE DMS_WORKFLOW_STATUS SET STEP_STATUS = 'WORKING',START_AT = SYSDATE ");
                etlSql.append("WHERE WF_ID = '").append(wfId).append("' ");
                etlSql.append("AND RUN_ID = '").append(runId).append("' ");
                etlSql.append("AND STEP_NO = ").append(stepNo+1);
                stat.executeUpdate(etlSql.toString());
                trans.commit();
                
//                String uSql1 = "SELECT DISTINCT S.ID,S.SCENE_ALIAS,T.USER_ID FROM ODI11_USER_SCENE_V T,ODI11_SCENE S , DMS_USER_VALUE_V D,WORKFLOW_TEMPLATE_STATUS W " +
//                    "WHERE T.SCENE_ID = S.ID AND S.LOCALE = 'zh_CN' AND T.SCENE_ID = '"
//                    + stepObj + "' AND W.ENTITY_CODE = D.VALUE_ID AND D.USER_ID = T.USER_ID AND W.COM_ID = '" + comId + "'" ;
//                ResultSet uRs1 = stat.executeQuery(uSql1);
//                while(uRs1.next()){
//                    afe.sendMail(uRs1.getString("SCENE_ALIAS"), comName, uRs1.getString("USER_ID"), commitUser, "执行接口！", "表单审批通过，请及时执行接口！", "");        
//                }
//                uRs1.close();
                break;
//                //继续执行下一步
//                stepNo = stepNo + 1 ;
            }else if("OPEN TEMPLATES".equals(nextTask)){
                //父节点完成，打开下一个步骤父节点子部门
                //查询是否已经初始化，否则执行初始化
                String stepStatus = "";
                String stepSql = "SELECT STEP_STATUS FROM DMS_WORKFLOW_STATUS T " + "WHERE T.WF_ID = '" + wfId + "' AND T.RUN_ID = '"
                    + runId + "' AND STEP_NO = " + (stepNo+1);
                ResultSet statusRs = stat.executeQuery(stepSql);
                if(statusRs.next()){
                    stepStatus = statusRs.getString("STEP_STATUS");  
                }
                if("N".equals(stepStatus)){
                    //打开模板，初始化模板输入状态表和审批流状态表
                    this.onlyOpenTemplates(stepObj, openCom, runId, stepNo+1);
                    //更新步骤状态为WORKING
                    int i = stepNo + 1;
                    String udnSql = "UPDATE DMS_WORKFLOW_STATUS SET STEP_STATUS = 'WORKING',START_AT = SYSDATE WHERE WF_ID = '" + wfId + "'"
                        + " AND RUN_ID = '" + runId + "'" + " AND STEP_NO = " + i;
                    stat.executeUpdate(udnSql);
                    trans.commit();    
                }
                statusRs.close();
                //打开父节点下所有部门的组合
                this.openComByChild(wfId, runId,templateId,comId, stepNo+1);
                break;
            }else if("APPROVE".equals(nextTask)){
                if(lastTask.equals("APPROVE")){
                    //若上一步为审批，则下一步没有审批对象，直接跳过
                    continue;    
                }
                ApproveflowEngine approveEgn = new ApproveflowEngine();
                //打开部门审批，发送邮件 
                approveEgn.startApproveEntity(wfId,runId, templateId, comId,comName,commitUser,stepNo);
                //改变工作流中审批步骤状态为进行中
                StringBuffer updateApp = new StringBuffer();
                updateApp.append("UPDATE DMS_WORKFLOW_STATUS SET STEP_STATUS = 'WORKING',START_AT = SYSDATE ");
                updateApp.append("WHERE WF_ID = '").append(wfId).append("' ");
                updateApp.append("AND RUN_ID = '").append(runId).append("' ");
                updateApp.append("AND STEP_NO =").append(stepNo+1);
                stat.executeUpdate(updateApp.toString());
                trans.commit();
                break;
            }else{
                break;    
            }
        }
        stat.close();
        }catch(Exception e){
            this._logger.severe(e);    
        }
    }
    
    public void startEtlNext(String wfId,String runId,String parent,int stepNo){
        DBTransaction trans =
            (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        
        //判断接口同一个实体父节点是否全部执行，否则不开启下一步
        String cSql = "SELECT 1 FROM WORKFLOW_ODI_STATUS T WHERE T.EXEC_STATUS <> 'Y' AND T.ENTITY_PARENT = '" + parent + "' "
            + "AND T.RUN_ID = '" + runId + "' AND T.STEP_NO = " + stepNo;

        try {
            ResultSet cRs = stat.executeQuery(cSql);
            if(cRs.next()){
                return;    
            }
            cRs.close();
        } catch (SQLException e) {
            this._logger.severe(e);
        }
        //判断parent是否为ALLENTITY，是则打开全部模板部门，否则按实体父节点走下一步
        boolean beforeTmp = false;
        String comId = "";
        String templateId = "";
        if(parent.equals("ALLENTITY")){
            beforeTmp = true;
        }else{
            //获取工作流中该父节点下的一张模板和部门的comid
            String nSql = "SELECT DISTINCT T.TEMPLATE_ID,T.COM_ID FROM WORKFLOW_TEMPLATE_STATUS T WHERE T.RUN_ID = '"
                + runId + "' AND T.ENTITY_CODE IN (SELECT D.ENTITY FROM DCM_ENTITY_PARENT D WHERE D.PARENT = '"
                + parent + "')";
            try {
                ResultSet rs = stat.executeQuery(nSql);
                if(rs.next()){
                    comId = rs.getString("COM_ID");
                    templateId = rs.getString("TEMPLATE_ID");
                }
                rs.close();
            } catch (SQLException e) {
                this._logger.severe(e);
            }
        }

        try{
        while(true){
            Map<String,String> nextMap = this.queryNextStep(wfId, runId, stepNo);  
            String nextTask = nextMap.get("STEP_TASK");
            String stepObj = nextMap.get("STEP_OBJECT");
            String openCom = nextMap.get("OPEN_COM");
            if("ETL".equals(nextTask)){           
                //新增odi逻辑
                String etlStatus = nextMap.get("STEP_STATUS");
                if("N".equals(etlStatus)){
                    this.initOdiStatus(wfId,runId, stepObj, stepNo+1, false);    
                }
                this.changeEtlStatus(wfId,runId, stepObj, comId, "N");
                //新增odi逻辑
                
                //跟新ETL步骤状态，发送邮件
                StringBuffer etlSql = new StringBuffer();
                etlSql.append("UPDATE DMS_WORKFLOW_STATUS SET STEP_STATUS = 'WORKING',START_AT = SYSDATE ");
                etlSql.append("WHERE WF_ID = '").append(wfId).append("' ");
                etlSql.append("AND RUN_ID = '").append(runId).append("' ");
                etlSql.append("AND STEP_NO = ").append(stepNo+1);
                stat.executeUpdate(etlSql.toString());
                trans.commit();
                
                break;

            }else if("OPEN TEMPLATES".equals(nextTask)){
                if(beforeTmp){
                    this.openTemplates(stepObj, openCom,wfId,runId,stepNo+1);
                    //打开模板完成，更新步骤状态表为working
                    String udSql = "UPDATE DMS_WORKFLOW_STATUS SET STEP_STATUS = 'WORKING',START_AT = SYSDATE WHERE WF_ID = '" + wfId + "'"
                        + " AND RUN_ID = '" + runId + "'" + " AND STEP_NO = " + (stepNo+1);
                    stat.executeUpdate(udSql);
                    trans.commit();
                }else{
                    //父节点完成，打开下一个步骤父节点子部门
                    //查询是否已经初始化，否则执行初始化
                    String stepStatus = "";
                    String stepSql = "SELECT STEP_STATUS FROM DMS_WORKFLOW_STATUS T " + "WHERE T.WF_ID = '" + wfId + "' AND T.RUN_ID = '"
                        + runId + "' AND STEP_NO = " + (stepNo+1);
                    ResultSet statusRs = stat.executeQuery(stepSql);
                    if(statusRs.next()){
                        stepStatus = statusRs.getString("STEP_STATUS");  
                    }
                    if("N".equals(stepStatus)){
                        //打开模板，初始化模板输入状态表和审批流状态表
                        this.onlyOpenTemplates(stepObj, openCom, runId, stepNo+1);
                        //更新步骤状态为WORKING
                        int i = stepNo + 1;
                        String udnSql = "UPDATE DMS_WORKFLOW_STATUS SET STEP_STATUS = 'WORKING',START_AT = SYSDATE WHERE WF_ID = '" + wfId + "'"
                            + " AND RUN_ID = '" + runId + "'" + " AND STEP_NO = " + i;
                        stat.executeUpdate(udnSql);
                        trans.commit();    
                    }
                    statusRs.close();
                    //打开父节点下所有部门的组合
                    this.openComByChild(wfId, runId,templateId,comId, stepNo+1);
                }
                break;
            }else if("APPROVE".equals(nextTask)){
                //接口后面不能直接跟审批步骤
                break;
            }else{
                break;    
            }
        }
        stat.close();
        }catch(Exception e){
            this._logger.severe(e);    
        }
    }
    
    public void changeCalc(String runId,String templateId,String comId,int stepNo,String status){
        DBTransaction trans =
            (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        String wSql = "";
        String aSql = "";
        if(status.equals("START")){
            wSql = "UPDATE WORKFLOW_TEMPLATE_STATUS T SET T.WRITE_STATUS = 'EXEC_CALC' WHERE T.RUN_ID = '" + runId + "' AND T.TEMPLATE_ID = '"
                + templateId + "' AND T.COM_ID = '" + comId + "' AND T.STEP_NO = " + stepNo;
            aSql = "UPDATE APPROVE_TEMPLATE_STATUS T SET T.APPROVAL_STATUS = 'EXEC_CALC' WHERE T.RUN_ID = '" + runId + "' AND T.TEMPLATE_ID = '"
                + templateId + "' AND T.COM_ID = '" + comId + "' AND T.STEP_NO = " + (stepNo + 1);
        }else{
            wSql = "UPDATE WORKFLOW_TEMPLATE_STATUS T SET T.WRITE_STATUS = 'Y' WHERE T.RUN_ID = '" + runId + "' AND T.TEMPLATE_ID = '"
                + templateId + "' AND T.COM_ID = '" + comId + "' AND T.STEP_NO = " + stepNo;
            aSql = "UPDATE APPROVE_TEMPLATE_STATUS T SET T.APPROVAL_STATUS = 'Y' WHERE T.RUN_ID = '" + runId + "' AND T.TEMPLATE_ID = '"
                + templateId + "' AND T.COM_ID = '" + comId + "' AND T.STEP_NO = " + (stepNo + 1);   
        }

        try {
            stat.executeUpdate(wSql);
            stat.executeUpdate(aSql);
            trans.commit();
            stat.close();
        } catch (SQLException e) {
            this._logger.severe(e);
        }
    }
    
    public Map<String,List<String>> getComIdMap(List<String> childList,String runId,int stepNo){
        DBTransaction trans =
            (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        Map<String,List<String>> comIdMap = new HashMap<String,List<String>>();
        
        String eSql = "SELECT T.TEMPLATE_ID,T.COM_ID FROM WORKFLOW_TEMPLATE_STATUS T WHERE T.RUN_ID = '" + runId + "'"
                        + " AND  T.STEP_NO = " + stepNo + " AND T.ENTITY_CODE IN("; 
        for(String entity : childList){
            eSql = eSql + "'" + entity + "',";    
        }
        eSql = eSql + "'')";
        try {
            ResultSet rs = stat.executeQuery(eSql);
            while(rs.next()){
                
                if(comIdMap.containsKey(rs.getString("TEMPLATE_ID"))){
                    comIdMap.get(rs.getString("TEMPLATE_ID")).add(rs.getString("COM_ID"));
                }else{
                    List<String> comIdList = new ArrayList<String>();
                    comIdList.add(rs.getString("COM_ID"));
                    comIdMap.put(rs.getString("TEMPLATE_ID"), comIdList);
                }
                
            }
            rs.close();
            stat.close();
        } catch (SQLException e) {
            this._logger.severe(e);
        }
        return comIdMap;    
    }
    
    public boolean executePro(String pro,List<String> childList,String wfId,String runId,int stepNo){
        DBTransaction trans =
            (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        CallableStatement cs =
            trans.createCallableStatement("{CALL " + pro +
                                          "(?,?,?,?,?,?,?)}", 0);
        
        boolean flag = false;
        List<String> argsList = new ArrayList<String>();
        
        String sql = "SELECT ARGS FROM DMS_WORKFLOW_PRO_ARGS T WHERE T.CALC_PRO = '" + pro + "' AND T.ENTITY_CODE IN (";
        for(String entity : childList){
            sql = sql + "'"+entity+"',";        
        }
        sql = sql + "'')";

        try {
            ResultSet rs = stat.executeQuery(sql);
            while(rs.next()){
                argsList.add(rs.getString("ARGS"));    
            }
            rs.close();
        } catch (SQLException e) {
            this._logger.severe(e);
        }
        
        //转换参数
        List<String> comIdList = this.getArgsComId(wfId, runId, argsList);
        
        for(String args : comIdList){
            //执行程序
            try {
                //执行程序
                cs.setString(1, "");
                cs.setString(2, args);
                cs.setString(3, this.curUser.getId());
                cs.setString(4, "EDIT");
                cs.setString(5, this.curUser.getLocale());
                cs.setString(6, args);
                //获取返回值
                cs.registerOutParameter(7, Types.VARCHAR);
                cs.execute();
                String str = cs.getString(7);
                if("true".equals(str)){
                    trans.commit();   
                    String iSql = "INSERT INTO WF_PRO_HISTORY VALUES('" + args + "','" + stepNo + "','" + this.curUser.getId()
                                          + "',SYSDATE,'" + wfId + "','" + pro + "')";
                    stat.executeUpdate(iSql);
                    trans.commit();
                    flag = true;
                }else{
                    trans.rollback();    
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(pro+"计算程序执行出错！"));
                    return false;
                }
            } catch (SQLException e) {
                this._logger.severe(e);
            }       
        }

        try {
            stat.close();
            cs.close();
        } catch (SQLException e) {
            this._logger.severe(e);
        }
        return flag;
    }
    
    public List<String> getArgsComId(String wfId,String runId,List<String> argsList){
        DBTransaction trans =
            (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        List<String> comIdList = new ArrayList<String>();
        //查询组合source表
        String sql = "SELECT C.CODE FROM DMS_WORKFLOWINFO T,WORKFLOW_TEMPLATE_STATUS W,DCM_TEMPLATE D,DCM_COMBINATION C "
            + "WHERE T.WF_RUNID = W.RUN_ID AND W.TEMPLATE_ID = D.ID AND D.COMBINATION_ID = C.ID "
            + "AND T.ID = '" + wfId + "'";
        //从source中查询组合ID
        String cSql = "SELECT ID FROM ";
        String source = "";
        try {
            ResultSet rs = stat.executeQuery(sql);
            if(rs.next()){
                source = rs.getString("CODE");
            }else{
                return new ArrayList<String>();    
            }
            rs.close();
            
            cSql = cSql + source + " WHERE 1=1";
            
            Map comMap = this.getWfCom(wfId, runId);
            String openCom = comMap.get("comCode").toString();
            openCom = openCom.substring(0, openCom.length() - 1);
            String[] str = openCom.split("#");
            for (int i = 0; i < str.length; i++) {
                if(str[i].startsWith("FY")){
                    cSql = cSql + " AND YEARS = '" + str[i] + "'";    
                }else if(str[i].startsWith("V")){
                    cSql = cSql + " AND VERSION = '" + str[i] + "'";
                }else{
                    cSql = cSql + " AND SCENARIO = '" + str[i] + "'";    
                }
            }
            
            if(source.equals("QB_ENTITYS_YEAR")){
                cSql = cSql + " AND QB_ENTITYS IN (";
            }else if(source.equals("FF_ENTITYS_YEAR")){
                cSql = cSql + " AND FF_ENTITYS IN (";
            }else if(source.equals("FY_SYSDEP_COM")){
                cSql = cSql + " AND FY_ENTITYS IN (";
            }
            
            for(String args : argsList){
                cSql = cSql + "'" + args + "',";
            }
            
            cSql = cSql + "'')";
            
            ResultSet cRs = stat.executeQuery(cSql);
            while(cRs.next()){
                comIdList.add(cRs.getString("ID")); 
            }
            cRs.close();
            stat.close();
            
        } catch (SQLException e) {
            this._logger.severe(e);
        }
        return comIdList;
    }
    
    
    public List<String> getStepPro(int stepNo,String wfId){
        DBTransaction trans =
            (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        List<String> proList = new ArrayList<String>();
        String pSql = "SELECT P.CALC_PRO,P.SEQ FROM DMS_WORKFLOW_STEPS T,DMS_WORKFLOW_STEP_PRO P WHERE T.ID = P.STEP_ID "
                        + "AND T.STEP_NO = " + stepNo + " AND  T.WF_ID = '" + wfId + "' " + "ORDER BY P.SEQ";
        
        try {
            ResultSet rs = stat.executeQuery(pSql);
            while(rs.next()){
                String pro = rs.getString("CALC_PRO");   
                proList.add(pro);
                System.out.println("add:"+pro);
            }
            rs.close();
            stat.close();
        } catch (SQLException e) {
            this._logger.severe(e);
        }

        return proList;    
        
    }
    
    public List<String> getChildEntity(String templateId , String comId){
        DBTransaction trans =
            (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        //查询部门的父节点下的所有子部门
        List<String> childList = new ArrayList<String>();
        StringBuffer pSql = new StringBuffer();
        pSql.append("select ENTITY from dcm_entity_parent d where d.parent = ( ");
        pSql.append("select distinct parent from workflow_template_status t1 , dcm_entity_parent t2 ");
        pSql.append("where t1.entity_code = t2.entity and t1.template_id = '").append(templateId).append("' ");
        pSql.append("and t1.com_id = '").append(comId).append("')");
        try{
            ResultSet rs = stat.executeQuery(pSql.toString());
            while(rs.next()){
                childList.add(rs.getString("ENTITY"));        
            }
            rs.close();
        }catch(Exception e){
            this._logger.severe(e);    
        }
        return childList;
    }
    
    public boolean checkParent(String runId, String templateId, String comId,int stepNo){
        boolean flag = false;
        DBTransaction trans =
            (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        //查询部门的父节点下的所有子部门
        List<String> childList = this.getChildEntity(templateId, comId);
        try {
            //检查输入状态表中所有子部门是否完成输入
            StringBuffer eSql = new StringBuffer();
            eSql.append("select 1 from workflow_template_status t where t.write_status <> 'Y' ");
            //eSql.append("and t.template_id = '").append(templateId).append("' ");
            eSql.append("and t.run_id = '").append(runId).append("' ");
            eSql.append("and t.step_no = ").append(stepNo).append(" ");
            eSql.append("and t.entity_code in (");
            for(String entityCode : childList){
                eSql.append("'").append(entityCode).append("',");
            }
            eSql.append("'')");
            ResultSet statusRs = stat.executeQuery(eSql.toString());
            if(statusRs.next()){
                //未完成    
                flag = false;
            }else{
                //父节点完成
                flag = true;
            }
            statusRs.close();
            stat.close();
        } catch (SQLException e) {
            this._logger.severe(e);
        }
        return flag;
    }
    
    //回退到指定输入状态
    public void retreat(String wfId,String runId,int stepNo,int backStepNo,List<String> backTemp,List<String> backEntity,String commitUser,String reason){
        DBTransaction trans =
            (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        List<Map<String,String>> stepList = new ArrayList<Map<String,String>>();

        try {
            StringBuffer bSql = new StringBuffer();
            bSql.append("select step_no,step_task,step_status,open_com,step_object from dms_workflow_status t ");
            bSql.append("where t.wf_id = '").append(wfId).append("' ");
            bSql.append("and t.run_id = '").append(runId).append("' ");
            bSql.append("and t.step_no <= ").append(stepNo);
            bSql.append(" and t.step_no >= ").append(backStepNo);
            bSql.append(" order by t.step_no asc ");
            //查询需要回退处理的步骤信息
            ResultSet bRs = stat.executeQuery(bSql.toString());
            while(bRs.next()){
                Map<String,String> stepMap = new HashMap<String,String>();
                stepMap.put("STEP_NO", bRs.getString("STEP_NO"));
                stepMap.put("STEP_TASK", bRs.getString("STEP_TASK"));
                stepMap.put("STEP_STATUS", bRs.getString("STEP_STATUS"));
                stepMap.put("OPEN_COM", bRs.getString("OPEN_COM"));
                stepMap.put("STEP_OBJECT", bRs.getString("STEP_OBJECT"));
                stepList.add(stepMap);
            }
            bRs.close();
            stat.close();
            this.retreatStep(wfId, runId,backTemp, backEntity, stepList,commitUser,reason);
        } catch (SQLException e) {
            this._logger.severe(e);
        }
    }
    
    //回退到父节点在工作流中的起点位置
    public void retreatStarted(String wfId,String runId,int stepNo,String templateId,String comId,String commitUser,String reason){
        DBTransaction trans =
            (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        List<Map<String,String>> stepList = new ArrayList<Map<String,String>>();
        StringBuffer bSql = new StringBuffer();
        bSql.append("select step_no,step_task,step_status,open_com,step_object from dms_workflow_status t ");
        bSql.append("where t.wf_id = '").append(wfId).append("' ");
        bSql.append("and t.run_id = '").append(runId).append("' ");
        bSql.append("and t.step_no <= ").append(stepNo);
        bSql.append(" order by t.step_no asc ");
        //查询需要回退处理的步骤信息
        try{
        ResultSet bRs = stat.executeQuery(bSql.toString());
        while(bRs.next()){
            Map<String,String> stepMap = new HashMap<String,String>();
            stepMap.put("STEP_NO", bRs.getString("STEP_NO"));
            stepMap.put("STEP_TASK", bRs.getString("STEP_TASK"));
            stepMap.put("STEP_STATUS", bRs.getString("STEP_STATUS"));
            stepMap.put("OPEN_COM", bRs.getString("OPEN_COM"));
            stepMap.put("STEP_OBJECT", bRs.getString("STEP_OBJECT"));
            stepList.add(stepMap);
        }
        bRs.close();
        
        List<String> backEntity = this.getChildEntity(templateId, comId);
        //查询步骤中涉及到的模板
        StringBuffer tSql = new StringBuffer();
        tSql.append("SELECT DISTINCT T.TEMPLATE_ID FROM WORKFLOW_TEMPLATE_STATUS T WHERE T.RUN_ID = '").append(runId).append("' ");
        tSql.append("AND T.STEP_NO < ").append(stepNo).append(" AND T.ENTITY_CODE IN (");
        for(String ety : backEntity){
            tSql.append("'").append(ety).append("',");         
        }
        tSql.append("'')");
        ResultSet tRs = stat.executeQuery(tSql.toString());
            List<String> backTemp = new ArrayList<String>();
            while(tRs.next()){
                backTemp.add(tRs.getString("TEMPLATE_ID"));        
            }
        stat.close();
        
        this.retreatStep(wfId, runId, backTemp, backEntity, stepList,commitUser,reason);
        }catch(Exception e){
            this._logger.severe(e);    
        }
    }
    
    //更改步骤状态，回退步骤
    public void retreatStep(String wfId,String runId,List<String> backTemp ,List<String> backEntity,
                            List<Map<String,String>> stepList,String commitUser,String reason){
        DBTransaction trans =
            (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        ApproveflowEngine afe = new ApproveflowEngine();
        //回退子部门
        List<String> childList = backEntity;
        try{
        boolean isFirst = true;
            boolean isFirstApprove = false;
        for(Map<String,String> stepMap : stepList){
            String stepTask = stepMap.get("STEP_TASK");
            int stepNo = Integer.parseInt(stepMap.get("STEP_NO"));
            String stepObj = stepMap.get("STEP_OBJECT");
            if("ETL".equals(stepTask)){
                //更改ETL步骤为工作中
                String eSql = "update dms_workflow_status t set t.step_status = 'WORKING',t.finish_at = '' " + "where t.wf_id = '" + wfId + 
                              "' and t.run_id = '" + runId + "' and t.step_no =" + stepNo;
                //查询父节点，更改接口对应参数状态为CLSOE
                String fSql = "UPDATE WORKFLOW_ODI_STATUS O SET O.EXEC_STATUS = 'CLOSE',O.FINISH_AT = '',O.EXEC_BY = '' WHERE O.RUN_ID = '" + runId + "' "
                    + "AND O.SCENE_ID = '" + stepObj + "' AND O.ENTITY_PARENT = ( SELECT DISTINCT D.PARENT FROM DCM_ENTITY_PARENT D WHERE D.ENTITY = '"
                    + childList.get(0) + "')";
                stat.executeUpdate(eSql);
                stat.executeUpdate(fSql);
                trans.commit();
                //发送邮件通知
                
            }else if("APPROVE".equals(stepTask)){
                //更改子部门的所有模板审批状态为未审批
                StringBuffer wspSql = new StringBuffer();
                wspSql.append("update approve_template_status t set t.approval_status = 'CLOSE',t.finish_at = '' ");
                wspSql.append("where t.run_id = '").append(runId).append("' ");
                wspSql.append("and t.step_no = ").append(stepNo);
                wspSql.append("and t.entity_id in (");
                //查询所有模板，组合，审批人，发送邮件
                StringBuffer tcuSql = new StringBuffer();
                tcuSql.append("SELECT T.TEMPLATE_ID,T.COM_ID,T.PERSON_ID,T.ENTITY_ID FROM APPROVE_TEMPLATE_STATUS T WHERE ");
                tcuSql.append("T.RUN_ID = '").append(runId).append("' ");
                tcuSql.append("AND T.STEP_NO = ").append(stepNo);
                tcuSql.append("AND T.ENTITY_ID IN (");

                for(String entityCode : childList){
                    wspSql.append("'").append(entityCode).append("',");
                    tcuSql.append("'").append(entityCode).append("',");
                }
                wspSql.append("'')");
                tcuSql.append("'')");
                
                if(isFirstApprove){
                   tcuSql.append(" AND T.TEMPLATE_ID IN (");
                   wspSql.append(" and t.template_id in (");
                   for(String tempCode : backTemp){
                       tcuSql.append("'").append(tempCode).append("',");
                       wspSql.append("'").append(tempCode).append("',");
                   }
                   tcuSql.append("'')");
                   wspSql.append("'')");  
                   isFirstApprove = false;
                }
                
//                tcuSql.append(" AND T.TEMPLATE_ID IN (");
//                wspSql.append(" and t.template_id in (");
//                for(String tempCode : backTemp){
//                    tcuSql.append("'").append(tempCode).append("',");
//                    wspSql.append("'").append(tempCode).append("',");
//                }
//                tcuSql.append("'')");
//                wspSql.append("'')");   
                
                stat.executeUpdate(wspSql.toString());
                //更改步骤状态为working
                String uSql = "update dms_workflow_status t set t.step_status = 'WORKING',t.finish_at = '' " + "where t.wf_id = '" + wfId + 
                              "' and t.run_id = '" + runId + "' and t.step_no =" + stepNo;
                stat.executeUpdate(uSql);
                trans.commit();
                
                Map<String,String> wfMap = this.getWfCom(wfId, runId);
                
                ResultSet mRs = stat.executeQuery(tcuSql.toString());
                //send mail
                while(mRs.next()){
                    String tid = mRs.getString("TEMPLATE_ID");
                    String uid = mRs.getString("PERSON_ID");
                    String entityName ="_" + this.getEntityName(mRs.getString("ENTITY_ID"));
                    String wfName = "【" + wfMap.get("wfName") + "】";
                    afe.sendMail(tid, entityName, uid,commitUser, wfName + "工作流回退！", "工作流回退，请等待表单重新提交审批！",reason);
                }
                
                this.stepIsFinish(wfId, runId, stepNo, stepTask);
                
            }else if("OPEN TEMPLATES".equals(stepTask)){
                //修改输入状态表为未输入
                StringBuffer wsrSql = new StringBuffer();
                wsrSql.append("update workflow_template_status t set t.write_status = 'CLOSE',t.finish_at = '' ");
                wsrSql.append("where t.run_id = '").append(runId).append("' ");
                wsrSql.append("and t.step_no = ").append(stepNo);
                wsrSql.append("and t.entity_code in (");
                //查询所有模板，组合，审批人，发送邮件
                StringBuffer tcwSql = new StringBuffer();
                tcwSql.append("SELECT T.TEMPLATE_ID,T.COM_ID,T.WRITE_BY,T.ENTITY_CODE FROM WORKFLOW_TEMPLATE_STATUS T WHERE ");
                tcwSql.append("T.RUN_ID = '").append(runId).append("' ");
                tcwSql.append("AND T.STEP_NO = ").append(stepNo);
                tcwSql.append("AND T.ENTITY_CODE IN (");
                
                for(String entityCode : childList){
                    wsrSql.append("'").append(entityCode).append("',");
                    tcwSql.append("'").append(entityCode).append("',");
                }
                wsrSql.append("'')");
                tcwSql.append("'')");
                
                if(isFirst){
                    tcwSql.append(" AND T.TEMPLATE_ID IN (");
                    wsrSql.append(" and t.template_id in (");
                    for(String tempCode : backTemp){
                        wsrSql.append("'").append(tempCode).append("',");
                        tcwSql.append("'").append(tempCode).append("',");
                    }
                    wsrSql.append("'')");
                    tcwSql.append("'')");
                }
                
                stat.executeUpdate(wsrSql.toString());
                //更改步骤状态为working
                String uSql = "update dms_workflow_status t set t.step_status = 'WORKING',t.finish_at = '' " + "where t.wf_id = '" + wfId + 
                              "' and t.run_id = '" + runId + "' and t.step_no =" + stepNo;
                stat.executeUpdate(uSql);
                trans.commit();
                ResultSet mRs = stat.executeQuery(tcwSql.toString());
                //最前的开模板步骤，打开组合
                if(isFirst){
                    //最前的打开模板步骤打开组合
                    this.changeComStatus(runId, stepNo, childList,backTemp, "OPEN");
                    isFirst = false ;
                    Map<String,List<String>> tempComMap = new HashMap<String,List<String>>();
                    Map<String,String> wfMap = this.getWfCom(wfId, runId);
                    //send mail
                    while(mRs.next()){
                        String tid = mRs.getString("TEMPLATE_ID");
                        String uid = mRs.getString("WRITE_BY");
                        String cid = mRs.getString("COM_ID");
                        if(tempComMap.containsKey(tid)){
                            tempComMap.get(tid).add(cid);    
                        }else{
                            if(backTemp.contains(tid)){
                                List<String> list = new ArrayList<String>();
                                list.add(cid);
                                tempComMap.put(tid, list);    
                            }
                        }
                        String entityName ="_" + this.getEntityName(mRs.getString("ENTITY_CODE"));
                        String wfName = "【" + wfMap.get("wfName") + "】";
                        afe.sendMail(tid, entityName, uid,commitUser, wfName + "工作流回退！", "工作流回退，请重新填写表单数据！",reason);
                    }
                    //开启输入状态
                    this.openWirteStatus(tempComMap, runId, stepNo);
                    //下一步是否为审批
                    if(this.nextIsApprove(wfId, runId, stepNo+1)){ isFirstApprove = true; }
                    
                    //this.passOtherApprove(backTemp, backEntity,runId,stepNo+1);
                }else{
                    //其他的打开模板步骤关闭组合
                    this.changeComStatus(runId, stepNo, childList,null, "CLOSE");
                    Map<String,String> wfMap = this.getWfCom(wfId, runId);
                    //send mail
                    while(mRs.next()){
                        String tid = mRs.getString("TEMPLATE_ID");
                        String uid = mRs.getString("WRITE_BY");
                        String entityName ="_" + this.getEntityName(mRs.getString("ENTITY_CODE"));
                        String wfName = "【" + wfMap.get("wfName") + "】";
                        afe.sendMail(tid, entityName, uid,commitUser, "工作流回退！", "工作流回退，请等待前面表单审批通过后，再重填表单数据！",reason);
                    }
                }
                
                this.stepIsFinish(wfId, runId, stepNo, stepTask);
            }
        }
            stat.close();
        }catch(Exception e){
            this._logger.severe(e);    
        }
    }
    
    public boolean nextIsApprove(String wfId,String runId,int stepNo){
        DBTransaction trans =
                    (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT); 
        String sql = "SELECT T.STEP_TASK FROM DMS_WORKFLOW_STATUS T WHERE T.WF_ID = '" + wfId + "' AND T.RUN_ID = '"
                        + runId + "' AND T.STEP_NO = " + stepNo;
        boolean flag = false;
        try {
            ResultSet rs = stat.executeQuery(sql);
            if(rs.next()){
                if(rs.getString("STEP_TASK").equals("APPROVE")){
                    flag = true;        
                }       
            }
            rs.close();
            stat.close();
        } catch (SQLException e) {
            this._logger.severe(e);
        }
        return flag;    
    }
    
//    public void passOtherApprove(List<String> backTemp,List<String> backEntity,String runId,int stepNo){
//        DBTransaction trans =
//            (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
//        Statement stat = trans.createStatement(DBTransaction.DEFAULT);   
//        
//        //更改子部门的未回退的模板审批状态为已审批
//        StringBuffer wspSql = new StringBuffer();
//        wspSql.append("update approve_template_status t set t.approval_status = 'Y' ");
//        wspSql.append("where t.run_id = '").append(runId).append("' ");
//        wspSql.append("and t.step_no = ").append(stepNo);
//        wspSql.append("and t.entity_id in (");
//
//        for(String entityCode : backEntity){
//            wspSql.append("'").append(entityCode).append("',");
//        }
//        wspSql.append("'')");
//        
//        wspSql.append(" and t.template_id not in (");
//        for(String entityCode : backEntity){
//            wspSql.append("'").append(entityCode).append("',");
//        }
//        wspSql.append("'')");
//
//        try {
//            stat.executeUpdate(wspSql.toString());
//            trans.commit();
//            stat.close();
//        } catch (SQLException e) {
//            this._logger.severe(e);
//        }
//    }
    
    public String getEntityName(String entityCode){
        String entityName = "";
        DBTransaction trans =
            (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        String eSql = "SELECT MEANING FROM DIM_ENTITYS T WHERE T.CODE = '" + entityCode + "'";
        try {
            ResultSet eRs = stat.executeQuery(eSql);
            if(eRs.next()){
                entityName = eRs.getString("MEANING");    
            }
            eRs.close();
            stat.close();
        } catch (SQLException e) {
            this._logger.severe(e);
        }
        return entityName;
    }
    
    //通过部门改变父节点下所有子部门和模板对应的组合状态
    public void changeComStatus(String runId,int stepNo,List<String> childList,List<String> backTemp,String status){
        DBTransaction trans =
            (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        //查询所有模板和对应的组合
        StringBuffer comSql = new StringBuffer();
        comSql.append("select template_id,com_id from workflow_template_status t ");
        comSql.append("where t.run_id = '").append(runId).append("' ");
        comSql.append("and t.step_no = ").append(stepNo);
        comSql.append("and t.entity_code in (");
        for(String entityCode : childList){
            comSql.append("'").append(entityCode).append("',");
        }
        comSql.append("'')");
        
        if(backTemp != null){
            comSql.append("and t.template_id in (");
            for(String tempCode : backTemp){
                comSql.append("'").append(tempCode).append("',");
            }
            comSql.append("'')");
        }
        
        try{
        ResultSet comRs = stat.executeQuery(comSql.toString());
        DBTransaction trans1 =
            (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat1 = trans.createStatement(DBTransaction.DEFAULT);
        while(comRs.next()){
            String temp_Id = comRs.getString("TEMPLATE_ID");
            String com_Id = comRs.getString("COM_ID");
            String upSql = "UPDATE DCM_TEMPLATE_COMBINATION T SET T.STATUS = '" + status + "' WHERE T.TEMPLATE_ID = '" + temp_Id
                + "' AND T.COM_RECORD_ID = '" + com_Id + "'";
            stat1.addBatch(upSql);
        }
            stat1.executeBatch();
            trans1.commit();
            comRs.close();
            stat1.close();
            stat.close();
        }catch(Exception e){
            this._logger.severe(e);
        }
    }
    
    public void passEtlStep(String runId,String entityParent,String odiParam,String sceneId){
        System.out.println(runId + ":" + entityParent + ":" + sceneId);
        DBTransaction trans =
            (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        //更改接口执行状态
        String uSql = "";
        if(odiParam == null || "".equals(odiParam)){
            uSql = "UPDATE WORKFLOW_ODI_STATUS T SET T.EXEC_STATUS = 'Y',T.EXEC_BY = '" + this.curUser.getId() 
                + "',T.FINISH_AT = SYSDATE WHERE T.RUN_ID = '" + runId
                + "' AND T.SCENE_ID = '" + sceneId + "' AND T.ENTITY_PARENT = '" + entityParent + "' AND T.ODI_ARGS IS NULL";  
        }else{
            uSql = "UPDATE WORKFLOW_ODI_STATUS T SET T.EXEC_STATUS = 'Y',T.EXEC_BY = '" + this.curUser.getId() 
                + "',T.FINISH_AT = SYSDATE WHERE T.RUN_ID = '" + runId
                + "' AND T.SCENE_ID = '" + sceneId + "' AND T.ENTITY_PARENT = '" + entityParent + "' AND T.ODI_ARGS ='" + odiParam + "'";
        }

        try {
            stat.executeUpdate(uSql);
            trans.commit();
            
            //查询工作流ID和步骤编码
            String sql = "SELECT D.WF_ID,D.STEP_NO FROM WORKFLOW_ODI_STATUS T,DMS_WORKFLOW_STATUS D WHERE T.RUN_ID = D.RUN_ID AND T.STEP_NO = D.STEP_NO AND D.STEP_TASK = 'ETL' " 
            + "AND T.RUN_ID = '" + runId + "' AND T.SCENE_ID = '" + sceneId + "' AND T.ENTITY_PARENT = '" + entityParent + "'";
            ResultSet rs = stat.executeQuery(sql);
            int stepNo = 0;
            String wfId = "";
            if(rs.next()){
                stepNo = rs.getInt("STEP_NO");
                wfId = rs.getString("WF_ID");
                this.startEtlNext(wfId,runId, entityParent, stepNo);
            }
            rs.close();
            stat.close();
            this.stepIsFinish(wfId, runId, stepNo, "ETL");
        } catch (SQLException e) {
            this._logger.severe(e);
        }
    }
    
}
