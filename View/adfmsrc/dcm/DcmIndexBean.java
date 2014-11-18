package dcm;

import dcm.template.TemplateTreeModel;

import dms.Menu.MenuBean;
import dms.Menu.MenuTreeModel;

import oracle.adf.share.logging.ADFLogger;

import oracle.adf.view.rich.component.rich.layout.RichPanelStretchLayout;

import org.apache.myfaces.trinidad.event.SelectionEvent;

public class DcmIndexBean {
    private TemplateTreeModel model;
    private static ADFLogger _logger =
        ADFLogger.createADFLogger(MenuBean.class);
    private RichPanelStretchLayout panelStretchLayout;

    public void setModel(TemplateTreeModel model) {
        this.model = model;
    }

    public TemplateTreeModel getModel() {
        if (null == this.model) {
            model = new TemplateTreeModel();
        }
        return this.model;
    }

    public void selectListener(SelectionEvent selectionEvent) {
        // Add event code here...
    }

    public void setPanelStretchLayout(RichPanelStretchLayout panelStretchLayout) {
        this.panelStretchLayout = panelStretchLayout;
    }

    public RichPanelStretchLayout getPanelStretchLayout() {
        return panelStretchLayout;
    }
}
