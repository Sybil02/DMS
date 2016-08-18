package dcm.template;

import dcm.ColumnDef;

import java.util.List;

public class TemplateEntity {
    private int startLine;
    private String templateId;
    private String temptable;
    private int columnSize;
    private String templateName;
    private String preGrogram;
    private String impGrogram;
    private String afterGrogram;
    private List<String> colsdef;
    private List<String> colType;

    public TemplateEntity(String templateId,String templateName, String temptable,
                          int columnSize, int startLine,String preGrogram,String impGrogram,String afterGrogram,List<String> colsdef,List<String> types) {
        this.startLine = startLine;
        this.templateId = templateId;
        this.temptable = temptable;
        this.columnSize = columnSize;
        this.templateName = templateName;
        this.preGrogram = preGrogram;
        this.impGrogram = impGrogram;
        this.afterGrogram = afterGrogram;
        this.colsdef = colsdef;
        this.colType = types;
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

    public void setColsdef(List<String> colsdef) {
        this.colsdef = colsdef;
    }

    public List<String> getColsdef() {
        return colsdef;
    }

    public void setPreGrogram(String preGrogram) {
        this.preGrogram = preGrogram;
    }

    public String getPreGrogram() {
        return preGrogram;
    }

    public void setImpGrogram(String impGrogram) {
        this.impGrogram = impGrogram;
    }

    public String getImpGrogram() {
        return impGrogram;
    }

    public void setAfterGrogram(String afterGrogram) {
        this.afterGrogram = afterGrogram;
    }

    public String getAfterGrogram() {
        return afterGrogram;
    }

    public void setColType(List<String> colType) {
        this.colType = colType;
    }

    public List<String> getColType() {
        return colType;
    }
}
