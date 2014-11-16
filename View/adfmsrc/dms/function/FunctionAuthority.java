package dms.function;

import common.ADFUtils;
import common.DmsUtils;

import java.util.ArrayList;
import java.util.List;

import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import oracle.adf.share.ADFContext;
import oracle.adf.view.rich.component.rich.input.RichSelectManyShuttle;
import oracle.adf.view.rich.context.AdfFacesContext;

import oracle.jbo.Row;

import org.apache.commons.lang.ObjectUtils;
import org.apache.myfaces.trinidad.event.SelectionEvent;

import team.epm.module.DmsModuleImpl;

public class FunctionAuthority {
    private RichSelectManyShuttle selectShuttle;
    private List<SelectItem> allItems = new ArrayList<SelectItem>();

    public FunctionAuthority() {
    }

    public List getSelectValue() {

        String roleId = getCurRoleId();
        DmsModuleImpl am =
            (DmsModuleImpl)ADFUtils.getApplicationModuleForDataControl("DmsModuleDataControl");
        List<String> functionList =
            am.getFunctionIdsByRoleId(roleId, ADFContext.getCurrent().getLocale().toString());
        return functionList;
    }

    public List getAllItems() {
        allItems=ADFUtils.selectItemsForIterator("DmsFunctionViewIterator","Id","Name");
        return allItems;
    }

    public void roleSelectListener(SelectionEvent selectionEvent) {
        DmsUtils.makeCurrent(selectionEvent);
        ADFContext aDFContext = ADFContext.getCurrent();
        aDFContext.getViewScope().put("curRoleId",
                                      this.getCurRole().getAttribute("Id"));
        this.selectShuttle.resetValue();
        AdfFacesContext adfFacesContext = AdfFacesContext.getCurrentInstance();
        adfFacesContext.addPartialTarget(this.selectShuttle);
    }

    public void selectListener(ValueChangeEvent valueChangeEvent) {
        List<String> newValue = (List<String>)valueChangeEvent.getNewValue();
        String roleId = getCurRoleId();
        DmsModuleImpl am =
            (DmsModuleImpl)ADFUtils.getApplicationModuleForDataControl("DmsModuleDataControl");
        am.updateRoleFunction(roleId, newValue);
    }

    private Row getCurRole() {
        return ADFUtils.findIterator("DmsRoleViewIterator").getCurrentRow();
    }


    private String getCurRoleId() {
        String curRoleId =
            ObjectUtils.toString(ADFContext.getCurrent().getViewScope().get("curRoleId"));
        curRoleId =
                curRoleId.length() > 0 ? curRoleId : ObjectUtils.toString(this.getCurRole() ==
                                                                          null ?
                                                                          "" :
                                                                          this.getCurRole().getAttribute("Id"));
        return curRoleId;
    }


    public void setSelectShuttle(RichSelectManyShuttle selectShuttle) {
        this.selectShuttle = selectShuttle;
    }

    public RichSelectManyShuttle getSelectShuttle() {
        return selectShuttle;
    }

    public void setSelectValue(List selectValue) {

    }

}
