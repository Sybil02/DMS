package dms.login;

import com.bea.security.utils.DigestUtils;

import common.DmsUtils;

import common.JSFUtils;

import common.MailSender;

import java.io.IOException;

import java.io.UnsupportedEncodingException;

import java.security.NoSuchAlgorithmException;

import java.util.HashMap;
import java.util.Map;

import java.util.Random;

import javax.el.ValueExpression;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import javax.faces.event.ActionEvent;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import oracle.adf.controller.ControllerContext;

import oracle.adf.controller.TaskFlowId;
import oracle.adf.share.ADFContext;
import oracle.adf.share.logging.ADFLogger;

import oracle.adf.view.rich.component.rich.RichPopup;
import oracle.adf.view.rich.component.rich.input.RichInputText;

import oracle.jbo.ApplicationModule;
import oracle.jbo.Row;
import oracle.jbo.ViewObject;
 

import org.apache.commons.lang.ObjectUtils;

import org.apache.myfaces.trinidad.render.ExtendedRenderKitService;

import org.apache.myfaces.trinidad.util.Service;

import team.epm.dms.view.DmsUserViewImpl;
import team.epm.dms.view.DmsUserViewRowImpl;

import team.epm.module.DmsModuleImpl;

import weblogic.servlet.security.ServletAuthentication;

public class LoginBean {
    private String msg;
    private String account;
    private String password;
    private static ADFLogger _logger =
        ADFLogger.createADFLogger(LoginBean.class);
    private RichInputText acc;
    private RichInputText mail;
    private RichPopup popup;
    

    public LoginBean() {
 
    }

