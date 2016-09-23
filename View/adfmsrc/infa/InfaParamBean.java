package infa;

import common.lov.DmsComBoxLov;

import java.util.LinkedHashMap;
import java.util.List;

import java.util.Map;

import javax.faces.model.SelectItem;

import oracle.adf.view.rich.component.rich.input.RichInputComboboxListOfValues;

public class InfaParamBean {
    private String pAlias;
    private String pName;
    private String pScope;
    private String pSource;
    private List<SelectItem> valuesList;
    private DmsComBoxLov comLov;
    private String choiceValue;
    private RichInputComboboxListOfValues inputCbLov;
    
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

    public void setComLov(DmsComBoxLov comLov) {
        this.comLov = comLov;
    }

    public DmsComBoxLov getComLov() {
        return comLov;
    }

    public void setInputCbLov(RichInputComboboxListOfValues inputCbLov) {
        this.inputCbLov = inputCbLov;
    }

    public RichInputComboboxListOfValues getInputCbLov() {
        return inputCbLov;
    }
}
