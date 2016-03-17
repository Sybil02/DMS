package infa;

import common.ADFUtils;

import common.DmsUtils;
import common.JSFUtils;

import dms.login.Person;

import infa.dataintegration.types.DIServerDetails;
import infa.dataintegration.types.DIServiceInfo;
import infa.dataintegration.types.ETaskRunMode;
import infa.dataintegration.types.LoginRequest;
import infa.dataintegration.types.Parameter;
import infa.dataintegration.types.ParameterArray;
import infa.dataintegration.types.SessionHeader;
import infa.dataintegration.types.TypeGetWorkflowDetailsExRequest;
import infa.dataintegration.types.TypeStartWorkflowExRequest;
import infa.dataintegration.types.TypeStartWorkflowExResponse;
import infa.dataintegration.types.WorkflowDetails;
import infa.dataintegration.types.WorkflowRequest;
import infa.dataintegration.ws.DataIntegrationClient;
import infa.dataintegration.ws.DataIntegrationInterface;

import infa.dataintegration.ws.Fault;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.List;

import javax.faces.event.ActionEvent;

import javax.xml.namespace.QName;
import javax.xml.ws.Holder;
import javax.xml.ws.Service;

import oracle.adf.share.ADFContext;
import oracle.adf.view.rich.component.rich.data.RichTable;
import oracle.adf.view.rich.component.rich.data.RichTree;

import oracle.adf.view.rich.context.AdfFacesContext;

import oracle.jbo.Row;
import oracle.jbo.ViewObject;

import org.apache.myfaces.trinidad.event.SelectionEvent;
import org.apache.myfaces.trinidad.model.RowKeySet;

public class InfaIndexBean {
    
    private InfaCatTreeModel infaTreeModel;
    private RichTable wfTable;

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
        
        //判断是否在执行
        if(isRunning(execRow)){
            JSFUtils.addFacesInformationMessage("接口正在运行！");
            return;
        }
        
        //参数
        String name = execRow.getAttribute("WorkflowName").toString();
        System.out.println(name+"......................");
        if(name.equals("Workflow2")){
            System.out.println("set the param .......................");
            this.showParamPop();
            this.startWorkflowEx(execRow, this.getParameter(execRow));
            
        }else{
            
            this.startWorkflowEx(execRow, null);
            
        }
        
    }
    
    private void showParamPop(){
        
    }
    
    private ParameterArray getParameter(Row execRow){
        ParameterArray paraArray = new ParameterArray();
        Parameter p1 = new Parameter();
        p1.setName("$$DNAME");
        p1.setValue("'SALES'");
        p1.setScope("Global");
        paraArray.getParameters().add(p1);
        Parameter p2 = new Parameter();
        p2.setName("$$Job");
        p2.setValue("'SALESMAN'");
        p2.setScope("Global");
        paraArray.getParameters().add(p2);
        return paraArray;    
    }
    
    private void startWorkflowEx(Row execRow,ParameterArray paramArray){
        
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
        
        if(paramArray.getParameters().size() > 0){
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
            this.createExecRecord(execRow, paramArray, response.getRunId());
        } catch (Fault e) {
            e.printStackTrace();
        }
        
        this.logout(sessionId);
        
    }
    
    private void createExecRecord(Row execRow,ParameterArray param,int runId){
        
    }
    
    private void getWfDetails(Row execRow,String sessionId,int runId){
        TypeGetWorkflowDetailsExRequest detailsReq = new TypeGetWorkflowDetailsExRequest();
        
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
            wfLog.getStartupTime();

        } catch (Fault e) {
            e.printStackTrace();
        }
        
    }
    
    private int[] getRunId(Row execRow){
        String userId = ((Person)ADFContext.getCurrent().getSessionScope().get("cur_user")).getId();
        String wfId = execRow.getAttribute("Id").toString();
        return null;    
    }
    
    private void logout(String sessionId){
        
    }
    
    private boolean hasParameter(Row execRow){
        boolean flag = false;    
        
        
        return flag;
    }
    
    private boolean isRunning(Row execRow){
        boolean flag = false;
        
        
        
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
    
}
