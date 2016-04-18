package dmsApproval;

import common.ADFUtils;

import common.DmsUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.faces.event.ActionEvent;

import oracle.jbo.ViewObject;
import oracle.jbo.server.DBTransaction;

public class ApprovalIndexBean {
    public ApprovalIndexBean() {
        super();
    }
    
    private List<AppParamBean> paramList;
    
    public void startApproval(ActionEvent actionEvent) {
        ViewObject vo = ADFUtils.findIterator("DmsUserApprovalVOIterator").getViewObject();
        String combination = vo.getCurrentRow().getAttribute("ValueSetId").toString();
        
        DBTransaction trans = (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(1);
        String sql = "SELECT V.ID,V.SOURCE,V.NAME,V.CODE,T.IS_APPROVAL,T.SEQ FROM DCM_COM_VS T,DMS_VALUE_SET V WHERE "
            + "T.VALUE_SET_ID = V.ID AND V.LOCALE = 'zh_CN' AND T.COMBINATION_ID = '" + combination + "' ORDER BY T.SEQ";
        ResultSet rs;
        this.paramList = new ArrayList<AppParamBean>();
        try {
            rs = stat.executeQuery(sql);
            while(rs.next()){
                String isApp = rs.getString("IS_APPROVAL");
                if(!"Y".equals(isApp)){
                    AppParamBean param = new AppParamBean();
                    param.setPName(rs.getString("NAME"));
                    param.setPSource(rs.getString("SOURCE"));
                    param.setPCode(rs.getString("CODE"));
                    this.paramList.add(param);
                }
            }
            rs.close();
            stat.close();
            
        } catch (SQLException e) {
                e.printStackTrace();
        }
        
        
        
    }

    public void closeApproval(ActionEvent actionEvent) {
        // Add event code here...
    }
}
