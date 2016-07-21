package dcm;

import common.lov.DmsComBoxLov;

import java.util.List;

import javax.faces.model.SelectItem;

public class ComHeader {
    private String valueSetId;
    private String name;
    private String srcTable;
    private String isAuthority;
    private String code;
    private List<SelectItem> values;
    private String value;
    private DmsComBoxLov comLov;

    public void setValueSetId(String valueSetId) {
        this.valueSetId = valueSetId;
    }

    public String getValueSetId() {
        return valueSetId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setSrcTable(String srcTable) {
        this.srcTable = srcTable;
    }

    public String getSrcTable() {
        return srcTable;
    }

    public void setIsAuthority(String isAuthority) {
        this.isAuthority = isAuthority;
    }

    public String getIsAuthority() {
        return isAuthority;
    }

    public void setValues(List<SelectItem> values) {
        this.values = values;
    }

    public List<SelectItem> getValues() {
        return values;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setComLov(DmsComBoxLov comLov) {
        this.comLov = comLov;
    }

    public DmsComBoxLov getComLov() {
        return comLov;
    }
}
