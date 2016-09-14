package infa;

import common.ADFUtils;

import common.DmsUtils;
import common.JSFUtils;

import dms.login.Person;

import infa.dataintegration.types.DIServerDate;
import infa.dataintegration.types.DIServerDetails;
import infa.dataintegration.types.DIServiceInfo;
import infa.dataintegration.types.ETaskRunMode;
import infa.dataintegration.types.EWorkflowRunStatus;
import infa.dataintegration.types.GetWorkflowLogRequest;
import infa.dataintegration.types.Log;
import infa.dataintegration.types.LoginRequest;
import infa.dataintegration.types.Parameter;
import infa.dataintegration.types.ParameterArray;
import infa.dataintegration.types.SessionHeader;
import infa.dataintegration.types.TypeGetWorkflowDetailsExRequest;
import infa.dataintegration.types.TypeStartWorkflowExRequest;
import infa.dataintegration.types.TypeStartWorkflowExResponse;
import infa.dataintegration.types.VoidRequest;
import infa.dataintegration.types.WorkflowDetails;
import infa.dataintegration.types.WorkflowRequest;
import infa.dataintegration.ws.DataIntegrationClient;
import infa.dataintegration.ws.DataIntegrationInterface;

import infa.dataintegration.ws.Fault;

import java.io.BufferedReader;
import java.io.File;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import java.io.UnsupportedEncodingException;

import java.net.MalformedURLException;
import java.net.URL;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import java.util.Map;

import javax.faces.event.ActionEvent;

import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import javax.xml.namespace.QName;
import javax.xml.ws.Holder;
import javax.xml.ws.Service;

import oracle.adf.share.ADFContext;
import oracle.adf.view.rich.component.rich.RichPopup;
import oracle.adf.view.rich.component.rich.data.RichTable;
import oracle.adf.view.rich.component.rich.data.RichTree;

import oracle.adf.view.rich.component.rich.input.RichSelectOneChoice;
import oracle.adf.view.rich.context.AdfFacesContext;

import oracle.jbo.Row;
import oracle.jbo.ViewCriteria;
import oracle.jbo.ViewCriteriaRow;
import oracle.jbo.ViewObject;

import oracle.jbo.server.DBTransaction;

import org.apache.commons.lang.ObjectUtils;
import org.apache.myfaces.trinidad.event.SelectionEvent;
import org.apache.myfaces.trinidad.model.RowKeySet;

public class InfaIndexBean {
    
    private InfaCatTreeModel infaTreeModel;
    private RichTable wfTable;
    private RichPopup detailsPop;
    private RichPopup paramPop;
    
    List<InfaParamBean> paramList = new ArrayList<InfaParamBean>();
    Map<String,RichSelectOneChoice> paraSocMap = new LinkedHashMap<String,RichSelectOneChoice>();
    private RichTable statusTable;
    private RichPopup logPop;
    private String firstName;
    String logMessage = null ;
    
    public InfaIndexBean() {
        super();
    }

    public void setInfaTreeModel(InfaCatTreeModel infaTreeModel) {
        this.infaTreeModel = infaTreeModel;
    }

    public InfaCatTreeModel getInfaTreeModel() {
        if(this.infaTreeModel == null){
            infaTreeModel = new InfaCatTreeModel();        
        }
        return infaTreeModel;
    }

    public void infaTreeSelection(SelectionEvent event) {
        RichTree tree = (RichTree)event.getSource();
        RowKeySet rsk = event.getAddedSet();
        if(rsk != null){
            if(rsk.size()==0){
                return;    
            }    
            Object rowKey = null;
            InfaCatTreeItem treeItem = null;
            rowKey = rsk.iterator().next();
            tree.setRowKey(rowKey);
            treeItem = (InfaCatTreeItem)tree.getRowData();
            String catId = treeItem.getId();
            ViewObject vo = ADFUtils.findIterator("InfaUserWorkflowVOIterator").getViewObject();
            vo.setNamedWhereClauseParam("catId", catId);
            vo.executeQuery();
            AdfFacesContext.getCurrentInstance().addPartialTarget(this.wfTable);
        }
    }

    public void setWfTable(RichTable wfTable) {
        this.wfTable = wfTable;
    }

    public RichTable getWfTable() {
        return wfTable;
    }

