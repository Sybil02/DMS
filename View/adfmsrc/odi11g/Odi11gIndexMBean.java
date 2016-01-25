package odi11g;

import common.ADFUtils;

import common.DmsUtils;

import common.JSFUtils;

import dms.login.Person;

import java.net.MalformedURLException;
import java.net.URL;

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

import javax.faces.model.SelectItem;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import odi11g.webservice.type.OdiCredentialType;
import odi11g.webservice.type.OdiGetSessionsStatusRequest;
import odi11g.webservice.type.OdiGetSessionsStatusResponse;
import odi11g.webservice.type.OdiStartScenRequest;
import odi11g.webservice.type.OdiStartType;
import odi11g.webservice.type.ScenarioRequestType;
import odi11g.webservice.type.SessionStatusType;
import odi11g.webservice.type.VariableType;
import odi11g.webservice.ws.RequestPortType;

import oracle.adf.share.ADFContext;
import oracle.adf.share.logging.ADFLogger;
import oracle.adf.view.rich.component.rich.RichPopup;
import oracle.adf.view.rich.component.rich.data.RichTable;
import oracle.adf.view.rich.component.rich.data.RichTree;

import oracle.adf.view.rich.context.AdfFacesContext;

import oracle.jbo.Key;
import oracle.jbo.Row;
import oracle.jbo.ViewCriteria;
import oracle.jbo.ViewCriteriaRow;
import oracle.jbo.ViewObject;

import oracle.jbo.server.DBTransaction;

import org.apache.commons.lang.ObjectUtils;
import org.apache.myfaces.trinidad.event.SelectionEvent;
import org.apache.myfaces.trinidad.model.RowKeySet;

import workapproveflow.WorkflowEngine;

public class Odi11gIndexMBean {
    private Odi11gCatTreeModel model;
    private Map valueList = new HashMap();
    private static ADFLogger _logger =
        ADFLogger.createADFLogger(Odi11gIndexMBean.class);
    private RichTable sceneTable;
    private RichPopup popup;
    private RichTable paramTable;
    private RichPopup statusPopup;
    private RichPopup exceptionPopup;

    public void setModel(Odi11gCatTreeModel model) {
        this.model = model;
    }

    public Odi11gCatTreeModel getModel() {
        if (null == this.model) {
            model = new Odi11gCatTreeModel();
        }
        return this.model;
    }

    public void selectListener(SelectionEvent selectionEvent) {
        RichTree tree = (RichTree)selectionEvent.getSource();
        RowKeySet rks = selectionEvent.getAddedSet();
        if (rks != null) {
            int setSize = rks.size();
            if (setSize == 0) {
                return;
            }
            Object rowKey = null;
            Odi11gCatTreeItem rowData = null;
            rowKey = rks.iterator().next();
            tree.setRowKey(rowKey);
            rowData = (Odi11gCatTreeItem)tree.getRowData();
            
            if( rowData.getChildren().size() < 1) {
                ViewObject vo =
                    ADFUtils.findIterator("Odi11AuthedSceneViewIterator").getViewObject();
                vo.setNamedWhereClauseParam("catId", rowData.getId());
                vo.executeQuery();
                AdfFacesContext.getCurrentInstance().addPartialTarget(this.sceneTable);
            }
        }
    }

    public void setSceneTable(RichTable sceneTable) {
        this.sceneTable = sceneTable;
    }

    public RichTable getSceneTable() {
        return sceneTable;
    }

    public void showExcutePopup(ActionEvent actionEvent) throws MalformedURLException {
        ViewObject paramVo =
            ADFUtils.findIterator("Odi11SceneParamExViewIterator").getViewObject();
        ViewObject sceneVo =
            ADFUtils.findIterator("Odi11AuthedSceneViewIterator").getViewObject();
        if (sceneVo.getCurrentRow() != null) {
            paramVo.setNamedWhereClauseParam("sceneId",
                                             sceneVo.getCurrentRow().getAttribute("Id"));
            paramVo.executeQuery();
            if (paramVo.hasNext()) {
                this.valueList.clear();
                while (paramVo.hasNext()) {
                    Row row = paramVo.next();
                    if (row.getAttribute("ValueSetId") != null) {
                        this.initValueSetValues((String)row.getAttribute("ValueSetId"),
                                                true);
                    }
                }
                RichPopup.PopupHints hint = new RichPopup.PopupHints();
                this.popup.show(hint);
            } else {
                //没有参数
                if(this.getRunNum("R",sceneVo.getCurrentRow().getAttribute("QueueCategory"))==0){
                    this.run(sceneVo.getCurrentRow(), new HashMap(),null);                  
                }else{

                    this.insertExecQueue(sceneVo.getCurrentRow(), new HashMap()); 
                    this.showQueueNum();
                    this.showStatusPopup(actionEvent);
                    this.checkRunAndQueue();
                }

            }
        }
    }
    
