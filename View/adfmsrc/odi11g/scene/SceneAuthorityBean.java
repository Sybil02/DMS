package odi11g.scene;

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
import oracle.adf.view.rich.component.rich.nav.RichCommandButton;

import oracle.adf.view.rich.context.AdfFacesContext;

import oracle.jbo.Key;
import oracle.jbo.Row;
import oracle.jbo.RowSetIterator;
import oracle.jbo.ViewObject;
import oracle.jbo.server.DBTransaction;

public class SceneAuthorityBean {
    private RichPopup popup;
    private RichTable unassignedScene;
    private RichTable assignedScene;
    private String roleName;
    private Person person =
            (Person)ADFContext.getCurrent().getSessionScope().get("cur_user");
    private DmsComBoxLov roleLov;

    public SceneAuthorityBean() {
        this.initinitRoleLov();
    }

    public void initinitRoleLov(){
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
    public void setPopup(RichPopup popup) {
        this.popup = popup;
    }

    public RichPopup getPopup() {
        return popup;
    }

    public void showAddPopup(ActionEvent actionEvent) {
        ViewObject unroleSceneView=ADFUtils.findIterator("Odi11UnauthedSceneViewIterator").getViewObject();
        Row roleRow=ADFUtils.findIterator("DmsEnabledRoleIterator").getViewObject().getCurrentRow();
        if(roleRow!=null){
            unroleSceneView.setNamedWhereClauseParam("roleId", roleRow.getAttribute("Id"));
            unroleSceneView.executeQuery();
            RichPopup.PopupHints hint =new RichPopup.PopupHints();
            this.popup.show(hint);
        }
    }

    public void remove(ActionEvent actionEvent) {
        if (this.assignedScene.getSelectedRowKeys() != null) {
            Iterator itr =
                this.assignedScene.getSelectedRowKeys().iterator();
            RowSetIterator rowSetIterator =
                ADFUtils.findIterator("Odi11RoleSceneViewIterator").getRowSetIterator();
            while(itr.hasNext()){
                List key = (List)itr.next();
                Row row = rowSetIterator.getRow((Key)key.get(0));
                if(row!=null){
                    row.remove();
                }
            }
            ADFUtils.findIterator("Odi11RoleSceneViewIterator").getViewObject().getApplicationModule().getTransaction().commit();
            AdfFacesContext.getCurrentInstance().addPartialTarget(this.assignedScene);
        } 
    }

    public void addScene(ActionEvent actionEvent) {
        if (this.unassignedScene.getSelectedRowKeys() != null) {
            ViewObject roleSceneVo =ADFUtils.findIterator("Odi11RoleSceneViewIterator").getViewObject();
            String roleId =(String)ADFUtils.findIterator("DmsEnabledRoleIterator").getViewObject().getCurrentRow().getAttribute("Id");
            Iterator itr = this.unassignedScene.getSelectedRowKeys().iterator();
            RowSetIterator rowSetIterator =
                ADFUtils.findIterator("Odi11UnauthedSceneViewIterator").getRowSetIterator();
            while (itr.hasNext()) {
                List key = (List)itr.next();
                Row sceneRow = rowSetIterator.getRow((Key)key.get(0));
                Row row = roleSceneVo.createRow();
                row.setAttribute("RoleId", roleId);
                row.setAttribute("SceneId", sceneRow.getAttribute("Id"));
                roleSceneVo.insertRow(row);
            }
            roleSceneVo.getApplicationModule().getTransaction().commit();
            ADFUtils.findIterator("Odi11RoleSceneViewIterator").getViewObject().executeQuery();
            ADFUtils.findIterator("Odi11UnauthedSceneViewIterator").getViewObject().executeQuery();
            AdfFacesContext.getCurrentInstance().addPartialTarget(this.assignedScene);
            AdfFacesContext.getCurrentInstance().addPartialTarget(this.unassignedScene);
        }
    }

    public void setUnassignedScene(RichTable unassignedScene) {
        this.unassignedScene = unassignedScene;
    }

    public RichTable getUnassignedScene() {
        return unassignedScene;
    }

    public void setAssignedScene(RichTable assignedScene) {
        this.assignedScene = assignedScene;
    }

    public RichTable getAssignedScene() {
        return assignedScene;
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