    //初始化调用workflow
    public void workflowExecute(ActionEvent actionEvent) {
        ViewObject vo = ADFUtils.findIterator("InfaUserWorkflowVOIterator").getViewObject();
        Row execRow = vo.getCurrentRow();
        //参数
        this.paramList.clear();
        this.paraSocMap.clear();
        if(this.hasParameter(execRow)){
            this.showParamPop();
        }else{
            this.startWorkflowEx(execRow, null);
        }
        
    }
    
    public void workflowExecByParam(ActionEvent actionEvent) {
        ViewObject vo = ADFUtils.findIterator("InfaUserWorkflowVOIterator").getViewObject();
        Row execRow = vo.getCurrentRow();
        
        for(InfaParamBean ipb : this.paramList){
            if(ipb.getChoiceValue() == null){
                JSFUtils.addFacesInformationMessage("参数不能为空！");
                return;    
            }    
        }
        
        ParameterArray param = this.getParameter();
        this.startWorkflowEx(execRow, param);
        this.paramPop.cancel();
    }
    
    private void showParamPop(){
        this.firstName = "";
        RichPopup.PopupHints hints = new RichPopup.PopupHints();
        this.paramPop.show(hints);
    }
    
    public void paramSelectListener(ValueChangeEvent valueChangeEvent) {
        System.out.println(valueChangeEvent.getNewValue()+"........");
        
        RichSelectOneChoice pSoc =
            (RichSelectOneChoice)valueChangeEvent.getSource();
        for(Object key : this.paraSocMap.keySet()){
            if(pSoc.equals(this.paraSocMap.get(key))){
                for(InfaParamBean ipb : this.paramList){
                    if(ipb.getPName().equals(key.toString())){
                        ipb.setChoiceValue("'" + valueChangeEvent.getNewValue().toString() + "'");
                    }
                }
                
            }  
        }
        
    }
    
    private ParameterArray getParameter(){
        ParameterArray paraArray = new ParameterArray();
            
        for(InfaParamBean ipb : this.paramList){
            Parameter pam = new Parameter();
            pam.setName(ipb.getPName());
            pam.setScope(ipb.getPScope());
            pam.setValue(ipb.getChoiceValue());
            paraArray.getParameters().add(pam);
        }
            
        return paraArray;    
    }
    
    private String getParamName(){
        String pnStr = "";
        for(InfaParamBean ipb : this.paramList){
            String value = ipb.getChoiceValue();
            List<SelectItem> ls = ipb.getValuesList();
            for(SelectItem item : ls){
                if(item.getValue().equals(value)){
                    pnStr = pnStr + "#" + item.getLabel();
                    break;    
                }    
            }
        }
        return pnStr;    
    }
    
    private void startWorkflowEx(Row execRow,ParameterArray paramArray){
        
       // String paramStr = "";
        String paramNameStr = "";
        if(paramArray != null && paramArray.getParameters().size() > 0){
         //   for(Parameter pat : paramArray.getParameters()){
//                paramStr = paramStr + "#" + pat.getName() + ":" + pat.getValue();
//            }
            paramNameStr = this.getParamName();
        }
        
        //判断是否在执行
        if(isRunning(execRow,paramNameStr)){
            JSFUtils.addFacesInformationMessage("接口正在运行！");
            return;
        }
        
        String folderName = execRow.getAttribute("Foldername").toString();
        String wfName = execRow.getAttribute("WorkflowName").toString();
        String runMode = execRow.getAttribute("RequestMode").toString();
        String domain = execRow.getAttribute("RepDomain").toString();
        String isServer = execRow.getAttribute("ServiceName").toString();
        String sessionId = this.getLogin(execRow);
        
        TypeStartWorkflowExRequest workflow = new TypeStartWorkflowExRequest();
        workflow.setFolderName(folderName);
        workflow.setWorkflowName(wfName);
        workflow.setReason("INFA-");
        
        if(paramArray != null && paramArray.getParameters().size() > 0){
            workflow.setParameters(paramArray);    
        }

        if(runMode.equals("NORMAL")){
            workflow.setRequestMode(ETaskRunMode.NORMAL); 
        }else{
            workflow.setRequestMode(ETaskRunMode.RECOVERY);
        }
        
        DIServiceInfo dinfo = new DIServiceInfo();
        dinfo.setDomainName(domain);
        dinfo.setServiceName(isServer);
        workflow.setDIServiceInfo(dinfo);
        
        System.out.println("set:"+sessionId);
        SessionHeader sHead = new SessionHeader();
        sHead.setSessionId(sessionId);
        
        DataIntegrationInterface DIService = this.getDIService(execRow);

        try {
            TypeStartWorkflowExResponse response = DIService.startWorkflowEx(workflow, sHead);
            System.out.println("StartWorkflow Return : " + response.getRunId());
            this.createExecRecord(execRow, paramNameStr, response.getRunId());
        } catch (Fault e) {
            JSFUtils.addFacesErrorMessage(e.getFaultInfo().getExtendedDetails());
            e.printStackTrace();
        }
        
        this.logout(execRow,sessionId);
        this.paramPop.cancel();
        RichPopup.PopupHints hints = new RichPopup.PopupHints();
        this.detailsPop.show(hints);
        
    }
    
