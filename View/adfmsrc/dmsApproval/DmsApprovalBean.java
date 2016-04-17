package dmsApproval;

import common.ADFUtils;

import common.DmsUtils;
import common.JSFUtils;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import javax.faces.event.ValueChangeEvent;

import oracle.adf.share.ADFContext;
import oracle.adf.view.rich.component.rich.RichPopup;

import oracle.adf.view.rich.component.rich.output.RichOutputText;

import oracle.jbo.Row;
import oracle.jbo.ViewObject;
import oracle.jbo.server.ViewRowImpl;

public class DmsApprovalBean {
    private RichPopup approvalPop;

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
        String valuesId = row.getAttribute("ValueSetId").toString();
        
        RichPopup.PopupHints hints = new RichPopup.PopupHints();
        this.approvalPop.show(hints);
    }

    public void setApprovalPop(RichPopup approvalPop) {
        this.approvalPop = approvalPop;
    }

    public RichPopup getApprovalPop() {
        return approvalPop;
    }

}
