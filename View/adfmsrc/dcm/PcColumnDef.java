package dcm;

import oracle.jbo.domain.Number;

import team.epm.dcm.view.DcmTemplateColumnViewRowImpl;

public class PcColumnDef {
    public PcColumnDef(DcmTemplateColumnViewRowImpl row) {
        this.id=row.getId();
        this.locale=row.getLocale();
        this.columnLable=row.getColumnLabel();
        this.dbTableCol= row.getDbTableCol();
        this.dataType = row.getDataType();
        
    }
    
    private String id;
    private String locale;
    private String columnLable;
    private String dbTableCol;
    private boolean isReadonly;
    private String dataType;

    public PcColumnDef(String columnLable, String dbTableCol,boolean isReadonly,String dataType) {
        this.columnLable = columnLable;
        this.dbTableCol = dbTableCol;
        this.isReadonly = isReadonly;
        this.dataType = dataType;
    }


    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getLocale() {
        return locale;
    }

    public void setColumnLable(String columnLable) {
        this.columnLable = columnLable;
    }

    public String getColumnLable() {
        return columnLable;
    }

    public void setDbTableCol(String dbTableCol) {
        this.dbTableCol = dbTableCol;
    }

    public String getDbTableCol() {
        return dbTableCol;
    }

    public void setIsReadonly(boolean isReadonly) {
        this.isReadonly = isReadonly;
    }

    public boolean isIsReadonly() {
        return isReadonly;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getDataType() {
        return dataType;
    }
}
