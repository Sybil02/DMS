package dms.groupUser;

import common.ADFUtils;

import common.DmsUtils;

import java.util.ArrayList;
import java.util.List;

import javax.faces.event.ValueChangeEvent;

import oracle.adf.model.BindingContext;
import oracle.adf.model.binding.DCIteratorBinding;
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

import org.apache.myfaces.trinidad.event.SelectionEvent;

import team.epm.dms.view.DmsUserGroupViewImpl;

public class UserGroupBean {
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
        
        if(valueChangeEvent.getNewValue()!=null){
            Integer[] newValue = (Integer[])valueChangeEvent.getNewValue();
            for(int i:newValue){
               Row row= userIter.getRowAtRangeIndex(i);
               Row userGroup= view.createRow();
               userGroup.setAttribute("GroupId", this.getCurGroup().getAttribute("Id"));
               userGroup.setAttribute("UserId", row.getAttribute("Id"));
               view.insertRow(userGroup);
            }
            view.getApplicationModule().getTransaction().commit();
        }
        if(valueChangeEvent.getOldValue()!=null){
            Integer[] oldValue = (Integer[])valueChangeEvent.getOldValue();
            for(int i:oldValue){
               Row row= userIter.getRowAtRangeIndex(i);
               view.deleteGroupUserByGroupIdAndUserId(row.getAttribute("Id")+"", 
                                                       this.getCurGroup().getAttribute("Id")+"");
            }
            view.getApplicationModule().getTransaction().commit();
        }
    }

    public Integer[] getSelectedUserList() {
        List<Integer> selectedUser = new ArrayList<Integer>();
        DCIteratorBinding userIter =
            ADFUtils.findIterator("DmsUserViewIterator");
        DCIteratorBinding groupedUserIter =
            ADFUtils.findIterator("DmsUserGroupedViewIterator");
        ViewObject groupedUserview = groupedUserIter.getViewObject();
        groupedUserview.setNamedWhereClauseParam("groupId",
                                                 this.getCurGroup().getAttribute("Id"));
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
        AdfFacesContext adfFacesContext = AdfFacesContext.getCurrentInstance();
        adfFacesContext.addPartialTarget(this.selectShuttle);
        BindingContainer bc=BindingContext.getCurrent().getCurrentBindingsEntry();
        JUCtrlListBinding listBinding = (JUCtrlListBinding)bc.get("DmsUserView");
        listBinding.clearSelectedIndices();
    }

    private Row getCurGroup() {
        return ADFUtils.findIterator("fetchEnabledGroupIter").getCurrentRow();
    }

    public void setSelectShuttle(RichSelectManyShuttle selectShuttle) {
        this.selectShuttle = selectShuttle;
    }

    public RichSelectManyShuttle getSelectShuttle() {
        return selectShuttle;
    }
}
