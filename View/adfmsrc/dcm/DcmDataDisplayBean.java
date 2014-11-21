package dcm;

import common.DmsUtils;

import dcm.template.TemplateTreeItem;
import dcm.template.TemplateTreeModel;

import dms.dynamicShell.TabContext;

import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;

import javax.faces.validator.ValidatorException;

import oracle.adf.share.ADFContext;
import oracle.adf.share.logging.ADFLogger;
import oracle.adf.view.rich.component.rich.data.RichTable;
import oracle.adf.view.rich.component.rich.data.RichTree;
import oracle.adf.view.rich.component.rich.layout.RichPanelStretchLayout;

import org.apache.myfaces.trinidad.event.SelectionEvent;
import org.apache.myfaces.trinidad.model.CollectionModel;
import org.apache.myfaces.trinidad.model.RowKeySet;

public class DcmDataDisplayBean {
    private CollectionModel dataModel;
    private static ADFLogger _logger =
        ADFLogger.createADFLogger(DcmDataDisplayBean.class);

    public CollectionModel getDataModel() {
        if (null == this.dataModel) {
            this.dataModel = new DcmDataTableModel();
        }
        return this.dataModel;
    }

    public void valueChangeListener(ValueChangeEvent valueChangeEvent) {
        Map rowData=(Map)this.dataModel.getRowData();
        if(((DcmDataTableModel)this.dataModel).getSelectedRows().size()>1){
            String msg=DmsUtils.getMsg("dcm.msg.can_not_select_multiple_row");
            FacesMessage fm =new FacesMessage("",msg);
            //throw new ValidatorException(fm);
            FacesContext.getCurrentInstance().addMessage(null, fm);
            return;
        }
        if(!DcmDataTableModel.OPERATE_CREATE.equals(rowData.get("OPERATION"))){
            rowData.put("OPERATION", DcmDataTableModel.OPERATE_UPDATE);
        }          
    }

    public void rowSelectionListener(SelectionEvent selectionEvent) {
        // Add event code here...
        RichTable table = (RichTable)selectionEvent.getSource();
        RowKeySet rks = selectionEvent.getAddedSet();
        if (rks != null) {
            int setSize = rks.size();
            if (setSize == 0) {
                return;
            }
            Object rowKey = rks.iterator().next();
            table.setRowKey(rowKey);
        }
    }

    public String operation_new() {
        // Add event code here...
        return null;
    }

    public String operation_delete() {
        // Add event code here...
        return null;
    }

    public String operation_save() {
        // Add event code here...
        return null;
    }

    public String operation_reset() {
        // Add event code here...
        return null;
    }

    public String operation_import() {
        // Add event code here...
        return null;
    }

    public String operation_export() {
        // Add event code here...
        return null;
    }

    public String operation_download() {
        // Add event code here...
        return null;
    }
}
