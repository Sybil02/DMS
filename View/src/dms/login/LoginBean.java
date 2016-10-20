package dms.login;

import com.bea.security.utils.DigestUtils;

import common.ADFUtils;
import common.DmsLog;
import common.DmsUtils;

import common.JSFUtils;

import common.MailSender;

import java.io.IOException;

import java.io.UnsupportedEncodingException;

import java.security.NoSuchAlgorithmException;

import java.sql.ResultSet;
import java.sql.Statement;

import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import java.util.UUID;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import javax.faces.event.ActionEvent;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import javax.mail.internet.ParseException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import oracle.adf.controller.ControllerContext;

import oracle.adf.share.ADFContext;
import oracle.adf.share.logging.ADFLogger;

import oracle.adf.view.rich.component.rich.RichPopup;
import oracle.adf.view.rich.component.rich.input.RichInputText;

import oracle.adf.view.rich.component.rich.output.RichOutputLabel;

import oracle.jbo.Row;
import oracle.jbo.ViewObject;

import oracle.jbo.server.DBTransaction;

import org.apache.commons.lang.ObjectUtils;

import team.epm.dms.model.DmsUserImpl;
import team.epm.dms.view.DmsUserViewImpl;
import team.epm.dms.view.DmsUserViewRowImpl;

import team.epm.module.DmsModuleImpl;