    private void createExecRecord(Row execRow,String paramStr,int runId){
        String wfId = execRow.getAttribute("Id").toString();
        
        ViewObject vo = DmsUtils.getInfaApplicationModule().getInfaWorkflowExecVO();
        Row row = vo.createRow();
        row.setAttribute("WorkflowId", wfId);
        row.setAttribute("Params", paramStr);
        row.setAttribute("ExecStatus", "R");
        row.setAttribute("RunId", runId);
        
        vo.insertRow(row);
        vo.getApplicationModule().getTransaction().commit();
    }
    
    private void getWfDetails(Row execRow,String sessionId,int runId){
        TypeGetWorkflowDetailsExRequest detailsReq = new TypeGetWorkflowDetailsExRequest();
        
        String wfId = execRow.getAttribute("Id").toString();
        String folderName = execRow.getAttribute("Foldername").toString();
        String wfName = execRow.getAttribute("WorkflowName").toString();
        String domain = execRow.getAttribute("RepDomain").toString();
        String isServer = execRow.getAttribute("ServiceName").toString();
        
        detailsReq.setFolderName(folderName);
        detailsReq.setWorkflowName(wfName);
        
        DIServiceInfo dinfo = new DIServiceInfo();
        dinfo.setDomainName(domain);
        dinfo.setServiceName(isServer);
        detailsReq.setDIServiceInfo(dinfo);
        
        detailsReq.setWorkflowRunId(runId);
        
        SessionHeader sHead = new SessionHeader();
        sHead.setSessionId(sessionId);
        
        DataIntegrationInterface DIService = this.getDIService(execRow);

        try {
            
            DIServerDetails wfLog = DIService.getWorkflowDetailsEx(detailsReq, sHead);
            wfLog.getWorkflowDetails();
            for(WorkflowDetails wfDt : wfLog.getWorkflowDetails()){
                DIServerDate date = wfDt.getEndTime();
                String dateStr = date.getYear() + "/" +date.getMonth() + "/" + date.getDate()
                                    + " " + date.getHours() + ":" +date.getMinutes() + ":" + date.getSeconds();
        
                EWorkflowRunStatus ws = wfDt.getWorkflowRunStatus();
                String st = ws.value();
                
                String sql = "";
                if(st.equals("SUCCEEDED")){
                    sql = "UPDATE INFA_WORKFLOW_EXEC T SET T.EXEC_STATUS = 'D',T.FINISH_TIME = TO_DATE('" + dateStr + "','yyyy/mm/dd hh24:mi:ss') " +
                        "WHERE T.RUN_ID = '" + runId + "' AND T.WORKFLOW_ID = '" + wfId + "'";
                    DmsUtils.getInfaApplicationModule().getTransaction().executeCommand(sql);
                    DmsUtils.getInfaApplicationModule().getTransaction().commit();
                }else if(st.equals("FAILED")){
                
                    GetWorkflowLogRequest logReq = new GetWorkflowLogRequest();
                    logReq.setDIServiceInfo(dinfo);
                    logReq.setFolderName(folderName);
                    logReq.setTimeout(100);
                    logReq.setWorkflowName(wfName);
                    logReq.setWorkflowRunId(runId);
                    Log log = DIService.getWorkflowLog(logReq, sHead);
                    String logStr = log.getBuffer();
                    String filePath = DmsUtils.writeToTxT(logStr, "INFA");
                
                    sql = "UPDATE INFA_WORKFLOW_EXEC T SET T.EXEC_STATUS = 'E',T.FINISH_TIME = TO_DATE('" + dateStr + "','yyyy/mm/dd hh24:mi:ss')" +
                        ",LOG_TEXT='" + filePath +"' WHERE T.RUN_ID = '" + runId + "' AND T.WORKFLOW_ID = '" + wfId + "'";
                    DmsUtils.getInfaApplicationModule().getTransaction().executeCommand(sql);
                    DmsUtils.getInfaApplicationModule().getTransaction().commit();
                    
                }else{
                    
                }
                System.out.println(wfDt.getRunErrorMessage());
                System.out.println(wfDt.getRunErrorCode());
            }
            
        } catch (Fault e) {
            JSFUtils.addFacesErrorMessage(e.getFaultInfo().getExtendedDetails());
            e.printStackTrace();
        }
        
    }
    
