package common;

import oracle.jbo.ApplicationModule;
import oracle.jbo.Row;
import oracle.jbo.client.Configuration;
import oracle.jbo.server.ViewObjectImpl;

public class InitSystem {
    private static ApplicationModule dms_am= Configuration.createRootApplicationModule("team.epm.module.DmsModule","DmsModuleLocal");
    private static ApplicationModule dcm_am=Configuration.createRootApplicationModule("team.epm.module.DcmModule","DcmModuleLocal");
    public static void main(String[] args) {
        initUser();
        initMenu();
        Configuration.releaseRootApplicationModule(dms_am, true);
        Configuration.releaseRootApplicationModule(dcm_am, true);
    }
    private static void initUser(){
       ViewObjectImpl view = (ViewObjectImpl)dms_am.findViewObject("DmsUserView"); 
       Row row=view.createRow();
       row.setAttribute("", "");
       view.insertRow(row);
       dms_am.getTransaction().commit();
    }
    private static void initMenu(){
        
    }
}
