package dcm;

import common.JSFUtils;

import dcm.template.TemplateTreeItem;
import dcm.template.TemplateTreeModel;

import dms.dynamicShell.TabContext;

import infa.dataintegration.types.DIServiceInfo;
import infa.dataintegration.types.ETaskRunMode;
import infa.dataintegration.types.LoginRequest;
import infa.dataintegration.types.SessionHeader;
import infa.dataintegration.types.WorkflowRequest;
import infa.dataintegration.ws.DataIntegrationInterface;

import infa.dataintegration.ws.Fault;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.HashMap;
import java.util.Map;

import javax.faces.context.FacesContext;

import javax.faces.event.ActionEvent;

import javax.xml.namespace.QName;
import javax.xml.ws.Holder;
import javax.xml.ws.Service;

import oracle.adf.share.ADFContext;
import oracle.adf.share.logging.ADFLogger;

import oracle.adf.view.rich.component.rich.data.RichTree;
import oracle.adf.view.rich.component.rich.layout.RichPanelStretchLayout;

import oracle.adf.view.rich.component.rich.nav.RichCommandButton;

import org.apache.myfaces.trinidad.event.SelectionEvent;
import org.apache.myfaces.trinidad.model.CollectionModel;
import org.apache.myfaces.trinidad.model.RowKeySet;

public class DcmIndexBean {
    private TemplateTreeModel model;
    private static ADFLogger _logger =
        ADFLogger.createADFLogger(DcmIndexBean.class);
    private RichCommandButton testConn;

    public void setModel(TemplateTreeModel model) {
        this.model = model;
    }

    public TemplateTreeModel getModel() {
        if (null == this.model) {
            model = new TemplateTreeModel();
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
            TemplateTreeItem rowData = null;
            rowKey = rks.iterator().next();
            tree.setRowKey(rowKey);
            rowData = (TemplateTreeItem)tree.getRowData();
            try {
                if( TabContext.getCurrentInstance().getSelectedTabIndex()!=-1){
                    TabContext.getCurrentInstance().removeCurrentTab();
                }
                if(TemplateTreeItem.TYPE_TEMPLATE.equals(rowData.getType())){
                    Map params=new HashMap();
                    params.put("curTemplateId",rowData.getId());
                    TabContext.getCurrentInstance().addTab(rowData.getLabel(), "/WEB-INF/dcmData/data_display_tsk.xml#data_display_tsk",params);
                }
                
            } catch (TabContext.TabOverflowException e) {
                _logger.severe(e);
            }
        }
    }
    
    
    
    /**************************************************************/
    
    private String httpStr ;
    private String prot;
    private String host;
    private String domain;
    private String rep;
    private String userName;
    private String pwd;
    private String webName;
    private String sessionId;


    public void setHttpStr(String httpStr) {
        this.httpStr = httpStr;
    }

    public String getHttpStr() {
        return httpStr;
    }

    public void setProt(String prot) {
        this.prot = prot;
    }

    public String getProt() {
        return prot;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getHost() {
        return host;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getDomain() {
        return domain;
    }

    public void setRep(String rep) {
        this.rep = rep;
    }

    public String getRep() {
        return rep;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getPwd() {
        return pwd;
    }

    public void setTestConn(RichCommandButton testConn) {
        this.testConn = testConn;
    }

    public RichCommandButton getTestConn() {
        return testConn;
    }

    public void setWebName(String webName) {
        this.webName = webName;
    }

    public String getWebName() {
        return webName;
    }
    
    public DataIntegrationInterface createDig() throws MalformedURLException {
        //"http://infa-pc:7334/wsh/services/BatchServices/DataIntegration?WSDL"
        String uri = httpStr + "://" + host + ":"
            + prot + "/wsh/services/BatchServices/DataIntegration?WSDL";
        String sapce = "http://www.informatica.com/wsh";
        System.out.println(uri+":"+sapce);
        
        URL url = new URL(uri);
        QName qname = new QName(sapce,"DataIntegrationService");
        Service service = Service.create(url, qname);
        DataIntegrationInterface dig = service.getPort(new QName(sapce,"DataIntegration"), 
                                                           DataIntegrationInterface.class);
        return dig;
    }

    public void Test(ActionEvent actionEvent) throws MalformedURLException,
                                                     Fault {
        // Add event code here...
        DataIntegrationInterface dig = this.createDig();
        // Add your code to call the desired methods.
        LoginRequest loginReq = new LoginRequest();
        loginReq.setRepositoryDomainName(domain);
        loginReq.setRepositoryName(rep);
        loginReq.setUserName(userName);
        loginReq.setPassword(pwd);

        SessionHeader sessionHeader = new SessionHeader();
        Holder<SessionHeader> Context =
            new Holder<SessionHeader>(sessionHeader);

        String str = dig.login(loginReq, Context);
        JSFUtils.addFacesInformationMessage("SUCCESS:"+str);
        this.sessionId = str;
        System.out.println(str);
        
    }
    
    private String folerName;
    private String wfName;
    private String reason;
    private String isName;
    public void startWorkflow(ActionEvent actionEvent) throws MalformedURLException, Fault {
        DataIntegrationInterface dig = this.createDig();
        
        WorkflowRequest workflow = new WorkflowRequest();
        
        workflow.setFolderName(folerName);
        workflow.setWorkflowName(wfName);
        workflow.setReason(reason);
        workflow.setRequestMode(ETaskRunMode.NORMAL);
        DIServiceInfo dinfo = new DIServiceInfo();
        dinfo.setDomainName(domain);
        dinfo.setServiceName(isName);
        workflow.setDIServiceInfo(dinfo);
        
        SessionHeader sHead = new SessionHeader();
        sHead.setSessionId(sessionId);
        
        dig.startWorkflow(workflow, sHead);
        
        JSFUtils.addFacesInformationMessage("START WORKFLOW SUCCESS!");
    }

    public void setFolerName(String folerName) {
        this.folerName = folerName;
    }

    public String getFolerName() {
        return folerName;
    }

    public void setWfName(String wfName) {
        this.wfName = wfName;
    }

    public String getWfName() {
        return wfName;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setIsName(String isName) {
        this.isName = isName;
    }

    public String getIsName() {
        return isName;
    }
}
