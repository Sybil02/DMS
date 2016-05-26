package dcm.template;

import dcm.ColumnDef;

import java.util.List;

public class TemplateEntity {
    private int startLine;
    private String templateId;
    private String temptable;
    private int columnSize;
    private String templateName;
    private List<ColumnDef> colsdef;

    public TemplateEntity(String templateId,String templateName, String temptable,
                          int columnSize, int startLine) {
        this.startLine = startLine;
        this.templateId = templateId;
        this.temptable = temptable;
        this.columnSize = columnSize;
        this.templateName = templateName;
    }

    public void setStartLine(int startLine) {
        this.startLine = startLine;
    }

    public int getStartLine() {
        return startLine;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemptable(String temptable) {
        this.temptable = temptable;
    }

    public String getTemptable() {
        return temptable;
    }

    public void setColumnSize(int columnSize) {
        this.columnSize = columnSize;
    }

    public int getColumnSize() {
        return columnSize;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setColsdef(List<ColumnDef> colsdef) {
        this.colsdef = colsdef;
    }

    public List<ColumnDef> getColsdef() {
        return colsdef;
    }
}
