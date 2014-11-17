package dms.valueSet;

import common.ADFUtils;
import common.DmsUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import oracle.adf.model.BindingContext;
import oracle.adf.model.binding.DCIteratorBinding;
import oracle.adf.share.ADFContext;
import oracle.adf.view.rich.component.rich.input.RichSelectManyShuttle;
import oracle.adf.view.rich.context.AdfFacesContext;

import oracle.binding.BindingContainer;

import oracle.jbo.ApplicationModule;
import oracle.jbo.Row;

import oracle.jbo.RowSetIterator;
import oracle.jbo.uicli.binding.JUCtrlListBinding;

import org.apache.commons.lang.ObjectUtils;
import org.apache.myfaces.trinidad.event.SelectionEvent;

import team.epm.dms.view.DmsGroupRoleViewImpl;
import team.epm.module.DmsModuleImpl;

public class ValueSetAuthorityBean {

    private RichSelectManyShuttle selectShuttle;
    private List<SelectItem> allItems=new ArrayList<SelectItem>();
    
    public ValueSetAuthorityBean() {
        super();
    }

    public List getSelectValue() {
        String valueSetId = getCurValueSetId();
        String roleId=getCurRoleId();
        DmsModuleImpl am = (DmsModuleImpl)ADFUtils.getApplicationModuleForDataControl("DmsModuleDataControl");
        List values=am.getValuesByRoleAndValueSetName(roleId, valueSetId);
        
        return values;
    }

    public List getAllValue() {
        
        Row cur_valueSet=getCurValueSet();
        if(cur_valueSet==null){
            return allItems;
        }                        
        String tablename = (String)cur_valueSet.getAttribute("Source");
        String roleId=getCurRoleId();
        DmsModuleImpl am = (DmsModuleImpl)ADFUtils.getApplicationModuleForDataControl("DmsModuleDataControl");
        List<Row> valueList= am.getValuesFromValueSet(tablename,ADFContext.getCurrent().getLocale().toString());
        for(Row row:valueList){
            
            SelectItem item=new SelectItem(row.getAttribute("ID"),(String)row.getAttribute("MEANING"),(String)row.getAttribute("MEANING"));
            allItems.add(item);
        }
        return allItems;
    }
    public void roleSelectListener(SelectionEvent selectionEvent) {
        DmsUtils.makeCurrent(selectionEvent);
        ADFContext aDFContext = ADFContext.getCurrent();
        aDFContext.getViewScope().put("curRoleId", this.getCurRole().getAttribute("Id"));
        aDFContext.getViewScope().put("curValueSetId", this.getCurValueSet().getAttribute("Id"));
        this.selectShuttle.resetValue();
        AdfFacesContext adfFacesContext = AdfFacesContext.getCurrentInstance();           
        adfFacesContext.addPartialTarget(this.selectShuttle);
    }
    public void selectListener(ValueChangeEvent valueChangeEvent) {
        // Add event code here...
//        BindingContainer bc=BindingContext.getCurrent().getCurrentBindingsEntry();
//        JUCtrlListBinding listBinding = (JUCtrlListBinding)bc.get("DmsGroupView");
//        listBinding.clearSelectedIndices();
//        RowSetIterator groupIter =
//            ADFUtils.findIterator("DmsGroupViewIterator").getRowSetIterator();
//        DCIteratorBinding groupRoleIter =
//            ADFUtils.findIterator("DmsGroupRoleViewIterator");
//        DmsGroupRoleViewImpl view = (DmsGroupRoleViewImpl)groupRoleIter.getViewObject();
        
        List<String> newValue = (List<String>)valueChangeEvent.getNewValue();
        if(newValue==null)
            newValue=new ArrayList<String>();
        String valueSetId = getCurValueSetId();
        String roleId=getCurRoleId();
        DmsModuleImpl am = (DmsModuleImpl)ADFUtils.getApplicationModuleForDataControl("DmsModuleDataControl");
        am.updateRoleValue(roleId, newValue, valueSetId);
    }

    private Row getCurRole() {
        return ADFUtils.findIterator("fetchEnabledRoleIter").getCurrentRow();
    }
    private Row getCurValueSet(){
        return ADFUtils.findIterator("DmsValueSetViewIterator").getCurrentRow();
    }
    private String getCurValueSetId(){
        String curValueSetId=ObjectUtils.toString(ADFContext.getCurrent().getViewScope().get("curValueSetId"));
        curValueSetId=curValueSetId.length()>0 ? curValueSetId :  ObjectUtils.toString(this.getCurValueSet()==null ? "":this.getCurValueSet().getAttribute("Id"));
        return curValueSetId;
    }
    private String getCurRoleId(){
        String curRoleId=ObjectUtils.toString(ADFContext.getCurrent().getViewScope().get("curRoleId"));
        curRoleId=curRoleId.length()>0 ? curRoleId :  ObjectUtils.toString(this.getCurRole()==null ? "":this.getCurRole().getAttribute("Id"));
        return curRoleId;
    }
    
   
    public void setSelectShuttle(RichSelectManyShuttle selectShuttle) {
        this.selectShuttle = selectShuttle;
    }

    public RichSelectManyShuttle getSelectShuttle() {
        return selectShuttle;
    }
    public void setSelectValue(List selectValue){
    
    }

}
