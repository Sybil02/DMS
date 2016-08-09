package dmsApproval;

import common.ADFUtils;

import common.DmsUtils;
import common.JSFUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import javax.faces.event.ValueChangeEvent;

import javax.faces.model.SelectItem;

import oracle.adf.share.ADFContext;
import oracle.adf.view.rich.component.rich.RichPopup;

import oracle.adf.view.rich.component.rich.output.RichOutputText;

import oracle.jbo.Row;
import oracle.jbo.ViewObject;
import oracle.jbo.server.DBTransaction;
import oracle.jbo.server.ViewRowImpl;

public class DmsApprovalBean {
    private RichPopup approvalPop;
    private List<SelectItem> entityList;

    public DmsApprovalBean() {
        super();
    }

    public void save(ActionEvent actionEvent) {
        ViewObject vo = ADFUtils.findIterator("DmsApprovalflowInfoVOIterator").getViewObject();
        vo.getApplicationModule().getTransaction().commit();
    }

    public void showApprovalEntity(ActionEvent actionEvent) {
        ViewObject vo = ADFUtils.findIterator("DmsApprovalflowInfoVOIterator").getViewObject();
        if(vo.getApplicationModule().getTransaction().isDirty()){
            JSFUtils.addFacesInformationMessage("请先保存数据再操作！");
            return;
        }
        
        Row row = vo.getCurrentRow();
        String comId = row.getAttribute("ValueSetId").toString();
        
        this.entityList = this.getEntityList(comId);
        
        RichPopup.PopupHints hints = new RichPopup.PopupHints();
        this.approvalPop.show(hints);
    }
    
    public List<SelectItem> getEntityList(String comId){
        List<SelectItem> list = new ArrayList<SelectItem>();
        DBTransaction trans = (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(1);
        String sql = "SELECT V.ID,V.SOURCE FROM DCM_COM_VS T,DMS_VALUE_SET V WHERE T.IS_APPROVAL = 'Y' "
            + "AND T.VALUE_SET_ID = V.ID AND V.LOCALE = 'zh_CN' AND T.COMBINATION_ID = '" + comId + "'";
        System.out.println("entity:"+sql);
        ResultSet rs;
        String source = "";
        try {
            rs = stat.executeQuery(sql);
            if(rs.next()){
                source = rs.getString("SOURCE");    
            }else{
                rs.close();
                stat.close();
                return list;
            }
            rs.close();
            
            String sql1 = "SELECT T.CODE,T.MEANING FROM " + source + " T WHERE T.LOCALE = '" 
                          + ADFContext.getCurrent().getLocale() + "'";
            ResultSet eRs = stat.executeQuery(sql1);    
            while(eRs.next()){
                SelectItem sim = new SelectItem();
                sim.setValue(eRs.getString("CODE"));
                sim.setLabel(eRs.getString("MEANING"));
                list.add(sim);
            }
            eRs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void setApprovalPop(RichPopup approvalPop) {
        this.approvalPop = approvalPop;
    }

    public RichPopup getApprovalPop() {
        return approvalPop;
    }

    public void setEntityList(List<SelectItem> entityList) {
        this.entityList = entityList;
    }

    public List<SelectItem> getEntityList() {
        return entityList;
    }
}
