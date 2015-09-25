package dcm;

import common.ADFUtils;
import common.JSFUtils;

import javax.faces.event.ValueChangeEvent;

import oracle.adfinternal.view.faces.model.binding.FacesCtrlListBinding;

import oracle.jbo.Row;

/*
 * 专门负责用户组名称的下拉框的类
 *
 * */
public class GroupNameComboboxBean {

    private ComboboxLOVBean groupNameLov;

    public GroupNameComboboxBean() {

    }

    public void setGroupNameLov(ComboboxLOVBean groupNameLov) {
        this.groupNameLov = groupNameLov;
    }

    public ComboboxLOVBean getGroupNameLov() {

        if (groupNameLov == null)
            groupNameLov = ComboboxLOVBean.createByBinding("GroupName");
        return groupNameLov;
    }
    
}
