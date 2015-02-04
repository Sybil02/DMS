package team.epm.dms.common;

import java.util.Map;

import oracle.adf.share.ADFContext;

import oracle.jbo.Session;
import oracle.jbo.server.ApplicationModuleImpl;

public class DmsApplicationModuleImpl extends ApplicationModuleImpl{
    @Override
    protected void prepareSession(Session session) {
        super.prepareSession(session);
        Map sessionScope = ADFContext.getCurrent().getSessionScope();
        Object userId = sessionScope.get("userId");
        if (userId != null) {
            this.getSession().getUserData().put("userId", userId);
        }
    }
}
