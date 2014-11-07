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
import team.epm.dms.view.DmsUserGroupViewImpl;

public class RoleGroupBean {
 
    private static ADFLogger logger=ADFLogger.createADFLogger(RoleGroupBean.class);
    String allItemsIteratorName = "DmsGroupViewIterator";
    String allItemsValueAttrName = "Id";
    String allItemsDisplayAttrName = "Name";
    String allItemsDescriptionAttrName = "Name";
    String selectedValuesIteratorName = "GroupSelectedUserViewIterator";
    String selectedValuesValueAttrName = "UserId";
    private Integer[] selectedList;
    private RichTable roleList;
    private RichSelectManyShuttle selectShuttle;
    List allItems;
    
    public RoleGroupBean() {

    }
    
    public void selectListener(ValueChangeEvent valueChangeEvent) {
        // Add event code here...
        BindingContainer bc=BindingContext.getCurrent().getCurrentBindingsEntry();
        JUCtrlListBinding listBinding = (JUCtrlListBinding)bc.get("DmsGroupView");
        listBinding.clearSelectedIndices();
        RowSetIterator groupIter =
            ADFUtils.findIterator("DmsGroupViewIterator").getRowSetIterator();
        DCIteratorBinding groupRoleIter =
            ADFUtils.findIterator("DmsGroupRoleViewIterator");
        DmsGroupRoleViewImpl view = (DmsGroupRoleViewImpl)groupRoleIter.getViewObject();
        
        if(valueChangeEvent.getNewValue()!=null){
            Integer[] newValue = (Integer[])valueChangeEvent.getNewValue();
            Integer[] oldValue = (Integer[])valueChangeEvent.getOldValue();
            List<Integer> newList=Arrays.asList(newValue);
            List<Integer> oldList=Arrays.asList(oldValue);
            for(Integer i:newList){
                if (!oldList.contains(i)){
                    Row row= groupIter.getRowAtRangeIndex(i);
                    Row groupRole= view.createRow();
                    groupRole.setAttribute("RoleId", this.getCurRoleId());
                    groupRole.setAttribute("GroupId", row.getAttribute("Id"));
                    view.insertRow(groupRole);
                }              
            }
            view.getApplicationModule().getTransaction().commit();
            for(Integer i:oldValue){
                if (!newList.contains(i)){
                    Row row= groupIter.getRowAtRangeIndex(i);
                    view.deleteGroupRoleByGroupIdAndRoleId(this.getCurRoleId(),
                                                           row.getAttribute("Id")+""
                                                            );
                }
            }
            view.getApplicationModule().getTransaction().commit();
//            for(int i:newValue){
//               Row row= groupIter.getRowAtRangeIndex(i);
//               Row groupRole= view.createRow();
//               groupRole.setAttribute("RoleId", this.getCurRoleId());
//               groupRole.setAttribute("GroupId", row.getAttribute("Id"));
//               view.insertRow(groupRole);
//            }
//            view.getApplicationModule().getTransaction().commit();
        }
//        if(valueChangeEvent.getOldValue()!=null){
//            Integer[] oldValue = (Integer[])valueChangeEvent.getOldValue();
//            for(int i:oldValue){
//               Row row= groupIter.getRowAtRangeIndex(i);
//               view.deleteGroupRoleByGroupIdAndRoleId(this.getCurRoleId(),
//                                                      row.getAttribute("Id")+""
//                                                       );
//            }
//            view.getApplicationModule().getTransaction().commit();
//        }
    }

    public Integer[] getSelectedGroupList() {
        
        List<Integer> selectedGroup = new ArrayList<Integer>();
        DCIteratorBinding groupIter =
            ADFUtils.findIterator("DmsGroupViewIterator");
        DCIteratorBinding groupedRoleIter =
            ADFUtils.findIterator("DmsGroupsForRoleViewIterator");
        ViewObject groupsForRoleview = groupedRoleIter.getViewObject();
        groupsForRoleview.setNamedWhereClauseParam("roleId",
                                                 this.getCurRoleId());
        groupsForRoleview.executeQuery();
        while (groupsForRoleview.hasNext()) {
            Row row = groupsForRoleview.next();
            //TODO
            Key key=new Key(new Object[]{row.getAttribute("Id"),row.getAttribute("Locale")});
            groupIter.setCurrentRowWithKey(key.toStringFormat(true));
            Integer indx = groupIter.getCurrentRowIndexInRange();

            selectedGroup.add(indx);
        }
        this.selectedList =
                selectedGroup.toArray(new Integer[selectedGroup.size()]);
        return this.selectedList;
    }
    
    public void setSelectedGroupList(Integer[] selectedList){
        this.selectedList=selectedList;
    }
    public List getAllItems() {
        if (allItems == null) {
            allItems =
                    ADFUtils.selectItemsForIterator(allItemsIteratorName, allItemsValueAttrName,
                                                    allItemsDisplayAttrName,
                                                    allItemsDescriptionAttrName);
        }


        return allItems;
    }
    
    public void setSelectedUserList(Integer[] selectedList) {
        this.selectedList = selectedList;
    }


    public void roleSelectListener(SelectionEvent selectionEvent) {
        //#{bindings.DmsGroupView.collectionModel.makeCurrent}
        DmsUtils.makeCurrent(selectionEvent);
        ADFContext aDFContext = ADFContext.getCurrent();
        aDFContext.getViewScope().put("curRoleId", this.getCurRole().getAttribute("Id"));
        this.selectShuttle.resetValue();
        AdfFacesContext adfFacesContext = AdfFacesContext.getCurrentInstance();           
        adfFacesContext.addPartialTarget(this.selectShuttle);
        /*
        BindingContainer bc=BindingContext.getCurrent().getCurrentBindingsEntry();
        JUCtrlListBinding listBinding = (JUCtrlListBinding)bc.get("DmsUserView");
        listBinding.clearSelectedIndices();
        listBinding.getSelectedIndices();
        */
    }
    
    private Row getCurRole() {
        return ADFUtils.findIterator("fetchEnabledRoleIter").getCurrentRow();
    }
    
    private String getCurRoleId(){
        String curRoleId=ObjectUtils.toString(ADFContext.getCurrent().getViewScope().get("curRoleId"));
        curRoleId=curRoleId.length()>0 ? curRoleId :  ObjectUtils.toString(this.getCurRole().getAttribute("Id"));
        return curRoleId;
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
}
