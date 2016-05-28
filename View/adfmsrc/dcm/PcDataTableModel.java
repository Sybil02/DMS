package dcm;

import java.util.List;

import java.util.Map;

import org.apache.myfaces.trinidad.model.CollectionModel;
import org.apache.myfaces.trinidad.model.RowKeySet;
import org.apache.myfaces.trinidad.model.RowKeySetImpl;

public class PcDataTableModel extends CollectionModel{
    private List<Map> data;
    private Map<String,String> labelMap;
    private List<PcColumnDef> pcColsDef;
    private int curRow = 0;
    public static final String OPERATE_CREATE="CREATE";
    public static final String OPERATE_UPDATE="UPDATE";
    public static final String OPERATE_DELETE="DELETE";
    private RowKeySet selectedRows=new RowKeySetImpl();
    
    public PcDataTableModel() {
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

    public void setData(List<Map> data) {
        this.data = data;
    }

    public List<Map> getData() {
        return data;
    }

    public void setPcColsDef(List<PcColumnDef> pcColsDef) {
        this.pcColsDef = pcColsDef;
    }

    public List<PcColumnDef> getPcColsDef() {
        return pcColsDef;
    }

    public void setCurRow(int curRow) {
        this.curRow = curRow;
    }

    public int getCurRow() {
        return curRow;
    }

    public static String getOPERATE_CREATE() {
        return OPERATE_CREATE;
    }

    public static String getOPERATE_UPDATE() {
        return OPERATE_UPDATE;
    }

    public static String getOPERATE_DELETE() {
        return OPERATE_DELETE;
    }

    public void setSelectedRows(RowKeySet selectedRows) {
        this.selectedRows = selectedRows;
    }

    public RowKeySet getSelectedRows() {
        return selectedRows;
    }

    public void setLabelMap(Map<String, String> labelMap) {
        this.labelMap = labelMap;
    }

    public Map<String, String> getLabelMap() {
        return labelMap;
    }
}
