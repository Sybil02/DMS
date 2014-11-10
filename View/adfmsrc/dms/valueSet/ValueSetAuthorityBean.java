package dms.valueSet;

import common.ADFUtils;
import common.DmsUtils;

import java.util.ArrayList;
import java.util.List;


import javax.faces.model.SelectItem;

import oracle.adf.share.ADFContext;
import oracle.adf.view.rich.component.rich.input.RichSelectManyShuttle;
import oracle.adf.view.rich.context.AdfFacesContext;

import oracle.jbo.ApplicationModule;
import oracle.jbo.Row;

import org.apache.commons.lang.ObjectUtils;
import org.apache.myfaces.trinidad.event.SelectionEvent;

import team.epm.module.DmsModuleImpl;

public class ValueSetAuthorityBean {

    private RichSelectManyShuttle selectShuttle;
    private List<SelectItem> allItems=new ArrayList<SelectItem>();
    
    public ValueSetAuthorityBean() {
        super();
    }

    public List getSelectValue() {
        return null;
    }

    public List getAllValue() {
        
        
        String tablename = (String)getCurValueSet().getAttribute("Source");
        String roleId=getCurRoleId();
        DmsModuleImpl am = (DmsModuleImpl)ADFUtils.getApplicationModuleForDataControl("DmsModuleDataControl");
        List<Row> valueList= am.getValuesFromValueSet(tablename,ADFContext.getCurrent().getLocale().toString());
        for(Row row:valueList){
            
            SelectItem item=new SelectItem(row.getAttribute("ID"),(String)row.getAttribute("MEANING"));
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
    
    private String getlocal(){
        
      return "";  
    }
    public void setSelectShuttle(RichSelectManyShuttle selectShuttle) {
        this.selectShuttle = selectShuttle;
    }

    public RichSelectManyShuttle getSelectShuttle() {
        return selectShuttle;
    }


}
