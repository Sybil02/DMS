package odi11g.webservice.ws;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import odi11g.webservice.type.OdiCredentialType;
import odi11g.webservice.type.OdiStartScenRequest;
import odi11g.webservice.type.OdiStartType;
import odi11g.webservice.type.ScenarioRequestType;

public class OdiInvokeBean {
    public static void main(String[] args) throws MalformedURLException {
        URL url=new URL("http://10.2.1.43:30000/oraclediagent/OdiInvoke?wsdl");
        QName qname=new QName("xmlns.oracle.com/odi/OdiInvoke/", "OdiInvoke");
        Service service=Service.create(url, qname);
        RequestPortType requestPortType=(RequestPortType)service.getPort(new QName("xmlns.oracle.com/odi/OdiInvoke/",
                                  "OdiInvokeRequestSOAP11port0"),
                                  RequestPortType.class);
        OdiStartScenRequest odiStartScenRequest=new OdiStartScenRequest();
        OdiCredentialType odiCredentialType=new OdiCredentialType();
        odiCredentialType.setOdiUser("SUPERVISOR");
        odiCredentialType.setOdiPassword("Welcome1");
        odiCredentialType.setWorkRepository("WORKREP1");
        odiStartScenRequest.setCredentials(odiCredentialType);
        ScenarioRequestType scenarioRequestType=new ScenarioRequestType();
        scenarioRequestType.setContext("GLOBAL");
        scenarioRequestType.setLogLevel(5);
        scenarioRequestType.setScenarioName("POP_ACCTGROUP");
        scenarioRequestType.setScenarioVersion("001");
        scenarioRequestType.setSessionName("DMS-INVOKE");
        scenarioRequestType.setSynchronous(false);
        odiStartScenRequest.setRequest(scenarioRequestType);
        OdiStartType response=requestPortType.invokeStartScen(odiStartScenRequest);
        System.out.println(response.getSession());
    }
}
