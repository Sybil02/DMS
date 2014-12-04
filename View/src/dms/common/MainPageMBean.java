package dms.common;

import com.sun.el.MethodExpressionLiteral;

import common.ADFUtils;

import dms.login.Person;

import java.util.Map;

import javax.faces.event.PhaseEvent;

import javax.faces.event.PhaseId;

import oracle.adf.share.ADFContext;
import oracle.adf.view.rich.component.rich.RichMenuBar;
import oracle.adf.view.rich.component.rich.nav.RichCommandLink;

import oracle.adf.view.rich.component.rich.nav.RichGoLink;

import oracle.jbo.Row;
import oracle.jbo.server.ViewObjectImpl;
import oracle.jbo.server.ViewRowImpl;

public class MainPageMBean {
    private RichMenuBar menuBar;

    public MainPageMBean() {

    }

    public void setMenuBar(RichMenuBar menuBar) {
        this.menuBar = menuBar;
    }

    public RichMenuBar getMenuBar() {
        return menuBar;
    }

    public void initMenuBar(PhaseEvent phaseEvent) {
        if (phaseEvent.getPhaseId().equals(PhaseId.RENDER_RESPONSE)) {
            Person curUser = (Person)ADFContext.getCurrent().getSessionScope().get("cur_user");
            Map authoriedFunction = (Map)ADFContext.getCurrent().getSessionScope().get("authoriedFunction");
            ViewObjectImpl view =
                (ViewObjectImpl)ADFUtils.findIterator("DmsMenuTreeViewIterator").getViewObject();
            view.setNamedWhereClauseParam("locale",curUser.getLocale());
            view.setNamedWhereClauseParam("p_id",null);
            view.executeQuery();
            this.menuBar.getChildren().clear();
            while (view.hasNext()) {
                Row row = view.next();
                if(authoriedFunction.get(row.getAttribute("FunctionId"))==null&&!"admin".equals(curUser.getAcc())){
                    continue;
                }
                RichGoLink c = new RichGoLink();
                c.setText(row.getAttribute("Label").toString());
                c.setInlineStyle("color:white;margin-left:10px");
                c.setDestination(row.getAttribute("Action").toString()+"?menu_id="+row.getAttribute("Id"));
                this.menuBar.getChildren().add(c);
            }
        }
    }
}
