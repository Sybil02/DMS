package dcm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import java.util.Map;

import org.apache.myfaces.trinidad.model.CollectionModel;
import org.apache.myfaces.trinidad.model.RowKeySet;
import org.apache.myfaces.trinidad.model.RowKeySetImpl;

public class DcmDataTableModel extends CollectionModel{
    private List<Map> data;
    private List<ColumnDef> colsdef;
    private int curRow;
    public static final String OPERATE_CREATE="CREATE";
    public static final String OPERATE_UPDATE="UPDATE";
    public static final String OPERATE_DELETE="DELETE";
    private RowKeySet selectedRows=new RowKeySetImpl();
        
    public DcmDataTableModel(){
    }
    public Object getRowKey() {
        return this.curRow==-1? null:this.curRow;
    }

    public void setRowKey(Object object) {
        this.curRow=null==object? -1 : (Integer)object;
    }

    public boolean isRowAvailable() {
        return this.curRow<this.data.size()&&this.curRow>-1;
    }

    public int getRowCount() {
        return this.data.size();
    }

    public Object getRowData() {
        if(this.isRowAvailable()){
            return this.data.get(this.curRow);
        }
        return null;
    }

    public int getRowIndex() {
        return this.curRow;
    }

    public void setRowIndex(int i) {
        this.curRow=i;
    }

    public Object getWrappedData() {
        return this.data;
    }

    public void setWrappedData(Object object) {
        this.data = (List<Map>)object;
    }

    public void setColsdef(List<ColumnDef> colsdef) {
        this.colsdef = colsdef;
    }

    public List<ColumnDef> getColsdef() {
        return colsdef;
    }

    public void setSelectedRows(RowKeySet selectedRows) {
        this.selectedRows = selectedRows;
    }

    public RowKeySet getSelectedRows() {
        return selectedRows;
    }
}
