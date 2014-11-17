package dms.groupUser;

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

import oracle.adf.view.rich.component.rich.input.RichInputText;
import oracle.adf.view.rich.component.rich.input.RichSelectManyShuttle;

import oracle.adf.view.rich.component.rich.output.RichOutputText;
import oracle.adf.view.rich.context.AdfFacesContext;

import oracle.binding.BindingContainer;

import oracle.jbo.Key;
import oracle.jbo.Row;

import oracle.jbo.RowSetIterator;
import oracle.jbo.ViewObject;

import oracle.jbo.uicli.binding.JUCtrlListBinding;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.myfaces.trinidad.component.core.input.CoreSelectManyShuttle;
import org.apache.myfaces.trinidad.event.SelectionEvent;

import team.epm.dms.view.DmsUserGroupViewImpl;

public class UserGroupBean {
    private static ADFLogger logger=ADFLogger.createADFLogger(UserGroupBean.class);
    private Integer[] selectedList;
    private RichTable groupList;
    private RichSelectManyShuttle selectShuttle;

    public UserGroupBean() {
    }

    public void userGroupListener(ValueChangeEvent valueChangeEvent) {
        // Add event code here...
        BindingContainer bc=BindingContext.getCurrent().getCurrentBindingsEntry();
        JUCtrlListBinding listBinding = (JUCtrlListBinding)bc.get("DmsUserView");
        listBinding.clearSelectedIndices();
        RowSetIterator userIter =
            ADFUtils.findIterator("DmsUserViewIterator").getRowSetIterator();
        DCIteratorBinding userGroupIter =
            ADFUtils.findIterator("DmsUserGroupViewIterator");
        DmsUserGroupViewImpl view = (DmsUserGroupViewImpl)userGroupIter.getViewObject();
        
        Integer[] newValue = (Integer[])valueChangeEvent.getNewValue();
        Integer[] oldValue = (Integer[])valueChangeEvent.getOldValue();
        
        if(newValue==null){
                for(int i:oldValue){
                   Row row= userIter.getRowAtRangeIndex(i);
                   view.deleteGroupUserByGroupIdAndUserId(ObjectUtils.toString(row.getAttribute("Id")), 
                                                           this.getCurGroupId());
                }
            view.getApplicationModule().getTransaction().commit();
            return ;
        }
        if(oldValue==null){
            for(Integer i:newValue){                   
                Row row= userIter.getRowAtRangeIndex(i);
                Row userGroup= view.createRow();
                userGroup.setAttribute("GroupId", this.getCurGroupId());
                userGroup.setAttribute("UserId", row.getAttribute("Id"));
                view.insertRow(userGroup);                             
            }
            view.getApplicationModule().getTransaction().commit();
            return ;
        }
        List<Integer> newList=Arrays.asList(newValue);
        List<Integer> oldList=Arrays.asList(oldValue);        
        for(Integer i:newList){
            if (!oldList.contains(i)){
                Row row= userIter.getRowAtRangeIndex(i);
                Row userGroup= view.createRow();
                userGroup.setAttribute("GroupId", this.getCurGroupId());
                userGroup.setAttribute("UserId", row.getAttribute("Id"));
                view.insertRow(userGroup);
            }              
        }
        view.getApplicationModule().getTransaction().commit();
        for(Integer i:oldValue){
            if (!newList.contains(i)){
                Row row= userIter.getRowAtRangeIndex(i);
                view.deleteGroupUserByGroupIdAndUserId(ObjectUtils.toString(row.getAttribute("Id")), 
                                                        this.getCurGroupId());
            }
        }
        view.getApplicationModule().getTransaction().commit();     
    }

    public Integer[] getSelectedUserList() {
        
        List<Integer> selectedUser = new ArrayList<Integer>();
        DCIteratorBinding userIter =
            ADFUtils.findIterator("DmsUserViewIterator");
        DCIteratorBinding groupedUserIter =
            ADFUtils.findIterator("DmsUserGroupedViewIterator");
        ViewObject groupedUserview = groupedUserIter.getViewObject();
        groupedUserview.setNamedWhereClauseParam("groupId",
                                                 this.getCurGroupId());
        groupedUserview.executeQuery();
        while (groupedUserview.hasNext()) {
            Row row = groupedUserview.next();
            //TODO
            Key key=new Key(new Object[]{row.getAttribute("Id")});
            userIter.setCurrentRowWithKey(key.toStringFormat(true));
            int indx = userIter.getCurrentRowIndexInRange();
            
            selectedUser.add(indx);
        }
        this.selectedList =
                selectedUser.toArray(new Integer[selectedUser.size()]);
        return this.selectedList;
    }

    public void setSelectedUserList(Integer[] selectedList) {
        this.selectedList = selectedList;
    }

    public void setGroupList(RichTable groupList) {
        this.groupList = groupList;
    }

    public RichTable getGroupList() {
        return groupList;
    }

    public void groupSelectListener(SelectionEvent selectionEvent) {
        //#{bindings.DmsGroupView.collectionModel.makeCurrent}
        DmsUtils.makeCurrent(selectionEvent);
        ADFContext aDFContext = ADFContext.getCurrent();
        aDFContext.getViewScope().put("curGroupId", this.getCurGroup().getAttribute("Id"));
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
}
