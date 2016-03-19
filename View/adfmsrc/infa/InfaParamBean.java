package infa;

import java.util.List;

import javax.faces.model.SelectItem;

public class InfaParamBean {
    private String pAlias;
    private String pName;
    private String pScope;
    private String pSource;
    private List<SelectItem> valuesList;
    
    private String choiceValue;
    
    public InfaParamBean() {
        super();
    }
    
    public InfaParamBean(String pAlias,String pName,String pScope,String pSource){
        this.pAlias = pAlias;
        this.pName = pName;
        this.pScope = pScope;
        this.pSource = pSource;
    }

    public void setPAlias(String pAlias) {
        this.pAlias = pAlias;
    }

    public String getPAlias() {
        return pAlias;
    }

    public void setPName(String pName) {
        this.pName = pName;
    }

    public String getPName() {
        return pName;
    }

    public void setPScope(String pScope) {
        this.pScope = pScope;
    }

    public String getPScope() {
        return pScope;
    }

    public void setPSource(String pSource) {
        this.pSource = pSource;
    }

    public String getPSource() {
        return pSource;
    }

    public void setValuesList(List<SelectItem> valuesList) {
        this.valuesList = valuesList;
    }

    public List<SelectItem> getValuesList() {
        return valuesList;
    }

    public void setChoiceValue(String choiceValue) {
        this.choiceValue = choiceValue;
    }

    public String getChoiceValue() {
        return choiceValue;
    }
}
