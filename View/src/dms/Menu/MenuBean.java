package dms.Menu;

import dms.dynamicShell.TabContext;
import dms.dynamicShell.TabContext.TabOverflowException;

import oracle.adf.controller.TaskFlowId;

import oracle.adf.share.logging.ADFLogger;
import oracle.adf.share.logging.ADFLoggerFactory;
import oracle.adf.view.rich.component.rich.data.RichTree;

import org.apache.myfaces.trinidad.event.SelectionEvent;
import org.apache.myfaces.trinidad.model.RowKeySet;

public class MenuBean {
    private StaticMenuTreeModel model;
    private static ADFLogger _logger =
        ADFLogger.createADFLogger(MenuBean.class);

    public void setModel(StaticMenuTreeModel model) {
        this.model = model;
    }

    public StaticMenuTreeModel getModel() {
        if (null == this.model) {
            model = new StaticMenuTreeModel();
        }
        return model;
    }

    public void selectMenu(SelectionEvent selectionEvent) {
        RichTree tree = (RichTree)selectionEvent.getSource();
        RowKeySet rks = selectionEvent.getAddedSet();
        if (rks != null) {
            int setSize = rks.size();
            if (setSize == 0) {
                return;
            }
            Object rowKey = null;
            MenuItem rowData = null;
            rowKey = rks.iterator().next();
            tree.setRowKey(rowKey);
            rowData = (MenuItem)tree.getRowData();
            if (rowData.getAction() != null) {
                try {
                    TabContext.getCurrentInstance().addOrSelectTab(rowData.getLabel(),
                                                                   rowData.getAction());
                } catch (TabContext.TabOverflowException e) {
                    _logger.severe(e);
                }
            }
        }
    }
}