import utils.system;

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
    DmsLog dmsLog = new DmsLog();
    private RichPopup popupPwd;
    private RichInputText newPwd;
    private RichInputText rePwd;
    private RichOutputLabel msgt;
    private int days=0;
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
            SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            //生成id
            String s = UUID.randomUUID().toString();
            //去除分隔符-
            String newId = s.substring(0,8)+s.substring(9,13)+s.substring(14,18)+s.substring(19,23)+s.substring(24);
            try {
                days = daysBetween(sdf.parse(row.getUpdatedAt().toString()),new Date());
                String encypt_pwd =DigestUtils.digestSHA1(ObjectUtils.toString(this.account).trim() +ObjectUtils.toString(this.password).trim());
                if (pwd.equals(encypt_pwd)) {
                    //登陆成功
                    if(days>=90){
                        this.showPwdPop();
                    }else{
                        dmsLog.loginMsg(this.account,newId);
                        this.initUserPreference(row,newId);
                        ExternalContext ectx =FacesContext.getCurrentInstance().getExternalContext();
                        ectx.redirect(ControllerContext.getInstance().getGlobalViewActivityURL("index"));
                    }
                } else {
                    this.msg =DmsUtils.getMsg("login.username_password_error");
                }
                
            } catch (Exception e) {
                this.msg = DmsUtils.getMsg("common.operation_failed_with_exception");
                this._logger.severe(e);
            }
        }
    }
    
    //显示错误框
    public void showPwdPop(){
        RichPopup.PopupHints ph = new RichPopup.PopupHints();
        this.popupPwd.show(ph);
    }
    
    //计算两个日期之间相差的天数
    public int daysBetween(Date smdate,Date bdate) throws ParseException    
        {    
            Calendar cal = Calendar.getInstance();    
            cal.setTime(smdate);    
            long time1 = cal.getTimeInMillis();                 
            cal.setTime(bdate);    
            long time2 = cal.getTimeInMillis();         
            long between_days=(time2-time1)/(1000*3600*24); 
           return Integer.parseInt(String.valueOf(between_days));           
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

    private void initUserPreference(DmsUserViewRowImpl user,String newId) {
        ADFContext.getCurrent().getSessionScope().put("cur_user",new Person(user));
        ADFContext.getCurrent().getSessionScope().put("newId", newId);
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

    public void logout(){
        String newId = (String)ADFContext.getCurrent().getSessionScope().get("newId");
        dmsLog.logoutMsg(newId);
        ADFContext.getCurrent().getSessionScope().remove("newId");
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
    //修改密码
    public void changePwd(ActionEvent actionEvent) {
        String pwd = ObjectUtils.toString(this.newPwd.getValue()).trim();
        String configPwd = ObjectUtils.toString(this.rePwd.getValue()).trim();
        DBTransaction trans = (DBTransaction)DmsUtils.getDcmApplicationModule().getTransaction();
        if(pwd.equals(configPwd)){
            if(DmsUserImpl.isPasswordValide(pwd)){
                DmsModuleImpl appModule=DmsUtils.getDmsApplicationModule();
                DmsUserViewImpl dmsUserView = appModule.getDmsUserView();
                dmsUserView.queryUserByAcc(ObjectUtils.toString(this.account).trim());
                DmsUserViewRowImpl row = (DmsUserViewRowImpl)dmsUserView.first();
                String userAcc = row.getAcc();
                dmsUserView.getApplicationModule().getSession().getUserData().put("userId",row.getId());
                String oldPwd = row.getPwd();
                String encyptPwd;
                try {
                    encyptPwd = DigestUtils.digestSHA1(userAcc+pwd);
                    String sqlQuery = "SELECT PASSWORD FROM HLS_PASSWORD_LOG WHERE USER_ID='"+row.getId()+"' ORDER BY CREATED_AT";
                    Statement statQuery = trans.createStatement(DBTransaction.DEFAULT);
                    ResultSet rs = statQuery.executeQuery(sqlQuery);
                    int count=0;
                    String deletePwd="";
                    if(oldPwd.equals(encyptPwd)){
                        this.msgt.setValue("新密码与正在使用的密码相同");
                        return;
                    }
                    while(rs.next()){
                        count++;
                        if(count==1){
                            deletePwd = rs.getString("PASSWORD");
                        }
                        if(rs.getString("PASSWORD").equals(encyptPwd)){
                            this.msgt.setValue("此密码在5次内使用过，请重新设置");
                            return;
                        }
                    }
                    Statement statDelete = trans.createStatement(DBTransaction.DEFAULT);
                    if(count>=5){
                        String sqlDelete = "DELETE FROM HLS_PASSWORD_LOG WHERE PASSWORD='"+deletePwd+"' AND USER_ID='"+row.getId()+"'";
                        statDelete.executeUpdate(sqlDelete);
                    }
                    row.setPwd(encyptPwd);
                    appModule.getTransaction().commit();
                    String sqlInsert = "INSERT INTO HLS_PASSWORD_LOG(USER_ID,PASSWORD,CREATED_AT) " +
                                        "VALUES('"+row.getId()+"','"+encyptPwd+"',SYSDATE)";
                    Statement statInsert = trans.createStatement(DBTransaction.DEFAULT);
                    statInsert.executeUpdate(sqlInsert);
                    statQuery.close();
                    statInsert.close();
                    statDelete.close();
                    trans.commit();
                    ExternalContext ectx =FacesContext.getCurrentInstance().getExternalContext();
                    ectx.redirect(ControllerContext.getInstance().getGlobalViewActivityURL("/faces/login"));
                } catch (Exception e) {
                    this._logger.severe(e);
                } 
            }else{
                this.msgt.setValue(DmsUtils.getMsg("dms.user.password_limit"));
            }
        }else{
            this.msgt.setValue(DmsUtils.getMsg("dms_user.password_inconsitent"));
        }
    }

    //忘记密码
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

    public void setPopupPwd(RichPopup popupPwd) {
        this.popupPwd = popupPwd;
    }

    public RichPopup getPopupPwd() {
        return popupPwd;
    }

    public void setNewPwd(RichInputText newPwd) {
        this.newPwd = newPwd;
    }

    public RichInputText getNewPwd() {
        return newPwd;
    }

    public void setRePwd(RichInputText rePwd) {
        this.rePwd = rePwd;
    }

    public RichInputText getRePwd() {
        return rePwd;
    }

    public void setMsgt(RichOutputLabel msgt) {
        this.msgt = msgt;
    }

    public RichOutputLabel getMsgt() {
        return msgt;
    }

}
