package workapproveflow;

import common.DmsUtils;

import dms.login.Person;

import dms.workflow.WorkflowEditBean;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.text.ParseException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import oracle.jbo.domain.Number;
import oracle.adf.share.ADFContext;
import oracle.adf.share.logging.ADFLogger;

import oracle.jbo.ViewObject;
import oracle.jbo.server.DBTransaction;

import team.epm.dcm.view.DcmTemplateCombinaVOImpl;
import team.epm.dcm.view.DcmTemplateCombinaVORowImpl;
import team.epm.dms.view.DmsWorkflowTemplateStatusVOImpl;
import team.epm.dms.view.DmsWorkflowTemplateStatusVORowImpl;

public class WorkflowEngine {
    //日志
    private static ADFLogger _logger =
        ADFLogger.createADFLogger(WorkflowEditBean.class);
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
        //保存所有模板，和模板要打开的部门编码
        Map<String,List<String>> tempEntityMap = new HashMap<String,List<String>>();
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
                List<String> sqlList = new ArrayList<String>();
                //部门编码
                List<String> entityList = new ArrayList<String>();
                while (entityRs.next()) {
                    entityList.add(entityRs.getString("ENTITY"));
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
                    sqlList.add(sqlStr);
                }
                //put模板ID 和 对应要打开的部门List
                tempEntityMap.put(entry.getKey(), entityList);
                //根据工作流选择的组合，查询出在模板组合表中的ID
                List<String> comIdList = new ArrayList<String>();
                for (String sql : sqlList) {
                    ResultSet comIdRs = stat.executeQuery(sql);
                    if (comIdRs.next()) {
                        String comId = comIdRs.getString("ID");
                        comIdList.add(comId);
                    }
                    comIdRs.close();
                }
                tempComMap.put(entry.getKey(), comIdList);
                entityRs.close();
            }
            //打开每张模板对应的组合
            this.openTempCom(tempComMap);
            //初始化每张模板的输入状态
            this.initTemplateStatus(runId, stepNo, tempEntityMap);
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
    private void initTemplateStatus(String runId,int stepNo,Map<String,List<String>> tempEntityMap){
        DmsWorkflowTemplateStatusVOImpl vo = DmsUtils.getDmsApplicationModule().getDmsWorkflowTemplateStatusVO();
        for(Map.Entry<String,List<String>> tempEntry : tempEntityMap.entrySet()){
            for(String entity : tempEntry.getValue()){
                DmsWorkflowTemplateStatusVORowImpl wtsRow = (DmsWorkflowTemplateStatusVORowImpl)vo.createRow();
                wtsRow.setRunId(runId);
                wtsRow.setStepNo(new Number(stepNo));
                wtsRow.setTemplateId(tempEntry.getKey());
                wtsRow.setEntityCode(entity);
                wtsRow.setWriteBy("");
                wtsRow.setWriteStatus("N");
                wtsRow.setFinishAt(null);
                vo.insertRow(wtsRow);
            }
        }
        vo.getApplicationModule().getTransaction().commit();
    }
}
