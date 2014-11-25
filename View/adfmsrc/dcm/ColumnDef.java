package dcm;

public class ColumnDef {
    private String code;
    private String label;
    private String type;
    private String readonly;
    private String isPk;
    private String visible;
    public ColumnDef(){
        
    }
    public ColumnDef(String code,String label,String type,String readonly){
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

    public void setReadonly(String readonly) {
        this.readonly = readonly;
    }

    public String isReadonly() {
        return readonly;
    }

    public void setIsPk(String isPk) {
        this.isPk = isPk;
    }

    public String getIsPk() {
        return isPk;
    }

    public void setVisible(String visible) {
        this.visible = visible;
    }

    public String getVisible() {
        return visible;
    }
}
