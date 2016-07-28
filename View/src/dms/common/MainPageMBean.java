package dms.common;

import com.sun.el.MethodExpressionLiteral;

import common.ADFUtils;

import common.DmsUtils;

import common.JSFUtils;

import dms.login.Person;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import javax.faces.event.PhaseEvent;

import javax.faces.event.PhaseId;

import javax.mail.internet.ParseException;

import oracle.adf.share.ADFContext;
import oracle.adf.view.rich.component.rich.RichMenuBar;
import oracle.adf.view.rich.component.rich.nav.RichCommandLink;

import oracle.adf.view.rich.component.rich.nav.RichGoLink;

import oracle.jbo.Row;
import oracle.jbo.server.DBTransaction;
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
    
    public void showDays(){
        DBTransaction trans = (DBTransaction)DmsUtils.getDcmApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        Person cUser = (Person)ADFContext.getCurrent().getSessionScope().get("cur_user");
        String sql = "SELECT UPDATED_AT FROM DMS_USER WHERE ID='"+cUser.getId()+"'";
        ResultSet rs;
        try {
            rs = stat.executeQuery(sql);
            rs.next();
            String updateTime = rs.getString("UPDATED_AT");
            SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            int days = daysBetween(sdf.parse(updateTime),new Date());
            System.out.println(days);
            if(days>=85&&days<90){
                JSFUtils.addFacesInformationMessage("密码即将过期，请尽快修改");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    //计算两个日期之间相差的天数
    public int daysBetween(Date smdate,Date bdate) throws ParseException    
        {    
            SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd"); 
            Calendar cal = Calendar.getInstance();    
            cal.setTime(smdate);    
            long time1 = cal.getTimeInMillis();                 
            cal.setTime(bdate);    
            long time2 = cal.getTimeInMillis();         
            long between_days=(time2-time1)/(1000*3600*24); 
           return Integer.parseInt(String.valueOf(between_days));           
        }    
}
