package common;

import dms.login.LoginBean;

import java.io.IOException;

import java.util.Map;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;

import oracle.adf.controller.ControllerContext;
import oracle.adf.controller.v2.lifecycle.Lifecycle;
import oracle.adf.controller.v2.lifecycle.PagePhaseEvent;
import oracle.adf.controller.v2.lifecycle.PagePhaseListener;
import oracle.adf.share.ADFContext;
import oracle.adf.share.logging.ADFLogger;

public class CheckLoginPhaseListener implements PagePhaseListener {
    private static ADFLogger _logger =
        ADFLogger.createADFLogger(CheckLoginPhaseListener.class);

    public CheckLoginPhaseListener() {

    }

    public void afterPhase(PagePhaseEvent pagePhaseEvent) {
        if (pagePhaseEvent.getPhaseId() == Lifecycle.PREPARE_RENDER_ID) {
            FacesContext fctx = FacesContext.getCurrentInstance();
            //获取request参数
            Map<String,String> params = fctx.getExternalContext().getRequestParameterMap();
            //获取当前请求的VIEW
            String curView = fctx.getViewRoot().getViewId();
            //如果不是登陆页面则判断用户是否已经登录            
            if (!"/login".equals(curView)&&!"/loginError.html".equals(curView)) {
                Object cur_user=ADFContext.getCurrent().getSessionScope().get("cur_user");
                if (cur_user==null) {
                    ExternalContext ectx = fctx.getExternalContext();
                    String viewId = "login";
                    ControllerContext controllerCtx = null;
                    controllerCtx = ControllerContext.getInstance();
                    String activityURL =
                        controllerCtx.getGlobalViewActivityURL(viewId);
                    try {
                        ectx.redirect(activityURL);
                    } catch (IOException e) {
                        _logger.severe(e);
                    }
                }
            }else{
                String _sso = params.get("_sso");
                String userName = params.get("username");
                String pwd = params.get("password");
                if("true".equals(_sso)){
                    LoginBean loginBean = new LoginBean();
                    loginBean.setAccount(userName);
                    loginBean.setPassword(pwd);
                    loginBean.autoLogin();
                }
            }
        }
}

    public void beforePhase(PagePhaseEvent pagePhaseEvent) {
    }
}