    private List<String> getRunId(Row execRow){
        String userId = ((Person)ADFContext.getCurrent().getSessionScope().get("cur_user")).getId();
        String wfId = execRow.getAttribute("Id").toString();
        
        ViewObject vo = DmsUtils.getInfaApplicationModule().getInfaWorkflowExecVO();
        ViewCriteria vc = vo.createViewCriteria();
        ViewCriteriaRow vr = vc.createViewCriteriaRow();
        vr.setAttribute("WorkflowId", "='" + wfId + "'");
        vr.setAttribute("ExecStatus", "='R'");
        vr.setConjunction(vr.VC_CONJ_AND);
        vc.addElement(vr);
        vo.applyViewCriteria(vc);
        vo.executeQuery();
        
        List<String> runIdList = new ArrayList<String>();
        while(vo.hasNext()){
            String runId = (String)vo.next().getAttribute("RunId");
            runIdList.add(runId);
        }
        vo.getViewCriteriaManager().setApplyViewCriteriaName(null);
        return runIdList;    
    }
    
    private void logout(Row execRow,String sessionId){
         DataIntegrationInterface DIService = this.getDIService(execRow);
         VoidRequest voidReq = new VoidRequest();
         SessionHeader sHead = new SessionHeader();
         sHead.setSessionId(sessionId);
        try {
            DIService.logout(voidReq, sHead);
        } catch (Fault e) {
            JSFUtils.addFacesErrorMessage(e.getFaultInfo().getExtendedDetails());
            e.printStackTrace();
        }
    }
    
    private boolean hasParameter(Row execRow){
        boolean flag = false;    
        
        String wfId = execRow.getAttribute("Id").toString();
        ViewObject vo = DmsUtils.getInfaApplicationModule().getInfaWorkflowParamQuery();
        
        ViewCriteria vc = vo.createViewCriteria();
        ViewCriteriaRow vr = vc.createViewCriteriaRow();
        vr.setAttribute("WorkflowId", "='" + wfId + "'");
        vr.setConjunction(vr.VC_CONJ_AND);
        vc.addElement(vr);
        vo.applyViewCriteria(vc);

        System.out.println(vo.getQuery());
        vo.executeQuery();

        while(vo.hasNext()){
            String pId =  vo.next().getAttribute("ParamsId").toString();
            this.initParamSoc(pId);
            flag = true;
        }
        
        vo.getViewCriteriaManager().setApplyViewCriteriaNames(null);

        return flag;
    }
    
    private void initParamSoc(String pId){
        DBTransaction trans = DmsUtils.getInfaApplicationModule().getDBTransaction();
        Statement stat = trans.createStatement(1);
        String locale = ((Person)ADFContext.getCurrent().getSessionScope().get("cur_user")).getLocale();
        
        StringBuffer sql = new StringBuffer("SELECT I.VS_ID,I.P_ALIAS,I.P_NAME,I.P_SCOPE,V.SOURCE FROM INFA_PARAMETER I,DMS_VALUE_SET V ");
        sql.append("WHERE I.VS_ID = V.ID AND I.LOCALE = V.LOCALE ");
        sql.append("AND I.LOCALE = '").append(locale).append("' ");
        sql.append("AND I.ID = '").append(pId).append("'");
        
        ResultSet rs = null;
        try {
            rs = stat.executeQuery(sql.toString());
            if(rs.next()){
                InfaParamBean ipb = new InfaParamBean(rs.getString("P_ALIAS"),rs.getString("P_NAME"),rs.getString("P_SCOPE"),rs.getString("SOURCE"));
                ipb.setValuesList(this.getValues(rs.getString("VS_ID"),rs.getString("SOURCE")));
                this.paramList.add(ipb);
            }
            rs.close();
            stat.close();
        } catch (SQLException e) {
            JSFUtils.addFacesErrorMessage(ObjectUtils.toString(e.getErrorCode()));
            e.printStackTrace();
        }
    }
    
