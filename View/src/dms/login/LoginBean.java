package dms.login;

import com.bea.security.utils.DigestUtils;

import common.ADFUtils;
import common.DmsUtils;

import dms.dynamicShell.TabContext;

import java.io.IOException;
import java.io.Serializable;

import java.io.UnsupportedEncodingException;

import java.security.NoSuchAlgorithmException;

import java.text.MessageFormat;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javax.faces.application.NavigationHandler;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import javax.faces.event.PhaseEvent;

import javax.faces.event.PhaseId;

import javax.naming.NamingException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import oracle.adf.controller.ControllerContext;
import oracle.adf.model.binding.DCBindingContainer;
import oracle.adf.model.BindingContext;

import oracle.adf.model.binding.DCIteratorBinding;
import oracle.adf.share.ADFContext;
import oracle.adf.share.logging.ADFLogger;

import oracle.adf.view.rich.context.AdfFacesContext;

import oracle.jbo.ApplicationModule;
import oracle.jbo.Row;
import oracle.jbo.ViewObject;
import oracle.jbo.client.Configuration;

import team.epm.dms.view.DmsUserViewImpl;
import team.epm.dms.view.DmsUserViewRowImpl;

import weblogic.servlet.security.ServletAuthentication;

public class LoginBean implements Serializable {
    private String msg;
    private String account;
    private String password;
    private static ADFLogger _logger =
        ADFLogger.createADFLogger(LoginBean.class);

    public LoginBean() {

    }

    public void login() {
        DCIteratorBinding binding =
            ADFUtils.findIterator("DmsUserViewIterator");
        DmsUserViewImpl dmsUserView = (DmsUserViewImpl)binding.getViewObject();
        dmsUserView.setWhereClause("DmsUser.Acc=:acc");
        dmsUserView.defineNamedWhereClauseParam("acc", null, null);
        dmsUserView.setNamedWhereClauseParam("acc",
                                             (this.account + "").trim());
        dmsUserView.executeQuery();
        dmsUserView.setWhereClause(null);
        dmsUserView.setWhereClauseParams(null);
        DmsUserViewRowImpl row = (DmsUserViewRowImpl)dmsUserView.first();
        if (row == null) {
            this.msg = DmsUtils.getMsg("login.account_not_exist");
        } else {
            String pwd = row.getPwd();
            try {
                String encypt_pwd =
                    DigestUtils.digestSHA1((this.account + "").trim() +
                                           (this.password + "").trim());
                if (pwd.equals(encypt_pwd)) {
                    //登陆成功
                    this.initUserPreference(row);
                    ExternalContext ectx =
                        FacesContext.getCurrentInstance().getExternalContext();
                    ectx.redirect(ControllerContext.getInstance().getGlobalViewActivityURL("index"));
                } else {
                    this.msg =
                            DmsUtils.getMsg("login.username_password_error");
                }
            } catch (Exception e) {
                this._logger.severe(e);
                this.msg = DmsUtils.getMsg("operation_failed_with_exception");
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
        ADFContext.getCurrent().getSessionScope().put("cur_user",
                                                      new Person(user));
        ApplicationModule applicationModule = user.getApplicationModule();
        applicationModule.getSession().getUserData().put("userId",
                                                         user.getId());
        ADFContext.getCurrent().getSessionScope().put("userId", user.getId());
    }

    public void logout() {
        ADFContext.getCurrent().getSessionScope().remove("cur_user");
        ExternalContext ectx =
            FacesContext.getCurrentInstance().getExternalContext();

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
