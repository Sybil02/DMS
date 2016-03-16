package infa;

import common.DmsUtils;

import common.JSFUtils;

import javax.faces.event.ActionEvent;

import oracle.adf.view.rich.component.rich.RichPopup;

public class InfaEditBean {
    private RichPopup paramPop;

    public InfaEditBean() {
        super();
    }

    public void setParamPop(RichPopup paramPop) {
        this.paramPop = paramPop;
    }

    public RichPopup getParamPop() {
        return paramPop;
    }

    public void paramSelect(ActionEvent actionEvent) {
        if(DmsUtils.getInfaApplicationModule().getTransaction().isDirty()){
            JSFUtils.addFacesInformationMessage(DmsUtils.getMsg("dms.common.save_data_alert"));
        }else{
            RichPopup.PopupHints hints = new RichPopup.PopupHints();
            this.paramPop.show(hints);  
        }
    }
}
