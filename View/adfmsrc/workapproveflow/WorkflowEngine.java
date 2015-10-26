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
                this.openTemplates(stepObj, openCom,runId,1);
                //打开模板完成，更新步骤状态表为working
                String udSql = "UPDATE DMS_WORKFLOW_STATUS SET STEP_STATUS = 'WORKING',START_AT = SYSDATE WHERE WF_ID = '" + wfId + "'"
                    + " AND RUN_ID = '" + runId + "'" + " AND STEP_NO = 1 ";
                stat.executeUpdate(udSql);
                trans.commit();
            }
            if (stepTask.equals("ETL")) {
                //更新ETL步骤为开始
                String ueSql = "UPDATE DMS_WORKFLOW_STATUS SET STEP_STATUS = 'WORKING',START_AT = SYSDATE WHERE WF_ID = '" + wfId + "'"
                    + " AND RUN_ID = '" + runId + "'" + " AND STEP_NO = 1 ";
                stat.executeUpdate(ueSql);
                trans.commit();
                //发送邮件提醒用户
                String uSql = "SELECT S.SCENE_ALIAS,USER_ID FROM ODI11_USER_SCENE_V T,ODI11_SCENE S WHERE T.SCENE_ID " +
                    "= S.ID AND S.LOCALE = 'zh_CN' AND T.SCENE_ID = '"
                    + stepObj + "'";
                ResultSet uRs = stat.executeQuery(uSql);
                while(uRs.next()){
                    afe.sendMail(uRs.getString("SCENE_ALIAS"),"", uRs.getString("USER_ID"), "工作流启动", "执行接口！", "工作流启动，请及时执行接口！", "");        
                }
                uRs.close();
                //跳过ETL
                int stepNo = 1 ;
                while(true){
                    Map<String,String> nextMap = this.queryNextStep(wfId, runId, stepNo);   
                    if("".equals(nextMap.get("STEP_TASK")) || nextMap.get("STEP_TASK")==null){
                        return;        
                    }
                    if("OPEN TEMPLATES".equals(nextMap.get("STEP_TASK"))){
                        this.openTemplates(nextMap.get("STEP_OBJECT"), openCom,runId,stepNo+1);
                        //打开模板完成，更新步骤状态表为working
                        String udSql = "UPDATE DMS_WORKFLOW_STATUS SET STEP_STATUS = 'WORKING',START_AT = SYSDATE WHERE WF_ID = '" + wfId + "'"
                            + " AND RUN_ID = '" + runId + "'" + " AND STEP_NO = " + (stepNo + 1);
                        stat.executeUpdate(udSql);
                        trans.commit();       
                        break;
                    }else if("ETL".equals(nextMap.get("STEP_TASK"))){
                        //更新ETL步骤为开始
                        String ueSql0 = "UPDATE DMS_WORKFLOW_STATUS SET STEP_STATUS = 'WORKING',START_AT = SYSDATE WHERE WF_ID = '" + wfId + "'"
                            + " AND RUN_ID = '" + runId + "'" + " AND STEP_NO = " + (stepNo + 1);
                        stat.executeUpdate(ueSql0);
                        trans.commit();    
                        stepNo = stepNo + 1;
                        //发送邮件提醒用户
                        String uSql0 = "SELECT S.SCENE_ALIAS,USER_ID FROM ODI11_USER_SCENE_V T,ODI11_SCENE S WHERE T.SCENE_ID " +
                            "= S.ID AND S.LOCALE = 'zh_CN' AND T.SCENE_ID = '"
                            + stepObj + "'";
                        ResultSet uRs0 = stat.executeQuery(uSql0);
                        while(uRs0.next()){
                            afe.sendMail(uRs0.getString("SCENE_ALIAS"),"", uRs0.getString("USER_ID"), "工作流启动", "执行接口！", "工作流，请及时执行接口！", "");        
                        }
                        uRs.close();
                    }
                }
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

    //打开模板，打开所有组合
    //stepObj 模板标签  openCom打开组合
    public void openTemplates(String stepObj, String openCom,String runId,int stepNo) {
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
                        System.out.println("sssssss:"+tempFlag);
                        if(tempFlag){
                            cis.append(" WHERE ");
                            tempFlag = false;
                        }
                        System.out.println(cEntry.getValue()+" = '"+valuesMap.get(cEntry.getValue()));
                        cis.append(cEntry.getKey()).append(" = '").append(valuesMap.get(cEntry.getKey())).append("' AND ");
                    }  
                }
                System.out.println(cis.toString());
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
                        System.out.println("sssssss:"+tempFlag);
                        if(tempFlag){
                            cis.append(" WHERE ");
                            tempFlag = false;
                        }
                        System.out.println(cEntry.getValue()+" = '"+valuesMap.get(cEntry.getValue()));
                        cis.append(cEntry.getKey()).append(" = '").append(valuesMap.get(cEntry.getKey())).append("' AND ");
                    }  
                }
                System.out.println(cis.toString());
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
                stat.addBatch(upWs.toString());
            }
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
            //开启模板输入状态
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
        //System.out.println("type:"+stepTask);
        if(stepTask.equals("OPEN TEMPLATES")){
            sql.append("SELECT 1 FROM WORKFLOW_TEMPLATE_STATUS WHERE WRITE_STATUS <> 'Y' ");
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
        if(isFinish){
            //this.startNextSteps(wfId,runId, stepNo);    
        }
    }
    
    public void startNext(String wfId,String runId,String templateId,String comId ,String comName,
                          int stepNo,String lastTask,String commitUser){
        //检查父节点是否完成输入或审批
        if(lastTask.equals("OPEN TEMPLATES")){
            if(!this.checkParent(runId, templateId, comId, stepNo)){
                return;    
            }    
        }else{
            ApproveflowEngine afe = new ApproveflowEngine();
            if(!(afe.checkParentApprove(runId, templateId, comId, stepNo) && this.checkParent(runId, templateId, comId, stepNo-1))){
                return;
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
                //跟新ETL步骤状态，发送邮件
                StringBuffer etlSql = new StringBuffer();
                etlSql.append("UPDATE DMS_WORKFLOW_STATUS SET STEP_STATUS = 'WORKING',START_AT = SYSDATE ");
                etlSql.append("WHERE WF_ID = '").append(wfId).append("' ");
                etlSql.append("AND RUN_ID = '").append(runId).append("' ");
                etlSql.append("AND STEP_NO = ").append(stepNo+1);
                stat.executeUpdate(etlSql.toString());
                trans.commit();
                //发送邮件提醒用户
                String uSql1 = "SELECT S.SCENE_ALIAS,USER_ID FROM ODI11_USER_SCENE_V T,ODI11_SCENE S WHERE T.SCENE_ID " +
                    "= S.ID AND S.LOCALE = 'zh_CN' AND T.SCENE_ID = '"
                    + stepObj + "'";
                ResultSet uRs1 = stat.executeQuery(uSql1);
                while(uRs1.next()){
                    afe.sendMail(uRs1.getString("SCENE_ALIAS"), comName, uRs1.getString("USER_ID"), commitUser, "执行接口！", "表单审批通过，请及时执行接口！", "");        
                }
                uRs1.close();
                //继续执行下一步
                stepNo = stepNo + 1 ;
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
    public void retreat(String wfId,String runId,int stepNo,int backStepNo,String templateId,String comId,String commitUser,String reason){
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
            this.retreatStep(wfId, runId,templateId, comId, stepList,commitUser,reason);
        } catch (SQLException e) {
            this._logger.severe(e);
        }
        //处理上上个到当前步骤之间的状态
        this.retreatStep(wfId, runId, templateId, comId, stepList,commitUser,reason);
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
        stat.close();
        this.retreatStep(wfId, runId, templateId, comId, stepList,commitUser,reason);
        }catch(Exception e){
            this._logger.severe(e);    
        }
    }
    
    //更改步骤状态，回退步骤
    public void retreatStep(String wfId,String runId,String templateId ,String comId,
                            List<Map<String,String>> stepList,String commitUser,String reason){
        DBTransaction trans =
            (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        ApproveflowEngine afe = new ApproveflowEngine();
        //查询所有子部门
        List<String> childList = this.getChildEntity(templateId, comId);
        try{
        boolean isFirst = true;
        for(Map<String,String> stepMap : stepList){
            String stepTask = stepMap.get("STEP_TASK");
            int stepNo = Integer.parseInt(stepMap.get("STEP_NO"));
            if("ETL".equals(stepTask)){
                continue;
            }else if("APPROVE".equals(stepTask)){
                //更改子部门的所有模板审批状态为未审批
                StringBuffer wspSql = new StringBuffer();
                wspSql.append("update approve_template_status t set t.approval_status = 'CLOSE' ");
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
                stat.executeUpdate(wspSql.toString());
                //更改步骤状态为working
                String uSql = "update dms_workflow_status t set t.step_status = 'WORKING',t.start_at = sysdate " + "where t.wf_id = '" + wfId + 
                              "' and t.run_id = '" + runId + "' and t.step_no =" + stepNo;
                stat.executeUpdate(uSql);
                trans.commit();
                ResultSet mRs = stat.executeQuery(tcuSql.toString());
                //send mail
                while(mRs.next()){
                    String tid = mRs.getString("TEMPLATE_ID");
                    String uid = mRs.getString("PERSON_ID");
                    String entityName ="_" + this.getEntityName(mRs.getString("ENTITY_ID"));
                    afe.sendMail(tid, entityName, uid,commitUser, "工作流回退！", "工作流回退，请等待表单重新提交审批！",reason);
                }
            }else if("OPEN TEMPLATES".equals(stepTask)){
                //修改输入状态表为未输入
                StringBuffer wsrSql = new StringBuffer();
                wsrSql.append("update workflow_template_status t set t.write_status = 'CLOSE' ");
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
                stat.executeUpdate(wsrSql.toString());
                //更改步骤状态为working
                String uSql = "update dms_workflow_status t set t.step_status = 'WORKING',t.start_at = sysdate " + "where t.wf_id = '" + wfId + 
                              "' and t.run_id = '" + runId + "' and t.step_no =" + stepNo;
                stat.executeUpdate(uSql);
                trans.commit();
                ResultSet mRs = stat.executeQuery(tcwSql.toString());
                //最前的开模板步骤，打开组合
                if(isFirst){
                    //最前的打开模板步骤打开组合
                    this.changeComStatus(runId, stepNo, childList, "OPEN");
                    isFirst = false ;
                    Map<String,List<String>> tempComMap = new HashMap<String,List<String>>();
                    //send mail
                    while(mRs.next()){
                        String tid = mRs.getString("TEMPLATE_ID");
                        String uid = mRs.getString("WRITE_BY");
                        String cid = mRs.getString("COM_ID");
                        if(tempComMap.containsKey(tid)){
                            tempComMap.get(tid).add(cid);    
                        }else{
                            List<String> list = new ArrayList<String>();
                            list.add(cid);
                            tempComMap.put(tid, list);
                        }
                        String entityName ="_" + this.getEntityName(mRs.getString("ENTITY_CODE"));
                        afe.sendMail(tid, entityName, uid,commitUser, "工作流回退！", "工作流回退，请重新填写表单数据！",reason);
                    }
                    //开启输入状态
                    this.openWirteStatus(tempComMap, runId, stepNo);
                }else{
                    //其他的打开模板步骤关闭组合
                    this.changeComStatus(runId, stepNo, childList, "CLOSE");
                    //send mail
                    while(mRs.next()){
                        String tid = mRs.getString("TEMPLATE_ID");
                        String uid = mRs.getString("WRITE_BY");
                        String entityName ="_" + this.getEntityName(mRs.getString("ENTITY_CODE"));
                        afe.sendMail(tid, entityName, uid,commitUser, "工作流回退！", "工作流回退，请等待前面表单审批通过后，再重填表单数据！",reason);
                    }
                }
            }
        }
            stat.close();
        }catch(Exception e){
            this._logger.severe(e);    
        }
    }
    
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
    public void changeComStatus(String runId,int stepNo,List<String> childList,String status){
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
}
