package common.lov;

import java.util.List;

import oracle.adf.view.rich.model.ListOfValuesModel;

public class DmsComBoxLov implements LOVItemSelectionListener{
    public DmsComBoxLov(List<ValueSetRow> valueList) {
        this.valueList = valueList;
    }
    
    private List<ValueSetRow> valueList;
    private String meaning;
    //定义ListOfValueModelImpl
    private ListOfValuesModelImpl _listOfValuesModel;

    //返回ListOfValueModel
    public ListOfValuesModel getListOfValuesModel() {
        if (_listOfValuesModel == null) {
            _listOfValuesModel = new ListOfValuesModelImpl(valueList);
            _listOfValuesModel.addItemSelectionListener(this);
        }
        return _listOfValuesModel;
    }
    //选择的值
    public void valueSelected(ValueSetRow value) {
        ValueSetRow rowData = value;
        if (rowData != null) {
            setMeaning(rowData.getMeaning());
        }
    }

    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }

    public String getMeaning() {
        return meaning;
    }

    public void setValueList(List<ValueSetRow> valueList) {
        this.valueList = valueList;
    }

    public List<ValueSetRow> getValueList() {
        return valueList;
    }
}
