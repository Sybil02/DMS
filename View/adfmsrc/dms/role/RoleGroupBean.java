package dms.role;

import common.ADFUtils;
import common.DmsUtils;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.faces.event.ValueChangeEvent;

import oracle.adf.model.BindingContext;
import oracle.adf.model.binding.DCIteratorBinding;
import oracle.adf.share.ADFContext;
import oracle.adf.share.logging.ADFLogger;
import oracle.adf.view.rich.component.rich.data.RichTable;
import oracle.adf.view.rich.component.rich.input.RichSelectManyShuttle;
import oracle.adf.view.rich.context.AdfFacesContext;

import oracle.binding.BindingContainer;

import oracle.jbo.Key;
import oracle.jbo.Row;
import oracle.jbo.RowSetIterator;
import oracle.jbo.ViewObject;
import oracle.jbo.uicli.binding.JUCtrlListBinding;

import org.apache.commons.lang.ObjectUtils;
import org.apache.myfaces.trinidad.event.SelectionEvent;

import team.epm.dms.view.DmsGroupRoleViewImpl;
import team.epm.dms.view.DmsRoleViewImpl;
import team.epm.dms.view.DmsUserGroupViewImpl;
import team.epm.module.DmsModuleImpl;

public class RoleGroupBean {
 
    private static ADFLogger logger=ADFLogger.createADFLogger(RoleGroupBean.class);
    private Integer[] selectedList;
    private RichTable roleList;
    private RichSelectManyShuttle selectShuttle;
    List allItems;
    
    public RoleGroupBean() {

    }
    
    public void selectListener(ValueChangeEvent valueChangeEvent) {
        // Add event code here...
        List<String> newValue = (List<String>)valueChangeEvent.getNewValue();
        if(newValue==null)
            newValue=new ArrayList<String>();
        String groupId = getCurGroupId();
        DmsModuleImpl am =
            (DmsModuleImpl)ADFUtils.getApplicationModuleForDataControl("DmsModuleDataControl");
        am.updateGroupRole(groupId, newValue);
    }

    public List<String> getSelectedRoleList() {
        
        DmsModuleImpl am =
            (DmsModuleImpl)ADFUtils.getApplicationModuleForDataControl("DmsModuleDataControl");
        List<String> selectedRole =
            am.getRoleIdsByGroupId(getCurGroupId());
        return selectedRole;
    }
    
    public List getAllItems() {
        allItems=ADFUtils.selectItemsForIterator("DmsRoleViewIterator","Id","RoleName");
        return allItems;
    }
    
    public void setSelectedRoleList(List<String> selectedList){
       
    }

    


    public void groupSelectListener(SelectionEvent selectionEvent) {
        DmsUtils.makeCurrent(selectionEvent);
        ADFContext aDFContext = ADFContext.getCurrent();
        aDFContext.getViewScope().put("curGroupId", this.getCurGroup().getAttribute("Id"));
        this.selectShuttle.resetValue();
        AdfFacesContext adfFacesContext = AdfFacesContext.getCurrentInstance();           
        adfFacesContext.addPartialTarget(this.selectShuttle);
      
    }
    
    private Row getCurGroup() {
        return ADFUtils.findIterator("fetchEnabledGroupIter").getCurrentRow();
    }
    
    private String getCurGroupId(){
        String curGroupId=ObjectUtils.toString(ADFContext.getCurrent().getViewScope().get("curGroupId"));
        curGroupId=curGroupId.length()>0 ? curGroupId :  ObjectUtils.toString(this.getCurGroup()==null ? "":this.getCurGroup().getAttribute("Id"));
        return curGroupId;
    }
    
    public void setSelectShuttle(RichSelectManyShuttle selectShuttle) {
        this.selectShuttle = selectShuttle;
    }

    public RichSelectManyShuttle getSelectShuttle() {
        return selectShuttle;
    }

    public void setRoleList(RichTable roleList) {
        this.roleList = roleList;
    }

    public RichTable getRoleList() {
        return roleList;
    }

    public void setAllItems(List allItems) {
        this.allItems = allItems;
    }


}