    private List<SelectItem> getValues(String valueSetId,String source){
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT T.CODE,T.MEANING FROM \"").append(source).append("\" T").append("  WHERE T.LOCALE='").append(ADFContext.getCurrent().getLocale()).append("'");
        sql.append("  AND EXISTS (SELECT 1 FROM DMS_USER_VALUE_V V")
                .append("  WHERE V.USER_ID = '")
                .append(((Person)ADFContext.getCurrent().getSessionScope().get("cur_user")).getId())
                .append("'")
                .append("  AND V.VALUE_SET_ID='")
                .append(valueSetId).append("'")
                .append("  AND V.VALUE_ID = T.CODE)");
        sql.append(" AND T.ENABLED='Y'  ORDER BY T.IDX");
        ViewObject vo =
            DmsUtils.getDmsApplicationModule().createViewObjectFromQueryStmt(null,
                                                                             sql.toString());
        vo.executeQuery();
        List<SelectItem> vsList = new ArrayList<SelectItem>();
        while (vo.hasNext()) {
            Row row = vo.next();
            SelectItem item = new SelectItem();
            item.setLabel(ObjectUtils.toString(row.getAttribute("MEANING")));
            item.setValue(ObjectUtils.toString(row.getAttribute("CODE")));
            vsList.add(item);
        }
        
        vo.remove();
        
