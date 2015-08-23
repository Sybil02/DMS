package workapproveflow;

import common.DmsUtils;

import dms.login.Person;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public void changeWfStatus(String wfId, String wfStatus) {
        try {
            System.out.println(wfId + ":" + wfStatus);
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
            System.out.println("row:" + i);
            trans.commit();
            state.close();
        } catch (SQLException e) {
            this._logger.severe(e);
        }
    }

    //初始化工作流步骤表

    public void initWfSteps(String wfId,
                            Map<String, Map<String, String>> comSelectMap) {
        StringBuffer openComPara = new StringBuffer();
        for (Map.Entry<String, Map<String, String>> entry :
             comSelectMap.entrySet()) {
            Map<String, String> map = entry.getValue();
            for (Map.Entry<String, String> mapEntry : map.entrySet()) {
                openComPara.append(mapEntry.getKey()).append(":").append(mapEntry.getValue()).append("#");
                //System.out.println(openComPara.toString());
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
        //生成runId
        String runId = java.util.UUID.randomUUID().toString().replace("-", "");
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
                this.openTemplates(stepObj, openCom,runId,1);
                //打开模板完成，更新步骤状态表为working
                String udSql = "UPDATE DMS_WORKFLOW_STATUS SET STEP_STATUS = 'WORKING' WHERE WF_ID = '" + wfId + "'"
                    + " AND RUN_ID = '" + runId + "'" + " AND STEP_NO = 1 ";
                stat.executeUpdate(udSql);
                trans.commit();
                stat.close();
            }
            if (stepTask.equals("ETL")) {

            }
            if (stepTask.equals("APPROVE")) {

            }
        } catch (SQLException e) {
            this._logger.severe(e);
        }
    }

    //OPEN TEMPLATES
    //stepObj 模板标签  openCom打开组合
    private void openTemplates(String stepObj, String openCom,String runId,int stepNo) {
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
        Map<String,Map<String,String>> tempEntityMap = new HashMap<String,Map<String,String>>();
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
                String entitySql =
                    "SELECT ENTITY,ENTITY_CODE FROM DCM_TEM_ENTITY_COM WHERE DCM_TEMPLATE_ID = '" +
                    entry.getKey() + "'";
                //查询每个模板在dcm_tem_entity_com中维护了的部门和部门对应的列名
                ResultSet entityRs = stat.executeQuery(entitySql);
                //存部门和该部门对应查询组合ID的sql
                Map<String,String> sqlMap = new HashMap<String,String>();
                //List<String> sqlList = new ArrayList<String>();
                //部门编码,和该部门对应的组合ID
                //List<String> entityList = new ArrayList<String>();
                Map<String,String> entityMap = new HashMap<String,String>();
                while (entityRs.next()) {
                    //entityList.add(entityRs.getString("ENTITY"));
                    StringBuffer comIdSql = new StringBuffer();
                    comIdSql.append("SELECT ID FROM ").append(entry.getValue()).append(" WHERE ");
                    comIdSql.append(entityRs.getString("ENTITY_CODE")).append(" = '").append(entityRs.getString("ENTITY")).append("' AND ");
                    //拼接工作流要打开的组合的值
                    for (Map.Entry<String, String> vEntry :
                         valuesMap.entrySet()) {
                        comIdSql.append(vEntry.getKey()).append("= '").append(vEntry.getValue()).append("' AND ");
                    }
                    //去掉最后一个多出的"AND ",加空格共四位
                    String sqlStr =
                        comIdSql.substring(0, comIdSql.length() - 4);
                    //System.out.println(sqlStr);
                    //sqlList.add(sqlStr);
                    sqlMap.put(entityRs.getString("ENTITY"),sqlStr);
                }
                //根据工作流选择的组合，查询出在模板组合表中的ID
                List<String> comIdList = new ArrayList<String>();
                for (Map.Entry<String,String> sqlEntry : sqlMap.entrySet()) {
                    ResultSet comIdRs = stat.executeQuery(sqlEntry.getValue());
                    if (comIdRs.next()) {
                        String comId = comIdRs.getString("ID");
                        comIdList.add(comId);
                        entityMap.put(sqlEntry.getKey(),comId);
                    }
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
            this.initTemplateStatus(runId, stepNo, tempEntityMap);
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
    //初始化模板部门数据输入状态表
    private void initTemplateStatus(String runId,int stepNo,Map<String,Map<String,String>> tempEntityMap){
        DmsWorkflowTemplateStatusVOImpl vo = DmsUtils.getDmsApplicationModule().getDmsWorkflowTemplateStatusVO();
        for(Map.Entry<String,Map<String,String>> tempEntry : tempEntityMap.entrySet()){
            for(Map.Entry<String,String> entry : tempEntry.getValue().entrySet()){
                DmsWorkflowTemplateStatusVORowImpl wtsRow = (DmsWorkflowTemplateStatusVORowImpl)vo.createRow();
                wtsRow.setRunId(runId);
                wtsRow.setStepNo(new Number(stepNo));
                wtsRow.setTemplateId(tempEntry.getKey());
                wtsRow.setEntityCode(entry.getKey());
                wtsRow.setComId(entry.getValue());
                wtsRow.setWriteBy("");
                wtsRow.setWriteStatus("N");
                wtsRow.setFinishAt(null);
                vo.insertRow(wtsRow);
            }
        }
        vo.getApplicationModule().getTransaction().commit();
    }
    //初始化每张模板的审批状态
    private void initApproveStatus(String runId,int stepNo,Map<String,Map<String,String>> tempEntityMap){
        DBTransaction trans =
            (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        //遍历模板和对应的部门
        try{
        for(Map.Entry<String,Map<String,String>> tempEntry : tempEntityMap.entrySet()){
            Map<String,List<String>> entityUserMap = new HashMap<String,List<String>>();
            for(Map.Entry<String,String> entityEntry : tempEntry.getValue().entrySet()){
                List<String> userList = new ArrayList<String>();
                String tempId = tempEntry.getKey();
                String entityCode = entityEntry.getKey();
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
            } 
            //DmsApproveTemplateStatusVOImpl vo = DmsUtils.getDmsApplicationModule().getDmsApproveTemplateStatusVO();
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
                    insert.append("'").append(tempEntityMap.get(tempEntry.getKey()).get(euEntry.getKey())).append("')");
                    System.out.println(insert.toString());
                    stat.addBatch(insert.toString());
                }    
            }
            stat.executeBatch();
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
        System.out.println("type:"+stepTask);
        if(stepTask.equals("OPEN TEMPLATES")){
            sql.append("SELECT 1 FROM WORKFLOW_TEMPLATE_STATUS WHERE WRITE_STATUS = 'N' ");
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
                finishSql.append("UPDATE DMS_WORKFLOW_STATUS SET STEP_STATUS = 'Y' WHERE ");
                finishSql.append("WF_ID = '").append(wfId).append("' ").append("AND RUN_ID = '").append(runId).append("' ").append("AND STEP_NO = ").append(stepNo);
                stat.executeUpdate(finishSql.toString());
                trans.commit();
            }
            rs.close();
            stat.close();
        } catch (SQLException e) {
            this._logger.severe(e);
        }
        if(isFinish){
            this.startNextSteps(wfId,runId, stepNo);    
        }
    }
    
    //启动下一步
    public void startNextSteps(String wfId,String runId,int stepNo){
        DBTransaction trans = (DBTransaction)DmsUtils.getDcmApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        Map<String,String> nextMap = this.queryNextStep(wfId, runId, stepNo);
        String stepTask = nextMap.get("STEP_TASK");
        //String stepStatus = nextMap.get("STEP_STATUS");
        String openCom = nextMap.get("OPEN_COM");
        String stepObj = nextMap.get("STEP_OBJECT");
        try{
        if(stepTask != null && stepTask != ""){
            if(stepTask.equals("ETL")){
                //更改ETL步骤状态为进行中
                StringBuffer etlSql = new StringBuffer();
                etlSql.append("UPDATE DMS_WORKFLOW_STATUS SET STEP_STATUS = 'WORKING' ");
                etlSql.append("WHERE WF_ID = '").append(wfId).append("' ");
                etlSql.append("AND RUN_ID = '").append(runId).append("' ");
                etlSql.append("AND STEP_NO = ").append(stepNo+1);
                stat.executeUpdate(etlSql.toString());
                trans.commit();
                System.out.println("更改ETL步骤状态为进行中");
            }else if(stepTask.equals("OPEN TEMPLATES")){
                //打开模板，初始化模板输入状态表和审批流状态表
                this.openTemplates(stepObj, openCom, runId, stepNo+1);
                //更新步骤状态为WORKING
                int i = stepNo + 1;
                String udnSql = "UPDATE DMS_WORKFLOW_STATUS SET STEP_STATUS = 'WORKING' WHERE WF_ID = '" + wfId + "'"
                    + " AND RUN_ID = '" + runId + "'" + " AND STEP_NO = " + i;
                stat.executeUpdate(udnSql);
                trans.commit();
            }else{
                //若为审批，则不操作，打开模板步骤已经初始化审批状态表
            }
        }else{
            //更新工作流状态为完成
            StringBuffer wfSql = new StringBuffer();
            wfSql.append("UPDATE DMS_WORKFLOWINFO SET WF_STATUS = 'N' WHERE ID = '").append(wfId).append("'");
            try {
                stat.executeUpdate(wfSql.toString());
                trans.commit();
            } catch (SQLException e) {
                this._logger.severe(e);
            }
        }
            stat.close();
        } catch (SQLException e) {
                this._logger.severe(e);
        }
    }
}
