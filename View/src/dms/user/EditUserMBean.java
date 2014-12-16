package dms.user;

import com.bea.security.utils.DigestUtils;

import common.DmsUtils;

import javax.faces.event.ActionEvent;

import oracle.adf.share.logging.ADFLogger;
import oracle.adf.view.rich.component.rich.RichPopup;
import oracle.adf.view.rich.component.rich.input.RichInputText;

import oracle.adf.view.rich.component.rich.output.RichOutputLabel;

import oracle.jbo.ViewObject;

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
        if (pwd.equals(newPwd)) {
            if(DmsUserImpl.isPasswordValide(newPwd)){
            ViewObject usrVo =
                DmsUtils.getDmsApplicationModule().getDmsUserView();
            String usrAcc = (String)usrVo.getCurrentRow().getAttribute("Acc");
            String encyptPwd;
            try {
                encyptPwd = DigestUtils.digestSHA1(usrAcc + pwd);
                usrVo.getCurrentRow().setAttribute("Pwd", encyptPwd);
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
