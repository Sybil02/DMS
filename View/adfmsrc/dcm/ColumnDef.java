package dcm;

public class ColumnDef {
    private String code;
    private String label;
    private String type;
    private boolean readonly;
    public ColumnDef(){
        
    }
    public ColumnDef(String code,String label,String type,boolean readonly){
        this.code=code;
        this.label=label;
        this.type=type;
        this.readonly=readonly;
    }
    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

    public boolean isReadonly() {
        return readonly;
    }
}