    private int getRunNum(String status,Object category){
        DBTransaction trans =
            (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        int num = 0;
        String sql = "SELECT COUNT(1) RUNNUM FROM ODI11_SCENE_EXEC T,ODI11_SCENE O WHERE T.SCENE_ID = O.ID AND O.LOCALE = 'zh_CN' "
                     + "AND T.EXEC_STATUS = '" + status + "' ";
        if(category == null || "".equals(category)){
            sql = sql + "AND O.QUEUE_CATEGORY IS NULL";
        }else{
            sql = sql + "AND O.QUEUE_CATEGORY = '" + category.toString() + "'";
        }
        
        try {
            ResultSet rs = stat.executeQuery(sql);
            if(rs.next()){
                num = rs.getInt("RUNNUM"); 
            }
            rs.close();
            stat.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return num;
    }

    public void refreshSceneStatus(ActionEvent actionEvent) throws MalformedURLException {
        this.refreshStatus((String)ADFUtils.findIterator("Odi11AuthedSceneViewIterator").getViewObject().getCurrentRow().getAttribute("Id"));
        this.showStatus();
    }

    public void setPopup(RichPopup popup) {
        this.popup = popup;
    }

    public RichPopup getPopup() {
        return popup;
    }

    public Map getValueList() {
        return this.valueList;
    }
    //获取值集数据

    private void initValueSetValues(String valueSetId, boolean isAuth) {
        Row vsRow[] =
            DmsUtils.getDmsApplicationModule().getDmsValueSetView().findByKey(new Key(new Object[] { valueSetId,
                                                                                                     ADFContext.getCurrent().getLocale().toString() }),
                                                                              1);
        if (vsRow != null && vsRow.length > 0) {
            String source = (String)vsRow[0].getAttribute("Source");
            StringBuffer sql = new StringBuffer();
            sql.append("SELECT T.CODE,T.MEANING FROM \"").append(source).append("\" T").append("  WHERE T.LOCALE='").append(ADFContext.getCurrent().getLocale()).append("'");
            if (isAuth) {
                sql.append("  AND EXISTS (SELECT 1 FROM DMS_USER_VALUE_V V")
                    .append("  WHERE V.USER_ID = '")
                    .append(((Person)ADFContext.getCurrent().getSessionScope().get("cur_user")).getId())
                    .append("'")
                    .append("  AND V.VALUE_SET_ID='")
                    .append(valueSetId).append("'")
                    .append("  AND V.VALUE_ID = T.CODE)");
            }
            sql.append(" AND T.ENABLED='Y'  ORDER BY T.IDX");
            ViewObject vo =
                DmsUtils.getDmsApplicationModule().createViewObjectFromQueryStmt(null,
                                                                                 sql.toString());
            vo.executeQuery();
            List vsList = new ArrayList();
            this.valueList.put(valueSetId, vsList);
            while (vo.hasNext()) {
                Row row = vo.next();
                SelectItem item = new SelectItem();
                item.setLabel(ObjectUtils.toString(row.getAttribute("MEANING")));
                item.setValue(ObjectUtils.toString(row.getAttribute("CODE")));
                vsList.add(item);
            }
            vo.remove();
        }
    }

    private RequestPortType getRequestPortType(Row sceneRow) throws MalformedURLException {
        String agentId =
            ObjectUtils.toString(sceneRow.getAttribute("AgentId"));
        ViewObject agentVo =
            DmsUtils.getOdi11gApplicationModule().getOdi11AgentView();
        Row agenRow =
            agentVo.findByKey(new Key(new Object[] { agentId, ADFContext.getCurrent().getLocale().toString() }),
                              1)[0];
        String agentHost =
            ObjectUtils.toString(agenRow.getAttribute("AgentHost"));
        String agentPort =
            ObjectUtils.toString(agenRow.getAttribute("AgentPort"));
        String agentContext =
            ObjectUtils.toString(agenRow.getAttribute("AgentContext"));
        String agentProtocol =
            ObjectUtils.toString(agenRow.getAttribute("Protocol"));
        String wsdlUrl =
            agentProtocol + "://" + agentHost + ":" + agentPort + "/" +
            agentContext + "/OdiInvoke?wsdl";
        URL url = new URL(wsdlUrl);
        QName qname =
            new QName("xmlns.oracle.com/odi/OdiInvoke/", "OdiInvoke");
        Service service = Service.create(url, qname);
        RequestPortType requestPortType =
            (RequestPortType)service.getPort(new QName("xmlns.oracle.com/odi/OdiInvoke/",
                                                       "OdiInvokeRequestSOAP11port0"),
                                             RequestPortType.class);
        return requestPortType;
    }

    private OdiCredentialType getOdiCredentialType(Row sceneRow) {
        String workrepId =
            ObjectUtils.toString(sceneRow.getAttribute("WorkrepId"));
        ViewObject workrepVo =
            DmsUtils.getOdi11gApplicationModule().getOdi11WorkrepView();
        Row workrepRow =
            workrepVo.findByKey(new Key(new Object[] { workrepId, ADFContext.getCurrent().getLocale().toString() }),
                                1)[0];
        String repName =
            ObjectUtils.toString(workrepRow.getAttribute("RepName"));
        String loginUser =
            ObjectUtils.toString(workrepRow.getAttribute("LoginUser"));
        String loginPwd =
            ObjectUtils.toString(workrepRow.getAttribute("LoginPwd"));
        OdiCredentialType odiCredentialType = new OdiCredentialType();
        odiCredentialType.setOdiUser(loginUser);
        odiCredentialType.setOdiPassword(loginPwd);
        odiCredentialType.setWorkRepository(repName);
        return odiCredentialType;
    }

    private void insertExecQueue(Row sceneRow, Map params){
        String sceneId = ObjectUtils.toString(sceneRow.getAttribute("Id"));
        StringBuffer parmStr = new StringBuffer();
        for (Object v : params.keySet()) {
            parmStr.append("#").append(v);
            parmStr.append(":").append(params.get(v));
        }

        //插入执行队列
        ViewObject execVo =
            DmsUtils.getOdi11gApplicationModule().getOdi11SceneExecView();
        String clearHistorySql =
            "delete from odi11_scene_exec t where t.scene_id='" + sceneId +
            "' and t.params " +
            (params.isEmpty() ? "is null" : ("='" + parmStr.toString() +
                                             "'"));
        ((DBTransaction)execVo.getApplicationModule().getTransaction()).executeCommand(clearHistorySql);
        Row execRow = execVo.createRow();
        execRow.setAttribute("SceneId", sceneId);
        execRow.setAttribute("Params", parmStr.toString());
        execRow.setAttribute("ExecStatus", "QUEUE");
        execVo.insertRow(execRow);
        execVo.getApplicationModule().getTransaction().commit();
    }

    private void run(Row sceneRow, Map params,String createBy) throws MalformedURLException {
        StringBuffer parmStr = new StringBuffer();
        for (Object v : params.keySet()) {
            parmStr.append("#").append(v);
            parmStr.append(":").append(params.get(v));
        }
        //不允许同一接口使用相同的参数同时执行
        if (this.isRunning((String)sceneRow.getAttribute("Id"),
                           parmStr.toString())) {
            JSFUtils.addFacesInformationMessage(DmsUtils.getMsg("odi11g.scene.running"));
            this.statusPopup.cancel();
            return;
        }
        try {
            String sceneName =
                ObjectUtils.toString(sceneRow.getAttribute("SceneName"));
            String sceneId = ObjectUtils.toString(sceneRow.getAttribute("Id"));
            String sceneVersion =
                ObjectUtils.toString(sceneRow.getAttribute("SceneVersion"));
            String sceneContext =
                ObjectUtils.toString(sceneRow.getAttribute("SceneContext"));
            //执行接口
            RequestPortType requestPortType =
                this.getRequestPortType(sceneRow);

            OdiStartScenRequest odiStartScenRequest =
                new OdiStartScenRequest();
            odiStartScenRequest.setCredentials(this.getOdiCredentialType(sceneRow));

            ScenarioRequestType scenarioRequestType =
                new ScenarioRequestType();
            scenarioRequestType.setContext(sceneContext);
            scenarioRequestType.setLogLevel(5);
            scenarioRequestType.setScenarioName(sceneName);
            scenarioRequestType.setScenarioVersion(sceneVersion);
            scenarioRequestType.setSessionName("DMS-" + sceneName);
            scenarioRequestType.setSynchronous(false);
            //处理参数
            for (Object key : params.keySet()) {
                VariableType var = new VariableType();
                var.setName((String)key);
                var.setValue((String)params.get(key));
                scenarioRequestType.getVariables().add(var);
            }
            odiStartScenRequest.setRequest(scenarioRequestType);

            OdiStartType response =
                requestPortType.invokeStartScen(odiStartScenRequest);
            String sessionNum = ((Long)response.getSession()).toString();
            
            //odi queue
            //接口运行，更新sessionNum
 
            //odi queue
            //插入执行记录
            ViewObject execVo =
                DmsUtils.getOdi11gApplicationModule().getOdi11SceneExecView();
            String clearHistorySql =
                "delete from odi11_scene_exec t where t.scene_id='" + sceneId +
                "' and t.params " +
                (params.isEmpty() ? "is null" : ("='" + parmStr.toString() +
                                                 "'"));
            ((DBTransaction)execVo.getApplicationModule().getTransaction()).executeCommand(clearHistorySql);
            Row execRow = execVo.createRow();
            execRow.setAttribute("SceneId", sceneId);
            execRow.setAttribute("Params", parmStr.toString());
            execRow.setAttribute("SessionNum", sessionNum);
            execRow.setAttribute("ExecStatus", "R");
            execVo.insertRow(execRow);
            execVo.getApplicationModule().getTransaction().commit();
            this.popup.cancel();
            this.showQueueNum();
            this.showStatus();
            RichPopup.PopupHints hint = new RichPopup.PopupHints();
            this.statusPopup.show(hint);
            
            if(createBy != null && !"".equals(createBy)){
                this.updateCreateBy(sceneId, parmStr.toString(), createBy);
            }
            //workflow odi
            final String sid = (String)sceneRow.getAttribute("Id");
            final String para = parmStr.toString();
            
            
            for(int i=0;i<10000;i++){
                try {
                    Thread.sleep(1000);
                    if(i%5==0){
                    if(!isRunning(sid, para)){
                        break;    
                    }
                    System.out.println("五秒后再次检查执行状态...");
                    }
                } catch (Exception e) {
                    this._logger.severe("ODI排队检查异常***************************************************"+e);
                }
            }
            
            Runnable rb = new Runnable(){
                public void run(){
                    try {
                        Thread.sleep(3000);
                        while(isRunning(sid, para)){
                            System.out.println("五秒后再次检查执行状态...");
                            Thread.sleep(5000);
                        }
                        
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }    
            };
            rb.run();
            
            if(!isRunning(sid,para)){
                if("D".equals(this.getStatus(sid,para))){
                    System.out.println("ETL IS FINISH........"+para);
                    //检查工作流接口
                    this.checkEtlStep(sid,params);   
                }
                
                //检查队列中是否还有待执行接口
                if(this.getRunNum("QUEUE",this.getCategroy(sid)) > 0 && this.getRunNum("R",this.getCategroy(sid)) == 0){
                    this.executeNext(this.getCategroy(sid));    
                    //this.showQueueNum();
                }
                
            }
            
            this.refreshStatus(sceneId);
            //workflow odi
            
        } catch (IndexOutOfBoundsException e) {
            this._logger.severe("Agent or Workrepository may not exits!");
            this._logger.severe(e);
        }
    }
    
    public Object getCategroy(String sid){
        Object category = null;
        
        ViewObject vo = DmsUtils.getOdi11gApplicationModule().getOdi11SceneView();
        ViewCriteria vc = vo.createViewCriteria();
        ViewCriteriaRow vcRow = vc.createViewCriteriaRow();
        vcRow.setAttribute("Id", "='" + sid + "'");
        vcRow.setConjunction(vcRow.VC_CONJ_AND);
        vc.addElement(vcRow);
        vo.applyViewCriteria(vc);
        vo.executeQuery();
        Row sceneRow = vo.first();
        category = sceneRow.getAttribute("QueueCategory");
        vo.getViewCriteriaManager().setApplyViewCriteriaName(null);
        
        return category;
    }
    
    public String getStatus(String sceneId,String params){
        String status = "D";
        ViewObject execVo =
            DmsUtils.getOdi11gApplicationModule().getOdi11SceneExecView();
        ViewCriteria vc = execVo.createViewCriteria();
        ViewCriteriaRow row = vc.createViewCriteriaRow();
        row.setAttribute("SceneId", "='" + sceneId + "'");

        if (params.length() > 0) {
            row.setAttribute("Params", "='" + params + "'");
        } else {
            row.setAttribute("Params", " is null");
        }
        row.setConjunction(row.VC_CONJ_AND);
        vc.addElement(row);
        execVo.applyViewCriteria(vc);
        execVo.executeQuery();
        if (execVo.hasNext()) {
            Row execRow = execVo.next();
            status = this.queryStatus(execRow);
        }
        execVo.getViewCriteriaManager().setApplyViewCriteriaNames(null);
        return status;
    }
    
    private void showQueueNum(){
        DBTransaction trans =
            (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        
        String cSql = "SELECT COUNT(1) NUM FROM ODI11_SCENE_EXEC T WHERE T.EXEC_STATUS = 'QUEUE'";
        String sql = "";
        try {
            ResultSet rs = stat.executeQuery(cSql);
            if(rs.next()){
                sql = "UPDATE ODI11_SCENE_EXEC T SET T.LOG_TEXT = '系统当前队列中" +rs.getString("NUM")+ "个接口待执行！'"
                    + " WHERE T.EXEC_STATUS = 'QUEUE'";
            }
            rs.close();
            if(sql.length() > 1){
                stat.executeUpdate(sql);
                trans.commit();
            }
            stat.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void executeNext(Object category){
        DBTransaction trans =
            (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        String sql = "SELECT T.SCENE_ID,T.PARAMS,T.CREATED_BY FROM ODI11_SCENE_EXEC T,ODI11_SCENE O "
            + "WHERE O.ID = T.SCENE_ID AND O.LOCALE = 'zh_CN'";
        if(category == null || "".equals(category)){
            sql = sql + " AND O.QUEUE_CATEGORY IS NULL " ;
        }else{
            sql = sql + " AND O.QUEUE_CATEGORY = '" + category.toString() + "' ";    
        }
        sql = sql + " AND T.EXEC_STATUS = 'QUEUE' ORDER BY T.CREATED_AT ASC";
        String createBy = "";
        ResultSet rs;
        try {
            String sceneId = "";
            String paramStr = "";
            rs = stat.executeQuery(sql);
            if(rs.next()){
                sceneId = rs.getString("SCENE_ID");
                paramStr = rs.getString("PARAMS");
                createBy = rs.getString("CREATED_BY");
            }
            rs.close();
            stat.close();

            Map params = new LinkedHashMap();
            
            //更新执行人为创建者
//            String bSql = "UPDATE ODI11_SCENE_EXEC T SET T.CREATED_BY = '" + createBy + "',T.UPDATED_BY = '" + createBy
//                + "' WHERE T.SCENE_ID = '" + sceneId + "' AND T.PARAMS = '" + paramStr + "'";
            
            paramStr = paramStr.substring(1, paramStr.length());
            String[] str = paramStr.split("#");
            for (int i = 0; i < str.length; i++) {
                String[] s = str[i].split(":");
                //s[0]:列名，s[1]：值
                params.put(s[0], s[1]);
            }
            
            ViewObject vo = DmsUtils.getOdi11gApplicationModule().getOdi11SceneView();
            ViewCriteria vc = vo.createViewCriteria();
            ViewCriteriaRow vcRow = vc.createViewCriteriaRow();
            vcRow.setAttribute("Id", "='"+sceneId+"'");
            vcRow.setConjunction(vcRow.VC_CONJ_AND);
            vc.addElement(vcRow);
            vo.applyViewCriteria(vc);
            vo.executeQuery();
            Row sceneRow = vo.first();
            vo.getViewCriteriaManager().setApplyViewCriteriaName(null);
            if(this.getRunNum("R",category)==0){
                this.run(sceneRow, params,createBy);
            }
//            stat.executeUpdate(bSql);
//            trans.commit();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void updateCreateBy(String sceneId,String paramStr,String createBy){
        DBTransaction trans =
            (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        //更新执行人为创建者
        String bSql = "UPDATE ODI11_SCENE_EXEC T SET T.CREATED_BY = '" + createBy + "',T.UPDATED_BY = '" + createBy
            + "' WHERE T.SCENE_ID = '" + sceneId + "' AND T.PARAMS = '" + paramStr + "'";

        try {
            stat.executeUpdate(bSql);
            trans.commit();
            stat.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
    
    public void checkEtlStep(String sceneId,Map paraMap){
        System.out.println("passEtlStepByParam");
        DBTransaction trans =
            (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        //查询所有正在等待执行的接口和对应的工作流组合
        String sql = "SELECT DISTINCT W.RUN_ID,W.ENTITY_PARENT,W.ODI_ARGS,T.OPEN_COM FROM WORKFLOW_ODI_STATUS W,DMS_WORKFLOW_STATUS T "
            + "WHERE W.RUN_ID = T.RUN_ID AND W.EXEC_STATUS = 'N' AND W.SCENE_ID = '" + sceneId + "'";
        WorkflowEngine wfe = new WorkflowEngine();
        try {
            ResultSet rs = stat.executeQuery(sql);
            while(rs.next()){
                String openCom = rs.getString("OPEN_COM");  
                String odiArgs = rs.getString("ODI_ARGS");
                String runId = rs.getString("RUN_ID");
                String entityParent = rs.getString("ENTITY_PARENT");
                if(this.compareParam(openCom, odiArgs, paraMap)){
                    System.out.println("*********************************SCCUSSE**************************************");
                    wfe.passEtlStep(runId, entityParent,odiArgs, sceneId);
                }
            }
            rs.close();
            stat.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public boolean compareParam(String openCom,String odiArgs,Map paraMap){
        //比较实体
        if(odiArgs != null && !"".equals(odiArgs)){
            if(!paraMap.containsValue(odiArgs)){
                return false;
            }    
        }
        
        //比较年场景版本
        openCom = openCom.substring(0, openCom.length() - 1);
        String[] str = openCom.split("#");
        int count = 0;
        for (int i = 0; i < str.length; i++) {
            String[] s = str[i].split(":");
            //s[0]:列名，s[1]：值 年份需要将FY15替换成2015格式
            if(s[1].startsWith("FY")){
                s[1] = s[1].replace("FY", "20");    
            }
            
            if(paraMap.containsValue(s[1])){
                count++;
            }
        }
        
        if(count == 3){
            return true;    
        }else{
            return false;   
        }
        
    }
    
    //检查系统接口运行状态，调用排队接口
    public void checkRunAndQueue(){
        ViewObject execVo =
            DmsUtils.getOdi11gApplicationModule().getOdi11SceneExecView();
        ViewCriteria vc = execVo.createViewCriteria();
        ViewCriteriaRow row = vc.createViewCriteriaRow();

        row.setAttribute("ExecStatus", " in ('R','Q','W')");
        row.setConjunction(row.VC_CONJ_AND);
        vc.addElement(row);
        execVo.applyViewCriteria(vc);
        execVo.executeQuery();
        if (execVo.hasNext()) {
            Row execRow = execVo.next();
            String sid = execRow.getAttribute("SceneId").toString();
            String status = this.queryStatus(execRow);
            if ("D,E".contains(status)) {
                if(this.getRunNum("QUEUE",this.getCategroy(sid)) > 0 && this.getRunNum("R",this.getCategroy(sid)) == 0){
                    this.executeNext(this.getCategroy(sid));    
                    //this.showQueueNum();
                }
            }
        }else{
            this.reStartQueue();
        }
        execVo.getViewCriteriaManager().setApplyViewCriteriaNames(null);
    }
    
    public void reStartQueue(){
        DBTransaction trans =
            (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        String sql = "SELECT DISTINCT QUEUE_CATEGORY FROM ODI11_SCENE";
        try {
            ResultSet rs = stat.executeQuery(sql);
            while(rs.next()){
                if(this.getRunNum("QUEUE",rs.getString("QUEUE_CATEGORY")) > 0 && this.getRunNum("R",rs.getString("QUEUE_CATEGORY")) == 0){
                    this.executeNext(rs.getString("QUEUE_CATEGORY"));    
                    //this.showQueueNum();
                }      
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private final boolean isRunning(String sceneId,
                              String params) throws MalformedURLException {
        boolean isRunning = false;
        ViewObject execVo =
            DmsUtils.getOdi11gApplicationModule().getOdi11SceneExecView();
        ViewCriteria vc = execVo.createViewCriteria();
        ViewCriteriaRow row = vc.createViewCriteriaRow();
        row.setAttribute("SceneId", "='" + sceneId + "'");

        if (params.length() > 0) {
            row.setAttribute("Params", "='" + params + "'");
        } else {
            row.setAttribute("Params", " is null");
        }
        row.setAttribute("ExecStatus", " in ('R','Q','W')");
        row.setConjunction(row.VC_CONJ_AND);
        vc.addElement(row);
        execVo.applyViewCriteria(vc);
        execVo.executeQuery();
        if (execVo.hasNext()) {
            Row execRow = execVo.next();
            String status = this.queryStatus(execRow);
            if ("R,Q,W".contains(status)) {
                isRunning = true;
            }
        }
        execVo.getViewCriteriaManager().setApplyViewCriteriaNames(null);
        return isRunning;
    }
    //获取执行状态

    private String queryStatus(Row execRow) {
        String status = "D";

        String sceneId = (String)execRow.getAttribute("SceneId");
        ViewObject vo =
            DmsUtils.getOdi11gApplicationModule().getOdi11SceneView();
        Row[] rows =
            vo.findByKey(new Key(new Object[] { sceneId, ADFContext.getCurrent().getLocale().toString() }),
                         1);
        if (rows.length > 0) {
            try{
            Row sceneRow = rows[0];
            RequestPortType requestPortType =
                this.getRequestPortType(sceneRow);
            OdiCredentialType odiCredentialType =
                this.getOdiCredentialType(sceneRow);
            OdiGetSessionsStatusRequest odiGetSessionsStatusRequest =
                new OdiGetSessionsStatusRequest();
            odiGetSessionsStatusRequest.setCredentials(odiCredentialType);
            odiGetSessionsStatusRequest.getSessionIds().add(Long.parseLong(execRow.getAttribute("SessionNum").toString()));
            OdiGetSessionsStatusResponse odiGetSessionsStatusResponse =
                requestPortType.getSessionStatus(odiGetSessionsStatusRequest);
            List<SessionStatusType> statuses =
                odiGetSessionsStatusResponse.getSessionStatusResponse();
            if (statuses.size() > 0) {
                status = statuses.get(0).getSessionStatus();
                String msg =
                    ObjectUtils.toString(statuses.get(0).getSessionMessage());
                execRow.setAttribute("ExecStatus", status);
                execRow.setAttribute("LogText",
                                     msg.length() <= 512 ? msg : (msg.substring(0,
                                                                                512)+"......"));
                if(this.hasException(ObjectUtils.toString(execRow.getAttribute("SessionNum")))){
                    execRow.setAttribute("HasException", "Y");
                }
                vo.getApplicationModule().getTransaction().commit();
                
            }
            }
            catch(Exception ex){
                if(ex.getMessage().contains("ODI-1701")){
                    execRow.remove();   
                    vo.getApplicationModule().getTransaction().commit();
                }
                this._logger.severe(ex);
            }
        }
        return status;
    }
    private boolean hasException(String sessionNum){
        boolean flag=false;
        String sql="select 1 from ODI11_SCENE_LOG t where t.session_num='"+sessionNum+"'";
        Statement stmt=DmsUtils.getOdi11gApplicationModule().getDBTransaction().createStatement(1);
        try {
            ResultSet rs=stmt.executeQuery(sql);
            if(rs.next()){
                flag=true;
            }
        } catch (SQLException e) {
            this._logger.severe(e);
        }
        return flag;
    }
    private void refreshStatus(String sceneId) throws MalformedURLException {
        ViewObject sceneVo =
            ADFUtils.findIterator("Odi11AuthedSceneViewIterator").getViewObject();
        ViewObject sceneExecVo =
            ADFUtils.findIterator("Odi11SceneExecViewIterator").getViewObject();
        String userId =
            ((Person)ADFContext.getCurrent().getSessionScope().get("cur_user")).getId();
        ViewCriteria vc = sceneExecVo.createViewCriteria();
        ViewCriteriaRow vcr = vc.createViewCriteriaRow();
        vcr.setAttribute("SceneId", "='" + sceneId + "'");
        //vcr.setAttribute("CreatedBy", "='" + userId + "'");
        vcr.setConjunction(vcr.VC_CONJ_AND);
        vc.addElement(vcr);
        sceneExecVo.applyViewCriteria(vc);
        sceneExecVo.executeQuery();
        sceneExecVo.getViewCriteriaManager().setApplyViewCriteriaNames(null);
        for (Row row : sceneExecVo.getAllRowsInRange()) {
            if ("R,Q,W".contains((String)row.getAttribute("ExecStatus"))) {
                queryStatus(row);
            }
        }
    }

    public void execute(ActionEvent actionEvent) throws MalformedURLException {
        ViewObject sceneVo =
            ADFUtils.findIterator("Odi11AuthedSceneViewIterator").getViewObject();
        Row[] rows =
            ADFUtils.findIterator("Odi11SceneParamExViewIterator").getAllRowsInRange();
        Map params = new LinkedHashMap();
        for (Row row : rows) {
            params.put(row.getAttribute("PName"), row.getAttribute("value"));
        }
        
        String sid = sceneVo.getCurrentRow().getAttribute("Id").toString();
        if(this.getRunNum("R",this.getCategroy(sid))==0){
            this.run(sceneVo.getCurrentRow(), params,null);                 
        }else{
            this.insertExecQueue(sceneVo.getCurrentRow(), params);   
            this.showQueueNum();
            
            //检查全部ODI状态，调用排队
            this.checkRunAndQueue();
            
            this.showStatusPopup(actionEvent);
            this.popup.cancel();
        }
        
    }

    public void setParamTable(RichTable paramTable) {
        this.paramTable = paramTable;
    }

    public RichTable getParamTable() {
        return paramTable;
    }

    public void showStatusPopup(ActionEvent actionEvent) throws MalformedURLException {
        this.checkRunAndQueue();
        this.refreshStatus((String)ADFUtils.findIterator("Odi11AuthedSceneViewIterator").getViewObject().getCurrentRow().getAttribute("Id"));
        this.showStatus();
        RichPopup.PopupHints hint = new RichPopup.PopupHints();
        this.statusPopup.show(hint);
    }

    private void showStatus() {
        ViewObject sceneVo =
            ADFUtils.findIterator("Odi11AuthedSceneViewIterator").getViewObject();
        ViewObject sceneExecVo =
            ADFUtils.findIterator("Odi11SceneExecViewIterator").getViewObject();
        String sceneId = (String)sceneVo.getCurrentRow().getAttribute("Id");
        String userId =
            ((Person)ADFContext.getCurrent().getSessionScope().get("cur_user")).getId();
        ViewCriteria vc = sceneExecVo.createViewCriteria();
        ViewCriteriaRow vcr = vc.createViewCriteriaRow();
        vcr.setAttribute("SceneId", "='" + sceneId + "'");
        vc.addElement(vcr);
        //vcr.setAttribute("CreatedBy", "='" + userId + "'");
        vcr.setConjunction(vcr.VC_CONJ_AND);
        vc.addElement(vcr);
        sceneExecVo.applyViewCriteria(vc);
        sceneExecVo.executeQuery();
        sceneExecVo.getViewCriteriaManager().setApplyViewCriteriaNames(null);
    }

    public void setStatusPopup(RichPopup statusPopup) {
        this.statusPopup = statusPopup;
    }

    public RichPopup getStatusPopup() {
        return statusPopup;
    }

    public void showExceptionData(ActionEvent actionEvent) {
        RichPopup.PopupHints hint=new RichPopup.PopupHints();
        this.exceptionPopup.show(hint);
    }

    public void setExceptionPopup(RichPopup exceptionPopup) {
        this.exceptionPopup = exceptionPopup;
    }

    public RichPopup getExceptionPopup() {
        return exceptionPopup;
    }
}
