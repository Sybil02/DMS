package dcm.combinantion;


import common.ADFUtils;


import javax.faces.event.ActionEvent;

import oracle.adf.model.BindingContext;
import oracle.adf.view.rich.component.rich.RichPopup;
import oracle.adf.view.rich.event.PopupCanceledEvent;
import oracle.adf.view.rich.event.PopupFetchEvent;

import oracle.binding.BindingContainer;
import oracle.binding.OperationBinding;

public class CombinationBean {
    private RichPopup popup;
    public CombinationBean() {
        super();
    }
    


    public void createInsertListener(PopupFetchEvent popupFetchEvent) {
        OperationBinding ob= ADFUtils.findOperation("CreateCombination");
//        BindingContainer bindings = getBindings();
//        OperationBinding operationBinding = bindings.getOperationBinding("CreateCombination");
//        operationBinding.execute();
        ob.execute();
        ob.getErrors();
 //       ADFUtils.findIterator("DcmCombinationView1Iterator").getViewObject().createRow();
    }

    public void closePopup(PopupCanceledEvent popupCanceledEvent) {
        OperationBinding ob= ADFUtils.findOperation("Rollback");
        ob.execute();
    }
    
    public void commit(){
        OperationBinding ob= ADFUtils.findOperation("Commit");
        ob.execute();
        popup.cancel();
    }
    
    public void setPopup(RichPopup popup) {
        this.popup = popup;
    }

    public RichPopup getPopup() {
        return popup;
    }

    public void commit(ActionEvent actionEvent) {
        OperationBinding ob= ADFUtils.findOperation("Commit");
        ADFUtils.findIterator("DcmCombinationView1Iterator").getViewObject().getRowSet();
        ob.execute();
    }
    public BindingContainer getBindings() {
        return BindingContext.getCurrent().getCurrentBindingsEntry();
    }
}
