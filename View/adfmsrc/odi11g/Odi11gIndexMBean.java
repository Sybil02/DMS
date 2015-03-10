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

public class Odi11gIndexMBean {
    private Odi11gCatTreeModel model;
    private Map valueList = new HashMap();
    private static ADFLogger _logger =
        ADFLogger.createADFLogger(Odi11gIndexMBean.class);
    private RichTable sceneTable;
    private RichPopup popup;
    private RichTable paramTable;
    private RichPopup statusPopup;

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
            ViewObject vo =
                ADFUtils.findIterator("Odi11AuthedSceneViewIterator").getViewObject();
            vo.setNamedWhereClauseParam("catId", rowData.getId());
            vo.executeQuery();
            AdfFacesContext.getCurrentInstance().addPartialTarget(this.sceneTable);
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
                this.run(sceneVo.getCurrentRow(), new HashMap());
            }
        }
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

    private void run(Row sceneRow, Map params) throws MalformedURLException {
        StringBuffer parmStr = new StringBuffer();
        for (Object v : params.values()) {
            parmStr.append("#").append(v);
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
            this.showStatus();
            RichPopup.PopupHints hint = new RichPopup.PopupHints();
            this.statusPopup.show(hint);
        } catch (IndexOutOfBoundsException e) {
            this._logger.severe("Agent or Workrepository may not exits!");
            this._logger.severe(e);
        }
    }

    private boolean isRunning(String sceneId,
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
        vcr.setAttribute("CreatedBy", "='" + userId + "'");
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
        this.run(sceneVo.getCurrentRow(), params);
    }

    public void setParamTable(RichTable paramTable) {
        this.paramTable = paramTable;
    }

    public RichTable getParamTable() {
        return paramTable;
    }

    public void showStatusPopup(ActionEvent actionEvent) throws MalformedURLException {
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
        vcr.setAttribute("CreatedBy", "='" + userId + "'");
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
}
