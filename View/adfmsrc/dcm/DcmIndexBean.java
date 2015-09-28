package dcm;

import dcm.template.TemplateTreeItem;
import dcm.template.TemplateTreeModel;

import dms.dynamicShell.Tab;
import dms.dynamicShell.TabContext;

import java.util.HashMap;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import oracle.adf.share.ADFContext;
import oracle.adf.share.logging.ADFLogger;

import oracle.adf.view.rich.component.rich.data.RichTree;
import oracle.adf.view.rich.component.rich.layout.RichPanelStretchLayout;

import oracle.adf.view.rich.render.ClientEvent;

import org.apache.myfaces.trinidad.event.FocusEvent;
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


    public void treeSelectListener(SelectionEvent selectionEvent) {
        RichTree tree = (RichTree)selectionEvent.getComponent();

        RowKeySet rks = tree.getSelectedRowKeys();
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
                TabContext tabContext = TabContext.getCurrentInstance();
                //如果不是第一个页面，那么要除去第一个页面才行
                if (TemplateTreeItem.TYPE_TEMPLATE.equals(rowData.getType())) {
                    if (tabContext.getSelectedTabIndex() != -1) {

                        Tab currentTab =
                            tabContext.getTabs().get(tabContext.getSelectedTabIndex());
                        //如果当前页面等于要打开的页面，那么就默认不打开了，否则删除当前页面再打开新的页面
                        if (currentTab.getParameters().get("curTemplateId").equals(rowData.getId())) {
                            return;
                        }
                        tabContext.removeCurrentTab();

                        Map params = new HashMap();
                        params.put("curTemplateId", rowData.getId());
                        params.put("dmsTabContext",
                                   TabContext.getCurrentInstance());

                        //如果页面数据搜集页面要增加页面的话要先删除页面,不管是否确定删除页面后面的方法会导致新的页面出现
                        //所以是不行的，必须等到成功删除才会弹出新的页面，因此先把tab内容保存一下，确定删除就增加保存的tab
                        //如果不删除页面就删除保存了的tab

                        if (tabContext.isTagSetDirty()) {
                            tabContext.saveTab(rowData.getLabel(),
                                               "/WEB-INF/dcmData/data_display_tsk.xml#data_display_tsk",
                                               params);
                        } else {
                            TabContext.getCurrentInstance().addTab(rowData.getLabel(),
                                                                   "/WEB-INF/dcmData/data_display_tsk.xml#data_display_tsk",
                                                                   params);
                        }
                    } else { //如果是第一个页面
                        Map params = new HashMap();
                        params.put("curTemplateId", rowData.getId());
                        params.put("dmsTabContext",
                                   TabContext.getCurrentInstance());

                        TabContext.getCurrentInstance().addTab(rowData.getLabel(),
                                                               "/WEB-INF/dcmData/data_display_tsk.xml#data_display_tsk",
                                                               params);
                    }
                }
            } catch (TabContext.TabOverflowException e) {
                _logger.severe(e);
            }
        }
    }
}