        return vsList;
    }
    
    private boolean isRunning(Row execRow,String paramStr){
        boolean flag = false;
        
        List<String> runIdList = this.getRunId(execRow);
        
        if(runIdList.size() > 0){
            String sessionId = this.getLogin(execRow);
            for(String runId : runIdList){
                this.getWfDetails(execRow, sessionId, Integer.parseInt(runId));
            }    
            this.logout(execRow,sessionId);
        }
        
        String wfId = execRow.getAttribute("Id").toString();
        ViewObject vo = DmsUtils.getInfaApplicationModule().getInfaWorkflowExecVO();
        ViewCriteria vc = vo.createViewCriteria();
        ViewCriteriaRow vr = vc.createViewCriteriaRow();
        vr.setAttribute("WorkflowId", "='"+wfId+"'");
        if(paramStr != null && !"".equals(paramStr)){
            vr.setAttribute("Params", paramStr);   
        }
        vr.setAttribute("ExecStatus", "R");
        vr.setConjunction(vr.VC_CONJ_AND);
        vc.addElement(vr);
        vo.applyViewCriteria(vc);
        vo.executeQuery();
        
        if(vo.hasNext()){
            flag = true;    
        }
        
        vo.getViewCriteriaManager().setApplyViewCriteriaName(null);
        
        return flag;
    }
    
    private String getLogin(Row execRow){
        String sessionId = "";
        
        String domain = execRow.getAttribute("RepDomain").toString();
        String repName = execRow.getAttribute("RepName").toString();
        String userName = execRow.getAttribute("UserName").toString();
        String passWord = execRow.getAttribute("Password").toString();
        System.out.println(domain + ":" + repName + ":" + userName + ":" + passWord);
        
        LoginRequest loginReq = new LoginRequest();
        loginReq.setRepositoryDomainName(domain);
        loginReq.setRepositoryName(repName);
        loginReq.setUserName(userName);
        loginReq.setPassword(passWord);
        
        SessionHeader sessionHeader = new SessionHeader();
        Holder<SessionHeader> Context =
            new Holder<SessionHeader>(sessionHeader);
        
        DataIntegrationInterface DIService = this.getDIService(execRow);

        try {
            sessionId = DIService.login(loginReq, Context);
        } catch (Fault e) {
            JSFUtils.addFacesErrorMessage(e.getFaultInfo().getExtendedDetails());
            e.printStackTrace();
        }
        System.out.println("login success:"+sessionId);
        return sessionId;    
    }
    
    private DataIntegrationInterface getDIService(Row execRow){
        String protocol = execRow.getAttribute("Protocol").toString();
        String host = execRow.getAttribute("ServerHost").toString();
        String port = execRow.getAttribute("ServerPort").toString();
        
        //"http://infa-pc:7334/wsh/services/BatchServices/DataIntegration?WSDL"
        String uri = protocol + "://" + host + ":"
            + port + "/wsh/services/BatchServices/DataIntegration?WSDL";
        String sapce = "http://www.informatica.com/wsh";
        System.out.println(uri);
        
        URL url = null;
        try {
            url = new URL(uri);
        } catch (MalformedURLException e) {
            JSFUtils.addFacesErrorMessage("WEBSERVICE URL ERROR!");
            e.printStackTrace();
        }
        
        DataIntegrationInterface diService = null;
        
        if(url != null){
            QName qname = new QName(sapce,"DataIntegrationService");
            Service service = Service.create(url, qname);
            diService = service.getPort(new QName(sapce,"DataIntegration"), 
                                                               DataIntegrationInterface.class);
        }else{
            JSFUtils.addFacesErrorMessage("WEBSERVICE URL ERROR!");
        }
        
        return diService;    
    }

    public void setDetailsPop(RichPopup detailsPop) {
        this.detailsPop = detailsPop;
    }

    public RichPopup getDetailsPop() {
        return detailsPop;
    }

    public void showStatusPop(ActionEvent actionEvent) {
        Row execRow = ADFUtils.findIterator("InfaUserWorkflowVOIterator").getViewObject().getCurrentRow();
        
        List<String> runIdList = this.getRunId(execRow);
        
        if(runIdList.size() > 0){
            String sessionId = this.getLogin(execRow);
            for(String runId : runIdList){
                this.getWfDetails(execRow, sessionId, Integer.parseInt(runId));
            }    
            this.logout(execRow,sessionId);
        }
        
        String wfId = execRow.getAttribute("Id").toString();
        String userId = ((Person)ADFContext.getCurrent().getSessionScope().get("cur_user")).getId();
        ViewObject vo = DmsUtils.getInfaApplicationModule().getInfaWorkflowExecVO();
        ViewCriteria vc = vo.createViewCriteria();
        ViewCriteriaRow vr = vc.createViewCriteriaRow();
        vr.setAttribute("WorkflowId", "='" + wfId + "'");
        vr.setAttribute("CreatedBy", "='" + userId + "'");
        vr.setConjunction(vr.VC_CONJ_AND);
        vc.addElement(vr);
        vo.applyViewCriteria(vc);
        vo.executeQuery();
        //vo.getViewCriteriaManager().setApplyViewCriteriaName(null);
        AdfFacesContext.getCurrentInstance().addPartialTarget(this.statusTable);
        
        RichPopup.PopupHints hints = new RichPopup.PopupHints();
        this.detailsPop.show(hints);
    }

    public void setParamList(List<InfaParamBean> paramList) {
        this.paramList = paramList;
    }

    public List<InfaParamBean> getParamList() {
        return paramList;
    }

    public void setParaSocMap(Map<String, RichSelectOneChoice> paraSocMap) {
        this.paraSocMap = paraSocMap;
    }

    public Map<String, RichSelectOneChoice> getParaSocMap() {
        return paraSocMap;
    }

    public void setParamPop(RichPopup paramPop) {
        this.paramPop = paramPop;
    }

    public RichPopup getParamPop() {
        return paramPop;
    }

    public void setStatusTable(RichTable statusTable) {
        this.statusTable = statusTable;
    }

    public RichTable getStatusTable() {
        return statusTable;
    }

    public void showLogTxt(ActionEvent actionEvent){
        this.logMessage = null;
        ViewObject vo = ADFUtils.findIterator("InfaWorkflowExecVOIterator").getViewObject();
        Row logRow = vo.getCurrentRow();
        Object obj = logRow.getAttribute("LogText");
        if(obj == null){
            return;    
        }
        String filePath = obj.toString();
        File file = new File(filePath);
        System.out.println(file.getAbsolutePath());
        if(file.exists() && file.isFile()){
            InputStreamReader isr;
            try {
                isr = new InputStreamReader(new FileInputStream(file),"GBK");
                BufferedReader br = new BufferedReader(isr);
                String temp =  null;
                while((temp = br.readLine()) != null){
                    this.logMessage = this.logMessage + temp + "\n";    
                }
            } catch (UnsupportedEncodingException e) {
                JSFUtils.addFacesErrorMessage("UnsupportedEncodingException:");
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                JSFUtils.addFacesErrorMessage("FileNotFoundException");
                e.printStackTrace();
            } catch (IOException e) {
                JSFUtils.addFacesErrorMessage("IOException");
                e.printStackTrace();
            }
        }
        
        RichPopup.PopupHints hints = new RichPopup.PopupHints();
        this.logPop.show(hints);
        
    }

    public void setLogPop(RichPopup logPop) {
        this.logPop = logPop;
    }

    public RichPopup getLogPop() {
        return logPop;
    }

    public void setLogMessage(String logMessage) {
        this.logMessage = logMessage;
    }

    public String getLogMessage() {
        return logMessage;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getFirstName() {
        return firstName;
    }
}
