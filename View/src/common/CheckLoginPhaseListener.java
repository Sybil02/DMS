package common;

import java.io.IOException;

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
            //获取当前请求的VIEW
            String curView = fctx.getViewRoot().getViewId();
            //如果不是登陆页面则判断用户是否已经登录            
            if (!"/login".equals(curView)) {
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
            }
        }
}

    public void beforePhase(PagePhaseEvent pagePhaseEvent) {
    }
}
