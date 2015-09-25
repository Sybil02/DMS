package dcm;

import common.JSFUtils;

import javax.faces.event.ValueChangeEvent;


/*
 * 专门负责角色的下拉框
 *
 * */
public class RoleNameComboboxBean {

    private ComboboxLOVBean roleNameLov;

    public RoleNameComboboxBean() {

    }
    
    public ComboboxLOVBean getRoleNameLov() {

        if (roleNameLov == null)
            roleNameLov = ComboboxLOVBean.createByBinding("RoleName");
        return roleNameLov; 
    }
    
}
