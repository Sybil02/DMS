package common;

import javax.faces.event.PhaseEvent;

import javax.faces.event.PhaseId;

import oracle.adf.controller.internal.binding.DCTaskFlowBinding;
import oracle.adf.model.BindingContext;
import oracle.adf.model.binding.DCBindingContainer;
import oracle.adf.model.binding.DCDataControl;
import oracle.adf.share.logging.ADFLogger;

import oracle.adf.view.rich.context.AdfFacesContext;

import oracle.binding.BindingContainer;

//import oracle.ui.pattern.dynamicShell.Tab;
import dms.dynamicShell.Tab;
//import oracle.ui.pattern.dynamicShell.TabContext;
import dms.dynamicShell.TabContext;


public class CheckPhaseListener implements javax.faces.event.PhaseListener {
    public CheckPhaseListener() {
        super();
    }
    private static ADFLogger _logger = ADFLogger.createADFLogger(CheckPhaseListener.class);
    public static final String PAGE_TEMPLATE_BINDING = "ptb1";

    @Override
    /**
    * While refreshing the curent tab seems more effective to do only just before render response
    * it turns out that when closing a tab, and then returning to a dirty tab, the data control
    * of the dirty tab is no longer seen as dirty, so we do it after the three phases that can
    * change the state of the current tab: apply request values, update model values and invoke application
    */
    public void afterPhase(PhaseEvent event) {
        if (event.getPhaseId() == PhaseId.APPLY_REQUEST_VALUES || event.getPhaseId() == PhaseId.UPDATE_MODEL_VALUES ||
            event.getPhaseId() == PhaseId.INVOKE_APPLICATION) {
            System.out.println("AFTER PHASE!");
            checkCurrentTabDirtyState();
        }
    }

    @Override
    public void beforePhase(PhaseEvent event) {
        System.out.println("beforePhase.....................................................................");
    }

    @Override
    public PhaseId getPhaseId() {
        return PhaseId.ANY_PHASE;
    }

    public void checkCurrentTabDirtyState() {
        System.out.println("tabContext");
        TabContext tabContext = TabContext.getCurrentInstance();
        if (tabContext == null || tabContext.getSelectedTabIndex() < 0 || BindingContext.getCurrent() == null) {
            return;
        }
        System.out.println("bc");
        BindingContainer bc = BindingContext.getCurrent().getCurrentBindingsEntry();
        if (bc == null) {
            return;
        }
        System.out.println("pageTemplateBc");
        DCBindingContainer pageTemplateBc = (DCBindingContainer) bc.get(PAGE_TEMPLATE_BINDING);
        if (pageTemplateBc == null) {
            return;
        }
        System.out.println("tfb");
        DCTaskFlowBinding tfb = (DCTaskFlowBinding) pageTemplateBc.get("r" + tabContext.getSelectedTabIndex());
        if (tfb == null || tfb.getExecutableBindings() == null || tfb.getExecutableBindings().size() == 0) {
            return;
        }
        System.out.println("taskFlowBc");
        DCBindingContainer taskFlowBc = (DCBindingContainer) tfb.getExecutableBindings().get(0);
        DCDataControl dc = taskFlowBc.getDataControl();
        if (dc == null) {
            // no data control, we cannot detect pending changes
            return;
        }
        boolean isDirty = dc != null && (dc.isTransactionDirty() || dc.isTransactionModified());
        
        // calling covenience method markCurrentTabDirty adds content area as partial target,
        // causing any popups currently displayed to be hidden again.
        // Therefore retrieve current tab instance and call setDirty directly
        // tabContext.markCurrentTabDirty(isDirty);
        Tab tab = tabContext.getTabs().get(tabContext.getSelectedTabIndex());
        System.out.println("isDirty");
        if (tab.isDirty() != isDirty) {
            System.out.println("tab isssssssssssssssssss dirty");
            _logger.severe("Setting dirty state of dynamic tab with index " + tab.getIndex() + " to " + isDirty);
            tab.setDirty(isDirty);
            AdfFacesContext.getCurrentInstance().addPartialTarget(tabContext.getTabsNavigationPane());
        }else{
            System.out.println("tab nottttttttttttttt dirty");    
        }
    }
}

