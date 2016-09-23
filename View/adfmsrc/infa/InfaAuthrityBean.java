package infa;

import common.ADFUtils;

import common.DmsUtils;

import common.lov.DmsComBoxLov;

import common.lov.ValueSetRow;

import dms.login.Person;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Iterator;

import java.util.List;

import javax.faces.event.ActionEvent;

import javax.faces.model.SelectItem;

import oracle.adf.share.ADFContext;
import oracle.adf.view.rich.component.rich.RichPopup;
import oracle.adf.view.rich.component.rich.data.RichTable;

import oracle.adf.view.rich.context.AdfFacesContext;

import oracle.jbo.Key;
import oracle.jbo.Row;
import oracle.jbo.RowSetIterator;
import oracle.jbo.ViewObject;
import oracle.jbo.server.DBTransaction;

public class InfaAuthrityBean {
    private RichTable authedTable;
    private RichPopup workflowPop;
    private RichTable unAuthedTable;
    private String roleName;
    private Person person =
            (Person)ADFContext.getCurrent().getSessionScope().get("cur_user");
    private DmsComBoxLov roleLov;

    public InfaAuthrityBean() {
        super();
        this.initRoleLov();
    }
    
    public void initRoleLov(){
        List<SelectItem> value = new ArrayList<SelectItem>();
        DBTransaction trans = (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        String sql = "SELECT T.ID,T.ROLE_NAME FROM DMS_ROLE T WHERE T.LOCALE='"+this.person.getLocale()+
                    "'  AND T.ENABLE_FLAG = 'Y' ORDER BY T.ROLE_NAME";
        System.out.println(sql);
        ResultSet rs ;
        try {
            rs = stat.executeQuery(sql);
            List<ValueSetRow> list = new ArrayList<ValueSetRow>();
            while(rs.next()){
                SelectItem item = new SelectItem();
                item.setLabel(rs.getString("ID"));
                item.setValue(rs.getString("ROLE_NAME"));
                value.add(item);
                ValueSetRow vsr = new ValueSetRow(rs.getString("ID"),rs.getString("ROLE_NAME"),rs.getString("ID"));
                list.add(vsr);
                this.roleName = rs.getString("ROLE_NAME");
            }
            this.roleLov = new DmsComBoxLov(list);
            ViewObject vo = ADFUtils.findIterator("DmsEnabledRoleIterator").getViewObject();
            String wc = " ROLE_NAME = '" + this.roleName + "'";
            vo.setWhereClause(wc);
            vo.executeQuery();
            if (vo.hasNext()) {
                Row row = vo.first();
                vo.setCurrentRow(row);
            }
//            AdfFacesContext.getCurrentInstance().addPartialTarget(this.authedTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void remove(ActionEvent actionEvent) {
        if(this.authedTable.getSelectedRowKeys() != null){
            Iterator iter = this.authedTable.getSelectedRowKeys().iterator();    
            RowSetIterator rsi = ADFUtils.findIterator("InfaRoleWorkflowVOIterator").getRowSetIterator();
            while(iter.hasNext()){
                List key = (List)iter.next();
                Row row = rsi.getRow((Key)key.get(0));
                if(row != null){
                    row.remove();    
                }
            }
            ADFUtils.findIterator("InfaRoleWorkflowVOIterator").getViewObject().getApplicationModule().getTransaction().commit();
            AdfFacesContext.getCurrentInstance().addPartialTarget(this.authedTable);
        }
    }

    public void showAddPop(ActionEvent actionEvent) {
        ViewObject unroleWfVO=ADFUtils.findIterator("InfaUnauthedWorkflowVOIterator").getViewObject();
        Row roleRow=ADFUtils.findIterator("DmsEnabledRoleIterator").getViewObject().getCurrentRow();
        if(roleRow!=null){
            unroleWfVO.setNamedWhereClauseParam("roleId", roleRow.getAttribute("Id"));
            unroleWfVO.executeQuery();
            RichPopup.PopupHints hint =new RichPopup.PopupHints();
            this.workflowPop.show(hint);
        }
    }

    public void setAuthedTable(RichTable authedTable) {
        this.authedTable = authedTable;
    }

    public RichTable getAuthedTable() {
        return authedTable;
    }

    public void setWorkflowPop(RichPopup workflowPop) {
        this.workflowPop = workflowPop;
    }

    public RichPopup getWorkflowPop() {
        return workflowPop;
    }

    public void addWf(ActionEvent actionEvent) {
        if(this.unAuthedTable.getSelectedRowKeys() != null){
            Iterator iter = this.unAuthedTable.getSelectedRowKeys().iterator();   
            ViewObject vo = ADFUtils.findIterator("InfaRoleWorkflowVOIterator").getViewObject();
            String roleId =(String)ADFUtils.findIterator("DmsEnabledRoleIterator").getViewObject().getCurrentRow().getAttribute("Id");
            RowSetIterator rsi = ADFUtils.findIterator("InfaUnauthedWorkflowVOIterator").getRowSetIterator();
            while(iter.hasNext()){
                List key = (List)iter.next();
                Row unRow = rsi.getRow((Key)key.get(0));
                Row row = vo.createRow();
                row.setAttribute("RoleId", roleId);
                row.setAttribute("WorkflowId", unRow.getAttribute("Id"));
                vo.insertRow(row);
            }
            vo.getApplicationModule().getTransaction().commit();
            ADFUtils.findIterator("InfaRoleWorkflowVOIterator").getViewObject().executeQuery();
            ADFUtils.findIterator("InfaUnauthedWorkflowVOIterator").getViewObject().executeQuery();
            AdfFacesContext.getCurrentInstance().addPartialTarget(this.authedTable);
            AdfFacesContext.getCurrentInstance().addPartialTarget(this.unAuthedTable);
        }
    }

    public void setUnAuthedTable(RichTable unAuthedTable) {
        this.unAuthedTable = unAuthedTable;
    }

    public RichTable getUnAuthedTable() {
        return unAuthedTable;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleLov(DmsComBoxLov roleLov) {
        this.roleLov = roleLov;
    }

    public DmsComBoxLov getRoleLov() {
        return roleLov;
    }
}