    public void login() {
        DmsModuleImpl appModule=DmsUtils.getDmsApplicationModule();
        DmsUserViewImpl dmsUserView = appModule.getDmsUserView();
        dmsUserView.queryUserByAcc(ObjectUtils.toString(this.account).trim());
        DmsUserViewRowImpl row = (DmsUserViewRowImpl)dmsUserView.first();
        if (row == null) {
            this.msg = DmsUtils.getMsg("login.account_not_exist");
        } else {
            String pwd = row.getPwd();
            try {
                String org_pwd = ObjectUtils.toString(this.password).trim();
                String encypt_pwd = DigestUtils.digestSHA1(ObjectUtils.toString(this.account).trim() +ObjectUtils.toString(this.password).trim());
                 
                //如果密码是空，而且输入的时候密码为空则进入界面，强制要求修改密码
                if(pwd == null && org_pwd.equals("")) {
                    this.initUserPreference(row);
                    String taskFlowId = "/WEB-INF/dmsUser/user_info_tsk.xml#user_info_tsk";
                    //把第一个要展示的页面放在session里面
                    ADFContext.getCurrent().getSessionScope().put("first_index", taskFlowId);
                    
                    FacesContext facesContext = FacesContext.getCurrentInstance();  
                    ExternalContext ectx = facesContext.getExternalContext();
                    ectx.redirect(ControllerContext.getInstance().getGlobalViewActivityURL("index"));
                }
                else if (pwd.equals(encypt_pwd)) {
                    //登陆成功
                    this.initUserPreference(row);
                    ExternalContext ectx =FacesContext.getCurrentInstance().getExternalContext();
                   
                    ectx.redirect(ControllerContext.getInstance().getGlobalViewActivityURL("index"));
                } else {
                    this.msg =DmsUtils.getMsg("login.username_password_error");
                }
            } catch (Exception e) {
                this.msg = DmsUtils.getMsg("common.operation_failed_with_exception");
                this._logger.severe(e);
            }
        }
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getAccount() {
        return account;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    private void initUserPreference(DmsUserViewRowImpl user) {
        ADFContext.getCurrent().getSessionScope().put("cur_user",new Person(user));
        user.getApplicationModule().getSession().getUserData().put("userId",user.getId());
        ADFContext.getCurrent().getSessionScope().put("userId", user.getId());  
        ViewObject vo=DmsUtils.getDmsApplicationModule().getDmsUserFunctionView();
        vo.setNamedWhereClauseParam("locale", user.getLocale());
        vo.executeQuery();
        Map authoriedFunction=new HashMap();
        while(vo.hasNext()){
            Row row=vo.next();
            authoriedFunction.put(row.getAttribute("FunctionId"), row.getAttribute("FunctionName"));
        }
        ADFContext.getCurrent().getSessionScope().put("authoriedFunction",authoriedFunction);       
    }

    public void logout() {
        ADFContext.getCurrent().getSessionScope().remove("cur_user");
        ExternalContext ectx =FacesContext.getCurrentInstance().getExternalContext();
        HttpSession session = (HttpSession)ectx.getSession(false);
        session.invalidate();
        HttpServletRequest request = (HttpServletRequest)ectx.getRequest();
        ServletAuthentication.logout(request);
        ServletAuthentication.invalidateAll(request);
        ServletAuthentication.killCookie(request);
        try {
            String url = ectx.getRequestContextPath() + "/faces/login";
            ectx.redirect(url);
        } catch (IOException e) {
            _logger.severe(e);
        }
    }

    public void forgetPassword(ActionEvent actionEvent) {
        String account=ObjectUtils.toString(this.acc.getValue()).trim();
        String mail=ObjectUtils.toString(this.mail.getValue()).trim();
        if(account.length()==0){
            JSFUtils.addFacesInformationMessage(DmsUtils.getMsg("login.input_account_msg"));
        }else if(mail.length()==0){
            JSFUtils.addFacesInformationMessage(DmsUtils.getMsg("login.mail_input.message"));
        }
        else{
            DmsUserViewImpl userVo=DmsUtils.getDmsApplicationModule().getDmsUserView();
            userVo.queryUserByAcc(account);
            DmsUserViewRowImpl row = (DmsUserViewRowImpl)userVo.first();
            if(row==null){
                JSFUtils.addFacesInformationMessage(DmsUtils.getMsg("login.account_not_exist"));
            }else{
                if(!mail.equals(row.getMail())){
                    JSFUtils.addFacesInformationMessage(DmsUtils.getMsg("login.acc.mail.msg"));
                }else{
                    String newPwd=DmsUtils.getRandomString(8);
                    try {
                        row.setPwd(DigestUtils.digestSHA1(row.getAcc() +newPwd));
                        this.sendMail(row.getMail(), 
                                      DmsUtils.getMsg("login.password_reset"), 
                                      DmsUtils.getMsg("login.pwd_reset_msg")+newPwd);
                        row.getApplicationModule().getTransaction().commit();
                        JSFUtils.addFacesInformationMessage(DmsUtils.getMsg("login.reset_mail"));
                    } catch (Exception e) {
                        this._logger.severe(e);
                        JSFUtils.addFacesInformationMessage(DmsUtils.getMsg("login.passwrod_reset_fail"));
                    }
                }
            }
        }
        this.popup.cancel();
    } 
    
    private void sendMail(String mailAddr,String title,String msg) throws AddressException,
                                             MessagingException {
        MailSender sender=new MailSender();
        sender.send(mailAddr, title, msg);
    }

    public void setAcc(RichInputText acc) {
        this.acc = acc;
    }

    public RichInputText getAcc() {
        return acc;
    }

    public void setMail(RichInputText mail) {
        this.mail = mail;
    }

    public RichInputText getMail() {
        return mail;
    }

    public void setPopup(RichPopup popup) {
        this.popup = popup;
    }

    public RichPopup getPopup() {
        return popup;
    }

    public TaskFlowId getDynamicTaskFlowId() {
        
        Object first_index = ADFContext.getCurrent().getSessionScope().get("first_index");
        ADFContext.getCurrent().getSessionScope().remove("first_index");
        
        String script = (String)ADFContext.getCurrent().getSessionScope().get("first_index_alert");
        ADFContext.getCurrent().getSessionScope().remove("first_index");
        
        if(first_index != null)
        {
            ADFContext.getCurrent().getSessionScope().remove("first_index");
            
            if(script != null) {
                FacesContext facesContext = FacesContext.getCurrentInstance();
                ExtendedRenderKitService service = 
                  Service.getRenderKitService(facesContext, ExtendedRenderKitService.class);
                service.addScript(facesContext,  script);
            }
            
            return TaskFlowId.parse(first_index.toString());
        }
        else
            return TaskFlowId.parse("/WEB-INF/blank.xml#blank");
    }
}
