package odi11g.scene;

import common.ADFUtils;

import common.DmsUtils;
import common.JSFUtils;

import javax.faces.event.ActionEvent;

import oracle.adf.view.rich.component.rich.RichPopup;

import oracle.jbo.Row;
import oracle.jbo.ViewObject;

public class SceneMBean {
    private RichPopup selectParamPopup;

    public SceneMBean() {
    }

    public void selectParam(ActionEvent actionEvent) {
        ViewObject sceneParamVO=ADFUtils.findIterator("Odi11SceneParamViewIterator").getViewObject();
        Row curSceneRow=ADFUtils.findIterator("Odi11SceneViewIterator").getViewObject().getCurrentRow();
        if(sceneParamVO.getApplicationModule().getTransaction().isDirty()){
            JSFUtils.addFacesErrorMessage(DmsUtils.getMsg("dms.common.save_data_alert"));   
        }
        else if(curSceneRow !=null){
            sceneParamVO.setNamedWhereClauseParam("sceneId", curSceneRow.getAttribute("Id"));
            sceneParamVO.executeQuery();
            RichPopup.PopupHints hint=new  RichPopup.PopupHints();
            this.selectParamPopup.show(hint);
        }
    }

    public void setSelectParamPopup(RichPopup selectParamPopup) {
        this.selectParamPopup = selectParamPopup;
    }

    public RichPopup getSelectParamPopup() {
        return selectParamPopup;
    }

    public void addSceneParam(ActionEvent actionEvent) {
        ViewObject sceneParamVO=ADFUtils.findIterator("Odi11SceneParamViewIterator").getViewObject();
        Row curSceneRow=ADFUtils.findIterator("Odi11SceneViewIterator").getViewObject().getCurrentRow();
        if(curSceneRow!=null){
            Row row= sceneParamVO.createRow();
            row.setAttribute("SceneId", curSceneRow.getAttribute("Id"));
            sceneParamVO.insertRow(row);
        }
    }
}
