package dcm;

import dcm.template.TemplateTreeModel;

import dms.Menu.MenuBean;
import dms.Menu.MenuTreeModel;

import oracle.adf.share.logging.ADFLogger;

public class DcmIndexBean {
    private TemplateTreeModel model;
    private static ADFLogger _logger =
        ADFLogger.createADFLogger(MenuBean.class);

    public void setModel(TemplateTreeModel model) {
        this.model = model;
    }

    public TemplateTreeModel getModel() {
        if (null == this.model) {
            model = new TemplateTreeModel();
        }
        return this.model;
    }
}
