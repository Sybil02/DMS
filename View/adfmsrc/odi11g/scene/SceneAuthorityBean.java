package odi11g.scene;

import common.ADFUtils;

import java.util.Iterator;
import java.util.List;

import javax.faces.event.ActionEvent;

import oracle.adf.view.rich.component.rich.RichPopup;
import oracle.adf.view.rich.component.rich.data.RichTable;
import oracle.adf.view.rich.component.rich.nav.RichCommandButton;

import oracle.adf.view.rich.context.AdfFacesContext;

import oracle.jbo.Key;
import oracle.jbo.Row;
import oracle.jbo.RowSetIterator;
import oracle.jbo.ViewObject;

public class SceneAuthorityBean {
    private RichPopup popup;
    private RichTable unassignedScene;
    private RichTable assignedScene;

    public SceneAuthorityBean() {
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
}
