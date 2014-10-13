package dms.view.uievent;

import common.ADFUtils;

import oracle.adf.model.BindingContext;
import oracle.adf.view.rich.event.DialogEvent;
import oracle.adf.view.rich.event.PopupCanceledEvent;
import oracle.adf.view.rich.event.PopupFetchEvent;

import oracle.binding.BindingContainer;
import oracle.binding.OperationBinding;

public class PopupUserBean {
    public PopupUserBean() {
        super();
    }

    public void editDialogListener(DialogEvent dialogEvent) {
        if (dialogEvent.getOutcome().name().equals("ok")) {
            OperationBinding operationBinding = ADFUtils.findOperation("Commit");
            operationBinding.execute();   
        } else if (dialogEvent.getOutcome().name().equals("cancel")) {
            BindingContainer bc=BindingContext.getCurrent().getCurrentBindingsEntry();  
            OperationBinding operationBinding =bc.getOperationBinding("Rollback");
           //OperationBinding operationBinding = ADFUtils.findOperation("Rollback");
            operationBinding.execute();
        }
    }
    public void editOrInsertPopupFetchListener(PopupFetchEvent popupFetchEvent) {
        // Add event code here...
        if(popupFetchEvent.getLaunchSourceClientId().contains("ctbinsert")){
            OperationBinding operationBinding=ADFUtils.findOperation("CreateInsert");
            operationBinding.execute();
        }
    }
    

    public void editOrInsertPopupCancelListener(PopupCanceledEvent popupCanceledEvent) {
        BindingContainer bc=BindingContext.getCurrent().getCurrentBindingsEntry();  
        OperationBinding operationBinding =bc.getOperationBinding("Rollback");
      //OperationBinding operationBinding = ADFUtils.findOperation("Rollback");
        operationBinding.execute();
    }
    
    public void deleteDialogListener(DialogEvent dialogEvent) {
            if (dialogEvent.getOutcome().equals(DialogEvent.Outcome.ok)) {
                doDelete();
            }
        }
    private void doDelete() {
            OperationBinding operb = ADFUtils.findOperation("Delete");
            Object result = operb.execute();
            if (operb.getErrors().isEmpty()) {
                operb = ADFUtils.findOperation("Commit");
                operb.execute();
            }
        }
}
