package dcm;

import java.util.List;

import javax.faces.model.SelectItem;

public class CalcParameter {
    public CalcParameter() {
        super();
    }
    //参数名称
    private String paraLabel;
    //值集ID
    private String valueSetId;
    //
    private String code;
    private List<SelectItem> values;

    public CalcParameter(String paraLabel, String valueSetId, String code,
                         List<SelectItem> values) {
        super();
        this.paraLabel = paraLabel;
        this.valueSetId = valueSetId;
        this.code = code;
        this.values = values;
    }

    public void setParaLabel(String paraLabel) {
        this.paraLabel = paraLabel;
    }

    public String getParaLabel() {
        return paraLabel;
    }

    public void setValueSetId(String valueSetId) {
        this.valueSetId = valueSetId;
    }

    public String getValueSetId() {
        return valueSetId;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setValues(List<SelectItem> values) {
        this.values = values;
    }

    public List<SelectItem> getValues() {
        return values;
    }
}
