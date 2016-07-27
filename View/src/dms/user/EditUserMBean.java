package dms.user;

import com.bea.security.utils.DigestUtils;

import common.ADFUtils;
import common.DmsUtils;

import java.sql.ResultSet;
import java.sql.Statement;

import javax.faces.event.ActionEvent;

import oracle.adf.share.logging.ADFLogger;
import oracle.adf.view.rich.component.rich.RichPopup;
import oracle.adf.view.rich.component.rich.input.RichInputText;

import oracle.adf.view.rich.component.rich.output.RichOutputLabel;

import oracle.jbo.ViewObject;

import oracle.jbo.server.DBTransaction;

import org.apache.commons.lang.ObjectUtils;

import team.epm.dms.model.DmsUserImpl;

public class EditUserMBean {
    private RichInputText newPwd;
    private RichInputText pwd;
    private static ADFLogger logger =
        ADFLogger.createADFLogger(EditUserMBean.class);
    private RichOutputLabel msg;
    private RichPopup popup;

    public EditUserMBean() {
    }

    public void setNewPwd(RichInputText newPwd) {
        this.newPwd = newPwd;
    }

    public RichInputText getNewPwd() {
        return newPwd;
    }

    public void setPwd(RichInputText pwd) {
        this.pwd = pwd;
    }

    public RichInputText getPwd() {
        return pwd;
    }

    public void setMsg(RichOutputLabel msg) {
        this.msg = msg;
    }

    public RichOutputLabel getMsg() {
        return msg;
    }

    public void changePwd(ActionEvent actionEvent) {
        String pwd = ObjectUtils.toString(this.pwd.getValue()).trim();
        String newPwd = ObjectUtils.toString(this.newPwd.getValue()).trim();
        DBTransaction trans = (DBTransaction)DmsUtils.getDcmApplicationModule().getTransaction();
        if (pwd.equals(newPwd)) {
            if(DmsUserImpl.isPasswordValide(newPwd)){
            ViewObject usrVo =
                ADFUtils.findIterator("DmsUserViewIterator").getViewObject();
            String usrAcc = (String)usrVo.getCurrentRow().getAttribute("Acc");
                String usrId = (String)usrVo.getCurrentRow().getAttribute("Id");
            String encyptPwd;
            try {
                encyptPwd = DigestUtils.digestSHA1(usrAcc + pwd);
                String sqlQuery = "SELECT PASSWORD FROM HLS_PASSWORD_LOG WHERE USER_ID='"+usrId+"' ORDER BY CREATED_AT";
                Statement statQuery = trans.createStatement(DBTransaction.DEFAULT);
                ResultSet rs = statQuery.executeQuery(sqlQuery);
                int count=0;
                String deletePwd="";
                while(rs.next()){
                    count++;
                    if(count==1){
                        deletePwd = rs.getString("PASSWORD");
                    }
                    if(rs.getString("PASSWORD").equals(encyptPwd)){
                        this.msg.setValue("此密码在5次内使用过，请重新设置");
                        return;
                    }
                }
                Statement statDelete = trans.createStatement(DBTransaction.DEFAULT);
                if(count>=5){
                    String sqlDelete = "DELETE FROM HLS_PASSWORD_LOG WHERE PASSWORD='"+deletePwd+"' AND USER_ID='"+usrId+"'";
                    statDelete.executeUpdate(sqlDelete);
                }
                usrVo.getCurrentRow().setAttribute("Pwd", encyptPwd);
                usrVo.getApplicationModule().getTransaction().commit();
                String sqlInsert = "INSERT INTO HLS_PASSWORD_LOG(USER_ID,PASSWORD,CREATED_AT) " +
                                    "VALUES('"+usrId+"','"+encyptPwd+"',SYSDATE)";
                Statement statInsert = trans.createStatement(DBTransaction.DEFAULT);
                statInsert.executeUpdate(sqlInsert);
                statQuery.close();
                statInsert.close();
                statDelete.close();
                trans.commit();
                this.popup.cancel();
            } catch (Exception e) {
                this.logger.severe(e);
            }
            }else{
                this.msg.setValue(DmsUtils.getMsg("dms.user.password_limit"));
            }
        } else {
            this.msg.setValue(DmsUtils.getMsg("dms_user.password_inconsitent"));
        }
    }

    public void setPopup(RichPopup popup) {
        this.popup = popup;
    }

    public RichPopup getPopup() {
        return popup;
    }

    public void hidePopup(ActionEvent actionEvent) {
        this.popup.cancel();
    }

    public void showPopup(ActionEvent actionEvent) {
        this.pwd.setValue("");
        this.newPwd.setValue("");
        this.msg.setValue("");
        RichPopup.PopupHints hints = new RichPopup.PopupHints();
        this.popup.show(hints);
    }
}
