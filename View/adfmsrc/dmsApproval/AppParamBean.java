package dmsApproval;

import java.util.List;

import javax.faces.model.SelectItem;

public class AppParamBean {
    String pName;
    String pCode;
    String pSource;
    String pChoiced;
    List<SelectItem> valueList;

    public void setPName(String pName) {
        this.pName = pName;
    }

    public String getPName() {
        return pName;
    }

    public void setPCode(String pCode) {
        this.pCode = pCode;
    }

    public String getPCode() {
        return pCode;
    }

    public void setPSource(String pSource) {
        this.pSource = pSource;
    }

    public String getPSource() {
        return pSource;
    }

    public void setPChoiced(String pChoiced) {
        this.pChoiced = pChoiced;
    }

    public String getPChoiced() {
        return pChoiced;
    }

    public void setValueList(List<SelectItem> valueList) {
        this.valueList = valueList;
    }

    public List<SelectItem> getValueList() {
        return valueList;
    }
}
