package dms.user;

import com.bea.security.utils.DigestUtils;

import common.ADFUtils;
import common.DmsUtils;

import dms.login.Person;

import javax.faces.event.ActionEvent;

import oracle.adf.share.ADFContext;
import oracle.adf.share.logging.ADFLogger;
import oracle.adf.view.rich.component.rich.RichPopup;
import oracle.adf.view.rich.component.rich.input.RichInputText;

import oracle.adf.view.rich.component.rich.output.RichOutputLabel;

import oracle.adf.view.rich.context.AdfFacesContext;
import oracle.adf.view.rich.render.ClientEvent;

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
                ADFUtils.findIterator("DmsUserViewIterator").getViewObject();
            String usrAcc = (String)usrVo.getCurrentRow().getAttribute("Acc");
            String encyptPwd;
            try {
                encyptPwd = DigestUtils.digestSHA1(usrAcc + pwd);
                usrVo.getCurrentRow().setAttribute("Pwd", encyptPwd);
                usrVo.getApplicationModule().getTransaction().commit();
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
    
    //判断这个用户是否设置了密码，如果没有设置，则自动弹出对话框
    public void isPwdSet(ClientEvent clientEvent) {
        Person person = (Person)ADFContext.getCurrent().getSessionScope().get("cur_user");
  
        if(person.getPwd() == null) {
            this.pwd.setValue("");
            this.newPwd.setValue("密码为空，请设置密码");
            RichPopup.PopupHints hints = new RichPopup.PopupHints();
            this.popup.show(hints);
            
            AdfFacesContext.getCurrentInstance().addPartialTarget(popup);
        }
            
    }
}
