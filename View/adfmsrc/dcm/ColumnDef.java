package dcm;

import oracle.adf.view.rich.model.ListOfValuesModel;

import oracle.jbo.domain.Date;
import oracle.jbo.domain.Number;

import team.epm.dcm.view.DcmTemplateColumnViewRowImpl;

public class ColumnDef {
    public ColumnDef(DcmTemplateColumnViewRowImpl row){
        this.id=row.getId();
        this.locale=row.getLocale();
        this.columnLabel=row.getColumnLabel();
        this.dbTableCol=row.getDbTableCol();
        this.createdAt=row.getCreatedAt();
        this.updatedAt=row.getUpdatedAt();
        this.updatedBy=row.getUpdatedBy();
        this.createdBy=row.getCreatedBy();
        this.isPk=row.getIsPk();
        this.readonly=row.getReadonly();
        this.dataType=row.getDataType();
        this.visible=row.getVisible();
        this.seq=row.getSeq();
        this.templateId=row.getTemplateId();
        this.valueSetId=row.getValueSetId();
        this.rollingMonth=row.getRollingMonth();
        this.rollingAttr=row.getRollingAttr();
        
    } 
    private ListOfValuesModel model;
    private String id;
    private String locale;
    private String columnLabel;
    private String dbTableCol;
    private Date createdAt;
    private Date updatedAt;
    private String updatedBy;
    private String createdBy;
    private String isPk;
    private String readonly;
    private String dataType;
    private String visible;
    private Number seq;
    private String templateId;
    private String valueSetId;
    private Number rollingMonth;
    private String rollingAttr;
    private int countNum = 0;
    
    
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

    public void setColumnLabel(String columnLabel) {
        this.columnLabel = columnLabel;
    }

    public String getColumnLabel() {
        return columnLabel;
    }

    public void setDbTableCol(String dbTableCol) {
        this.dbTableCol = dbTableCol;
    }

    public String getDbTableCol() {
        return dbTableCol;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setIsPk(String isPk) {
        this.isPk = isPk;
    }

    public String getIsPk() {
        return isPk;
    }

    public void setReadonly(String readonly) {
        this.readonly = readonly;
    }

    public String getReadonly() {
        return readonly;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getDataType() {
        return dataType;
    }

    public void setVisible(String visible) {
        this.visible = visible;
    }

    public String getVisible() {
        return visible;
    }

    public void setSeq(Number seq) {
        this.seq = seq;
    }

    public Number getSeq() {
        return seq;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setValueSetId(String valueSetId) {
        this.valueSetId = valueSetId;
    }

    public String getValueSetId() {
        return valueSetId;
    }

    public void setRollingMonth(Number rollingMonth) {
        this.rollingMonth = rollingMonth;
    }

    public Number getRollingMonth() {
        return rollingMonth;
    }

    public void setRollingAttr(String rollingAttr) {
        this.rollingAttr = rollingAttr;
    }

    public String getRollingAttr() {
        return rollingAttr;
    }

    public void setCountNum(int countNum) {
        this.countNum = countNum;
    }

    public int getCountNum() {
        return countNum;
    }

    public void setModel(ListOfValuesModel model) {
        this.model = model;
    }

    public ListOfValuesModel getModel() {
        return model;
    }
}
