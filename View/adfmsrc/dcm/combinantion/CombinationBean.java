package dcm.combinantion;


import common.ADFUtils;


import common.DmsUtils;
import common.JSFUtils;

import java.sql.SQLException;

import java.util.ArrayList;

import java.util.List;

import javax.faces.event.ActionEvent;

import oracle.adf.model.BindingContext;
import oracle.adf.model.binding.DCIteratorBinding;
import oracle.adf.share.ADFContext;
import oracle.adf.view.rich.component.rich.RichPopup;
import oracle.adf.view.rich.event.PopupCanceledEvent;
import oracle.adf.view.rich.event.PopupFetchEvent;

import oracle.binding.BindingContainer;
import oracle.binding.OperationBinding;

import oracle.jbo.Key;
import oracle.jbo.Row;
import oracle.jbo.RowIterator;

import oracle.jbo.RowSetIterator;

import oracle.jbo.ViewObject;

import team.epm.dcm.view.DcmCombinationViewImpl;
import team.epm.dcm.view.DcmCombinationViewRowImpl;

public class CombinationBean {
    private RichPopup popup;
    public CombinationBean() {
        super();
    }

    public void closePopup(PopupCanceledEvent popupCanceledEvent) {
        OperationBinding ob= ADFUtils.findOperation("Rollback");
        ob.execute();
    }
    

    
    public void setPopup(RichPopup popup) {
        this.popup = popup;
    }

    public RichPopup getPopup() {
        return popup;
    }

    public BindingContainer getBindings() {
        return BindingContext.getCurrent().getCurrentBindingsEntry();
    }

    public void createTable(ActionEvent actionEvent) {
        List<String> columns=new ArrayList<String>();
        List<String> srcTables=new ArrayList<String>();
        String locale=ADFContext.getCurrent().getLocale().toString();
        DCIteratorBinding combinationIter=ADFUtils.findIterator("DcmCombinationView1Iterator");
        DCIteratorBinding valueSetIter=ADFUtils.findIterator("DmsValueSetViewIterator");
        ViewObject valuelookup=valueSetIter.getViewObject();
        valuelookup.executeQuery();
        RowSetIterator valuerowset =valueSetIter.getRowSetIterator();
        DcmCombinationViewRowImpl combinationRow = (DcmCombinationViewRowImpl)combinationIter.getCurrentRow();
        RowIterator comVsRow=combinationRow.getDcmComVsView();
        while(comVsRow.hasNext()){
            Row relationrow=comVsRow.next();
            String valueSetId = (String)relationrow.getAttribute("ValueSetId");   
              Key key = new Key(new Object[]{valueSetId,locale});        
            Row[] valuesetRow=valuelookup.findByKey(key,1);
            columns.add((String)valuesetRow[0].getAttribute("Code"));
            srcTables.add((String)valuesetRow[0].getAttribute("Source"));
        }
        if(columns.size()<1){
            JSFUtils.addFacesErrorMessage(DmsUtils.getMsg("dcm.combiantion.must_have_vs"));
            return;
        }
        DcmCombinationViewImpl vo = (DcmCombinationViewImpl)combinationIter.getViewObject();
        try {
            vo.createTable(combinationRow.getCode(),columns);
            this.refreshCombinationRecord((String)combinationIter.getCurrentRow().getAttribute("Code"), srcTables,columns);
        } catch (SQLException e) {
            JSFUtils.addFacesErrorMessage(DmsUtils.getMsg("common.operation_failed_with_exception"));
        }
    }
    
    private void refreshCombinationRecord(String combiantonCode,List<String> srcTables,List<String> valueSetCodes) throws SQLException {
        DCIteratorBinding combinationIter=ADFUtils.findIterator("DcmCombinationView1Iterator");
        DcmCombinationViewImpl vo = (DcmCombinationViewImpl)combinationIter.getViewObject();
        vo.refreshCombinationRecord(combiantonCode, srcTables,valueSetCodes);
    }
    
    public void createInsertListener(PopupFetchEvent popupFetchEvent) {
            OperationBinding ob= ADFUtils.findOperation("CreateCombination");
            ob.execute();
        }

    public void refreshRecord(ActionEvent actionEvent) {
        List<String> columns=new ArrayList<String>();
        List<String> srcTables=new ArrayList<String>();
        String locale=ADFContext.getCurrent().getLocale().toString();
        DCIteratorBinding combinationIter=ADFUtils.findIterator("DcmCombinationView1Iterator");
        DCIteratorBinding valueSetIter=ADFUtils.findIterator("DmsValueSetViewIterator");
        ViewObject valuelookup=valueSetIter.getViewObject();
        valuelookup.executeQuery();
        RowSetIterator valuerowset =valueSetIter.getRowSetIterator();
        DcmCombinationViewRowImpl combinationRow = (DcmCombinationViewRowImpl)combinationIter.getCurrentRow();
        RowIterator comVsRow=combinationRow.getDcmComVsView();
        while(comVsRow.hasNext()){
            Row relationrow=comVsRow.next();
            String valueSetId = (String)relationrow.getAttribute("ValueSetId");   
              Key key = new Key(new Object[]{valueSetId,locale});        
            Row[] valuesetRow=valuelookup.findByKey(key,1);
            columns.add((String)valuesetRow[0].getAttribute("Code"));
            srcTables.add((String)valuesetRow[0].getAttribute("Source"));
        }
        DcmCombinationViewImpl vo = (DcmCombinationViewImpl)combinationIter.getViewObject();
        if(combinationIter.getCurrentRow()==null){
            JSFUtils.addFacesErrorMessage(DmsUtils.getMsg("dcm.combination.refresh_record_warning"));
        }
        try {
            this.refreshCombinationRecord((String)combinationIter.getCurrentRow().getAttribute("Code"), srcTables,columns);
        } catch (SQLException e) {
            JSFUtils.addFacesErrorMessage(DmsUtils.getMsg("common.operation_failed_with_exception"));
        }
    }
}
