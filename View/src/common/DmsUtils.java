package common;

import java.util.ResourceBundle;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import oracle.adf.model.binding.DCIteratorBinding;
import oracle.adf.view.rich.component.rich.data.RichTable;

import oracle.jbo.Key;
import oracle.jbo.uicli.binding.JUCtrlHierBinding;

import oracle.jbo.uicli.binding.JUCtrlHierNodeBinding;

import org.apache.myfaces.trinidad.event.SelectionEvent;
import org.apache.myfaces.trinidad.model.CollectionModel;

import team.epm.module.DcmModuleImpl;
import team.epm.module.DmsModuleImpl;
import team.epm.module.Odi11gModuleImpl;

public class DmsUtils {
    public DmsUtils() {
        super();
    }

    public static String getMsg(String key) {
        FacesContext context = FacesContext.getCurrentInstance();
        ResourceBundle bundle =
            context.getApplication().getResourceBundle(context, "dms_v");
        return bundle.getString(key);
    }

    public static void makeCurrent(SelectionEvent selectionEvent) {

        RichTable _table = (RichTable)selectionEvent.getSource();
        //the Collection Model is the object that provides the
        //structured data
        //for the table to render
        CollectionModel _tableModel = (CollectionModel)_table.getValue();
        //the ADF object that implements the CollectionModel is
        //JUCtrlHierBinding. It is wrapped by the CollectionModel API
        JUCtrlHierBinding _adfTableBinding =
            (JUCtrlHierBinding)_tableModel.getWrappedData();
        //Acess the ADF iterator binding that is used with
        //ADF table binding
        DCIteratorBinding _tableIteratorBinding =
            _adfTableBinding.getDCIteratorBinding();

        //the role of this method is to synchronize the table component
        //selection with the selection in the ADF model
        Object _selectedRowData = _table.getSelectedRowData();
        //cast to JUCtrlHierNodeBinding, which is the ADF object
        //that represents a row
        JUCtrlHierNodeBinding _nodeBinding =
            (JUCtrlHierNodeBinding)_selectedRowData;
        //get the row key from the node binding and set it
        //as the current row in the iterator
        Key _rwKey = _nodeBinding.getRowKey();
        _tableIteratorBinding.setCurrentRowWithKey(_rwKey.toStringFormat(true));
    }
    public static DmsModuleImpl getDmsApplicationModule(){
        return (DmsModuleImpl)ADFUtils.getApplicationModuleForDataControl("DmsModuleDataControl");
    }
    public static DcmModuleImpl getDcmApplicationModule(){
        return (DcmModuleImpl)ADFUtils.getApplicationModuleForDataControl("DcmModuleDataControl");
    }
    public static Odi11gModuleImpl getOdi11gApplicationModule(){
        return (Odi11gModuleImpl)ADFUtils.getApplicationModuleForDataControl("Odi11gModuleDataControl");
    }
}
