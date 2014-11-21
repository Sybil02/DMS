package dcm;

import dcm.template.TemplateTreeItem;
import dcm.template.TemplateTreeModel;

import dms.dynamicShell.TabContext;

import java.util.HashMap;
import java.util.Map;

import javax.faces.context.FacesContext;

import oracle.adf.share.ADFContext;
import oracle.adf.share.logging.ADFLogger;

import oracle.adf.view.rich.component.rich.data.RichTree;
import oracle.adf.view.rich.component.rich.layout.RichPanelStretchLayout;

import org.apache.myfaces.trinidad.event.SelectionEvent;
import org.apache.myfaces.trinidad.model.CollectionModel;
import org.apache.myfaces.trinidad.model.RowKeySet;

public class DcmIndexBean {
    private TemplateTreeModel model;
    private static ADFLogger _logger =
        ADFLogger.createADFLogger(DcmIndexBean.class);

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
        RichTree tree = (RichTree)selectionEvent.getSource();
        RowKeySet rks = selectionEvent.getAddedSet();
        if (rks != null) {
            int setSize = rks.size();
            if (setSize == 0) {
                return;
            }
            Object rowKey = null;
            TemplateTreeItem rowData = null;
            rowKey = rks.iterator().next();
            tree.setRowKey(rowKey);
            rowData = (TemplateTreeItem)tree.getRowData();
            try {
                if( TabContext.getCurrentInstance().getSelectedTabIndex()!=-1){
                    TabContext.getCurrentInstance().removeCurrentTab();
                }
                if(TemplateTreeItem.TYPE_TEMPLATE.equals(rowData.getType())){
                    Map params=new HashMap();
                    params.put("curTemplateId",rowData.getId());
                    TabContext.getCurrentInstance().addTab(rowData.getLabel(), "/WEB-INF/dcmData/data_display_tsk.xml#data_display_tsk",params);
                }
                
            } catch (TabContext.TabOverflowException e) {
                _logger.severe(e);
            }
        }
    }
}
