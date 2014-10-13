package dms.user;

import com.bea.security.utils.DigestUtils;

import com.sun.tools.ws.wsdl.document.BindingOperation;

import common.ADFUtils;

import java.io.UnsupportedEncodingException;

import java.security.NoSuchAlgorithmException;

import java.util.Map;

import javax.faces.event.ActionEvent;

import oracle.adf.share.ADFContext;

import oracle.binding.OperationBinding;

import oracle.jbo.ApplicationModule;
import oracle.jbo.client.Configuration;

import team.epm.dms.view.DmsUserViewImpl;
import team.epm.dms.view.DmsUserViewRowImpl;

public class PasswordManager {

    private String oldpsw;
    private String newpsw;
    private String comfirmpsw;

    public PasswordManager() {
        super();
    }

    public void updatepsw(ActionEvent actionEvent) {
        ADFContext adfc = ADFContext.getCurrent();
        Map map = adfc.getSessionScope();
        DmsUserViewRowImpl cur_user = (DmsUserViewRowImpl)map.get("cur_user");
        //
        //        if (cur_user != null) {
        //            ApplicationModule am =
        //                Configuration.createRootApplicationModule("team.epm.module.DmsModule",
        //                                                          "DmsModuleLocal");
        //            DmsUserViewImpl dmsUserView =
        //                (DmsUserViewImpl)am.findViewObject("DmsUserView");
        //            dmsUserView.setWhereClause("DmsUser.Acc=:acc");
        //            dmsUserView.defineNamedWhereClauseParam("acc", null, null);
        //            dmsUserView.setNamedWhereClauseParam("acc",
        //                                                 (cur_user.getAcc() + "").trim());
        //            dmsUserView.executeQuery();
        //            dmsUserView.setWhereClause(null);
        //            dmsUserView.setWhereClauseParams(null);
        //            DmsUserViewRowImpl row = (DmsUserViewRowImpl)dmsUserView.first();
        //
        //            if (row != null) {
        //                String encypt_pwd="";
        //                try {
        //                    encypt_pwd =
        //                            DigestUtils.digestSHA1((cur_user.getAcc() + "").trim() +
        //                                                   (oldpsw +
        //                                                    "").trim());
        //                } catch (NoSuchAlgorithmException e) {
        //                } catch (UnsupportedEncodingException e) {
        //                }
        //                if (row.getPwd().equals(encypt_pwd)) {
        //                    if(newpsw.equals(comfirmpsw)){
        //                        String new_encypt="";
        //                        try {
        //                            new_encypt =
        //                                    DigestUtils.digestSHA1((cur_user.getAcc() + "").trim() +
        //                                                   (newpsw +
        //                                                    "").trim());
        //                        } catch (NoSuchAlgorithmException e) {
        //                        } catch (UnsupportedEncodingException e) {
        //                        }
        //                        row.setPwd(new_encypt);
        //                        am.getTransaction().commit();
        //                        map.put("cur_user", row);
        //
        //                    }
        //                }
        //            }
        //        }
        if (cur_user != null) {
            if (oldpsw.equals(newpsw)) {
                OperationBinding ob = ADFUtils.findOperation("updatepassword");
                Map paramap = ob.getParamsMap();
                paramap.put("acc", cur_user.getAcc());
                paramap.put("oldpwd", oldpsw);
                paramap.put("newpwd", newpsw);
                ob.execute();
            }
        }
    }

    public void setOldpsw(String oldpsw) {
        this.oldpsw = oldpsw;
    }

    public String getOldpsw() {
        return oldpsw;
    }

    public void setNewpsw(String newpsw) {
        this.newpsw = newpsw;
    }

    public String getNewpsw() {
        return newpsw;
    }

    public void setComfirmpsw(String comfirmpsw) {
        this.comfirmpsw = comfirmpsw;
    }

    public String getComfirmpsw() {
        return comfirmpsw;
    }

    public String cb1_action() {
        // Add event code here...
        return null;
    }


}
