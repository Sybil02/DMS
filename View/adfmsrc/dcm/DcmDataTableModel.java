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
    private RowKeySet selectedRows;
        
    public DcmDataTableModel(){
        ColumnDef cols01=new ColumnDef("ID","标识","String",false);
        ColumnDef cols02=new ColumnDef("NAME","名称","String",false);
        this.colsdef=new ArrayList<ColumnDef>();
        this.colsdef.add(cols01);
        this.colsdef.add(cols02);
        
        this.data=new ArrayList<Map>();
        
        Map row01=new HashMap();
        row01.put("ID","01");
        row01.put("NAME","百度");
        this.data.add(row01);
        
        Map row02=new HashMap();
        row02.put("ID","02");
        row02.put("NAME","腾讯");
        this.data.add(row02);
        
        Map row03=new HashMap();
        row03.put("ID","03");
        row03.put("NAME","阿里");
        this.data.add(row03);
        
        this.selectedRows=new RowKeySetImpl();
        this.selectedRows.add(1);
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
