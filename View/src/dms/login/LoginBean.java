package dms.login;

import com.bea.security.utils.DigestUtils;

import common.DmsUtils;

import java.io.IOException;

import java.util.HashMap;
import java.util.Map;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import oracle.adf.controller.ControllerContext;

import oracle.adf.share.ADFContext;
import oracle.adf.share.logging.ADFLogger;

import oracle.jbo.ApplicationModule;
import oracle.jbo.Row;
import oracle.jbo.ViewObject;

import org.apache.commons.lang.ObjectUtils;

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
                String encypt_pwd =DigestUtils.digestSHA1(ObjectUtils.toString(this.account).trim() +ObjectUtils.toString(this.password).trim());
                if (pwd.equals(encypt_pwd)) {
                    //登陆成功
                    this.initUserPreference(row);
                    ExternalContext ectx =FacesContext.getCurrentInstance().getExternalContext();
                    ectx.redirect(ControllerContext.getInstance().getGlobalViewActivityURL("index"));
                } else {
                    this.msg =DmsUtils.getMsg("login.username_password_error");
                }
            } catch (Exception e) {
                this.msg = DmsUtils.getMsg("operation_failed_with_exception");
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
}
