package dcm.template;

import oracle.jbo.domain.Date;
import oracle.jbo.domain.Number;

import team.epm.dcm.view.DcmTemplateViewRowImpl;

public class TemplateEO {
    public TemplateEO(DcmTemplateViewRowImpl row) {
        this.Id=row.getId();
        this.locale=row.getLocale();
        this.createdAt=row.getCreatedAt();
        this.updatedAt=row.getUpdatedAt();
        this.createdBy=row.getCreatedBy();
        this.updatedBy=row.getUpdatedBy();
        this.readonly=row.getReadonly();
        this.seq=row.getSeq();
        this.description=row.getDescription();
        this.dbTable=row.getDbTable();
        this.dbView=row.getDbView();
        this.tmpTable=row.getTmpTable();
        this.preProgram=row.getPreProgram();
        this.handleProgram=row.getHandleProgram();
        this.afterProgram=row.getAfterProgram();
        this.handleMode=row.getHandleMode();
        this.templateFile=row.getTemplateFile();
        this.dataStartLine=row.getDataStartLine();
        this.combinationId=row.getCombinationId();
        this.categoryId=row.getCategoryId();
        this.name=row.getName();
    }
    private String Id;
    private String locale;
    private String name;
    private Date createdAt;
    private Date updatedAt;
    private String updatedBy;
    private String createdBy;
    private String readonly;
    private Number seq;
    private String description;
    private String dbTable;
    private String dbView;
    private String tmpTable;
    private String preProgram;
    private String handleProgram;
    private String afterProgram;
    private String handleMode;
    private String templateFile;
    private Number dataStartLine;
    private String combinationId;
    private String categoryId;

    public void setId(String Id) {
        this.Id = Id;
    }

    public String getId() {
        return Id;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getLocale() {
        return locale;
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

    public void setReadonly(String readonly) {
        this.readonly = readonly;
    }

    public String getReadonly() {
        return readonly;
    }

    public void setSeq(Number seq) {
        this.seq = seq;
    }

    public Number getSeq() {
        return seq;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDbTable(String dbTable) {
        this.dbTable = dbTable;
    }

    public String getDbTable() {
        return dbTable;
    }

    public void setDbView(String dbView) {
        this.dbView = dbView;
    }

    public String getDbView() {
        return dbView;
    }

    public void setTmpTable(String tmpTable) {
        this.tmpTable = tmpTable;
    }

    public String getTmpTable() {
        return tmpTable;
    }

    public void setPreProgram(String preProgram) {
        this.preProgram = preProgram;
    }

    public String getPreProgram() {
        return preProgram;
    }

    public void setHandleProgram(String handleProgram) {
        this.handleProgram = handleProgram;
    }

    public String getHandleProgram() {
        return handleProgram;
    }

    public void setAfterProgram(String afterProgram) {
        this.afterProgram = afterProgram;
    }

    public String getAfterProgram() {
        return afterProgram;
    }

    public void setHandleMode(String handleMode) {
        this.handleMode = handleMode;
    }

    public String getHandleMode() {
        return handleMode;
    }

    public void setTemplateFile(String templateFile) {
        this.templateFile = templateFile;
    }

    public String getTemplateFile() {
        return templateFile;
    }

    public void setDataStartLine(Number dataStartLine) {
        this.dataStartLine = dataStartLine;
    }

    public Number getDataStartLine() {
        return dataStartLine;
    }

    public void setCombinationId(String combinationId) {
        this.combinationId = combinationId;
    }

    public String getCombinationId() {
        return combinationId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
