package odi11g;

import common.ADFUtils;

import oracle.adf.share.logging.ADFLogger;
import oracle.adf.view.rich.component.rich.data.RichTable;
import oracle.adf.view.rich.component.rich.data.RichTree;

import oracle.adf.view.rich.context.AdfFacesContext;

import oracle.jbo.ViewObject;

import org.apache.myfaces.trinidad.event.SelectionEvent;
import org.apache.myfaces.trinidad.model.RowKeySet;

public class Odi11gIndexMBean {
    private Odi11gCatTreeModel model;
    private static ADFLogger _logger =
        ADFLogger.createADFLogger(Odi11gIndexMBean.class);
    private RichTable sceneTable;

    public void setModel(Odi11gCatTreeModel model) {
        this.model = model;
    }

    public Odi11gCatTreeModel getModel() {
        if (null == this.model) {
            model = new Odi11gCatTreeModel();
        }
        return this.model;
    }

    public void selectListener(SelectionEvent selectionEvent) {
        RichTree tree = (RichTree)selectionEvent.getSource();
        RowKeySet rks = selectionEvent.getAddedSet();
        if (rks != null) {
            int setSize = rks.size();
            if (setSize == 0) {
                return;
            }
            Object rowKey = null;
            Odi11gCatTreeItem rowData = null;
            rowKey = rks.iterator().next();
            tree.setRowKey(rowKey);
            rowData = (Odi11gCatTreeItem)tree.getRowData();
            ViewObject vo=ADFUtils.findIterator("Odi11AuthedSceneViewIterator").getViewObject();
            vo.setNamedWhereClauseParam("catId", rowData.getId());
            vo.executeQuery();
            AdfFacesContext.getCurrentInstance().addPartialTarget(this.sceneTable);
        }
    }

    public void setSceneTable(RichTable sceneTable) {
        this.sceneTable = sceneTable;
    }

    public RichTable getSceneTable() {
        return sceneTable;
    }
}
