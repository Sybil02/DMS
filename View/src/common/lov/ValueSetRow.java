package common.lov;

public class ValueSetRow {
    public ValueSetRow(String code,String meaning,String rowId) {
        this.code = code;
        this.meaning = meaning;
        this.rowId = rowId;
    }
    
    private String code;
    private String meaning;
    private String rowId;

    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }

    public String getMeaning() {
        return meaning;
    }

    public void setRowId(String rowId) {
        this.rowId = rowId;
    }

    public String getRowId() {
        return rowId;
    }
}
