package workapproveflow.workflow;

import java.util.List;

import javax.faces.model.SelectItem;

public class WorkflowValueSet {
    public WorkflowValueSet() {
        super();
    }
    //值集名称
    private String valueSetName;
    //值集ID
    private String valueSetId;
    //值集list
    private List<SelectItem> valueList;

    public WorkflowValueSet(String valueSetName, String valueSetId,
                            List<SelectItem> valueList) {
        super();
        this.valueSetName = valueSetName;
        this.valueSetId = valueSetId;
        this.valueList = valueList;
    }

    public void setValueSetName(String valueSetName) {
        this.valueSetName = valueSetName;
    }

    public String getValueSetName() {
        return valueSetName;
    }

    public void setValueSetId(String valueSetId) {
        this.valueSetId = valueSetId;
    }

    public String getValueSetId() {
        return valueSetId;
    }

    public void setValueList(List<SelectItem> valueList) {
        this.valueList = valueList;
    }

    public List<SelectItem> getValueList() {
        return valueList;
    }
}
